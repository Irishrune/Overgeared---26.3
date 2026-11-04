"""Minecraft 26.3 data-driven steel barter migration (source-level checks).

This checks syntax, tier/tag linkage and the prior barter values; actual
Minecraft trade registration still requires a successful mod build/startup.
"""
import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "common/src/main/resources/data"

def load(relative):
    return json.loads((DATA / relative).read_text(encoding="utf-8"))

class DataDrivenSteelTradeTests(unittest.TestCase):
    def test_steel_for_emeralds(self):
        trade = load("overgeared/villager_trade/smith/steel_to_emeralds.json")
        self.assertEqual(trade["wants"], {"id":"overgeared:steel_ingot","count":1})
        self.assertEqual(trade["gives"], {"id":"minecraft:emerald","count":2})
        self.assertEqual(trade["max_uses"], 12)
        self.assertEqual(trade["xp"], 5)

    def test_emeralds_for_steel(self):
        trade = load("overgeared/villager_trade/smith/emeralds_to_steel.json")
        self.assertEqual(trade["wants"], {"id":"minecraft:emerald","count":2})
        self.assertEqual(trade["gives"], {"id":"overgeared:steel_ingot","count":1})
        self.assertEqual(trade["max_uses"], 12)
        self.assertEqual(trade["xp"], 5)

    def test_merged_smith_level_one_tag(self):
        tag = load("minecraft/tags/villager_trade/common_smith/level_1.json")
        self.assertFalse(tag["replace"], "Must preserve vanilla smith trades")
        self.assertEqual(set(tag["values"]), {
            "overgeared:smith/steel_to_emeralds",
            "overgeared:smith/emeralds_to_steel"
        })

    def test_trade_fields_target_263(self):
        for filename in ("steel_to_emeralds", "emeralds_to_steel"):
            trade = load(f"overgeared/villager_trade/smith/{filename}.json")
            self.assertEqual(set(trade.keys()), {
                "wants","gives","max_uses","xp","reputation_discount"
            })
            self.assertEqual(trade["reputation_discount"], 0.05)
            self.assertTrue(trade["wants"]["count"] > 0)
            self.assertLessEqual(trade["wants"]["count"], 64)

if __name__ == "__main__":
    unittest.main()
