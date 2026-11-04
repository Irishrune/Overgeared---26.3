package net.stirdrem.overgeared.block.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.stirdrem.overgeared.registry.DeferredHolder;
import net.stirdrem.overgeared.registry.DeferredRegister;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.ModBlocks;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, OvergearedMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteelSmithingAnvilBlockEntity>> STEEL_SMITHING_ANVIL_BE =
            BLOCK_ENTITIES.register("smithing_table_be", () ->
                    FabricBlockEntityTypeBuilder.create(SteelSmithingAnvilBlockEntity::new,
                                    ModBlocks.SMITHING_ANVIL.get()).
                            build());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TierASmithingAnvilBlockEntity>> TIER_A_SMITHING_ANVIL_BE =
            BLOCK_ENTITIES.register("tier_a_smithing_table_be", () ->
                    FabricBlockEntityTypeBuilder.create(TierASmithingAnvilBlockEntity::new,
                                    ModBlocks.TIER_A_SMITHING_ANVIL.get()).
                            build());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TierBSmithingAnvilBlockEntity>> TIER_B_SMITHING_ANVIL_BE =
            BLOCK_ENTITIES.register("tier_b_smithing_table_be", () ->
                    FabricBlockEntityTypeBuilder.create(TierBSmithingAnvilBlockEntity::new,
                                    ModBlocks.TIER_B_SMITHING_ANVIL.get()).
                            build());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StoneSmithingAnvilBlockEntity>> STONE_SMITHING_ANVIL_BE =
            BLOCK_ENTITIES.register("stone_smithing_table_be", () ->
                    FabricBlockEntityTypeBuilder.create(StoneSmithingAnvilBlockEntity::new,
                                    ModBlocks.STONE_SMITHING_ANVIL.get()).
                            build());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AlloySmelterBlockEntity>> ALLOY_FURNACE_BE =
            BLOCK_ENTITIES.register("alloy_furnace_be", () ->
                    FabricBlockEntityTypeBuilder.create(AlloySmelterBlockEntity::new,
                            ModBlocks.ALLOY_FURNACE.get()).build());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NetherAlloySmelterBlockEntity>> NETHER_ALLOY_FURNACE_BE =
            BLOCK_ENTITIES.register("nether_alloy_furnace_be", () ->
                    FabricBlockEntityTypeBuilder.create(NetherAlloySmelterBlockEntity::new,
                            ModBlocks.NETHER_ALLOY_FURNACE.get()).build());
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CastingSmelterBlockEntity>> CAST_FURNACE_BE =
            BLOCK_ENTITIES.register("casting_furnace_be", () ->
                    FabricBlockEntityTypeBuilder.create(CastingSmelterBlockEntity::new,
                            ModBlocks.CAST_FURNACE.get()).build());

    public static void register() {
        BLOCK_ENTITIES.register();
    }
}
