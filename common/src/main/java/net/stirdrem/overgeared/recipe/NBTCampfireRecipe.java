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
public final class NBTCampfireRecipe extends AbstractCookingRecipe {
    public NBTCampfireRecipe(Recipe.CommonInfo commonInfo, AbstractCookingRecipe.CookingBookInfo bookInfo,
            Ingredient ingredient, ItemStackTemplate result, float experience, int cookingTime) {
        super(commonInfo, bookInfo, ingredient, result, experience, cookingTime);
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        ItemStack output = super.assemble(input);
        output.set(ModComponents.HEATED_COMPONENT.get(), true);
        output.remove(ModComponents.FORGING_QUALITY.get());
        return output;
    }

    @Override
    protected Item furnaceIcon() {
        return Items.CAMPFIRE;
    }

    @Override
    public RecipeType<? extends AbstractCookingRecipe> getType() {
        return RecipeType.CAMPFIRE_COOKING;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CAMPFIRE;
    }

    @Override
    public RecipeSerializer<NBTCampfireRecipe> getSerializer() {
        return ModRecipeSerializers.CAMPFIRE_HEATING.get();
    }

    public static final class Serializer {
        public static final MapCodec<NBTCampfireRecipe> CODEC =
                AbstractCookingRecipe.cookingMapCodec(NBTCampfireRecipe::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, NBTCampfireRecipe> STREAM_CODEC =
                AbstractCookingRecipe.cookingStreamCodec(NBTCampfireRecipe::new);
    }
}
