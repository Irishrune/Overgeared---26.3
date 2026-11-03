package net.stirdrem.overgeared.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Optional;

public interface IAlloyRecipe {
    List<Optional<Ingredient>> getIngredientsList();

    ItemStack getResultItem(HolderLookup.Provider provider);

    float getExperience();

    boolean isShaped();
}
