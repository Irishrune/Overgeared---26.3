package net.stirdrem.overgeared.recipe;

import net.stirdrem.overgeared.util.IngredientDisplay;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;

public record ItemToToolTypeRecipe(Ingredient input, String toolType) implements OvergearedStationRecipe<SingleRecipeInput> {

    @Override
    public boolean matches(SingleRecipeInput recipeInput, Level level) {
        return input.test(recipeInput.getItem(0));
    }

    @Override
    public ItemStack assemble(SingleRecipeInput recipeInput) {
        return ItemStack.EMPTY; // purely data-driven recipe
    }

    // Legacy helper kept for Overgeared callers; no longer in Recipe (26.3).
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    // Legacy helper kept for Overgeared callers; no longer in Recipe (26.3).
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return ModRecipeSerializers.ITEM_TO_TOOLTYPE.get();
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return ModRecipeTypes.ITEM_TO_TOOLTYPE.get();
    }

    public List<ItemStack> getItems() {
        return java.util.Arrays.asList(IngredientDisplay.stacks(input));
    }

    // ----------------------------------------------------
    // Serializer
    // ----------------------------------------------------
    public static final class Serializer {
        public static final MapCodec<ItemToToolTypeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("item").forGetter(r -> r.input),
                Codec.STRING.fieldOf("tooltype").forGetter(r -> r.toolType)
        ).apply(instance, ItemToToolTypeRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ItemToToolTypeRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, r -> r.input,
                ByteBufCodecs.STRING_UTF8, r -> r.toolType,
                ItemToToolTypeRecipe::new
        );

    }
}
