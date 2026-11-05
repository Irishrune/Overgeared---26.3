#!/usr/bin/env python3
"""Generate Minecraft 26.3 item definitions, item models and blockstates.

Overgeared's upstream multiloader branch ships textures and block geometry but
omits the item model definitions that 1.21.4+ requires. This deterministic
source-data migration preserves the original textures; it does not replace the
custom block models or invent new textures. Only registered item/block IDs are
used. Can run in --check to detect drift without modifying files.
"""
from __future__ import annotations

import argparse
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'common/src/main/resources/assets/overgeared'
ITEM_REGISTRY = ROOT / 'common/src/main/java/net/stirdrem/overgeared/item/ModItems.java'
BLOCK_REGISTRY = ROOT / 'common/src/main/java/net/stirdrem/overgeared/block/ModBlocks.java'

FURNACES = {'alloy_furnace', 'nether_alloy_furnace', 'casting_furnace'}
ANVILS = {'smithing_anvil', 'tier_a_smithing_anvil', 'tier_b_smithing_anvil', 'stone_anvil'}
TOOLS = {'sword', 'axe', 'pickaxe', 'shovel', 'hoe', 'hammer', 'tongs', 'tong', 'blade', 'head'}


def registered_items() -> set[str]:
    source = ITEM_REGISTRY.read_text(encoding='utf-8')
    return set(re.findall(r'\b(?:registerSimpleItem|registerItem)\(\s*"([a-z_0-9]+)"', source))


def registered_blocks() -> set[str]:
    source = BLOCK_REGISTRY.read_text(encoding='utf-8')
    return set(re.findall(r'\bregisterBlock\("([a-z_0-9]+)"', source))


def json_text(data: dict) -> str:
    return json.dumps(data, indent=2, ensure_ascii=False) + '\n'


def blockstate_variants(block: str) -> dict:
    if block == 'steel_block':
        return {'variants': {'': {'model': 'overgeared:block/steel_block'}}}
    if block not in FURNACES | ANVILS:
        raise ValueError(f'Unexpected block with no blockstate template: {block}')
    variants = {}
    rotation = {'north': 0, 'east': 90, 'south': 180, 'west': 270}
    for facing, angle in rotation.items():
        for lit in (False, True) if block in FURNACES else (None,):
            model = f'overgeared:block/{block}'
            key = f'facing={facing}'
            if lit is not None:
                key += f',lit={str(lit).lower()}'
                if lit:
                    model += '_on'
            variant = {'model': model}
            if angle:
                variant['y'] = angle
            variants[key] = variant
    return {'variants': variants}


def assets() -> dict[Path, dict]:
    blocks = registered_blocks()
    items = registered_items()
    if len(items) < 60 or len(blocks) != 9:
        raise ValueError('Unexpected upstream registry structure; do not emit incomplete assets')
    result: dict[Path, dict] = {}
    for item in sorted(items):
        layers = {}
        texture = RES / 'textures/item' / f'{item}.png'
        if texture.exists():
            layers['layer0'] = f'overgeared:item/{item}'
        elif item == 'lingering_arrow':
            # This special item ships two textures, not a single icon.
            for index, suffix in enumerate(('base', 'head')):
                part = f'lingering_arrow_{suffix}'
                if not (RES / f'textures/item/{part}.png').exists():
                    raise FileNotFoundError(part)
                layers[f'layer{index}'] = f'overgeared:item/{part}'
        else:
            raise FileNotFoundError(f'Missing icon texture for registered item: {item}')
        handheld = any(item.endswith('_' + suffix) for suffix in TOOLS) or item.endswith('_sword')
        result[RES / f'models/item/{item}.json'] = {
            'parent': 'minecraft:item/handheld' if handheld else 'minecraft:item/generated',
            'textures': layers,
        }
        client_model = {'type': 'minecraft:model', 'model': f'overgeared:item/{item}'}
        if item in {'lingering_arrow', 'iron_arrow', 'steel_arrow', 'diamond_arrow'}:
            # ColorProviderRegistry.ITEM was removed in 1.21.4. Vanilla's
            # potion tint source reads the stack's POTION_CONTENTS component,
            # preserving the original per-arrow tint behavior without Java
            # client color hooks. White (-1) means no potion color applied.
            client_model['tints'] = [{'type': 'minecraft:potion', 'default': -1}]
        if item in {'iron_arrow', 'steel_arrow', 'diamond_arrow'}:
            variants = []
            for threshold, variant in ((1, 'tipped'), (2, 'lingering')):
                name = f'{variant}_{item}'
                result[RES / f'models/item/{name}.json'] = {
                    'parent': 'minecraft:item/generated',
                    'textures': {'layer0': f'overgeared:item/{name}_base', 'layer1': f'overgeared:item/{name}_head'},
                }
                variants.append({'threshold': threshold, 'model': {
                    'type': 'minecraft:model', 'model': f'overgeared:item/{name}',
                    'tints': [{'type': 'minecraft:constant', 'value': -1}, {'type': 'minecraft:potion', 'default': -1}],
                }})
            client_model.pop('tints', None)
            client_model = {'type': 'minecraft:range_dispatch', 'property': 'overgeared:potion_type',
                            'fallback': client_model, 'entries': variants}
        elif item == 'lingering_arrow':
            client_model['tints'] = [{'type': 'minecraft:constant', 'value': -1}, {'type': 'minecraft:potion', 'default': -1}]
        result[RES / f'items/{item}.json'] = {'model': client_model}

    # BlockItem instances are registered in ModBlocks; these refer to block
    # geometry rather than generating sprite-only inventory representations.
    for block in sorted(blocks):
        blockmodel = RES / f'models/block/{block}.json'
        if block == 'steel_block':
            texture = RES / 'textures/block/steel_block.png'
            if not texture.is_file():
                raise FileNotFoundError(texture)
            result[blockmodel] = {
                'parent': 'minecraft:block/cube_all',
                'textures': {'all': 'overgeared:block/steel_block'},
            }
        elif not blockmodel.is_file():
            raise FileNotFoundError(f'Existing block model missing: {blockmodel}')
        result[RES / f'items/{block}.json'] = {
            'model': {'type': 'minecraft:model', 'model': f'overgeared:block/{block}'}
        }
        state = RES / f'blockstates/{block}.json'
        if block != 'drafting_table':
            # Drafting table has a handmade blockstate. Other blockstates are
            # deterministic and checked on every run, not only when missing.
            result[state] = blockstate_variants(block)
    return result


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--check', action='store_true', help='exit nonzero if assets are missing/outdated')
    args = parser.parse_args()
    expected = assets()
    changed = []
    for path, data in expected.items():
        desired = json_text(data)
        if not path.exists() or path.read_text(encoding='utf-8') != desired:
            changed.append(path)
            if not args.check:
                path.parent.mkdir(parents=True, exist_ok=True)
                path.write_text(desired, encoding='utf-8')
    print(f'26.3 assets: {len(expected)} generated definitions, {len(changed)} ' + ('out of date' if args.check else 'written'))
    if args.check and changed:
        for path in changed[:20]:
            print('  drift:', path.relative_to(ROOT))
        raise SystemExit(1)


if __name__ == '__main__':
    main()
