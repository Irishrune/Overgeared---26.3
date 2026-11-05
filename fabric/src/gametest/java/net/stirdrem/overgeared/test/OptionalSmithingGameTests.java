package net.stirdrem.overgeared.test;

import java.util.*;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.stirdrem.overgeared.ForgingQuality;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.custom.AlloySmelterBlock;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.components.ModComponents;
import net.stirdrem.overgeared.item.ModItems;
import net.stirdrem.overgeared.recipe.*;
import net.stirdrem.overgeared.util.QualityTooltips;

public class OptionalSmithingGameTests {
    private static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    private static void near(double value, double expected, String message) {
        check(Math.abs(value - expected) < .001, message + ": expected " + expected + ", got " + value);
    }
    private static double contribution(ItemStack stack, EquipmentSlot slot, Holder<Attribute> attribute) {
        double[] total = {0};
        stack.forEachModifier(slot, (a, m) -> { if (a.equals(attribute)) total[0] += m.amount(); });
        return total[0];
    }
    private static Recipe<?> recipe(GameTestHelper test, String name) {
        return test.getLevel().recipeAccess().getRecipes().stream().filter(r -> r.id().identifier().toString().equals(name))
                .findFirst().orElseThrow().value();
    }

    @GameTest public void qualityChangesRealStackStatsAndNames(GameTestHelper test) {
        ForgingQuality[] qualities = {ForgingQuality.POOR, ForgingQuality.WELL, ForgingQuality.EXPERT, ForgingQuality.PERFECT, ForgingQuality.MASTER};
        int[] durability = {200, 250, 275, 300, 325};
        double[] bonuses = {.9, 1, 1.05, 1.1, 1.15};
        for (int i = 0; i < qualities.length; i++) {
            ForgingQuality quality = qualities[i];
            ItemStack tool = new ItemStack(Items.IRON_PICKAXE);
            ItemStack armor = new ItemStack(Items.IRON_CHESTPLATE);
            ItemStack sword = new ItemStack(Items.IRON_SWORD);
            for (ItemStack stack : List.of(tool, armor, sword)) stack.set(ModComponents.FORGING_QUALITY.get(), quality);
            check(tool.getMaxDamage() == durability[i], "Quality durability missing for " + quality);
            near(tool.getDestroySpeed(Blocks.STONE.defaultBlockState()), 6 * bonuses[i], "Quality mining speed " + quality);
            near(contribution(armor, EquipmentSlot.CHEST, Attributes.ARMOR), 6 * bonuses[i], "Quality armor " + quality);
            near(contribution(sword, EquipmentSlot.MAINHAND, Attributes.ATTACK_DAMAGE), 5 * bonuses[i], "Quality weapon damage " + quality);
            near(contribution(sword, EquipmentSlot.MAINHAND, Attributes.ATTACK_DAMAGE), 5 * bonuses[i], "Repeated query stacked bonuses");
            check(!QualityTooltips.lines(tool).isEmpty(), "Quality tooltip missing");
            if (quality != ForgingQuality.WELL) {
                check(tool.getStyledHoverName().getStyle().getColor().equals(net.minecraft.network.chat.TextColor.fromLegacyFormat(quality.getColor())), "Quality name color missing");
                tool.set(DataComponents.CUSTOM_NAME, Component.literal("Jerry's Pick"));
                check(tool.getHoverName().getString().equals("Jerry's Pick"), "Quality replaced custom name");
                check(tool.getHoverName().getStyle().getColor().equals(net.minecraft.network.chat.TextColor.fromLegacyFormat(quality.getColor())), "Custom name lost quality color");
            }
        }
        ItemStack normal = new ItemStack(Items.IRON_PICKAXE);
        var originalStyle = normal.getStyledHoverName().getStyle();
        normal.set(ModComponents.FORGING_QUALITY.get(), ForgingQuality.WELL);
        check(normal.getStyledHoverName().getStyle().equals(originalStyle), "Normal quality recolored item");
        test.succeed();
    }

    @GameTest public void assembledMasterworkRetainsStatsAndGoldName(GameTestHelper test) {
        ItemStack head = new ItemStack(ModItems.IRON_PICKAXE_HEAD.get());
        head.set(ModComponents.FORGING_QUALITY.get(), ForgingQuality.MASTER);
        head.set(ModComponents.POLISHED.get(), true);
        head.set(ModComponents.HEATED_COMPONENT.get(), false);
        var assembly = (OvergearedShapelessRecipe) recipe(test, "overgeared:iron_pickaxe");
        var tool = assembly.assemble(CraftingInput.of(2, 1, List.of(head, new ItemStack(Items.STICK))));
        check(tool.get(ModComponents.FORGING_QUALITY.get()) == ForgingQuality.MASTER, "Assembly lost masterwork");
        check(tool.getMaxDamage() == 325, "Assembly lost durability benefit");
        check(tool.getStyledHoverName().getStyle().getColor().getValue() == 0xFFAA00, "Assembly lost gold name");
        test.succeed();
    }

    @GameTest public void bothAnvilsAreCraftable(GameTestHelper test) {
        var stone = (CraftingRecipe) recipe(test, "overgeared:stone_anvil");
        var input = CraftingInput.of(3, 3, List.of(new ItemStack(Items.STONE), new ItemStack(Items.STONE), new ItemStack(Items.STONE),
                ItemStack.EMPTY, new ItemStack(Items.STONE), ItemStack.EMPTY, ItemStack.EMPTY, new ItemStack(Items.STONE), ItemStack.EMPTY));
        check(stone.matches(input, test.getLevel()) && stone.assemble(input).is(ModBlocks.STONE_SMITHING_ANVIL.get().asItem()), "Stone anvil recipe failed");
        var iron = (CraftingRecipe) recipe(test, "overgeared:smithing_anvil");
        input = CraftingInput.of(2, 1, List.of(new ItemStack(Items.ANVIL), new ItemStack(Items.IRON_INGOT)));
        check(iron.matches(input, test.getLevel()) && iron.assemble(input).is(ModBlocks.SMITHING_ANVIL.get().asItem()), "Smithing anvil recipe failed");
        test.succeed();
    }

    @GameTest public void allCopperPartsAndArmorForgeOnStone(GameTestHelper test) {
        BlockPos pos = new BlockPos(1, 2, 1);
        test.setBlock(pos.below(), Blocks.STONE);
        test.setBlock(pos, ModBlocks.STONE_SMITHING_ANVIL.get());
        var anvil = test.getBlockEntity(pos, AbstractSmithingAnvilBlockEntity.class);
        int count = 0;
        for (var holder : RecipeLookups.all(test.getLevel(), ModRecipeTypes.FORGING.get())) {
            ForgingRecipe forge = holder.value();
            if (!holder.id().identifier().getPath().startsWith("copper_")) continue;
            for (int slot = 0; slot < 12; slot++) anvil.getItemHandler().setStackInSlot(slot, ItemStack.EMPTY);
            for (int i = 0; i < forge.getForgingIngredients().size(); i++) {
                var ingredient = forge.getForgingIngredients().get(i);
                if (ingredient.ingredient().isEmpty()) continue;
                var stack = new ItemStack(ingredient.ingredient().get().items().findFirst().orElseThrow());
                if (ingredient.requiresHeated()) stack.set(ModComponents.HEATED_COMPONENT.get(), true);
                anvil.getItemHandler().setStackInSlot((i / forge.getWidth()) * 3 + i % forge.getWidth(), stack);
            }
            check(anvil.hasRecipe(), "Stone anvil rejects " + holder.id());
            for (int hit = 0; hit < forge.getHammeringRequired(); hit++) anvil.increaseForgingProgress(test.getLevel(), anvil.getBlockPos(), anvil.getBlockState());
            check(anvil.getItemHandler().getStackInSlot(10).is(forge.getResultItem(test.getLevel().registryAccess()).getItem()), "No output for " + holder.id());
            count++;
        }
        check(count >= 12, "Copper coverage incomplete");
        test.succeed();
    }

    @GameTest public void furnacesFaceThePlacingPlayer(GameTestHelper test) {
        for (var block : List.of(ModBlocks.ALLOY_FURNACE.get(), ModBlocks.NETHER_ALLOY_FURNACE.get())) {
            for (Direction facing : List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)) {
                var context = new DirectionalPlaceContext(test.getLevel(), test.absolutePos(new BlockPos(1, 2, 1)), facing,
                        new ItemStack(block.asItem()), Direction.UP);
                check(block.getStateForPlacement(context).getValue(AlloySmelterBlock.FACING) == context.getHorizontalDirection().getOpposite(), "Furnace faces away");
            }
        }
        test.succeed();
    }

    @GameTest public void anvilExplainsInvalidIngredientsAndTier(GameTestHelper test) {
        BlockPos pos = new BlockPos(1, 2, 1);
        test.setBlock(pos.below(), Blocks.STONE);
        test.setBlock(pos, ModBlocks.STONE_SMITHING_ANVIL.get());
        var anvil = test.getBlockEntity(pos, AbstractSmithingAnvilBlockEntity.class);
        anvil.getItemHandler().setStackInSlot(0, new ItemStack(Items.DIRT));
        check(((TranslatableContents) anvil.forgingProblem().getContents()).getKey().endsWith("ingredients_mismatch"), "Missing ingredient explanation");
        anvil.getItemHandler().setStackInSlot(0, new ItemStack(ModItems.HEATED_CRUDE_STEEL.get()));
        check(((TranslatableContents) anvil.forgingProblem().getContents()).getKey().endsWith("anvil_tier"), "Missing tier explanation");
        for (int i = 0; i < 3; i++) anvil.getItemHandler().setStackInSlot(i, new ItemStack(ModItems.HEATED_COPPER_INGOT.get()));
        var blueprint = new ItemStack(ModItems.BLUEPRINT.get());
        blueprint.set(ModComponents.BLUEPRINT_DATA.get(), new net.stirdrem.overgeared.components.BlueprintData("perfect", "sword", 0));
        anvil.getItemHandler().setStackInSlot(11, blueprint);
        check(((TranslatableContents) anvil.forgingProblem().getContents()).getKey().endsWith("wrong_blueprint"), "Missing blueprint explanation");
        test.succeed();
    }

    @GameTest public void qualityDefinitionsHaveValidAttributesAndSync(GameTestHelper test) {
        var defs = net.stirdrem.overgeared.datapack.QualityAttributeReloadListener.getAll();
        check(defs.size() == 2, "Expected armor and damage quality definitions");
        for (var def : defs) check(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.getValue(def.attribute()) != null,
                "Unknown quality attribute " + def.attribute());
        var packet = new net.stirdrem.overgeared.networking.packet.QualityDefinitionsS2CPacket(
                net.stirdrem.overgeared.datapack.QualityAttributeReloadListener.snapshotJson());
        var buffer = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try {
            var codec = net.stirdrem.overgeared.networking.packet.QualityDefinitionsS2CPacket.STREAM_CODEC;
            codec.encode(buffer, packet);
            check(codec.decode(buffer).equals(packet), "Quality definition sync lost data");
        } finally { buffer.release(); }
        test.succeed();
    }
}
