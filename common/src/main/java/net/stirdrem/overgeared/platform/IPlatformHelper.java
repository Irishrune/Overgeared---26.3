package net.stirdrem.overgeared.platform;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.LootContext;
import net.stirdrem.overgeared.registry.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import java.util.function.Consumer;

/**
 * Everything the common code needs from the mod loader.
 */
public interface IPlatformHelper {

    /** @return "NeoForge" or "Fabric" */
    String getPlatformName();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    /** @return true when running on the physical client */
    boolean isPhysicalClient();

    /** Hands a register to the loader so its entries are registered at the right time. */
    <R> void register(DeferredRegister<R> register);

    /** Creates a menu type whose client-side constructor receives extra data written by the server. */
    <T extends AbstractContainerMenu> MenuType<T> createMenuType(MenuFactory<T> factory);

    /** Opens a menu, sending extra data that is read by the client-side constructor of the menu. */
    void openMenu(ServerPlayer player, MenuProvider provider, Consumer<RegistryFriendlyByteBuf> extraData);

    void sendToServer(CustomPacketPayload payload);

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    void sendToAllPlayers(CustomPacketPayload payload);

    @Nullable
    MinecraftServer getCurrentServer();

    /** A creative tab builder; vanilla's needs a row and column which both loaders fill in themselves. */
    CreativeModeTab.Builder creativeModeTabBuilder();

    /** 26.3 cooking fuel burn duration resolved against this workstation's container context. */
    int getBurnTime(ItemStack stack, LootContext context);

    /** Allows hopper insertion without evaluating a context-sensitive burn duration. */
    boolean isCookingFuel(ItemStack stack);

    /** What is left in the crafting grid after the stack is used, honouring stack-sensitive remainders. */
    ItemStack getCraftingRemainder(ItemStack stack);

    /**
     * A non-empty ingredient encoded inline in a JSON object (e.g. {@code {"item": ..., "remainder": true}}),
     * supporting the loader's custom ingredient types.
     */
    MapCodec<Ingredient> ingredientMapCodec();

    /** Fires the loader's "item crafted" hook, if it has one. */
    void firePlayerCraftingEvent(Player player, ItemStack crafted, Container craftMatrix);

    @FunctionalInterface
    interface MenuFactory<T extends AbstractContainerMenu> {
        T create(int containerId, Inventory inventory, RegistryFriendlyByteBuf data);
    }
}
