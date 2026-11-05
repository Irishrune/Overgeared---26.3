package net.stirdrem.overgeared.recipe;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.List;

/** 26.3 server-side recipe manager access and all-recipe enumeration. */
public final class RecipeLookups {
    private RecipeLookups() { }

    /** The client does not hold the server recipe database in 26.3. */
    public static RecipeManager managerOrNull(Level level) {
        return level instanceof ServerLevel server ? server.recipeAccess() : null;
    }

    public static RecipeManager serverManager(Level level) {
        RecipeManager manager = managerOrNull(level);
        if (manager == null) {
            throw new IllegalStateException("Custom recipe enumeration requires a server level");
        }
        return manager;
    }

    @SuppressWarnings("unchecked")
    public static <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> all(
            Level level, RecipeType<T> type) {
        RecipeManager manager = managerOrNull(level);
        // Minecraft 26.3 keeps RecipeMap private. Query the public holder
        // collection and retain the recipe-type identity check that byType
        // previously enforced. This is for server-side custom recipe scans.
        if (manager == null) {
            return List.of();
        }
        return manager.getRecipes().stream()
                .filter(holder -> holder.value().getType() == type)
                .map(holder -> (RecipeHolder<T>) holder)
                .toList();
    }
}
