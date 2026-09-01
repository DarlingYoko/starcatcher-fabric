package net.nikdo53.neobackports.registry;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Fabric shim for NeoBackports' {@code DeferredHolder} — see FABRIC_PORT_PLAN.md §5.1.
 * Collects a (key, supplier) pair; {@link #bind} performs the real vanilla registration
 * once the owning {@link DeferredRegisterTyped} is flushed.
 *
 * Implements vanilla {@link Holder}&lt;T&gt; (like the real NeoForge type does) so the ~300
 * call sites across the tree that pass a {@code SCItems.X}/{@code SCBlocks.X} field directly
 * into APIs expecting a {@code Holder}/{@code ItemLike} (recipes, tags, `Ingredient.of`, …)
 * compile unchanged — discovered 2026-09-01 (P9) once the codebase was for the first time
 * actually fully attributed by javac (see §10's corrected diagnosis: earlier "toolchain bug"
 * reports were javac silently skipping full attribution whenever a mixin target failed to
 * resolve, not a real Loom/Mixin bug).
 */
public class DeferredHolder<R, T extends R> implements Holder<T>, Supplier<T>
{
    private final ResourceKey<R> key;
    private final Supplier<? extends T> factory;
    private T value;
    private Holder<T> holder;

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

    @SuppressWarnings("unchecked")
    T bind(Registry<R> registry)
    {
        value = factory.get();
        Registry.register(registry, key.location(), value);
        // the registered value is exactly T, so the wrapping Holder<R> is really a Holder<T>
        holder = (Holder<T>) (Holder<?>) registry.wrapAsHolder(value);
        return value;
    }

    @Override
    public T get()
    {
        if (value == null)
            throw new IllegalStateException("Tried to access " + key + " before it was registered");
        return value;
    }

    @Override
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

    @Override
    public boolean isBound()
    {
        return value != null;
    }

    public Holder<T> asHolder()
    {
        if (holder == null)
            throw new IllegalStateException("Tried to access " + key + " before it was registered");
        return holder;
    }

    @Override
    public boolean is(ResourceLocation location)
    {
        return asHolder().is(location);
    }

    @Override
    public boolean is(ResourceKey<T> resourceKey)
    {
        return asHolder().is(resourceKey);
    }

    @Override
    public boolean is(Predicate<ResourceKey<T>> predicate)
    {
        return asHolder().is(predicate);
    }

    @Override
    public boolean is(TagKey<T> tagKey)
    {
        return asHolder().is(tagKey);
    }

    @Override
    public Stream<TagKey<T>> tags()
    {
        return asHolder().tags();
    }

    @Override
    public Either<ResourceKey<T>, T> unwrap()
    {
        return asHolder().unwrap();
    }

    @Override
    public Optional<ResourceKey<T>> unwrapKey()
    {
        return asHolder().unwrapKey();
    }

    @Override
    public Kind kind()
    {
        return asHolder().kind();
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner)
    {
        return asHolder().canSerializeIn(owner);
    }
}
