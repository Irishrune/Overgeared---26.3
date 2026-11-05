package net.stirdrem.overgeared.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.stirdrem.overgeared.block.ModBlocks;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.item.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.stirdrem.overgeared.event.AnvilMinigameEvents;
import net.stirdrem.overgeared.screen.*;

/** Exercises actual server-to-client menu opening and the 26.3 rendering pipeline. */
public class PortClientGameTests implements FabricClientGameTest {
    private record Station(Block block, Class<? extends Screen> screen, String screenshot) {}

    @Override public void runTest(ClientGameTestContext context) {
        System.out.println("Overgeared client test: creating world");
        try (var world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            System.out.println("Overgeared client test: world rendered");
            world.getServer().runOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                player.awardRecipes(player.level().recipeAccess().getRecipes().stream()
                        .filter(recipe -> recipe.value() instanceof net.stirdrem.overgeared.recipe.ForgingRecipe).toList());
            });
            context.waitFor(client -> !client.player.getRecipeBook().getCollection(
                    net.stirdrem.overgeared.recipe.ForgingRecipeCategories.TOOLS).isEmpty());
            Station[] stations = {
                new Station(ModBlocks.STONE_SMITHING_ANVIL.get(), StoneSmithingAnvilScreen.class, "stone-anvil"),
                new Station(ModBlocks.SMITHING_ANVIL.get(), SteelSmithingAnvilScreen.class, "iron-anvil"),
                new Station(ModBlocks.TIER_A_SMITHING_ANVIL.get(), TierASmithingAnvilScreen.class, "tier-a-anvil"),
                new Station(ModBlocks.TIER_B_SMITHING_ANVIL.get(), TierBSmithingAnvilScreen.class, "tier-b-anvil"),
                new Station(ModBlocks.ALLOY_FURNACE.get(), AlloySmelterScreen.class, "alloy-furnace"),
                new Station(ModBlocks.NETHER_ALLOY_FURNACE.get(), NetherAlloySmelterScreen.class, "nether-alloy-furnace"),
                new Station(ModBlocks.CAST_FURNACE.get(), CastingSmelterScreen.class, "casting-furnace"),
                new Station(ModBlocks.DRAFTING_TABLE.get(), BlueprintWorkbenchScreen.class, "blueprint-workbench")
            };
            for (Station station : stations) {
                System.out.println("Overgeared client test: opening " + station.screenshot());
                var pos = world.getServer().computeOnServer(server -> world.getConnection().getServerPlayer().blockPosition().offset(2, 0, 0));
                world.getServer().runOnServer(server -> {
                    var player = world.getConnection().getServerPlayer();
                    var level = player.level();
                    level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
                    level.setBlockAndUpdate(pos, station.block().defaultBlockState());
                    if (level.getBlockEntity(pos) instanceof AbstractSmithingAnvilBlockEntity anvil) {
                        for (int i = 0; i < 3; i++) anvil.getItemHandler().setStackInSlot(i, new ItemStack(ModItems.HEATED_IRON_INGOT.get()));
                        anvil.getItemHandler().setStackInSlot(9, new ItemStack(ModItems.COPPER_SMITHING_HAMMER.get()));
                    }
                });
                // A real player can only interact after the placed workstation is visible.
                context.waitFor(client -> client.level.getBlockState(pos).is(station.block())
                        && (!(station.block() instanceof net.minecraft.world.level.block.EntityBlock)
                            || client.level.getBlockEntity(pos) != null));
                world.getServer().runOnServer(server -> {
                    var player = world.getConnection().getServerPlayer();
                    var level = player.level();
                    level.getBlockState(pos).useWithoutItem(level, player,
                            new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
                });
                context.waitForScreen(station.screen());
                context.waitTicks(5);
                context.takeScreenshot(station.screenshot());
                if (context.computeOnClient(client -> client.gui.screen() instanceof AbstractSmithingAnvilScreen<?>)) {
                    context.waitFor(client -> client.player.containerMenu instanceof AbstractSmithingAnvilMenu menu
                            && menu.getGhostResult().is(ModItems.IRON_PICKAXE_HEAD.get()));
                    context.runOnClient(client -> client.gui.screen().children().stream()
                            .filter(ImageButton.class::isInstance).map(ImageButton.class::cast)
                            .findFirst().orElseThrow().onPress(new KeyEvent(40, 40, 0)));
                    context.waitTicks(5);
                    context.takeScreenshot(station.screenshot() + "-recipes");
                }
                context.runOnClient(client -> client.player.closeContainer());
                world.getConnection().waitForServerboundPackets();
                System.out.println("Overgeared client test: passed " + station.screenshot());
            }
            forgeCopperBlade(context, world);
            checkQualityTooltips(context);
        }
    }

    /** Exercise player input, session packets and server crafting together. */
    private void forgeCopperBlade(ClientGameTestContext context, TestSingleplayerContext world) {
        System.out.println("Overgeared client test: forging copper blade with hammer clicks");
        var pos = world.getServer().computeOnServer(server -> world.getConnection().getServerPlayer().blockPosition().offset(2, 0, 0));
        world.getServer().runOnServer(server -> {
            var player = world.getConnection().getServerPlayer();
            player.setGameMode(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.COPPER_SMITHING_HAMMER.get()));
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            var level = player.level();
            level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(pos, ModBlocks.STONE_SMITHING_ANVIL.get().defaultBlockState());
            var anvil = (AbstractSmithingAnvilBlockEntity) level.getBlockEntity(pos);
            anvil.getItemHandler().setStackInSlot(0, new ItemStack(ModItems.HEATED_COPPER_INGOT.get()));
            anvil.getItemHandler().setStackInSlot(3, new ItemStack(ModItems.HEATED_COPPER_INGOT.get()));
            if (!anvil.hasRecipe() || anvil.getRequiredProgress() != 3) {
                throw new AssertionError("Copper blade must be a valid three-hit recipe");
            }
            player.containerMenu.broadcastChanges();
        });
        context.waitFor(client -> client.player.getMainHandItem().is(ModItems.COPPER_SMITHING_HAMMER.get())
                && client.level.getBlockState(pos).is(ModBlocks.STONE_SMITHING_ANVIL.get())
                && client.level.getBlockEntity(pos) instanceof AbstractSmithingAnvilBlockEntity);
        context.getInput().holdKey(options -> options.keyShift);
        context.waitFor(client -> client.player.isCrouching());
        world.getConnection().waitForServerboundPackets();
        context.getInput().lookAt(pos);
        context.getInput().pressKey(options -> options.keyUse);
        context.getInput().releaseKey(options -> options.keyShift);
        context.waitFor(client -> AnvilMinigameEvents.isVisible() && !client.player.isCrouching());
        if (context.computeOnClient(client -> ((AbstractSmithingAnvilBlockEntity) client.level.getBlockEntity(pos)).hasRecipe())) {
            throw new AssertionError("Regression must run without a client-side recipe database");
        }
        context.takeScreenshot("copper-blade-forging-timer");
        for (int hit = 1; hit <= 3; hit++) {
            int expectedHits = hit;
            context.getInput().lookAt(pos);
            context.waitTicks(5);
            context.getInput().pressKey(options -> options.keyUse);
            world.getConnection().waitForServerboundPackets();
            world.getServer().waitFor(server -> {
                var player = world.getConnection().getServerPlayer();
                var anvil = (AbstractSmithingAnvilBlockEntity) player.level().getBlockEntity(pos);
                return player.getMainHandItem().getDamageValue() == expectedHits
                        && (expectedHits < 3 ? anvil.getProgress() == expectedHits
                            : anvil.getItemHandler().getStackInSlot(10).is(ModItems.COPPER_SWORD_BLADE.get()));
            });
            System.out.println("Overgeared client test: copper blade hammer hit " + hit + " registered on server");
        }
        context.waitFor(client -> !AnvilMinigameEvents.isVisible());
        world.getServer().runOnServer(server -> {
            var player = world.getConnection().getServerPlayer();
            var anvil = (AbstractSmithingAnvilBlockEntity) player.level().getBlockEntity(pos);
            if (!anvil.getItemHandler().getStackInSlot(0).isEmpty()
                    || !anvil.getItemHandler().getStackInSlot(3).isEmpty()
                    || anvil.getItemHandler().getStackInSlot(10).getCount() != 1) {
                throw new AssertionError("Three hammer clicks must consume two ingots and produce exactly one blade");
            }
        });
        System.out.println("Overgeared client test: passed copper blade forging through player input");
        // Copper plates use normal hammering, which must also work on stone.
        world.getServer().runOnServer(server -> {
            var player = world.getConnection().getServerPlayer();
            var anvil = (AbstractSmithingAnvilBlockEntity) player.level().getBlockEntity(pos);
            anvil.getItemHandler().setStackInSlot(10, ItemStack.EMPTY);
            anvil.getItemHandler().setStackInSlot(0, new ItemStack(net.minecraft.world.item.Items.COPPER_INGOT));
        });
        context.waitTicks(5);
        for (int hit = 1; hit <= 3; hit++) {
            int expectedHits = hit;
            context.getInput().lookAt(pos);
            context.waitTicks(5);
            context.getInput().pressKey(options -> options.keyUse);
            world.getConnection().waitForServerboundPackets();
            world.getServer().waitFor(server -> {
                var player = world.getConnection().getServerPlayer();
                var anvil = (AbstractSmithingAnvilBlockEntity) player.level().getBlockEntity(pos);
                return player.getMainHandItem().getDamageValue() == 3 + expectedHits
                        && (expectedHits < 3 ? anvil.getProgress() == expectedHits
                            : anvil.getItemHandler().getStackInSlot(10).is(ModItems.COPPER_PLATE.get()));
            });
        }
        System.out.println("Overgeared client test: passed direct copper plate hammering on stone");
    }

    private void checkQualityTooltips(ClientGameTestContext context) {
        context.runOnClient(client -> {
            var stack = new ItemStack(net.minecraft.world.item.Items.IRON_PICKAXE);
            stack.set(net.stirdrem.overgeared.components.ModComponents.FORGING_QUALITY.get(), net.stirdrem.overgeared.ForgingQuality.MASTER);
            var lines = stack.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.of(client.level), client.player,
                    net.minecraft.world.item.TooltipFlag.NORMAL);
            if (lines.getFirst().getStyle().getColor().getValue() != 0xFFAA00
                    || lines.stream().noneMatch(c -> c.getString().contains("+30%"))
                    || lines.stream().noneMatch(c -> c.getString().contains("+15%"))) {
                throw new AssertionError("Masterwork client tooltip must show gold name, durability and mining bonuses: " + lines);
            }
            var armor = new ItemStack(net.minecraft.world.item.Items.IRON_CHESTPLATE);
            armor.set(net.stirdrem.overgeared.components.ModComponents.FORGING_QUALITY.get(), net.stirdrem.overgeared.ForgingQuality.PERFECT);
            var armorLines = armor.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.of(client.level), client.player,
                    net.minecraft.world.item.TooltipFlag.NORMAL);
            if (armorLines.stream().noneMatch(c -> c.getString().contains("+0.60"))) {
                throw new AssertionError("Client is missing the server's armor bonus definition: " + armorLines);
            }
        });
        System.out.println("Overgeared client test: passed quality names and real tooltip bonuses");
    }
}

