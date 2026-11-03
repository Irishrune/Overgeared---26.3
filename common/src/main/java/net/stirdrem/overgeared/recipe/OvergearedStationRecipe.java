package net.stirdrem.overgeared.recipe;

import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Shared Minecraft 26.3 recipe-book contract for Overgeared workstations.
 *
 * <p>Forge, casting, knapping and alloy recipes are handled by their own custom
 * menus. They are intentionally not auto-placed by the vanilla recipe book.
 * Each recipe can override group() and showNotification() for its own metadata.
 * This interface does not change ingredient matching or result assembly.</p>
 */
public interface OvergearedStationRecipe<T extends RecipeInput> extends Recipe<T> {
    @Override
    default String group() {
        return "";
    }

    @Override
    default boolean showNotification() {
        return true;
    }

    @Override
    default PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    default RecipeBookCategory recipeBookCategory() {
        return null;
    }

    @Override
    default boolean isSpecial() {
        return true;
    }
}
