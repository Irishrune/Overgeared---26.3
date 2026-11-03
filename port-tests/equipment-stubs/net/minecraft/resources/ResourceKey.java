package net.minecraft.resources;
public record ResourceKey<T>(String name) { public static <T> ResourceKey<T> create(ResourceKey<?> root, String name){return new ResourceKey<>(name);} }
