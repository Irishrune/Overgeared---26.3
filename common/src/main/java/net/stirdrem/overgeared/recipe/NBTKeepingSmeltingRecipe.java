package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.stirdrem.overgeared.components.ModComponents;

/** Preserve Overgeared's component handling on a 26.3 cooking recipe. */
public final class NBTKeepingSmeltingRecipe extends SmeltingRecipe {
    public NBTKeepingSmeltingRecipe(Recipe.CommonInfo commonInfo, AbstractCookingRecipe.CookingBookInfo bookInfo,
            Ingredient ingredient, ItemStackTemplate result, float experience, int cookingTime) {
        super(commonInfo, bookInfo, ingredient, result, experience, cookingTime);
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        ItemStack output = super.assemble(input);
        output.applyComponents(input.getItem(0).getComponentsPatch());
        
        return output;
    }

    @Override
    public RecipeSerializer<NBTKeepingSmeltingRecipe> getSerializer() {
        return ModRecipeSerializers.NBT_SMELTING.get();
    }

    public static final class Serializer {
        public static final MapCodec<NBTKeepingSmeltingRecipe> CODEC =
                AbstractCookingRecipe.cookingMapCodec(NBTKeepingSmeltingRecipe::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, NBTKeepingSmeltingRecipe> STREAM_CODEC =
                AbstractCookingRecipe.cookingStreamCodec(NBTKeepingSmeltingRecipe::new);
    }
}
