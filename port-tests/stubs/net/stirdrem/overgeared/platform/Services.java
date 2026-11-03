package net.stirdrem.overgeared.platform;
import net.stirdrem.overgeared.registry.DeferredRegister;
public final class Services {
  public static final Platform PLATFORM = new Platform();
  public static class Platform {
    public <R> void register(DeferredRegister<R> register) {
       register.getEntriesWithFactories().forEach((holder, factory) -> ((net.stirdrem.overgeared.registry.DeferredHolder<R,R>)holder).bind(factory.get()));
    }
  }
}
