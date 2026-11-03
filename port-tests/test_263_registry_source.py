"""Source-level checks for Minecraft 26.3 registry-key placement.

These checks catch accidental regressions in the adapted source; they cannot
prove the complete mod compiles against Minecraft's real API.
"""
from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'common/src/main/java/net/stirdrem/overgeared'


class RegistrySourceTests(unittest.TestCase):
    def test_block_factories_apply_registry_key(self):
        source = (JAVA / 'block/ModBlocks.java').read_text()
        blocks = re.findall(
            r'public static final DeferredBlock<[^\n]+?= registerBlock\("([^\"]+)",'
            r'(.*?)\);', source, re.S)
        self.assertEqual(9, len(blocks))
        for name, factory in blocks:
            with self.subTest(block=name):
                self.assertIn('key ->', factory)
                self.assertIn('.setId(key)', factory)
        self.assertIn('BLOCKS.registerWithKey(name, block)', source)
        self.assertIn('ModItems.ITEMS.registerWithKey(name, key ->', source)
        self.assertIn('.useBlockDescriptionPrefix().setId(key)', source)

    def test_named_items_get_registry_key_before_construction(self):
        source = (JAVA / 'item/ModItems.java').read_text()
        items = re.findall(r'public static final DeferredItem<[^\n]+?= registerItem\("([^\"]+)",(.*?)\);', source, re.S)
        self.assertEqual(32, len(items))
        for name, factory in items:
            with self.subTest(item=name):
                self.assertIn('key ->', factory)
                if name == 'diamond_upgrade_smithing_template':
                    self.assertIn('DiamondUpgradeTemplateItem.createDiamondUpgradeTemplate(key)', factory)
                else:
                    self.assertIn('.setId(key)', factory)
        self.assertIn('ITEMS.registerWithKey(name, itemFactory)', source)

    def test_simple_and_custom_item_factories_are_key_aware(self):
        source = (JAVA / 'registry/DeferredRegister.java').read_text()
        self.assertIn('factory.apply(properties.setId(key))', source)
        self.assertIn('new BlockItem(block.get(), properties.useBlockDescriptionPrefix().setId(key))', source)
        self.assertIn('return registerSimpleItem(name, new Item.Properties())', source)
        self.assertIn('return register(name, id -> factory.apply(ResourceKey.create(Registries.ITEM, id)))', source)

    def test_template_uses_confirmed_263_constructor(self):
        source = (JAVA / 'item/custom/DiamondUpgradeTemplateItem.java').read_text()
        block = source.split('public static SmithingTemplateItem createDiamondUpgradeTemplate(', 1)[1].split('\n  private static ', 1)[0]
        self.assertIn('new Item.Properties().setId(key)', block)
        self.assertNotIn('DIAMOND_UPGRADE,', block)
        self.assertIn('net.minecraft.world.item.SmithingTemplateItem', source)


if __name__ == '__main__':
    unittest.main()
