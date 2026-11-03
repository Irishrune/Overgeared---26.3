package net.stirdrem.overgeared.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.stirdrem.overgeared.registry.DeferredBlock;
import net.stirdrem.overgeared.registry.DeferredRegister;
import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.OvergearedMod;
import net.stirdrem.overgeared.block.custom.*;
import net.stirdrem.overgeared.item.ModItems;

import java.util.function.Function;
import net.minecraft.resources.ResourceKey;
import java.util.function.ToIntFunction;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(OvergearedMod.MOD_ID);

    public static final DeferredBlock<SteelSmithingAnvil> SMITHING_ANVIL = registerBlock("smithing_anvil",
            key -> new SteelSmithingAnvil(AnvilTier.IRON, BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL).noOcclusion().setId(key)));

    public static final DeferredBlock<TierASmithingAnvil> TIER_A_SMITHING_ANVIL = registerBlock("tier_a_smithing_anvil",
            key -> new TierASmithingAnvil(AnvilTier.ABOVE_A, BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL).noOcclusion().setId(key)));

    public static final DeferredBlock<TierBSmithingAnvil> TIER_B_SMITHING_ANVIL = registerBlock("tier_b_smithing_anvil",
            key -> new TierBSmithingAnvil(AnvilTier.ABOVE_B, BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL).noOcclusion().setId(key)));

    public static final DeferredBlock<StoneSmithingAnvil> STONE_SMITHING_ANVIL = registerBlock("stone_anvil",
            key -> new StoneSmithingAnvil(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).noOcclusion().setId(key)));

    public static final DeferredBlock<Block> STEEL_BLOCK = registerBlock("steel_block",
            key -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(key)));

    public static final DeferredBlock<BlueprintWorkbenchBlock> DRAFTING_TABLE = registerBlock("drafting_table",
            key -> new BlueprintWorkbenchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE).setId(key)));

    public static final DeferredBlock<AlloySmelterBlock> ALLOY_FURNACE = registerBlock("alloy_furnace",
            key -> new AlloySmelterBlock(BlockBehaviour.Properties
                    .ofFullCopy(Blocks.BRICKS)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()
                    .strength(3.5F, 6.0F)
                    .lightLevel(litBlockEmission()).setId(key)));

    public static final DeferredBlock<NetherAlloySmelterBlock> NETHER_ALLOY_FURNACE = registerBlock("nether_alloy_furnace",
            key -> new NetherAlloySmelterBlock(BlockBehaviour.Properties
                    .ofFullCopy(Blocks.NETHER_BRICKS)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()
                    .strength(3.5F, 6.0F)
                    .lightLevel(litBlockEmission()).setId(key)));

    public static final DeferredBlock<CastingSmelterBlock> CAST_FURNACE = registerBlock("casting_furnace",
            key -> new CastingSmelterBlock(BlockBehaviour.Properties
                    .ofFullCopy(Blocks.RED_NETHER_BRICKS)
                    .noOcclusion()
                    .requiresCorrectToolForDrops()
                    .strength(3.5F, 6.0F)
                    .lightLevel(litBlockEmission()).setId(key)));

    private static ToIntFunction<BlockState> litBlockEmission() {
        return (p_50763_) -> p_50763_.getValue(BlockStateProperties.LIT) ? 13 : 0;
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(
            String name, Function<ResourceKey<Block>, T> block) {
        DeferredBlock<T> toReturn = BLOCKS.registerWithKey(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.registerWithKey(name, key -> new BlockItem(block.get(), new Item.Properties().useBlockDescriptionPrefix().setId(key)));
    }

    public static void register() {
        BLOCKS.register();
    }
}
