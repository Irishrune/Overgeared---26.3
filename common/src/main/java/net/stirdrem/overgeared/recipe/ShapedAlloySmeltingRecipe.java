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
import java.util.Map;
import java.util.Optional;

/**
 * 2x2 alloy pattern; Optional.empty means a required EMPTY slot, not a wildcard.
 * Actual pattern rows and key bindings are retained for JSON encode/decode round trips.
 */
public final class ShapedAlloySmeltingRecipe implements OvergearedStationRecipe<RecipeInput>, IAlloyRecipe {
    private static final int GRID = 4;
    private static final int WIDTH = 2;
    private final String group;
    private final CraftingBookCategory category;
    private final List<Optional<Ingredient>> pattern;
    private final ItemStackTemplate output;
    private final float experience;
    private final int cookingTime;
    private final List<String> patternRows;
    private final Map<String, Ingredient> patternKey;

    private ShapedAlloySmeltingRecipe(String group, CraftingBookCategory category,
            List<Optional<Ingredient>> pattern, ItemStackTemplate output,
            float experience, int cookingTime, List<String> patternRows, Map<String, Ingredient> patternKey) {
        if (pattern.size() != GRID) throw new IllegalArgumentException("Expected " + GRID + " pattern cells");
        this.group = group;
        this.category = category;
        this.pattern = List.copyOf(pattern);
        this.output = output;
        this.experience = experience;
        this.cookingTime = cookingTime;
        this.patternRows = List.copyOf(patternRows);
        this.patternKey = Map.copyOf(patternKey);
    }

    public static ShapedAlloySmeltingRecipe fromPattern(String group, CraftingBookCategory category,
            List<String> rows, Map<String, Ingredient> keys, ItemStackTemplate output,
            float experience, int cookingTime) {
        if (rows.size() != WIDTH) throw new IllegalArgumentException("Expected " + WIDTH + " rows");
        List<Optional<Ingredient>> slots = new ArrayList<>(GRID);
        for (String row : rows) {
            if (row.length() != WIDTH) throw new IllegalArgumentException("Expected " + WIDTH + " columns");
            for (int x = 0; x < WIDTH; x++) {
                char symbol = row.charAt(x);
                if (symbol == ' ') {
                    slots.add(Optional.empty());
                } else {
                    Ingredient ingredient = keys.get(String.valueOf(symbol));
                    if (ingredient == null) throw new IllegalArgumentException("Undefined symbol: " + symbol);
                    slots.add(Optional.of(ingredient));
                }
            }
        }
        return new ShapedAlloySmeltingRecipe(group, category, slots, output, experience, cookingTime, rows, keys);
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        if (level.isClientSide()) return false;
        for (int i = 0; i < GRID; i++) {
            if (!Ingredient.testOptionalIngredient(pattern.get(i), input.getItem(i))) return false;
        }
        return true;
    }

    @Override public ItemStack assemble(RecipeInput input) { return output.create(); }
    public boolean canCraftInDimensions(int w, int h) { return w >= WIDTH && h >= WIDTH; }
    @Override public List<Optional<Ingredient>> getIngredientsList() { return pattern; }
    @Override public ItemStack getResultItem(HolderLookup.Provider provider) { return output.create(); }
    public ItemStack getResultItem() { return output.create(); }
    @Override public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() { return ModRecipeSerializers.SHAPED_ALLOY_SMELTING.get(); }
    @Override public RecipeType<? extends Recipe<RecipeInput>> getType() { return ModRecipeTypes.SHAPED_ALLOY_SMELTING.get(); }
    @Override public String group() { return getGroup(); }
    public String getGroup() { return group; }
    public CraftingBookCategory category() { return category; }
    public CraftingBookCategory getCraftingBookCategory() { return category; }
    @Override public float getExperience() { return experience; }
    @Override public boolean isShaped() { return true; }
    public int getCookingTime() { return cookingTime; }
    public List<Optional<Ingredient>> getPattern() { return pattern; }
    public List<String> patternRows() { return patternRows; }
    public Map<String, Ingredient> patternKey() { return patternKey; }

    public static final class Serializer {
        public static final MapCodec<ShapedAlloySmeltingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedAlloySmeltingRecipe::getGroup),
                CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(ShapedAlloySmeltingRecipe::category),
                Codec.STRING.listOf().fieldOf("pattern").forGetter(ShapedAlloySmeltingRecipe::patternRows),
                Codec.unboundedMap(Codec.STRING, Ingredient.CODEC).fieldOf("key").forGetter(ShapedAlloySmeltingRecipe::patternKey),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.output),
                Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(ShapedAlloySmeltingRecipe::getExperience),
                Codec.INT.optionalFieldOf("cookingtime", 200).forGetter(ShapedAlloySmeltingRecipe::getCookingTime)
        ).apply(instance, ShapedAlloySmeltingRecipe::fromPattern));

        public static final StreamCodec<RegistryFriendlyByteBuf, ShapedAlloySmeltingRecipe> STREAM_CODEC = StreamCodec.of(
                (buf, recipe) -> {
                    ByteBufCodecs.STRING_UTF8.encode(buf, recipe.group);
                    buf.writeEnum(recipe.category);
                    for (Optional<Ingredient> ingredient : recipe.pattern) {
                        Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.encode(buf, ingredient);
                    }
                    ItemStackTemplate.STREAM_CODEC.encode(buf, recipe.output);
                    ByteBufCodecs.FLOAT.encode(buf, recipe.experience);
                    ByteBufCodecs.VAR_INT.encode(buf, recipe.cookingTime);
                    // The runtime matching pattern is sent; data-driven JSON pattern/key are server-only.
                },
                buf -> {
                    String group = ByteBufCodecs.STRING_UTF8.decode(buf);
                    CraftingBookCategory category = buf.readEnum(CraftingBookCategory.class);
                    List<Optional<Ingredient>> slots = new ArrayList<>(GRID);
                    for (int i = 0; i < GRID; i++) {
                        slots.add(Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.decode(buf));
                    }
                    ItemStackTemplate output = ItemStackTemplate.STREAM_CODEC.decode(buf);
                    float experience = ByteBufCodecs.FLOAT.decode(buf);
                    int time = ByteBufCodecs.VAR_INT.decode(buf);
                    return new ShapedAlloySmeltingRecipe(group, category, slots, output, experience, time, List.of(), Map.of());
                }
        );
    }
}
