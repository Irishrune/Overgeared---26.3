package net.stirdrem.overgeared.test;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
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
import net.stirdrem.overgeared.screen.*;

/** Exercises actual server-to-client menu opening and the 26.3 rendering pipeline. */
public class PortClientGameTests implements FabricClientGameTest {
    private record Station(Block block, Class<? extends Screen> screen, String screenshot) {}

    @Override public void runTest(ClientGameTestContext context) {
        System.out.println("Overgeared client test: creating world");
        try (var world = context.worldBuilder().create()) {
            world.getConnection().waitForChunksRender();
            System.out.println("Overgeared client test: world rendered");
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
                world.getServer().runOnServer(server -> {
                    var player = world.getConnection().getServerPlayer();
                    var level = player.level();
                    var pos = player.blockPosition().offset(2, 0, 0);
                    level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
                    level.setBlockAndUpdate(pos, station.block().defaultBlockState());
                    if (level.getBlockEntity(pos) instanceof AbstractSmithingAnvilBlockEntity anvil) {
                        for (int i = 0; i < 3; i++) anvil.getItemHandler().setStackInSlot(i, new ItemStack(ModItems.HEATED_IRON_INGOT.get()));
                        anvil.getItemHandler().setStackInSlot(9, new ItemStack(ModItems.COPPER_SMITHING_HAMMER.get()));
                    }
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
        }
    }
}

