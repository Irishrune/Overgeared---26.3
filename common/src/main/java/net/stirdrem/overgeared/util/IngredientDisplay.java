package net.stirdrem.overgeared.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Preview stacks for optional recipe viewers and blacklist reporting.
 * In Minecraft 26.3 Ingredient.items() exposes holders, not prebuilt stacks.
 * Never use these previews to decide a recipe match: call Ingredient.test(stack).
 */
public final class IngredientDisplay {
    private IngredientDisplay() { }

    @SuppressWarnings("deprecation")
    public static ItemStack[] stacks(Ingredient ingredient) {
        return ingredient.items().map(holder -> new ItemStack(holder.value())).toArray(ItemStack[]::new);
    }
}
