"""Minecraft 26.3 material and fletching compatibility source guards."""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]/"common/src/main/java/net/stirdrem/overgeared"
FORGING=(ROOT/"recipe/ForgingRecipe.java").read_text(encoding="utf-8")
FLETCHING=(ROOT/"screen/FletchingStationMenu.java").read_text(encoding="utf-8")
INTERACTIONS=(ROOT/"event/ModItemInteractEvents.java").read_text(encoding="utf-8")

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

    def test_fletching_leftovers_use_server_drop_prediction(self):
        self.assertIn("player.drop(stack, false, Prediction.SERVER_ONLY)", FLETCHING)
        self.assertIn("import net.minecraft.util.Prediction;", FLETCHING)

    def test_item_interactions_use_new_swing_and_drop_apis(self):
        self.assertIn("player.swing(event.getHand(), SwingAnimation.DEFAULT, true)", INTERACTIONS)
        self.assertIn("event.getHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND", INTERACTIONS)
        self.assertIn("SoundEvents.ITEM_BREAK.value()", INTERACTIONS)
        self.assertIn("SoundEvents.STONE_HIT.value()", INTERACTIONS)
        for variable in ("polishedItem", "cooledIngot", "resultArrow"):
            self.assertIn(f"player.drop({variable}, false, Prediction.SERVER_ONLY)", INTERACTIONS)
        self.assertNotIn("InteractionResult.sidedSuccess(", INTERACTIONS)
        self.assertIn('syncData.getLongOr("anvilPos", 0L)', INTERACTIONS)

if __name__=="__main__":
    unittest.main()
