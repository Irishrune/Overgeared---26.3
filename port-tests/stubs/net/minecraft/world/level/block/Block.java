package net.minecraft.world.level.block;
import net.minecraft.resources.ResourceKey;
public class Block {
    public final ResourceKey<Block> id;
    public Block(ResourceKey<Block> id) {
        if (id==null) throw new IllegalStateException("Block id not set");
        this.id=id;
    }
}
