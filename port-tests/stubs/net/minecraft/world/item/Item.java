package net.minecraft.world.item;
import net.minecraft.resources.ResourceKey;
public class Item {
    public final ResourceKey<Item> id;
    public Item(Properties properties) {
        if (properties.id==null) throw new IllegalStateException("Item id not set");
        this.id=properties.id;
    }
    public static class Properties {
        private ResourceKey<Item> id;
        public Properties setId(ResourceKey<Item> id) {this.id=id;return this;}
        public Properties useBlockDescriptionPrefix() {return this;}
    }
}
