package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.*;
import net.minecraft.world.flag.FeatureFlagSet;

/** Carries the grid and required anvil tier to the recipe-book client. */
public record ForgingRecipeDisplay(ShapedCraftingRecipeDisplay grid, String tier) implements RecipeDisplay {
    public static final MapCodec<ForgingRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        ShapedCraftingRecipeDisplay.MAP_CODEC.forGetter(ForgingRecipeDisplay::grid),
        Codec.STRING.fieldOf("anvil_tier").forGetter(ForgingRecipeDisplay::tier)
    ).apply(i, ForgingRecipeDisplay::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ForgingRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
        ShapedCraftingRecipeDisplay.STREAM_CODEC, ForgingRecipeDisplay::grid,
        ByteBufCodecs.STRING_UTF8, ForgingRecipeDisplay::tier, ForgingRecipeDisplay::new);
    public static final Type<ForgingRecipeDisplay> TYPE = new Type<>(CODEC, STREAM_CODEC);
    @Override public SlotDisplay result() { return grid.result(); }
    @Override public SlotDisplay craftingStation() { return grid.craftingStation(); }
    @Override public Type<ForgingRecipeDisplay> type() { return TYPE; }
    @Override public boolean isEnabled(FeatureFlagSet flags) { return grid.isEnabled(flags); }
}
