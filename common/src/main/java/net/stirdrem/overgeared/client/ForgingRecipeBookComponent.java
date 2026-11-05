package net.stirdrem.overgeared.client;

import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.*;
import net.stirdrem.overgeared.screen.AbstractSmithingAnvilMenu;
import java.util.List;

public class ForgingRecipeBookComponent extends RecipeBookComponent<AbstractSmithingAnvilMenu> {
    private static final WidgetSprites FILTER = new WidgetSprites(
        OvergearedMod.loc("recipe_book/anvil_enabled"), OvergearedMod.loc("recipe_book/anvil_disabled"),
        OvergearedMod.loc("recipe_book/anvil_enabled_highlighted"), OvergearedMod.loc("recipe_book/anvil_disabled_highlighted"));
    public ForgingRecipeBookComponent(AbstractSmithingAnvilMenu menu) {
        super(menu, List.of(new TabInfo(SearchRecipeBookCategory.valueOf("OVERGEARED_FORGING")),
            new TabInfo(ModItems.IRON_PICKAXE_HEAD.get(), ForgingRecipeCategories.TOOLS),
            new TabInfo(Items.IRON_CHESTPLATE, ForgingRecipeCategories.ARMORS),
            new TabInfo(Items.ANVIL, ForgingRecipeCategories.MISC)));
    }
    public void hide() { setVisible(false); }
    @Override protected WidgetSprites getFilterButtonTextures() { return FILTER; }
    @Override protected Component getRecipeFilterName() { return Component.translatable("overgeared.container.recipe_book.forgeable"); }
    @Override protected boolean isCraftingSlot(Slot slot) {
        return slot == menu.getResultSlot() || menu.getInputSlots().contains(slot.index);
    }
    private boolean canDisplay(RecipeDisplay display) {
        if (!(display instanceof ForgingRecipeDisplay forging)) return false;
        AnvilTier tier = AnvilTier.fromDisplayName(forging.tier());
        return tier != null && menu.getBlockEntity().getAnvilTier().isAtLeast(tier);
    }
    @Override protected void selectMatchingRecipes(RecipeCollection collection, StackedItemContents contents) {
        collection.selectRecipes(contents, this::canDisplay);
    }
    @Override protected void fillGhostRecipe(GhostSlots ghosts, RecipeDisplay display, ContextMap context) {
        if (!canDisplay(display)) return;
        var grid = ((ForgingRecipeDisplay)display).grid();
        ghosts.setResult(menu.getResultSlot(), context, grid.result());
        // Server placement uses the top-left origin; ghosts must use the same coordinates.
        for (int y = 0; y < grid.height(); y++) for (int x = 0; x < grid.width(); x++) {
            ghosts.setInput(menu.slots.get(menu.getInputSlots().get(y * 3 + x)), context, grid.ingredients().get(y * grid.width() + x));
        }
    }
}
