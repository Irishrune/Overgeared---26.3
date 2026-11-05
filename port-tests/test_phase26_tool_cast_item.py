"""Source guards for Minecraft 26.3 modular tool-cast item interaction.

These do not replace a real cast insertion/recovery/play test.
"""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
SRC=(ROOT/"common/src/main/java/net/stirdrem/overgeared/item/custom/ToolCastItem.java").read_text(encoding="utf-8")

class ToolCast26_3Tests(unittest.TestCase):
    def test_held_cast_material_recovery_uses_new_interaction_result(self):
        self.assertIn("public InteractionResult use(Level level, Player player, InteractionHand hand)", SRC)
        self.assertIn("private InteractionResult calculateAndReturnMaterials(", SRC)
        self.assertIn("return InteractionResult.SUCCESS;", SRC)
        self.assertIn("return InteractionResult.FAIL;", SRC)
        self.assertNotIn("InteractionResultHolder", SRC)
        self.assertIn("castStack.set(ModComponents.CAST_DATA.get(), data.cleared())", SRC)
        self.assertIn("castStack.set(ModComponents.CAST_DATA.get(), data.withoutInputs())", SRC)

    def test_cast_insertion_and_material_returns_remain(self):
        self.assertIn("public boolean overrideStackedOnOther(", SRC)
        self.assertIn("public boolean overrideOtherStackedOnMe(", SRC)
        self.assertIn("insertMaterial(castStack, slotStack, player)", SRC)
        self.assertIn("insertMaterial(castStack, otherStack, player)", SRC)
        self.assertIn("data.withAddedMaterial(mat, value).withAddedInput(singleMaterial)", SRC)
        self.assertIn("slot.remove(1);", SRC)
        self.assertIn("otherStack.shrink(1);", SRC)
        self.assertIn("player.drop(output.copy(), false, Prediction.SERVER_ONLY)", SRC)
        self.assertIn("player.drop(inputItem.copy(), false, Prediction.SERVER_ONLY)", SRC)

    def test_263_tooltip_consumer_retains_cast_readout(self):
        self.assertIn("Item.TooltipContext context, TooltipDisplay tooltipDisplay,", SRC)
        self.assertIn("Consumer<Component> tooltip, TooltipFlag flag)", SRC)
        self.assertIn("super.appendHoverText(stack, context, tooltipDisplay, tooltip, flag)", SRC)
        self.assertIn("tooltip.accept(", SRC)
        self.assertNotIn("tooltip.add(", SRC)
        self.assertIn("tooltip.overgeared.tool_cast.quality", SRC)
        self.assertIn("tooltip.overgeared.tool_cast.materials", SRC)
        self.assertIn("tooltip.overgeared.tool_cast.amount", SRC)
        self.assertIn("tooltip.overgeared.cast_right_click", SRC)

    def test_cast_name_and_server_feedback(self):
        self.assertIn('super.getDescriptionId().replace("item", "cast")', SRC)
        self.assertIn("serverPlayer.sendSystemMessage(Component.translatable", SRC)
        self.assertNotIn("player.displayClientMessage", SRC)

if __name__=="__main__":
    unittest.main()
