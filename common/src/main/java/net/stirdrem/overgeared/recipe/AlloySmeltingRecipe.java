package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AlloySmeltingRecipe implements OvergearedStationRecipe<RecipeInput>, IAlloyRecipe {
    private final String group;
    private final CookingBookCategory category;
    private final List<Ingredient> inputs;
    private final ItemStackTemplate output;
    private final float experience;
    private final int cookingTime;

    public AlloySmeltingRecipe(String group, CookingBookCategory category, List<Ingredient> inputs, ItemStackTemplate output, float experience, int cookingTime) {
        this.group = group;
        this.category = category;
        this.inputs = inputs;
        this.output = output;
        this.experience = experience;
        this.cookingTime = cookingTime;
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        // Don't bother checking on client side for performance reasons
        if (level.isClientSide()) return false;

        // Create a list of non-empty ingredient-item pairs to match
        List<Ingredient> remainingIngredients = new ArrayList<>();
        List<ItemStack> remainingItems = new ArrayList<>();

        // Collect all recipe ingredients (skip empty ingredients if any)
        for (Ingredient ingredient : inputs) {
            if (!ingredient.isEmpty()) {
                remainingIngredients.add(ingredient);
            }
        }

        // Collect all non-empty items from the input slots
        for (int i = 0; i < 4; i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                remainingItems.add(stack);
            }
        }

        // Must have same number of non-empty items as non-empty ingredients
        if (remainingItems.size() != remainingIngredients.size()) {
            return false;
        }

        // Try to match every stack in inventory to an ingredient
        for (ItemStack stack : remainingItems) {
            boolean matched = false;

            for (int i = 0; i < remainingIngredients.size(); i++) {
                if (remainingIngredients.get(i).test(stack)) {
                    remainingIngredients.remove(i); // consume one ingredient
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                return false; // found a stack that doesn't match any remaining ingredient
            }
        }

        // All ingredients matched successfully
        return remainingIngredients.isEmpty();
    }

    @Override
    public ItemStack assemble(RecipeInput input) {
        return output.create();
    }

    // Legacy helper kept for Overgeared callers; no longer in Recipe (26.3).
    public boolean canCraftInDimensions(int w, int h) {
        return true;
    }

    // Legacy helper kept for Overgeared callers; no longer in Recipe (26.3).
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return output.create();
    }

    @Override
    public String group() {
        return getGroup();
    }

    public String getGroup() {
        return group;
    }

    public CookingBookCategory getCraftingBookCategory() {
        return category;
    }

    public float getExperience() {
        return experience;
    }

    @Override
    public boolean isShaped() {
        return false;
    }

    public int getCookingTime() {
        return cookingTime;
    }

    public List<Optional<Ingredient>> getIngredientsList() {
        return inputs.stream().map(Optional::of).toList();
    }

    public ItemStack getResultItem() {
        return output.create();
    }

    @Override
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return ModRecipeSerializers.ALLOY_SMELTING.get();
    }

    @Override
    public RecipeType<? extends Recipe<RecipeInput>> getType() {
        return ModRecipeTypes.ALLOY_SMELTING.get();
    }

    public static final class Serializer {
        public static final MapCodec<AlloySmeltingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.fieldOf("group").forGetter(r -> r.group),
                CookingBookCategory.CODEC.optionalFieldOf("category", CookingBookCategory.MISC).forGetter(r -> r.category),
                Ingredient.CODEC.listOf(0, 4).fieldOf("ingredients").forGetter(r -> r.inputs),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.output),
                Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(r -> r.experience),
                Codec.INT.optionalFieldOf("cookingtime", 200).forGetter(r -> r.cookingTime)
        ).apply(instance, AlloySmeltingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, AlloySmeltingRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, r -> r.group,
                ByteBufCodecs.fromCodec(CookingBookCategory.CODEC), r -> r.category,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list(4)), r -> r.inputs,
                ItemStackTemplate.STREAM_CODEC, r -> r.output,
                ByteBufCodecs.FLOAT, r -> r.experience,
                ByteBufCodecs.INT, r -> r.cookingTime,
                AlloySmeltingRecipe::new
        );

    }
}
