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
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.components.CastData;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.entity.BaseSmithingAnvilBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import java.util.*;
import com.google.gson.JsonParser;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

public class PortGameTests {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static Recipe<?> recipe(GameTestHelper test, String id) {
        return test.getLevel().recipeAccess().getRecipes().stream()
                .filter(holder -> holder.id().identifier().toString().equals(id))
                .findFirst().orElseThrow(() -> new AssertionError("Recipe missing: " + id)).value();
    }

    private static RecipeInput input(ItemStack... stacks) {
        return new RecipeInput() {
            public ItemStack getItem(int slot) { return stacks[slot]; }
            public int size() { return stacks.length; }
        };
    }

    @GameTest public void gameplayDataLoads(GameTestHelper test) {
        check(new ItemStack(ModItems.SMITHING_HAMMER.get()).is(ModTags.Items.SMITHING_HAMMERS), "Missing hammer tag");
        check(new ItemStack(ModItems.HEATED_IRON_INGOT.get()).is(ModTags.Items.HEATED_METALS), "Missing heated-metal tag");
        check(RecipeLookups.all(test.getLevel(), ModRecipeTypes.FORGING.get()).size() >= 40, "Missing generated forging recipes");
        check(RecipeLookups.all(test.getLevel(), ModRecipeTypes.CASTING.get()).size() >= 20, "Missing generated casting recipes");
        check(RecipeLookups.all(test.getLevel(), ModRecipeTypes.FLETCHING.get()).size() >= 5, "Missing generated fletching recipes");
        check(net.stirdrem.overgeared.datapack.QualityAttributeReloadListener.INSTANCE.getAllItems().contains(Items.IRON_PICKAXE),
                "Quality attributes do not recognize tools after reload");
        check(net.stirdrem.overgeared.datapack.RockInteractionReloadListener.INSTANCE.getAll().stream()
                        .anyMatch(interaction -> interaction.matches(Blocks.STONE.defaultBlockState(), new ItemStack(Items.FLINT))),
                "Flint-on-stone interaction missing after reload");
        test.succeed();
    }

    @GameTest public void forgingAndAssemblyPreserveQuality(GameTestHelper test) {
        ForgingRecipe forge = (ForgingRecipe)recipe(test, "overgeared:iron_pickaxe_head");
        ItemStack[] grid = new ItemStack[12];
        Arrays.fill(grid, ItemStack.EMPTY);
        // Shift the three-wide recipe to the bottom row, preserving the real anvil layout.
        for (int i = 6; i < 9; i++) grid[i] = new ItemStack(ModItems.HEATED_IRON_INGOT.get());
        check(forge.matches(input(grid), test.getLevel()), "Shifted forging recipe does not match");
        grid[0] = new ItemStack(Items.STICK);
        check(!forge.matches(input(grid), test.getLevel()), "Forging accepted an unrelated extra item");
        grid[0] = ItemStack.EMPTY;
        ItemStack head = forge.assemble(input(grid));
        check(head.is(ModItems.IRON_PICKAXE_HEAD.get()), "Wrong forged head");
        head.set(ModComponents.FORGING_QUALITY.get(), ForgingQuality.EXPERT);
        head.set(ModComponents.CREATOR.get(), "Port test smith");
        head.set(ModComponents.POLISHED.get(), true);
        CraftingInput assembly = CraftingInput.of(2, 1, List.of(new ItemStack(Items.STICK), head));
        var assemble = (OvergearedShapelessRecipe)recipe(test, "overgeared:iron_pickaxe");
        check(assemble.display().size() == 1, "Tool assembly missing from vanilla recipe book");
        check(assemble.matches(assembly, test.getLevel()), "Head and handle cannot be assembled");
        ItemStack tool = assemble.assemble(assembly);
        check(tool.is(Items.IRON_PICKAXE), "Wrong assembled tool");
        check(tool.get(ModComponents.FORGING_QUALITY.get()) == ForgingQuality.EXPERT, "Assembly lost quality");
        check("Port test smith".equals(tool.get(ModComponents.CREATOR.get())), "Assembly lost creator");
        test.succeed();
    }

    @GameTest public void castingRetainsMoldAndFinishingState(GameTestHelper test) {
        var recipe = (CastSmeltingRecipe)recipe(test, "overgeared:iron_pickaxe_head_from_cast_smelting");
        ItemStack mold = new ItemStack(ModItems.CLAY_TOOL_CAST.get());
        mold.set(ModComponents.CAST_DATA.get(), new CastData("expert", "pickaxe", Map.of("iron", 27),
                27, 27, List.of(new ItemStack(Items.IRON_INGOT, 3)), ItemStack.EMPTY, false));
        var input = new SingleRecipeInput(mold);
        check(recipe.matches(input, test.getLevel()), "Filled mold does not match smelting recipe");
        ItemStack result = recipe.assemble(input);
        check(result.is(ModItems.CLAY_TOOL_CAST.get()), "Smelting discarded the mold");
        CastData cast = result.get(ModComponents.CAST_DATA.get());
        check(cast != null && cast.output().is(ModItems.IRON_PICKAXE_HEAD.get()), "Missing cast head");
        check(cast.materials().isEmpty() && cast.heated(), "Cast material/heat transition failed");
        check(cast.output().get(ModComponents.FORGING_QUALITY.get()) == ForgingQuality.EXPERT, "Cast quality lost");
        check(Boolean.FALSE.equals(cast.output().get(ModComponents.POLISHED.get())), "Cast bypassed polishing");
        check(!recipe.matches(new SingleRecipeInput(result), test.getLevel()), "Finished mold can be recast");
        test.succeed();
    }

    @GameTest public void alloyFurnacesConsumeFuelAndPreserveQuantities(GameTestHelper test) {
        BlockPos pos = new BlockPos(1, 2, 1);
        test.setBlock(pos.below(), Blocks.STONE);
        test.setBlock(pos, ModBlocks.ALLOY_FURNACE.get());
        var alloy = test.getBlockEntity(pos, net.stirdrem.overgeared.block.entity.AlloySmelterBlockEntity.class);
        for (int i = 0; i < 3; i++) alloy.setItem(i, new ItemStack(Items.IRON_INGOT, 2));
        alloy.setItem(3, new ItemStack(Items.COAL, 2));
        alloy.setItem(4, new ItemStack(Items.LAVA_BUCKET));
        for (int tick = 0; tick < 250; tick++) {
            net.stirdrem.overgeared.block.entity.AlloySmelterBlockEntity.tick(test.getLevel(), alloy.getBlockPos(), alloy.getBlockState(), alloy);
        }
        check(alloy.getItem(5).is(ModItems.CRUDE_STEEL.get()) && alloy.getItem(5).getCount() == 4, "Alloy furnace output quantity changed");
        for (int i = 0; i < 4; i++) check(alloy.getItem(i).getCount() == 1, "Alloy furnace consumed the wrong input count");
        check(alloy.getItem(4).is(Items.BUCKET), "Alloy furnace lost the fuel container");

        test.setBlock(pos, ModBlocks.NETHER_ALLOY_FURNACE.get());
        var nether = test.getBlockEntity(pos, net.stirdrem.overgeared.block.entity.NetherAlloySmelterBlockEntity.class);
        for (int i = 0; i < 3; i++) nether.setItem(i, new ItemStack(Items.IRON_INGOT));
        nether.setItem(3, new ItemStack(Items.COAL));
        nether.setItem(9, new ItemStack(Items.LAVA_BUCKET));
        for (int tick = 0; tick < 150; tick++) {
            net.stirdrem.overgeared.block.entity.NetherAlloySmelterBlockEntity.tick(test.getLevel(), nether.getBlockPos(), nether.getBlockState(), nether);
        }
        check(nether.getItem(10).is(ModItems.CRUDE_STEEL.get()) && nether.getItem(10).getCount() == 4, "Nether alloy furnace output quantity changed");
        for (int i = 0; i < 9; i++) check(nether.getItem(i).isEmpty(), "Nether alloy furnace did not consume its ingredients");
        check(nether.getItem(9).is(Items.BUCKET), "Nether alloy furnace lost the fuel container");
        test.succeed();
    }

    @GameTest public void anvilInventoryAndOwnershipSurviveSave(GameTestHelper test) {
        BlockPos pos = new BlockPos(1, 2, 1);
        test.setBlock(pos.below(), Blocks.STONE);
        test.setBlock(pos, ModBlocks.STONE_SMITHING_ANVIL.get());
        var anvil = test.getBlockEntity(pos, BaseSmithingAnvilBlockEntity.class);
        UUID owner = UUID.fromString("de54e4e4-9813-4c60-b659-e3542b940cb8");
        anvil.setOwner(owner);
        ItemStack head = new ItemStack(ModItems.IRON_PICKAXE_HEAD.get());
        head.set(ModComponents.FORGING_QUALITY.get(), ForgingQuality.PERFECT);
        anvil.getItemHandler().setStackInSlot(7, head);
        var saved = anvil.saveWithFullMetadata(test.getLevel().registryAccess());
        var restored = (BaseSmithingAnvilBlockEntity)BlockEntity.loadStatic(anvil.getBlockPos(), anvil.getBlockState(),
                saved, test.getLevel().registryAccess());
        check(restored != null && owner.equals(restored.getOwnerUUID()), "Anvil owner lost on save");
        check(restored.getItemHandler().getStackInSlot(7).get(ModComponents.FORGING_QUALITY.get()) == ForgingQuality.PERFECT,
                "Anvil saved slot or quality changed");
        test.succeed();
    }

    @GameTest public void customMiningTiersKeepTheirProgression(GameTestHelper test) {
        var copper = new ItemStack(ModItems.COPPER_PICKAXE.get());
        var steel = new ItemStack(ModItems.STEEL_PICKAXE.get());
        check(copper.isCorrectToolForDrops(Blocks.IRON_ORE.defaultBlockState()), "Copper cannot harvest iron");
        check(!copper.isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState()), "Copper bypasses iron tier");
        check(steel.isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()), "Steel cannot harvest obsidian");
        check(!steel.isCorrectToolForDrops(Blocks.ANCIENT_DEBRIS.defaultBlockState()), "Steel bypasses diamond tier");
        test.succeed();
    }

    @GameTest public void vanillaRecipesRemainAvailable(GameTestHelper test) {
        for (String id : List.of("stone_pickaxe", "iron_pickaxe", "golden_pickaxe", "diamond_pickaxe",
                "iron_chestplate", "golden_chestplate", "diamond_chestplate", "bucket", "cauldron",
                "shears", "flint_and_steel", "arrow", "spectral_arrow", "tipped_arrow", "netherite_ingot")) {
            check(recipe(test, "minecraft:" + id) instanceof CraftingRecipe, "Vanilla crafting replaced: " + id);
        }
        var blast = recipe(test, "minecraft:iron_ingot_from_blasting_raw_iron");
        check(blast instanceof BlastingRecipe, "Vanilla raw iron blasting replaced");
        check(((BlastingRecipe)blast).assemble(new SingleRecipeInput(new ItemStack(Items.RAW_IRON))).is(Items.IRON_INGOT),
                "Vanilla blast output changed");
        CraftingInput input = CraftingInput.of(3, 3, List.of(new ItemStack(Items.IRON_INGOT), new ItemStack(Items.IRON_INGOT),
                new ItemStack(Items.IRON_INGOT), ItemStack.EMPTY, new ItemStack(Items.STICK), ItemStack.EMPTY,
                ItemStack.EMPTY, new ItemStack(Items.STICK), ItemStack.EMPTY));
        var vanilla = test.getLevel().recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, test.getLevel()).orElseThrow();
        var result = vanilla.value().assemble(input);
        check(result.is(Items.IRON_PICKAXE) && !result.has(ModComponents.FORGING_QUALITY.get()),
                "Vanilla crafting must produce a normal pickaxe");
        test.succeed();
    }

    @GameTest public void brewingKeepsEachBottlesTippingUses(GameTestHelper test) {
        BlockPos pos = new BlockPos(1, 2, 1);
        test.setBlock(pos.below(), Blocks.STONE);
        test.setBlock(pos, Blocks.BREWING_STAND);
        var stand = test.getBlockEntity(pos, BrewingStandBlockEntity.class);
        var thick = new ItemStack(Items.POTION);
        thick.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.THICK));
        thick.set(ModComponents.TIPPED_USES.get(), 3);
        var water = new ItemStack(Items.POTION);
        water.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
        water.set(ModComponents.TIPPED_USES.get(), 1);
        stand.setItem(0, thick);
        stand.setItem(1, water);
        stand.setItem(3, new ItemStack(Items.CHORUS_FRUIT));
        stand.setItem(4, new ItemStack(Items.BLAZE_POWDER));
        for (int i = 0; i < 402; i++) BrewingStandBlockEntity.serverTick(test.getLevel(), stand.getBlockPos(), stand.getBlockState(), stand);
        check(stand.getItem(0).is(Items.DRAGON_BREATH), "Dragon breath brewing recipe did not run");
        check(stand.getItem(0).getOrDefault(ModComponents.TIPPED_USES.get(), 0) == 3, "Brewing reset tipping uses");
        check(stand.getItem(1).is(Items.POTION) && stand.getItem(1).getOrDefault(ModComponents.TIPPED_USES.get(), 0) == 1,
                "Brewing changed the unrelated bottle");
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

    @GameTest public void transferredComponentsKeepTheResultItemDefaults(GameTestHelper test) {
        var ops = test.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE);
        var json = JsonParser.parseString("""
            {"pattern":["H"],"key":{"H":{"ingredient":"overgeared:iron_pickaxe_head",
             "requires_heated":true,"transfer_nbt":true}},"result":{"id":"minecraft:iron_pickaxe"}}
            """);
        var recipe = ForgingRecipe.Serializer.CODEC.codec().parse(ops, json).getOrThrow();
        ItemStack[] grid = new ItemStack[12];
        Arrays.fill(grid, ItemStack.EMPTY);
        var head = new ItemStack(ModItems.IRON_PICKAXE_HEAD.get());
        head.set(ModComponents.FORGING_QUALITY.get(), ForgingQuality.EXPERT);
        head.set(ModComponents.CREATOR.get(), "Mixed-material smith");
        grid[8] = head;
        check(!recipe.matches(input(grid), test.getLevel()), "Heated ingredient requirement ignored");
        head.set(ModComponents.HEATED_COMPONENT.get(), true);
        check(recipe.matches(input(grid), test.getLevel()), "Heated recipe cannot match the shifted head");
        var result = recipe.assemble(input(grid));
        var expected = new ItemStack(Items.IRON_PICKAXE);
        check(result.get(ModComponents.FORGING_QUALITY.get()) == ForgingQuality.EXPERT, "Transferred quality lost");
        check("Mixed-material smith".equals(result.get(ModComponents.CREATOR.get())), "Transferred creator lost");
        check(Objects.equals(expected.get(DataComponents.ITEM_MODEL), result.get(DataComponents.ITEM_MODEL)), "Tool inherited the head model");
        check(expected.getMaxStackSize() == result.getMaxStackSize(), "Tool inherited the head stack size");
        test.succeed();
    }

    @GameTest public void mixedCastMaterialsSurviveSaveAndNetwork(GameTestHelper test) {
        var mold = new ItemStack(ModItems.CLAY_TOOL_CAST.get());
        Map<String, Integer> materials = Map.of("iron", 18, "copper", 9);
        mold.set(ModComponents.CAST_DATA.get(), new CastData("expert", "pickaxe", materials, 27, 27,
                List.of(new ItemStack(Items.IRON_INGOT, 2), new ItemStack(Items.COPPER_INGOT)), ItemStack.EMPTY, true));
        var ops = test.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE);
        var encoded = ItemStack.CODEC.encodeStart(ops, mold).getOrThrow();
        var saved = ItemStack.CODEC.parse(ops, encoded).getOrThrow();
        check(saved.get(ModComponents.CAST_DATA.get()).materials().equals(materials), "Saved cast lost a material");
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), test.getLevel().registryAccess());
        try {
            ItemStack.STREAM_CODEC.encode(buffer, mold);
            var synced = ItemStack.STREAM_CODEC.decode(buffer).get(ModComponents.CAST_DATA.get());
            check(synced.materials().equals(materials) && synced.amount() == 27 && synced.heated(), "Network cast data changed");
            check(buffer.readableBytes() == 0, "Cast left network bytes unread");
        } finally { buffer.release(); }
        test.succeed();
    }

    @GameTest public void hammeringConsumesIngredientsAndProducesAHead(GameTestHelper test) {
        BlockPos pos = new BlockPos(1, 2, 1);
        test.setBlock(pos.below(), Blocks.STONE);
        test.setBlock(pos, ModBlocks.STONE_SMITHING_ANVIL.get());
        var anvil = test.getBlockEntity(pos, BaseSmithingAnvilBlockEntity.class);
        for (int i = 6; i < 9; i++) anvil.getItemHandler().setStackInSlot(i, new ItemStack(ModItems.HEATED_IRON_INGOT.get(), 2));
        anvil.getItemHandler().setStackInSlot(9, new ItemStack(ModItems.COPPER_SMITHING_HAMMER.get()));
        check(anvil.hasRecipe(), "Stone anvil cannot forge iron head");
        for (int i = 0; i < 3; i++) anvil.increaseForgingProgress(test.getLevel(), anvil.getBlockPos(), anvil.getBlockState());
        var result = anvil.getItemHandler().getStackInSlot(10);
        check(result.is(ModItems.IRON_PICKAXE_HEAD.get()) && result.getCount() == 1, "Hammering produced no head");
        for (int i = 6; i < 9; i++) check(anvil.getItemHandler().getStackInSlot(i).getCount() == 1, "Hammering consumed the wrong amount");
        check(Boolean.TRUE.equals(result.get(ModComponents.HEATED_COMPONENT.get())), "Forged head skipped quenching");
        check(Boolean.FALSE.equals(result.get(ModComponents.POLISHED.get())), "Forged head skipped polishing");
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
