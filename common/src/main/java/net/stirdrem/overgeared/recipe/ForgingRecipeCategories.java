package net.stirdrem.overgeared.recipe;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.stirdrem.overgeared.OvergearedMod;

public final class ForgingRecipeCategories {
    public static final RecipeBookCategory TOOLS = register("forging_tools");
    public static final RecipeBookCategory ARMORS = register("forging_armors");
    public static final RecipeBookCategory MISC = register("forging_misc");
    private static RecipeBookCategory register(String name) {
        return Registry.register(BuiltInRegistries.RECIPE_BOOK_CATEGORY, OvergearedMod.loc(name), new RecipeBookCategory());
    }
    public static void register() {
        Registry.register(BuiltInRegistries.RECIPE_DISPLAY, OvergearedMod.loc("forging"), ForgingRecipeDisplay.TYPE);
    }
}
