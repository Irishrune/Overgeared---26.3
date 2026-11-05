package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.stirdrem.overgeared.components.ModComponents;

/** Preserve Overgeared's component handling on a 26.3 cooking recipe. */
public final class NBTKeepingBlastingRecipe extends AbstractCookingRecipe {
    public NBTKeepingBlastingRecipe(Recipe.CommonInfo commonInfo, AbstractCookingRecipe.CookingBookInfo bookInfo,
            Ingredient ingredient, ItemStackTemplate result, float experience, int cookingTime) {
        super(commonInfo, bookInfo, ingredient, result, experience, cookingTime);
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        ItemStack output = super.assemble(input);
        output.applyComponents(input.getItem(0).getComponentsPatch());
        
        return output;
    }

    // Keep these custom serializers while retaining the vanilla furnace
    // recipe category. The 26.3 concrete BlastingRecipe serializer is no longer
    // covariant with a subclass-specific recipe serializer.
    @Override
    public RecipeType<? extends AbstractCookingRecipe> getType() {
        return RecipeType.BLASTING;
    }

    @Override
    protected Item furnaceIcon() {
        return Items.BLAST_FURNACE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return switch (category()) {
            case BLOCKS -> RecipeBookCategories.BLAST_FURNACE_BLOCKS;
            case FOOD, MISC -> RecipeBookCategories.BLAST_FURNACE_MISC;
        };
    }

    @Override
    public RecipeSerializer<NBTKeepingBlastingRecipe> getSerializer() {
        return ModRecipeSerializers.NBT_BLASTING.get();
    }

    public static final class Serializer {
        public static final MapCodec<NBTKeepingBlastingRecipe> CODEC =
                AbstractCookingRecipe.cookingMapCodec(NBTKeepingBlastingRecipe::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, NBTKeepingBlastingRecipe> STREAM_CODEC =
                AbstractCookingRecipe.cookingStreamCodec(NBTKeepingBlastingRecipe::new);
    }
}
