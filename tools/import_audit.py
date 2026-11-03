#!/usr/bin/env python3
"""Conservative inventory of 1.21.1 Mojang-mapped imports requiring review for 26.3.

Does not auto-rewrite imports: package AND class names changed in 26.1+.
"""
import collections
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
AREAS = {
    'items / crafting': ('net.minecraft.world.item.', 'net.minecraft.world.inventory.'),
    'blocks / block entities': ('net.minecraft.world.level.block.', 'net.minecraft.world.level.material.'),
    'worlds / entity APIs': ('net.minecraft.world.level.', 'net.minecraft.world.entity.', 'net.minecraft.server.level.'),
    'registries / resources / positions': ('net.minecraft.core.', 'net.minecraft.resources.'),
    'rendering / client UI': ('net.minecraft.client.renderer.', 'net.minecraft.client.gui.', 'net.minecraft.client.model.'),
    'text / NBT / network': ('net.minecraft.network.chat.', 'net.minecraft.network.protocol.', 'net.minecraft.nbt.'),
}

counts = collections.Counter()
top = collections.Counter()
examples = collections.defaultdict(list)
file_count = 0
for folder in ['common/src/main/java', 'fabric/src/main/java']:
    for file in (ROOT / folder).rglob('*.java'):
        file_count += 1
        for imp in re.findall(r'^import\s+(?:static\s+)?(net\.minecraft\.[\w.]+)\s*;', file.read_text(), flags=re.M):
            top[imp] += 1
            for area, prefixes in AREAS.items():
                if any(imp.startswith(prefix) for prefix in prefixes):
                    counts[area] += 1
                    if len(examples[area]) < 5:
                        examples[area].append((file.relative_to(ROOT), imp))
                    break

out = [
    '# Import migration inventory — Overgeared Fabric 26.3', '',
    'This is a **source import inventory**, not an error count or a successful compile.',
    'Some imports are counted in more than one conceptual area in regular discussions; this',
    'inventory assigns each import to the first matching group. It does NOT rewrite source.', '',
    f'- Java files inspected: **{file_count}**',
    f'- Count of direct Minecraft imports with old package paths in selected groups: **{sum(counts.values())}**', '',
    '| Group | Imports |', '|---|---:|',
]
for name, value in counts.most_common():
    out.append(f'| {name} | {value} |')
out.extend(['', '## Examples for porting', ''])
for area, _ in counts.most_common():
    out.extend([f'### {area}', ''])
    for path, imp in examples[area]:
        out.append(f'- `{path}` — `{imp}`')
    out.append('')
out += ['## Most repeated Minecraft imports', '']
for imp, count in top.most_common(20):
    out.append(f'- {count} × `{imp}`')
out += ['', 'Do **not** replace packages globally without validating renamed types and methods.',
    'Minecraft 26.3 uses new unobfuscated names and different packages.',
    'See `https://mappings.relativitymc.org/26.3/index.html` and Fabric docs.', '']
p = ROOT / 'IMPORT-AUDIT.md'
p.write_text('\n'.join(out))
print(f'Wrote {p}: {sum(counts.values())} likely legacy imports in selected groups')
