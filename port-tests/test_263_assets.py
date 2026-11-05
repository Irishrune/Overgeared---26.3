"""Static Minecraft 26.3 resource regression checks (do not require Gradle)."""
from __future__ import annotations

import json
import subprocess
import sys
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'common/src/main/resources/assets/overgeared'
sys.path.insert(0, str(ROOT / 'tools'))
from generate_263_assets import assets, registered_blocks, registered_items  # noqa: E402


class AssetTests(unittest.TestCase):
    def test_generated_definitions_match_source(self):
        definitions = assets()
        self.assertEqual(len(definitions), 186)
        for file, obj in definitions.items():
            with self.subTest(file=file.name):
                self.assertTrue(file.is_file(), file)
                self.assertEqual(json.loads(file.read_text('utf-8')), obj)
        result = subprocess.run(
            [sys.executable, str(ROOT / 'tools/generate_263_assets.py'), '--check'],
            capture_output=True, text=True,
        )
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)

    def test_every_registered_item_has_model_definition_and_valid_texture(self):
        for item in registered_items():
            with self.subTest(item=item):
                client = json.loads((ASSETS / f'items/{item}.json').read_text('utf-8'))
                definition = client['model']
                variants = [definition]
                if definition['type'] == 'minecraft:range_dispatch':
                    variants = [definition['fallback']] + [e['model'] for e in definition['entries']]
                self.assertEqual(variants[0]['model'], f'overgeared:item/{item}')
                for variant in variants:
                    model = json.loads((ASSETS / ('models/' + variant['model'].split(':')[1] + '.json')).read_text('utf-8'))
                    for name in model['textures'].values():
                        namespace, path = name.split(':', 1)
                        self.assertEqual(namespace, 'overgeared')
                        self.assertTrue((ASSETS / f'textures/{path}.png').is_file(), name)

    def test_every_registered_block_has_blockstate_and_inventory_definition(self):
        for block in registered_blocks():
            with self.subTest(block=block):
                client = json.loads((ASSETS / f'items/{block}.json').read_text('utf-8'))
                self.assertEqual(client['model']['model'], f'overgeared:block/{block}')
                state = json.loads((ASSETS / f'blockstates/{block}.json').read_text('utf-8'))
                self.assertIn('variants', state)
                for variant in state['variants'].values():
                    self.assertTrue((ASSETS / ('models/' + variant['model'].split(':')[1] + '.json')).is_file(), variant)

    def test_all_json_resources_parse(self):
        for json_file in (ROOT / 'common/src/main/resources').rglob('*.json'):
            with self.subTest(json=json_file.relative_to(ROOT)):
                json.loads(json_file.read_text('utf-8'))


if __name__ == '__main__':
    unittest.main()
