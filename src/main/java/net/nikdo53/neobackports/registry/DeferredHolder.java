package net.nikdo53.neobackports.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

/**
 * Fabric shim for NeoBackports' {@code DeferredHolder} — see FABRIC_PORT_PLAN.md §5.1.
 * Collects a (key, supplier) pair; {@link #bind} performs the real vanilla registration
 * once the owning {@link DeferredRegisterTyped} is flushed.
 */
public class DeferredHolder<R, T extends R> implements Supplier<T>
{
    private final ResourceKey<R> key;
    private final Supplier<? extends T> factory;
    private T value;
    private Holder<R> holder;

    DeferredHolder(ResourceKey<R> key, Supplier<? extends T> factory)
    {
        this.key = key;
        this.factory = factory;
    }

    /**
     * For types with no real vanilla {@link Registry} to bind against (e.g.
     * {@code DataComponentType}, see FABRIC_PORT_PLAN.md §5.2) — the value is already known,
     * so this returns an already-bound holder with no {@link #bind} call needed.
     */
    static <R, T extends R> DeferredHolder<R, T> bound(ResourceKey<R> key, T value)
    {
        DeferredHolder<R, T> holder = new DeferredHolder<>(key, () -> value);
        holder.value = value;
        return holder;
    }

    T bind(Registry<R> registry)
    {
        value = factory.get();
        Registry.register(registry, key.location(), value);
        holder = registry.wrapAsHolder(value);
        return value;
    }

    @Override
    public T get()
    {
        if (value == null)
            throw new IllegalStateException("Tried to access " + key + " before it was registered");
        return value;
    }

    public T value()
    {
        return get();
    }

    public ResourceKey<R> getKey()
    {
        return key;
    }

    public ResourceLocation getId()
    {
        return key.location();
    }

    public boolean isBound()
    {
        return value != null;
    }

    public Holder<R> asHolder()
    {
        if (holder == null)
            throw new IllegalStateException("Tried to access " + key + " before it was registered");
        return holder;
    }
}
