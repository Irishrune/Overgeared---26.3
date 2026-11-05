"""Regression checks for 26.3 knapping and custom recipe migrations.

These source tests must be followed by a real Java compilation and in-game checks.
"""
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]/"common/src/main/java/net/stirdrem/overgeared"
ROCK=(ROOT/"item/custom/KnappableRockItem.java").read_text(encoding="utf-8")
NETHER=(ROOT/"recipe/NetherAlloySmeltingRecipe.java").read_text(encoding="utf-8")

class Phase27KnappingAndRecipeTests(unittest.TestCase):
    def test_knapping_preserves_two_handed_rock_entry(self):
        self.assertIn("public InteractionResult use(Level level, Player player, InteractionHand hand)",ROCK)
        self.assertIn("mainHand.getItem() instanceof KnappableRockItem",ROCK)
        self.assertIn("offHand.getItem() instanceof KnappableRockItem",ROCK)
        self.assertIn("Menus.openMenu(serverPlayer, new RockKnappingMenuProvider()",ROCK)
        self.assertIn("ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, mainHand)",ROCK)
        self.assertIn("ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, offHand)",ROCK)
        self.assertIn("return InteractionResult.PASS;",ROCK)
        self.assertIn("return InteractionResult.SUCCESS;",ROCK)
        self.assertNotIn("InteractionResultHolder",ROCK)

    def test_knapping_tooltip_uses_263_component_consumer(self):
        self.assertIn("TooltipDisplay display,",ROCK)
        self.assertIn("Consumer<Component> tooltipComponents",ROCK)
        self.assertIn("tooltipComponents.accept(",ROCK)
        self.assertIn("super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag)",ROCK)
        self.assertIn("tooltip.overgeared.knappable",ROCK)

    def test_nether_alloy_recipe_has_one_override(self):
        self.assertNotIn("@Override\n    @Override",NETHER)
        self.assertIn("public String group()",NETHER)
        self.assertIn("getIngredientsList()",NETHER)
        self.assertIn("return result.create()",NETHER)

if __name__=="__main__":
    unittest.main()
