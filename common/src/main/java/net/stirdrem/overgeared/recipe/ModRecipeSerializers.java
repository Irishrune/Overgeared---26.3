package net.stirdrem.overgeared.recipe;

import net.minecraft.core.registries.Registries;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.stirdrem.overgeared.registry.DeferredRegister;
import net.stirdrem.overgeared.OvergearedMod;

import java.util.function.Supplier;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, OvergearedMod.MOD_ID);

    public static final Supplier<RecipeSerializer<DisabledRecipe>> DISABLED =
            RECIPE_SERIALIZERS.register("disabled", () -> recipe(DisabledRecipe.Serializer.CODEC, DisabledRecipe.Serializer.STREAM_CODEC));

    public static final Supplier<RecipeSerializer<ForgingRecipe>> FORGING_SERIALIZER =
            RECIPE_SERIALIZERS.register("forging", () -> recipe(ForgingRecipe.Serializer.CODEC, ForgingRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<RockKnappingRecipe>> ROCK_KNAPPING_SERIALIZER =
            RECIPE_SERIALIZERS.register("rock_knapping", () -> recipe(RockKnappingRecipe.Serializer.CODEC, RockKnappingRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<OvergearedShapelessRecipe>> CRAFTING_SHAPELESS =
            RECIPE_SERIALIZERS.register("crafting_shapeless", () -> recipe(OvergearedShapelessRecipe.Serializer.CODEC, OvergearedShapelessRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<BlueprintCloningRecipe>> CRAFTING_BLUEPRINTCLONING =
            RECIPE_SERIALIZERS.register("crafting_cloning", () -> recipe(BlueprintCloningRecipe.Serializer.CODEC, BlueprintCloningRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<DynamicToolCastRecipe>> CRAFTING_DYNAMIC_TOOL_CAST =
            RECIPE_SERIALIZERS.register("crafting_cast", () -> recipe(DynamicToolCastRecipe.CODEC, DynamicToolCastRecipe.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<ClayToolCastRecipe>> CLAY_TOOL_CAST =
            RECIPE_SERIALIZERS.register("crafting_initial_cast", () -> recipe(ClayToolCastRecipe.Serializer.CODEC, ClayToolCastRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<FletchingRecipe>> FLETCHING_SERIALIZER =
            RECIPE_SERIALIZERS.register("fletching", () -> recipe(FletchingRecipe.Serializer.CODEC, FletchingRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<CastSmeltingRecipe>> CAST_SMELTING =
            RECIPE_SERIALIZERS.register("cast_smelting", () -> recipe(CastSmeltingRecipe.Serializer.CODEC, CastSmeltingRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<CastBlastingRecipe>> CAST_BLASTING =
            RECIPE_SERIALIZERS.register("cast_blasting", () -> recipe(CastBlastingRecipe.Serializer.CODEC, CastBlastingRecipe.Serializer.STREAM_CODEC));

    public static final Supplier<RecipeSerializer<NBTKeepingSmeltingRecipe>> NBT_SMELTING =
            RECIPE_SERIALIZERS.register("nbt_smelting", () -> recipe(NBTKeepingSmeltingRecipe.Serializer.CODEC, NBTKeepingSmeltingRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<NBTKeepingBlastingRecipe>> NBT_BLASTING =
            RECIPE_SERIALIZERS.register("nbt_blasting", () -> recipe(NBTKeepingBlastingRecipe.Serializer.CODEC, NBTKeepingBlastingRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<AlloySmeltingRecipe>> ALLOY_SMELTING =
            RECIPE_SERIALIZERS.register("alloy_smelting", () -> recipe(AlloySmeltingRecipe.Serializer.CODEC, AlloySmeltingRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<ShapedAlloySmeltingRecipe>> SHAPED_ALLOY_SMELTING =
            RECIPE_SERIALIZERS.register("shaped_alloy_smelting", () -> recipe(ShapedAlloySmeltingRecipe.Serializer.CODEC, ShapedAlloySmeltingRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<NetherAlloySmeltingRecipe>> NETHER_ALLOY_SMELTING =
            RECIPE_SERIALIZERS.register("nether_alloy_smelting", () -> recipe(NetherAlloySmeltingRecipe.Serializer.CODEC, NetherAlloySmeltingRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<ShapedNetherAlloySmeltingRecipe>> SHAPED_NETHER_ALLOY_SMELTING =
            RECIPE_SERIALIZERS.register("shaped_nether_alloy_smelting", () -> recipe(ShapedNetherAlloySmeltingRecipe.Serializer.CODEC, ShapedNetherAlloySmeltingRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<ItemToToolTypeRecipe>> ITEM_TO_TOOLTYPE =
            RECIPE_SERIALIZERS.register("item_to_tooltype", () -> recipe(ItemToToolTypeRecipe.Serializer.CODEC, ItemToToolTypeRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<CoolingRecipe>> COOLING_SERIALIZER =
            RECIPE_SERIALIZERS.register("cooling", () -> recipe(CoolingRecipe.Serializer.CODEC, CoolingRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<GrindingRecipe>> GRINDING_SERIALIZER =
            RECIPE_SERIALIZERS.register("grinding", () -> recipe(GrindingRecipe.Serializer.CODEC, GrindingRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<CastingRecipe>> CASTING =
            RECIPE_SERIALIZERS.register("casting", () -> recipe(CastingRecipe.Serializer.CODEC, CastingRecipe.Serializer.STREAM_CODEC));
    public static final Supplier<RecipeSerializer<NBTCampfireRecipe>> CAMPFIRE_HEATING =
            RECIPE_SERIALIZERS.register("campfire_heating", () -> recipe(NBTCampfireRecipe.Serializer.CODEC, NBTCampfireRecipe.Serializer.STREAM_CODEC));

    /** The Minecraft 26.3 recipe serializer is a codec-pair record, not an interface. */
    private static <T extends Recipe<?>> RecipeSerializer<T> recipe(
            MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> networkCodec) {
        return new RecipeSerializer<>(codec, networkCodec);
    }

    public static void register() {
        RECIPE_SERIALIZERS.register();
    }
}

