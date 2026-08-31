package net.nikdo53.neobackports.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Fabric shim for NeoBackports' {@code DeferredRegisterTyped} — see FABRIC_PORT_PLAN.md §5.1.
 * {@link #register(String, Supplier)} only collects entries; the real vanilla
 * {@code Registry.register(...)} calls happen in {@link #register(IEventBus)}, so callers
 * (StarcatcherFabric.onInitialize) must invoke registries in dependency order (e.g. blocks
 * before the items/block-entities that reference them via {@code DeferredBlock.get()}).
 */
public class DeferredRegisterTyped<T>
{
    protected final ResourceKey<? extends Registry<T>> registryKey;
    protected final Registry<T> resolvedRegistry;
    protected final String modid;
    protected final List<DeferredHolder<T, ? extends T>> entries = new ArrayList<>();
    private boolean flushed = false;

    protected DeferredRegisterTyped(ResourceKey<? extends Registry<T>> registryKey, Registry<T> resolvedRegistry, String modid)
    {
        this.registryKey = registryKey;
        this.resolvedRegistry = resolvedRegistry;
        this.modid = modid;
    }

    public static <T> DeferredRegisterTyped<T> create(ResourceKey<? extends Registry<T>> registryKey, String modid)
    {
        return new DeferredRegisterTyped<>(registryKey, null, modid);
    }

    public static <T> DeferredRegisterTyped<T> create(Registry<T> registry, String modid)
    {
        return new DeferredRegisterTyped<>(registry.key(), registry, modid);
    }

    public static Items createItems(String modid)
    {
        return new Items(modid);
    }

    public static Blocks createBlocks(String modid)
    {
        return new Blocks(modid);
    }

    protected ResourceKey<T> keyFor(String name)
    {
        return ResourceKey.create(registryKey, new ResourceLocation(modid, name));
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> supplier)
    {
        DeferredHolder<T, I> holder = new DeferredHolder<>(keyFor(name), supplier);
        entries.add(holder);
        return holder;
    }

    @SuppressWarnings("unchecked")
    private Registry<T> resolveRegistry()
    {
        if (resolvedRegistry != null) return resolvedRegistry;
        Registry<?> found = BuiltInRegistries.REGISTRY.get(registryKey.location());
        if (found == null)
            throw new IllegalStateException("Unknown registry " + registryKey.location()
                    + " — if this is a Starcatcher custom registry, make sure it's created "
                    + "(FabricRegistryBuilder) before this DeferredRegisterTyped is flushed, see FABRIC_PORT_PLAN.md §5.1");
        return (Registry<T>) found;
    }

    public void register(IEventBus bus)
    {
        if (flushed) return;
        flushed = true;
        Registry<T> registry = resolveRegistry();
        for (DeferredHolder<T, ? extends T> entry : entries)
            entry.bind(registry);
    }

    public List<DeferredHolder<T, ? extends T>> getEntries()
    {
        return Collections.unmodifiableList(entries);
    }

    public static class Items extends DeferredRegisterTyped<Item>
    {
        private Items(String modid)
        {
            super(BuiltInRegistries.ITEM.key(), BuiltInRegistries.ITEM, modid);
        }

        @Override
        public <I extends Item> DeferredItem<I> register(String name, Supplier<? extends I> supplier)
        {
            DeferredItem<I> holder = new DeferredItem<>(keyFor(name), supplier);
            entries.add(holder);
            return holder;
        }
    }

    public static class Blocks extends DeferredRegisterTyped<Block>
    {
        private Blocks(String modid)
        {
            super(BuiltInRegistries.BLOCK.key(), BuiltInRegistries.BLOCK, modid);
        }

        @Override
        public <I extends Block> DeferredBlock<I> register(String name, Supplier<? extends I> supplier)
        {
            DeferredBlock<I> holder = new DeferredBlock<>(keyFor(name), supplier);
            entries.add(holder);
            return holder;
        }
    }
}
