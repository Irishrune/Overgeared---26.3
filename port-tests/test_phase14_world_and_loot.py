"""Source guards for 26.3 private Level field and loot integer provider migration.

CI additionally runs the real Gradle / Java 25 compiler against Minecraft 26.3.
"""
from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]
COMMON = ROOT / "common/src/main/java/net/stirdrem/overgeared"
TARGETS = [
    "block/entity/AbstractSmithingAnvilBlockEntity.java",
    "block/entity/AlloySmelterBlockEntity.java",
    "block/entity/CastingSmelterBlockEntity.java",
    "block/entity/NetherAlloySmelterBlockEntity.java",
    "block/entity/StoneSmithingAnvilBlockEntity.java",
    "block/custom/AbstractSmithingAnvil.java",
    "block/custom/BlueprintWorkbenchBlock.java",
    "block/custom/SteelSmithingAnvil.java",
    "block/custom/StoneSmithingAnvil.java",
    "block/custom/TierASmithingAnvil.java",
    "block/custom/TierBSmithingAnvil.java",
    "event/ModItemInteractEvents.java",
    "heateditem/HeatedItem.java",
    "item/custom/KnappableRockItem.java",
    "item/custom/ToolCastItem.java",
    "mixin/ContainerCoolingMixin.java",
    "mixin/InventoryCoolingMixin.java",
    "mixin/MobHeatedItemMixin.java",
    "mixin/ItemEntityCoolingMixin.java",
    "screen/AbstractSmithingAnvilMenu.java",
    "screen/RockKnappingMenu.java"
]

class Phase14WorldLootApiTests(unittest.TestCase):
    def test_uses_public_level_side_method(self):
        for path in TARGETS:
            source = (COMMON / path).read_text(encoding="utf-8")
            with self.subTest(path=path):
                self.assertIsNone(re.search(r"\.isClientSide\b(?!\s*\()", source))
                self.assertIn(".isClientSide()", source)

    def test_nullable_import_uses_active_annotation(self):
        source = (COMMON / "event/ModItemInteractEvents.java").read_text(encoding="utf-8")
        self.assertIn("org.jetbrains.annotations.Nullable",source)
        self.assertNotIn("javax.annotation.Nullable",source)

    def test_loot_rolls_use_new_integer_provider(self):
        source = (ROOT / "fabric/src/main/java/net/stirdrem/overgeared/fabric/OvergearedFabric.java").read_text(encoding="utf-8")
        self.assertIn("number.ints.ConstantValue",source)
        self.assertIn("Holder.direct(new ConstantValue(1))",source)
        self.assertNotIn("ConstantValue.exactly(1)",source)

if __name__ == "__main__":
    unittest.main()
