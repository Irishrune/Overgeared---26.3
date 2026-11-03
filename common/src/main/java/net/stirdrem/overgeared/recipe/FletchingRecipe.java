package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.Optional;

/** Blank tip/shaft/feather slots are required empty; absent potion remains optional. */
public final class FletchingRecipe implements OvergearedStationRecipe<RecipeInput> {
    private final Optional<Ingredient> tip, shaft, feather, potion;
    private final ItemStackTemplate result;
    private final Optional<ItemStackTemplate> resultTipped, resultLingering;
    private final String tippedTag, lingeringTag;

    public FletchingRecipe(Optional<Ingredient> tip, Optional<Ingredient> shaft,
            Optional<Ingredient> feather, Optional<Ingredient> potion,
            ItemStackTemplate result, Optional<ItemStackTemplate> resultTipped,
            Optional<ItemStackTemplate> resultLingering, String tippedTag, String lingeringTag) {
        this.tip = tip;
        this.shaft = shaft;
        this.feather = feather;
        this.potion = potion;
        this.result = result;
        this.resultTipped = resultTipped;
        this.resultLingering = resultLingering;
        this.tippedTag = tippedTag;
        this.lingeringTag = lingeringTag;
    }

    @Override public boolean matches(RecipeInput input, Level level) {
        return Ingredient.testOptionalIngredient(tip, input.getItem(0))
                && Ingredient.testOptionalIngredient(shaft, input.getItem(1))
                && Ingredient.testOptionalIngredient(feather, input.getItem(2))
                // Absent potion is a wildcard, not a requirement that the potion slot is empty.
                && potion.map(value -> value.test(input.getItem(3))).orElse(true);
    }

    @Override public ItemStack assemble(RecipeInput input) { return result.create(); }
    public Optional<Ingredient> getTip() { return tip; }
    public Optional<Ingredient> getShaft() { return shaft; }
    public Optional<Ingredient> getFeather() { return feather; }
    public Optional<Ingredient> getPotion() { return potion; }
    public boolean hasPotion() { return potion.isPresent(); }
    public ItemStack getDefaultResult() { return result.create(); }
    public ItemStack getTippedResult() { return resultTipped.map(ItemStackTemplate::create).orElse(ItemStack.EMPTY); }
    public ItemStack getLingeringResult() { return resultLingering.map(ItemStackTemplate::create).orElse(ItemStack.EMPTY); }
    public boolean canCraftInDimensions(int w, int h) { return true; }
    public ItemStack getResultItem(HolderLookup.Provider provider) { return getDefaultResult(); }
    @Override public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() { return ModRecipeSerializers.FLETCHING_SERIALIZER.get(); }
    @Override public RecipeType<? extends Recipe<RecipeInput>> getType() { return ModRecipeTypes.FLETCHING.get(); }
    public boolean hasTippedResult() { return resultTipped.isPresent(); }
    public boolean hasLingeringResult() { return resultLingering.isPresent(); }
    public String getTippedTag() { return tippedTag; }
    public String getLingeringTag() { return lingeringTag; }

    public static final class Serializer {
        public static final MapCodec<FletchingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.optionalFieldOf("tip").forGetter(r -> r.tip),
                Ingredient.CODEC.optionalFieldOf("shaft").forGetter(r -> r.shaft),
                Ingredient.CODEC.optionalFieldOf("feather").forGetter(r -> r.feather),
                Ingredient.CODEC.optionalFieldOf("potion").forGetter(r -> r.potion),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result),
                ItemStackTemplate.CODEC.optionalFieldOf("result_tipped").forGetter(r -> r.resultTipped),
                ItemStackTemplate.CODEC.optionalFieldOf("result_lingering").forGetter(r -> r.resultLingering),
                Codec.STRING.optionalFieldOf("tipped_tag", "Potion").forGetter(r -> r.tippedTag),
                Codec.STRING.optionalFieldOf("lingering_tag", "LingeringPotion").forGetter(r -> r.lingeringTag)
        ).apply(instance, FletchingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, FletchingRecipe> STREAM_CODEC = StreamCodec.of(
                (buffer, recipe) -> {
                    Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.encode(buffer, recipe.tip);
                    Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.encode(buffer, recipe.shaft);
                    Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.encode(buffer, recipe.feather);
                    Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.encode(buffer, recipe.potion);
                    ItemStackTemplate.STREAM_CODEC.encode(buffer, recipe.result);
                    buffer.writeBoolean(recipe.resultTipped.isPresent());
                    recipe.resultTipped.ifPresent(v -> ItemStackTemplate.STREAM_CODEC.encode(buffer, v));
                    buffer.writeBoolean(recipe.resultLingering.isPresent());
                    recipe.resultLingering.ifPresent(v -> ItemStackTemplate.STREAM_CODEC.encode(buffer, v));
                    buffer.writeUtf(recipe.tippedTag);
                    buffer.writeUtf(recipe.lingeringTag);
                }, buffer -> {
                    Optional<Ingredient> tip = Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.decode(buffer);
                    Optional<Ingredient> shaft = Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.decode(buffer);
                    Optional<Ingredient> feather = Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.decode(buffer);
                    Optional<Ingredient> potion = Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.decode(buffer);
                    ItemStackTemplate result = ItemStackTemplate.STREAM_CODEC.decode(buffer);
                    Optional<ItemStackTemplate> tipped = buffer.readBoolean()
                            ? Optional.of(ItemStackTemplate.STREAM_CODEC.decode(buffer)) : Optional.empty();
                    Optional<ItemStackTemplate> lingering = buffer.readBoolean()
                            ? Optional.of(ItemStackTemplate.STREAM_CODEC.decode(buffer)) : Optional.empty();
                    return new FletchingRecipe(tip, shaft, feather, potion, result, tipped, lingering,
                            buffer.readUtf(), buffer.readUtf());
                }
        );
    }
}
