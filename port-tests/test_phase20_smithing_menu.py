"""Minecraft 26.3 smithing-anvil menu source regression checks.

These verify that porting the recipe-book interfaces does not silently remove
the manually placed forging grid, blueprint/hammer slots or safe inventory
transfer. They do not replace actual Minecraft GUI or server tests.
"""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
MENU = (ROOT / "common/src/main/java/net/stirdrem/overgeared/screen/AbstractSmithingAnvilMenu.java").read_text(encoding="utf-8")


class SmithingAnvilRecipeBook26_3Tests(unittest.TestCase):
    def test_new_recipe_book_menu_contract(self):
        self.assertIn("extends RecipeBookMenu {", MENU)
        self.assertNotIn("extends RecipeBookMenu<", MENU)
        self.assertIn("public PostPlaceAction handlePlacement(boolean useMaxItems, boolean allowDroppingItemsToClear,", MENU)
        self.assertIn("RecipeHolder<?> recipeHolder, ServerLevel serverLevel,", MENU)
        self.assertIn("fillCraftSlotsStackedContents(StackedItemContents contents)", MENU)
        self.assertIn("void fillStackedContents(StackedItemContents contents)", MENU)
        self.assertIn("contents.accountSimpleStack(stack)", MENU)
        self.assertNotIn("new ServerPlaceRecipe", MENU)

    def test_hammer_blueprint_and_output_remain_distinct(self):
        self.assertIn("iItemHandler, 9, 152, 61", MENU)
        self.assertIn("iItemHandler, 11, 95, 53", MENU)
        self.assertIn("iItemHandler, 10, 124, 35", MENU)
        self.assertIn("craftingSlotIndices.add(slotIndex.index)", MENU)
        self.assertIn("return stack.is(ModTags.Items.SMITHING_HAMMERS)", MENU)
        self.assertIn("return false;", MENU)

    def test_placement_preflights_all_ingredients_before_mutations(self):
        self.assertIn("int[] remaining = new int[this.slots.size()]", MENU)
        self.assertIn("java.util.Arrays.fill(sourceSlot, -1)", MENU)
        self.assertIn("remaining[i]--;", MENU)
        self.assertIn("if (matchingSource < 0) return PostPlaceAction.PLACE_GHOST_RECIPE", MENU)
        self.assertIn("if (target.hasItem())", MENU)
        self.assertIn("Slot from = this.slots.get(src)", MENU)
        self.assertIn("ItemStack one = from.remove(1)", MENU)
        self.assertIn("this.broadcastChanges();", MENU)
        self.assertNotIn("clearContainer(this.player, this.craftingContainer)", MENU)
        self.assertLess(MENU.index("if (matchingSource < 0)"), MENU.index("ItemStack one = from.remove(1)"))

    def test_result_craft_callback_matches_new_stack_api(self):
        self.assertIn("stack.onCraftedBy(AbstractSmithingAnvilMenu.this.player, this.removeCount)", MENU)
        self.assertIn("firePlayerCraftingEvent(", MENU)
        self.assertIn("getCurrentRecipeHolder().ifPresent", MENU)


if __name__ == "__main__":
    unittest.main()
