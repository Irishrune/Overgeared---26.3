"""Source checks for Minecraft 26.3 generated-quality forged trade."""
import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "common/src/main/java/net/stirdrem/overgeared"
DATA = ROOT / "common/src/main/resources/data"

class ForgedTradeQualityTests(unittest.TestCase):
    def test_registered_before_data_load(self):
        entry = (JAVA / "OvergearedMod.java").read_text(encoding="utf-8")
        self.assertIn("ForgedTradeQualityFunction.register();", entry)
        self.assertLess(entry.index("ModComponents.register();"),
                        entry.index("ForgedTradeQualityFunction.register();"))

    def test_copper_sword_uses_one_roll_for_quality_and_price(self):
        source = (JAVA / "loot/ForgedTradeQualityFunction.java").read_text(encoding="utf-8")
        self.assertIn("ForgingQualityHelper.rollQuality(context.getRandom(), villagerLevel)", source)
        self.assertIn("ForgingQualityHelper.applyQuality(stack, quality)", source)
        self.assertIn("DataComponents.ADDITIONAL_TRADE_COST", source)
        self.assertIn("ForgingQualityHelper.getDurabilityMultiplier(quality)", source)
        self.assertIn("BuiltInRegistries.LOOT_FUNCTION_TYPE", source)

    def test_263_trade_schema(self):
        path = DATA / "overgeared/villager_trade/weaponsmith/1/copper_sword.json"
        trade = json.loads(path.read_text(encoding="utf-8"))
        self.assertEqual(trade["wants"], {"id":"minecraft:emerald","count":8})
        self.assertEqual(trade["gives"], {"id":"overgeared:copper_sword","count":1})
        self.assertEqual(trade["given_item_modifier"], {
            "type":"overgeared:set_forged_trade_quality","villager_level":1,"base_price":8
        })
        self.assertNotIn("given_item_modifiers", trade)
        self.assertEqual((trade["max_uses"],trade["xp"]),(12,2))

    def test_weaponsmith_tag_preserves_vanilla(self):
        tag=json.loads((DATA / "minecraft/tags/villager_trade/weaponsmith/level_1.json").read_text(encoding="utf-8"))
        self.assertFalse(tag["replace"])
        self.assertIn("overgeared:weaponsmith/1/copper_sword",tag["values"])

    def test_all_original_forged_items_are_data_driven(self):
        """Keep all equipment choices and profession levels from the old Java trade lists."""
        groups = {
            "weaponsmith": (
                ["iron_sword_blade","golden_sword_blade","steel_sword_blade",
                 "copper_sword_blade","copper_sword","steel_sword"]
            ),
            "toolsmith": (
                [f"{m}_{part}_head" for part in ("pickaxe","axe","shovel","hoe")
                 for m in ("iron","golden","steel","copper")]
                + [f"{m}_{part}" for m in ("copper","steel")
                   for part in ("pickaxe","axe","shovel","hoe")]
            ),
            "armorer": (
                [f"{m}_{part}" for m in ("copper","steel")
                   for part in ("helmet","chestplate","leggings","boots")]
            )
        }
        generated = set()
        for profession, names in groups.items():
            for item in names:
                level, uses, xp, base = (
                    (1,12,2,8) if item.startswith("copper_") else
                    (2,10,5,14) if item.startswith("iron_") else
                    (2,10,5,16) if item.startswith("golden_") else
                    (3,6,10,24)
                )
                path = DATA / f"overgeared/villager_trade/{profession}/{level}/{item}.json"
                trade = json.loads(path.read_text(encoding="utf-8"))
                self.assertEqual(trade["gives"]["id"], f"overgeared:{item}")
                self.assertEqual(trade["wants"], {"id":"minecraft:emerald","count":base})
                self.assertEqual(trade["given_item_modifier"], {
                    "type":"overgeared:set_forged_trade_quality",
                    "villager_level":level, "base_price":base
                })
                self.assertEqual((trade["max_uses"],trade["xp"]),(uses,xp))
                self.assertNotIn("given_item_modifiers", trade)
                tagfile = DATA / f"minecraft/tags/villager_trade/{profession}/level_{level}.json"
                tag = json.loads(tagfile.read_text(encoding="utf-8"))
                self.assertFalse(tag["replace"])
                self.assertIn(f"overgeared:{profession}/{level}/{item}", tag["values"])
                generated.add(str(path))
        self.assertEqual(len(generated), 38)

if __name__ == "__main__":
    unittest.main()
