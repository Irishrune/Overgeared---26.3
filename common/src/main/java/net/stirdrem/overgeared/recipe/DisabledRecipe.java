package net.stirdrem.overgeared.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/**
 * Keeps a vanilla recipe identifier resolvable while disabling its shortcut.
 * In 26.3 advancements hold registry references to recipes; removing a recipe
 * with a load condition leaves those references unbound and prevents world load.
 */
public final class DisabledRecipe extends CustomRecipe {
    private static final DisabledRecipe INSTANCE = new DisabledRecipe();
    @Override public boolean matches(CraftingInput input, Level level) { return false; }
    @Override public ItemStack assemble(CraftingInput input) { return ItemStack.EMPTY; }
    @Override public RecipeSerializer<DisabledRecipe> getSerializer() { return ModRecipeSerializers.DISABLED.get(); }

    public static final class Serializer {
        public static final MapCodec<DisabledRecipe> CODEC = MapCodec.unit(INSTANCE);
        public static final StreamCodec<RegistryFriendlyByteBuf, DisabledRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    }
}
