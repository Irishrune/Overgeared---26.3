package net.minecraft.core.registries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
public class Registries {
    public static final ResourceKey<Registry<Item>> ITEM = new ResourceKey<>(new Identifier("minecraft","root"),new Identifier("minecraft","item"));
    public static final ResourceKey<Registry<Block>> BLOCK = new ResourceKey<>(new Identifier("minecraft","root"),new Identifier("minecraft","block"));
}
