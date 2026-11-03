#!/usr/bin/env python3
"""Check for known accidental Yarn-package imports in the Fabric 26.3 port.

The Fabric 26.3 unobfuscated source uses Mojang packages. The previous source
branch mistakenly applied two migrations to Yarn-style identifiers. This
validator ensures those known mistakes do not silently return. This is NOT a
comprehensive verification of every class, method or 26.3 API signature.
"""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parents[1]
BAD = (
    'net.minecraft.util.Identifier', 'net.minecraft.item.',
    'net.minecraft.block.', 'net.minecraft.util.math.',
    'net.minecraft.entity.player.', 'net.minecraft.recipe.',
    'net.minecraft.text.', 'net.minecraft.network.RegistryByteBuf',
    'net.minecraft.network.codec.PacketCodec',
    'net.minecraft.world.WorldAccessor',
)

problems = []
for source_root in ('common/src/main/java', 'fabric/src/main/java'):
    for file in sorted((ROOT / source_root).rglob('*.java')):
        for line_no, line in enumerate(file.read_text().splitlines(), 1):
            if line.strip().startswith('//') or line.strip().startswith('*'):
                continue
            for old in BAD:
                if old in line:
                    problems.append(f'{file.relative_to(ROOT)}:{line_no}: {old}')

if problems:
    print(f'Found {len(problems)} known stale namespace references:')
    print('\n'.join(problems[:100]))
    sys.exit(1)
print('Known stale Yarn-style 26.3 import namespaces: 0 occurrences')
