package net.nikdo53.neobackports.datamaps;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Backs {@link DataMapType} — see FABRIC_PORT_PLAN.md §5.6. One reload listener per distinct
 * registry (in this mod: {@code minecraft:item} and {@code starcatcher:fish}), registered lazily
 * the first time any {@code DataMapType} targeting that registry is {@code .build()}-ed.
 * <p>
 * Loads each registered type's exact expected file path (derived from its id, same convention
 * real NeoForge data maps use: {@code data/<id-namespace>/data_maps/<registry-folder>/<id-path>.json})
 * via {@code ResourceManager.getResourceStack(...)}, which already returns every loaded pack's
 * contribution to that exact path in priority order — so a third-party datapack can add to (or
 * remove from) this mod's own data maps by writing into the same path, without this mod needing
 * to scan directories itself.
 */
class DataMapRegistry
{
    private static final Logger LOGGER = LoggerFactory.getLogger("StarcatcherDataMaps");
    private static final Map<ResourceKey<? extends Registry<?>>, List<DataMapType<?, ?>>> BY_REGISTRY = new HashMap<>();
    private static final java.util.Set<ResourceKey<? extends Registry<?>>> LISTENERS_REGISTERED = new java.util.HashSet<>();

    static synchronized void register(DataMapType<?, ?> type)
    {
        BY_REGISTRY.computeIfAbsent(type.registryKey(), k -> new ArrayList<>()).add(type);

        if (LISTENERS_REGISTERED.add(type.registryKey()))
        {
            ResourceManagerHelper.get(PackType.SERVER_DATA)
                    .registerReloadListener(new Listener(type.registryKey()));
        }
    }

    private static String folderFor(ResourceLocation registryLoc)
    {
        return registryLoc.getNamespace().equals("minecraft")
                ? registryLoc.getPath()
                : registryLoc.getNamespace() + "/" + registryLoc.getPath();
    }

    private static class Listener extends SimplePreparableReloadListener<Map<DataMapType<?, ?>, Map<ResourceLocation, Object>>>
            implements IdentifiableResourceReloadListener
    {
        private final ResourceKey<? extends Registry<?>> registryKey;
        private final ResourceLocation fabricId;

        Listener(ResourceKey<? extends Registry<?>> registryKey)
        {
            this.registryKey = registryKey;
            this.fabricId = new ResourceLocation("starcatcher", "data_maps/" + folderFor(registryKey.location()));
        }

        @Override
        public ResourceLocation getFabricId()
        {
            return fabricId;
        }

        @Override
        protected Map<DataMapType<?, ?>, Map<ResourceLocation, Object>> prepare(ResourceManager resourceManager, ProfilerFiller profiler)
        {
            Map<DataMapType<?, ?>, Map<ResourceLocation, Object>> result = new HashMap<>();
            String folder = folderFor(registryKey.location());

            for (DataMapType<?, ?> type : BY_REGISTRY.getOrDefault(registryKey, List.of()))
            {
                Map<ResourceLocation, Object> values = new HashMap<>();
                ResourceLocation fileLoc = new ResourceLocation(type.id().getNamespace(),
                        "data_maps/" + folder + "/" + type.id().getPath() + ".json");

                for (Resource resource : resourceManager.getResourceStack(fileLoc))
                {
                    try (Reader reader = resource.openAsReader())
                    {
                        var json = GsonHelper.parse(reader);

                        if (json.has("values"))
                        {
                            for (var entry : GsonHelper.getAsJsonObject(json, "values").entrySet())
                            {
                                ResourceLocation entryId = new ResourceLocation(entry.getKey());
                                var valueJson = entry.getValue();
                                if (valueJson.isJsonObject() && valueJson.getAsJsonObject().has("value"))
                                    valueJson = valueJson.getAsJsonObject().get("value");

                                var finalValueJson = valueJson;
                                type.codec().parse(com.mojang.serialization.JsonOps.INSTANCE, finalValueJson)
                                        .resultOrPartial(error -> LOGGER.error("Couldn't parse data map entry {} in {}: {}", entryId, fileLoc, error))
                                        .ifPresent(v -> values.put(entryId, v));
                            }
                        }

                        if (json.has("remove"))
                        {
                            for (var el : GsonHelper.getAsJsonArray(json, "remove"))
                                values.remove(new ResourceLocation(el.getAsString()));
                        }
                    }
                    catch (IOException e)
                    {
                        LOGGER.error("Couldn't read data map {}", fileLoc, e);
                    }
                }

                result.put(type, values);
            }

            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void apply(Map<DataMapType<?, ?>, Map<ResourceLocation, Object>> loaded, ResourceManager resourceManager, ProfilerFiller profiler)
        {
            for (var entry : loaded.entrySet())
                ((DataMapType<?, Object>) entry.getKey()).reload(entry.getValue());
        }
    }
}
