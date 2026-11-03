import json
import re
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ITEMS = (ROOT/'common/src/main/java/net/stirdrem/overgeared/item/ModItems.java').read_text()

class TestEquipmentPort(unittest.TestCase):
    def test_equipment_uses_263_components(self):
        for mat in ('STEEL','COPPER'):
            for kind in ('SWORD','PICKAXE','AXE','HOE','SHOVEL'):
                self.assertIn(f'{mat}_{kind} = registerItem(', ITEMS)
                # The registration field must lead to the proper .tool property
                p = ITEMS.split(f'{mat}_{kind} = registerItem(',1)[1].split(');',1)[0]
                self.assertIn(f'.{kind.lower()}(ModToolTiers.{mat}',p)
            for kind in ('HELMET','CHESTPLATE','LEGGINGS','BOOTS'):
                p=ITEMS.split(f'{mat}_{kind} = registerItem(',1)[1].split(');',1)[0]
                self.assertIn(f'.humanoidArmor(ModArmorMaterials.{mat}, ArmorType.{kind})',p)
    def test_tools_are_component_based(self):
        self.assertNotIn('new SwordItem(',ITEMS)
        self.assertNotIn('new PickaxeItem(',ITEMS)
        self.assertNotIn('new ArmorItem(',ITEMS)
        self.assertIn('.tool(ToolMaterial.WOOD,', ITEMS)
        self.assertIn('.tool(ModToolTiers.COPPER,', ITEMS)
        self.assertIn('.tool(ModToolTiers.STEEL,', ITEMS)
        hammer=(ROOT/'common/src/main/java/net/stirdrem/overgeared/item/custom/SmithingHammer.java').read_text()
        tongs=(ROOT/'common/src/main/java/net/stirdrem/overgeared/item/custom/Tongs.java').read_text()
        self.assertIn('stack.hurtAndBreak(2, entity, EquipmentSlot.MAINHAND)',hammer)
        self.assertIn('stack.hurtAndBreak(2, entity, EquipmentSlot.MAINHAND)',tongs)
    def test_repair_and_mining_tags(self):
        for mat,repair,parent in [('steel','overgeared:steel_ingot','iron'),('copper','minecraft:copper_ingot','stone')]:
            base=ROOT/'common/src/main/resources/data/overgeared/tags'
            self.assertEqual(json.loads((base/'item'/f'repairs_{mat}_tools.json').read_text())['values'],[repair])
            self.assertEqual(json.loads((base/'block'/f'incorrect_for_{mat}_tool.json').read_text())['values'],[f'#minecraft:incorrect_for_{parent}_tool'])
    def test_equipment_assets_resolve_vanilla_fallbacks(self):
        for mat,texture in [('steel','minecraft:iron'),('copper','minecraft:copper')]:
            p=ROOT/'common/src/main/resources/assets/overgeared/equipment'/f'{mat}.json'
            data=json.loads(p.read_text())
            self.assertEqual(set(data['layers']),{'humanoid','humanoid_baby','humanoid_leggings'})
            for k in data['layers']:self.assertEqual(data['layers'][k],[{'texture':texture}])
    def test_legacy_armor_renderer_unwired(self):
        client=(ROOT/'fabric/src/main/java/net/stirdrem/overgeared/fabric/client/OvergearedFabricClient.java').read_text()
        self.assertNotIn('registerArmorModel(',client)
        self.assertNotIn('ArmorRenderer.register(',client)

if __name__ == '__main__': unittest.main()
