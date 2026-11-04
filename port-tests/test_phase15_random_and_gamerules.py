"""Minecraft 26.3 world random source and typed GameRules migrations."""
from pathlib import Path
import re
import unittest

ROOT=Path(__file__).resolve().parents[1]
COMMON=ROOT/"common/src/main/java/net/stirdrem/overgeared"
TARGETS=[
    "event/ModItemInteractEvents.java",
    "block/entity/AlloySmelterBlockEntity.java",
    "block/entity/NetherAlloySmelterBlockEntity.java",
    "block/entity/CastingSmelterBlockEntity.java",
    "util/QualityBreak.java"
]

class WorldRandomAndGameRulesTests(unittest.TestCase):
    def test_random_field_is_replaced_with_public_accessor(self):
        for path in TARGETS:
            source=(COMMON/path).read_text(encoding="utf-8")
            with self.subTest(path=path):
                self.assertIsNone(re.search(r"\.random\b(?!\s*\()",source))
                self.assertIn(".getRandom()",source)

    def test_limited_crafting_uses_typed_game_rule_from_server(self):
        source=(COMMON/"event/ModItemInteractEvents.java").read_text(encoding="utf-8")
        self.assertIn("net.minecraft.world.level.gamerules.GameRules",source)
        self.assertIn("getGameRules().get(GameRules.LIMITED_CRAFTING)",source)
        self.assertNotIn("RULE_LIMITED_CRAFTING",source)

if __name__=="__main__":
    unittest.main()
