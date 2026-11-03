package net.stirdrem.overgeared.registry;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceKey;
public class DeferredItem<T extends Item> extends DeferredHolder<Item,T> {
    public DeferredItem(ResourceKey<Item> key) {super(key);}
}
