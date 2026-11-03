package net.stirdrem.overgeared.registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.resources.ResourceKey;
public class DeferredBlock<T extends Block> extends DeferredHolder<Block,T> {
    public DeferredBlock(ResourceKey<Block> key) {super(key);}
}
