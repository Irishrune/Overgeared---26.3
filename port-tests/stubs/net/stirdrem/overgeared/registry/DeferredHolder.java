package net.stirdrem.overgeared.registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import java.util.function.Supplier;
public class DeferredHolder<R,T extends R> implements Supplier<T> {
    protected final ResourceKey<R> key;
    private T value;
    public DeferredHolder(ResourceKey<R> key) {this.key=key;}
    public ResourceKey<R> getKey() {return key;}
    public Identifier getId() {return key.identifier();}
    public void bind(R value) {this.value=(T)value;}
    public T get() {return value;}
    @Override public boolean equals(Object other) {return other instanceof DeferredHolder<?,?> holder && key.equals(holder.key);}
    @Override public int hashCode() {return key.hashCode();}
}
