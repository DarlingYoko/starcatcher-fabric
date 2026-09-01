package com.wdiscute.starcatcher.datagen;

import com.wdiscute.starcatcher.Starcatcher;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

/**
 * Was `DatapackBuiltinEntriesProvider` (Forge) — Fabric's dynamic-registry datagen equivalent is
 * `FabricDynamicRegistryProvider`; the actual `RegistrySetBuilder.add(FISH_REGISTRY_KEY, ...)` call
 * moved to `SCDataGenerator.buildRegistry(...)` (the `DataGeneratorEntrypoint` hook Fabric provides
 * for exactly this). See FABRIC_PORT_PLAN.md §9 (P6).
 *
 * Known gap, documented not silently dropped: the original's `registerConditions` gated every
 * cross-mod fish entry (aquaculture, tide, etc.) behind a `ModLoadedCondition` so datapacks generated
 * on a dev machine with every compat mod installed wouldn't reference missing items/fish at runtime
 * on a server without them. `FabricDynamicRegistryProvider` has no per-entry condition hook at all
 * (Fabric's condition system, `fabric-resource-conditions-api-v1`, isn't wired into the dynamic
 * registry codec path the way NeoForge's `ConditionalOps` is) — so that gating is lost for now.
 * Not a runtime crash risk (the registry codec still round-trips fine at load time either way), just
 * a loss of the "don't even emit compat entries for absent mods" datagen optimization.
 */
public class DGSCFishingPropertiesProvider extends FabricDynamicRegistryProvider
{
    public DGSCFishingPropertiesProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture)
    {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(HolderLookup.Provider registries, Entries entries)
    {
        entries.addAll(registries.lookupOrThrow(Starcatcher.FISH_REGISTRY_KEY));
    }

    @Override
    public String getName()
    {
        return "FishingProperties";
    }
}
