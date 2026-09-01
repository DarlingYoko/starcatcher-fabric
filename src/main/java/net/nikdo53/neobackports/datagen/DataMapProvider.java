package net.nikdo53.neobackports.datagen;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.nikdo53.neobackports.datamaps.DataMapType;
import net.nikdo53.neobackports.registry.DeferredHolder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Fabric shim for NeoForge's datagen-time {@code DataMapProvider} — see FABRIC_PORT_PLAN.md §9.
 * Emits the exact JSON shape (and file path convention: {@code data/<namespace>/data_maps/
 * <registry-folder>/<id-path>.json}) that {@link net.nikdo53.neobackports.datamaps.DataMapRegistry}
 * already loads at runtime. Deliberately narrower than real NeoForge's builder (no per-entry
 * network-sync flag — not meaningful here, see {@link DataMapType.Builder#synced}; no per-entry
 * conditions — dropped the same way {@code DGSCFishingPropertiesProvider} already dropped
 * {@code ModLoadedCondition} gating, see its own doc comment) but supports every entry-reference
 * form actually used by this mod's data maps: a {@link Holder}, a {@link TagKey} (resolved via the
 * registry lookup at generation time), a {@link ResourceKey}, a raw {@link ResourceLocation}, or a
 * {@link DeferredHolder} (resolved via its id, so it doesn't need to be bound/registered yet).
 */
public abstract class DataMapProvider implements DataProvider
{
    private final PackOutput packOutput;
    private final CompletableFuture<HolderLookup.Provider> lookupProvider;
    private final List<Builder<?, ?>> builders = new ArrayList<>();

    protected DataMapProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        this.packOutput = packOutput;
        this.lookupProvider = lookupProvider;
    }

    protected abstract void gather(HolderLookup.Provider provider);

    protected <T, R> Builder<T, R> builder(DataMapType<R, T> type)
    {
        Builder<T, R> builder = new Builder<>(type, packOutput);
        builders.add(builder);
        return builder;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output)
    {
        return lookupProvider.thenCompose(provider ->
        {
            for (Builder<?, ?> builder : builders) builder.provider = provider;
            gather(provider);

            List<CompletableFuture<?>> futures = new ArrayList<>();
            for (Builder<?, ?> builder : builders) futures.add(builder.write(output));
            return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public String getName()
    {
        return "Data Maps";
    }

    private static String folderFor(ResourceLocation registryLoc)
    {
        return registryLoc.getNamespace().equals("minecraft")
                ? registryLoc.getPath()
                : registryLoc.getNamespace() + "/" + registryLoc.getPath();
    }

    public static class Builder<T, R>
    {
        private final DataMapType<R, T> type;
        private final PackOutput packOutput;
        private final Map<ResourceLocation, T> values = new LinkedHashMap<>();
        private HolderLookup.Provider provider;

        private Builder(DataMapType<R, T> type, PackOutput packOutput)
        {
            this.type = type;
            this.packOutput = packOutput;
        }

        public Builder<T, R> add(Holder<R> holder, T value, boolean replace)
        {
            holder.unwrapKey().ifPresent(key -> values.put(key.location(), value));
            return this;
        }

        public Builder<T, R> add(DeferredHolder<R, ? extends R> holder, T value, boolean replace)
        {
            values.put(holder.getId(), value);
            return this;
        }

        public Builder<T, R> add(ResourceKey<R> key, T value, boolean replace)
        {
            values.put(key.location(), value);
            return this;
        }

        public Builder<T, R> add(ResourceLocation location, T value, boolean replace)
        {
            values.put(location, value);
            return this;
        }

        public Builder<T, R> add(TagKey<R> tag, T value, boolean replace)
        {
            provider.lookupOrThrow(type.registryKey()).get(tag).ifPresent(set ->
            {
                for (Holder<R> holder : set)
                    holder.unwrapKey().ifPresent(key -> values.put(key.location(), value));
            });
            return this;
        }

        private CompletableFuture<?> write(CachedOutput output)
        {
            JsonObject valuesJson = new JsonObject();
            for (Map.Entry<ResourceLocation, T> entry : values.entrySet())
            {
                type.codec().encodeStart(JsonOps.INSTANCE, entry.getValue())
                        .resultOrPartial(DataProvider.LOGGER::error)
                        .ifPresent(json -> valuesJson.add(entry.getKey().toString(), json));
            }

            JsonObject root = new JsonObject();
            root.add("values", valuesJson);

            String kind = "data_maps/" + folderFor(type.registryKey().location());
            java.nio.file.Path path = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, kind).json(type.id());

            return DataProvider.saveStable(output, root, path);
        }
    }
}
