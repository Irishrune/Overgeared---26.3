package net.stirdrem.overgeared.recipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.stirdrem.overgeared.config.ServerConfig;

public class DragonBreathBrewingRecipe extends BrewingRecipe {
    public static final MapCodec<BrewingRecipe> CODEC = BrewingRecipe.MAP_CODEC.xmap(
        r -> new DragonBreathBrewingRecipe(r.getInput(), r.getReagent(), r.getOutput()), r -> r);
    public static final StreamCodec<RegistryFriendlyByteBuf, BrewingRecipe> NETWORK_CODEC = BrewingRecipe.STREAM_CODEC.map(
        r -> new DragonBreathBrewingRecipe(r.getInput(), r.getReagent(), r.getOutput()), r -> r);
    public DragonBreathBrewingRecipe(PotionIngredient input, PotionIngredient reagent, ItemStackTemplate output) {
        super(input, reagent, output);
    }
    @Override public boolean matches(BrewingInput input) {
        return ServerConfig.SERVER_CONFIG.isLoaded() && ServerConfig.ENABLE_DRAGON_BREATH_RECIPE.get() && super.matches(input);
    }
    public static final RecipeSerializer<BrewingRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, NETWORK_CODEC);
    public static void register() {
        net.minecraft.core.Registry.register(net.minecraft.core.registries.BuiltInRegistries.RECIPE_SERIALIZER,
            net.stirdrem.overgeared.OvergearedMod.loc("dragon_breath_brewing"), SERIALIZER);
    }
    @Override public RecipeSerializer<BrewingRecipe> getSerializer() { return SERIALIZER; }
}
