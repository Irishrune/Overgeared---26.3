package net.stirdrem.overgeared.test;

import com.mojang.serialization.JsonOps;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.*;
import net.stirdrem.overgeared.util.ModTags;

public class PortGameTests {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    @GameTest public void gameplayDataLoads(GameTestHelper test) {
        check(new ItemStack(ModItems.SMITHING_HAMMER.get()).is(ModTags.Items.SMITHING_HAMMERS), "Missing hammer tag");
        check(new ItemStack(ModItems.HEATED_IRON_INGOT.get()).is(ModTags.Items.HEATED_METALS), "Missing heated-metal tag");
        check(RecipeLookups.all(test.getLevel(), ModRecipeTypes.FORGING.get()).size() >= 40, "Missing generated forging recipes");
        check(RecipeLookups.all(test.getLevel(), ModRecipeTypes.CASTING.get()).size() >= 20, "Missing generated casting recipes");
        check(RecipeLookups.all(test.getLevel(), ModRecipeTypes.FLETCHING.get()).size() >= 5, "Missing generated fletching recipes");
        test.succeed();
    }

    @GameTest public void forgingSettingsRoundTrip(GameTestHelper test) {
        RecipeBookSettings settings = new RecipeBookSettings();
        settings.setOpen(OvergearedMod.FORGING, true);
        settings.setFiltering(OvergearedMod.FORGING, true);
        check(settings.copy().isOpen(OvergearedMod.FORGING), "Copy lost forging open state");
        RecipeBookSettings replaced = new RecipeBookSettings();
        replaced.replaceFrom(settings);
        check(replaced.isFiltering(OvergearedMod.FORGING), "Replacement lost forging filter");
        var encoded = RecipeBookSettings.MAP_CODEC.codec().encodeStart(JsonOps.INSTANCE, settings).getOrThrow();
        var saved = RecipeBookSettings.MAP_CODEC.codec().parse(JsonOps.INSTANCE, encoded).getOrThrow();
        check(saved.isOpen(OvergearedMod.FORGING) && saved.isFiltering(OvergearedMod.FORGING), "Saved settings lost forging state");
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            RecipeBookSettings.STREAM_CODEC.encode(buf, settings);
            var synced = RecipeBookSettings.STREAM_CODEC.decode(buf);
            check(synced.isOpen(OvergearedMod.FORGING) && synced.isFiltering(OvergearedMod.FORGING), "Network settings lost forging state");
            check(buf.readableBytes() == 0, "Settings left network bytes unread");
        } finally { buf.release(); }
        test.succeed();
    }

    @GameTest public void forgingDisplaysAndFlagsRoundTrip(GameTestHelper test) {
        var ops = test.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE);
        for (var holder : RecipeLookups.all(test.getLevel(), ModRecipeTypes.FORGING.get())) {
            ForgingRecipe recipe = holder.value();
            var encoded = ForgingRecipe.Serializer.CODEC.codec().encodeStart(ops, recipe).getOrThrow();
            var decoded = ForgingRecipe.Serializer.CODEC.codec().parse(ops, encoded).getOrThrow();
            check(decoded.getAnvilTier().equals(recipe.getAnvilTier()), "Tier lost: " + holder.id());
            check(decoded.getKey().equals(recipe.getKey()), "Ingredient flags lost: " + holder.id());
            check(recipe.display().size() == 1 && recipe.display().getFirst() instanceof ForgingRecipeDisplay, "Missing client recipe display");
        }
        test.succeed();
    }
}
