"""Minecraft 26.3 arrow class routing and native potion-tint resource checks.

All four arrow variants must keep their dynamic potion coloring after removal
of the legacy item ColorProviderRegistry. These tests don't test visual output.
"""
from pathlib import Path
import unittest
import json

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/"common/src/main/java/net/stirdrem/overgeared"
ASSETS=ROOT/"common/src/main/resources/assets/overgeared/items"
CLIENT=(ROOT/"fabric/src/main/java/net/stirdrem/overgeared/fabric/client/OvergearedFabricClient.java").read_text(encoding="utf-8")

class ArrowModel26_3Tests(unittest.TestCase):
    def test_projectile_paths_use_263_arrow_package(self):
        for file in ("entity/custom/UpgradeArrowEntity.java","entity/custom/LingeringArrowEntity.java",
                     "item/custom/UpgradeArrowItem.java","item/custom/LingeringArrowItem.java",
                     "block/UpgradeArrowDispenseBehavior.java"):
            with self.subTest(file=file):
                src=(JAVA/file).read_text(encoding="utf-8")
                self.assertIn("import net.minecraft.world.entity.projectile.arrow.AbstractArrow;",src)
                self.assertNotIn("import net.minecraft.world.entity.projectile.AbstractArrow;",src)

    def test_all_arrows_use_vanilla_potion_tint_source(self):
        for name in ("iron_arrow","steel_arrow","diamond_arrow","lingering_arrow"):
            with self.subTest(name=name):
                data=json.loads((ASSETS/(name+".json")).read_text(encoding="utf-8"))
                model=data['model']
                if name != 'lingering_arrow':
                    self.assertEqual('minecraft:range_dispatch',model['type'])
                    self.assertEqual('overgeared:potion_type',model['property'])
                    self.assertEqual('overgeared:item/'+name,model['fallback']['model'])
                    self.assertNotIn('tints',model['fallback'])
                    self.assertEqual([1,2],[entry['threshold'] for entry in model['entries']])
                    variants=[entry['model'] for entry in model['entries']]
                else: variants=[model]
                for variant in variants:
                    self.assertEqual('minecraft:model',variant['type'])
                    self.assertEqual([{'type':'minecraft:constant','value':-1},
                                      {'type':'minecraft:potion','default':-1}],variant['tints'])

    def test_fabric_client_does_not_use_removed_color_api(self):
        self.assertNotIn("ColorProviderRegistry.ITEM.register(",CLIENT)
        self.assertNotIn("import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;",CLIENT)
        self.assertNotIn("EntityModelLayerRegistry",CLIENT)
        self.assertIn("registerRenderers()",CLIENT)

if __name__=="__main__":
    unittest.main()
