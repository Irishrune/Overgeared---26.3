package net.stirdrem.overgeared.fabric;

import com.mojang.serialization.MapCodec;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
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
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.item.crafting.Ingredient;
import net.stirdrem.overgeared.platform.IPlatformHelper;
import net.stirdrem.overgeared.registry.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class FabricPlatformHelper implements IPlatformHelper {
    /** Tracked through the server lifecycle events. */
    @Nullable
    static MinecraftServer currentServer;

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public boolean isPhysicalClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> void register(DeferredRegister<R> register) {
        Registry<R> registry = (Registry<R>) BuiltInRegistries.REGISTRY.getValue(register.getRegistryKey().identifier());
        if (registry == null) {
            throw new IllegalStateException("Unknown registry " + register.getRegistryKey().identifier());
        }
        register.getEntriesWithFactories().forEach((holder, factory) -> Registry.register(registry, holder.getKey(), factory.get()));
    }

    @Override
    public <T extends AbstractContainerMenu> MenuType<T> createMenuType(MenuFactory<T> factory) {
        // Fabric 26.3 replaces screenhandler.v1 with menu.v1. Keep the legacy
        // byte-buffer menu contract intact while using the new typed menu API.
        return new ExtendedMenuType<>(
                (int containerId, Inventory inventory, byte[] data) -> factory.create(containerId, inventory,
                        new RegistryFriendlyByteBuf(Unpooled.wrappedBuffer(data), inventory.player.registryAccess())),
                ByteBufCodecs.BYTE_ARRAY);
    }

    @Override
    public void openMenu(ServerPlayer player, MenuProvider provider, Consumer<RegistryFriendlyByteBuf> extraData) {
        player.openMenu(new ExtendedMenuProvider<byte[]>() {
            @Override
            public byte[] getScreenOpeningData(ServerPlayer serverPlayer) {
                RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), serverPlayer.registryAccess());
                try {
                    extraData.accept(buf);
                    byte[] bytes = new byte[buf.readableBytes()];
                    buf.readBytes(bytes);
                    return bytes;
                } finally {
                    buf.release();
                }
            }

            @Override
            public Component getDisplayName() {
                return provider.getDisplayName();
            }

            @Override
            public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player menuPlayer) {
                return provider.createMenu(containerId, inventory, menuPlayer);
            }
        });
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToAllPlayers(CustomPacketPayload payload) {
        MinecraftServer server = currentServer;
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    @Override
    public @Nullable MinecraftServer getCurrentServer() {
        return currentServer;
    }

    @Override
    public CreativeModeTab.Builder creativeModeTabBuilder() {
        return CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0);
    }

    @Override
    public int getBurnTime(ItemStack stack, LootContext context) {
        // Match AbstractFurnaceBlockEntity in Minecraft 26.3. Vanilla fuels
        // can use data-driven context providers rather than a static registry.
        return ResolvableInt.getFromItem(
                stack, DataComponents.COOKING_FUEL, CookingFuel::burnTime, context, 0);
    }

    @Override
    public boolean isCookingFuel(ItemStack stack) {
        // Like vanilla's fuel slot, accept items carrying the cooking-fuel component.
        // Their actual burn time is evaluated only in a server container context.
        return !stack.isEmpty() && stack.has(DataComponents.COOKING_FUEL);
    }

    @Override
    public ItemStack getCraftingRemainder(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStackTemplate remainder = stack.getItem().getCraftingRemainder();
        return remainder == null ? ItemStack.EMPTY : remainder.create();
    }

    @Override
    public MapCodec<Ingredient> ingredientMapCodec() {
        // Vanilla only has a list-or-object codec; the object form can be read as a map.
        return MapCodec.assumeMapUnsafe(Ingredient.CODEC);
    }

    @Override
    public void firePlayerCraftingEvent(Player player, ItemStack crafted, Container craftMatrix) {
        // Fabric has no equivalent event.
    }
}
