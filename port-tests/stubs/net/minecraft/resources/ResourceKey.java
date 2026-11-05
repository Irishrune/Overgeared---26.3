package net.minecraft.resources;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Registry;
public record ResourceKey<T>(Identifier registry, Identifier identifier) {
    public static <T> ResourceKey<T> create(ResourceKey<? extends Registry<T>> registryKey,Identifier id) {
        return new ResourceKey<>(registryKey.identifier(),id);
    }
}
