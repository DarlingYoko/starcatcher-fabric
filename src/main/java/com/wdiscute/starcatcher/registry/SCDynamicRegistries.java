package com.wdiscute.starcatcher.registry;

import com.wdiscute.starcatcher.Starcatcher;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.nikdo53.neobackports.eventbus.IEventBus;

/**
 * Replaces {@code SCModEvents.addDatapackRegistry} (a {@code DataPackRegistryEvent.NewRegistry}
 * handler) — see FABRIC_PORT_PLAN.md §5.7. Unlike {@link SCCustomRegistries}' plain programmatic
 * registries, {@code FISH_REGISTRY_KEY} is datapack-driven (JSON-defined fish, reloadable) and
 * synced to clients — Fabric's {@code DynamicRegistries.registerSynced} covers both in one call
 * (the original Forge call used the same {@code FishProperties.CODEC} for both the data and
 * network codec slots, so the single-codec overload applies here too). Every real call site
 * already reaches this registry through vanilla {@code RegistryAccess.registryOrThrow(...)}, so
 * nothing downstream needed to change once this is registered early enough at init.
 */
public class SCDynamicRegistries
{
    public static void register(IEventBus bus)
    {
        DynamicRegistries.registerSynced(Starcatcher.FISH_REGISTRY_KEY, FishProperties.CODEC);
    }
}
