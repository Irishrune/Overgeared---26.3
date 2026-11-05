"""Minecraft 26.3 material and fletching compatibility source guards."""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]/"common/src/main/java/net/stirdrem/overgeared"
FORGING=(ROOT/"recipe/ForgingRecipe.java").read_text(encoding="utf-8")
FLETCHING=(ROOT/"screen/FletchingStationMenu.java").read_text(encoding="utf-8")

class ForgeQuenchingAndFletchingTests(unittest.TestCase):
    def test_armor_quenching_is_component_based(self):
        self.assertIn("result.create().get(DataComponents.EQUIPPABLE)", FORGING)
        self.assertIn("equippable != null && equippable.slot().isArmor()", FORGING)
        self.assertIn("actualNeedQuenching = false", FORGING)
        self.assertNotIn("instanceof ArmorItem", FORGING)
        self.assertNotIn("import net.minecraft.world.item.ArmorItem;", FORGING)

    def test_fletching_potion_lookup_is_263_compatible(self):
        self.assertIn("import org.jetbrains.annotations.Nullable;", FLETCHING)
        self.assertNotIn("import javax.annotation.Nullable;", FLETCHING)
        self.assertIn("BuiltInRegistries.POTION.getValue(location)", FLETCHING)
        self.assertIn("BuiltInRegistries.POTION.wrapAsHolder(potion)", FLETCHING)
        self.assertIn("return Potions.WATER;", FLETCHING)

if __name__=="__main__":
    unittest.main()
