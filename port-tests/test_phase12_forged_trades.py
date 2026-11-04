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
        self.assertIn("ForgingQualityHelper.priceForQuality(quality)", source)
        self.assertIn("BuiltInRegistries.LOOT_FUNCTION_TYPE", source)

    def test_263_trade_schema(self):
        path = DATA / "overgeared/villager_trade/weaponsmith/1/copper_sword.json"
        trade = json.loads(path.read_text(encoding="utf-8"))
        self.assertEqual(trade["wants"], {"id":"minecraft:emerald","count":7})
        self.assertEqual(trade["gives"], {"id":"overgeared:copper_sword","count":1})
        self.assertEqual(trade["given_item_modifier"], {
            "type":"overgeared:set_forged_trade_quality","villager_level":1,"base_price":7
        })
        self.assertNotIn("given_item_modifiers", trade)
        self.assertEqual((trade["max_uses"],trade["xp"]),(12,2))

    def test_weaponsmith_tag_preserves_vanilla(self):
        tag=json.loads((DATA / "minecraft/tags/villager_trade/weaponsmith/level_1.json").read_text(encoding="utf-8"))
        self.assertFalse(tag["replace"])
        self.assertIn("overgeared:weaponsmith/1/copper_sword",tag["values"])

if __name__ == "__main__":
    unittest.main()
