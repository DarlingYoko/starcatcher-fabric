package com.wdiscute.starcatcher.datagen;

import com.wdiscute.starcatcher.Starcatcher;
import com.wdiscute.starcatcher.registry.fishing.FishingPropertiesRegistry;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.Collections;
import java.util.List;

/**
 * Formerly the Forge `@Mod.EventBusSubscriber` `DataGenerators.gatherData(GatherDataEvent)` —
 * see FABRIC_PORT_PLAN.md §9 (P6). `DGSCBiomeModifierProvider` was dropped entirely (its whole
 * body was already commented out — it registered zero entries under Forge either) and
 * `DGSCDataMapsProvider` is intentionally not wired in yet: it needs a `DataMapProvider` datagen
 * shim that doesn't exist, and its content depends on the still-unported `com.wdiscute.sellingbin`
 * companion mod (§7bis.3/P8) for currencies/sellable values.
 */
public class SCDataGenerator implements DataGeneratorEntrypoint
{
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator)
    {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

        pack.addProvider(DGSCFishingPropertiesProvider::new);
        pack.addProvider(DGSCItemModelProvider::new);

        DGSCBlocksTagsProvider blockTags = pack.addProvider(DGSCBlocksTagsProvider::new);
        pack.addProvider((output, registriesFuture) -> new DGSCItemsTagsProvider(output, registriesFuture, blockTags));

        pack.addProvider(DGSCFPTagsProvider::new);
        pack.addProvider(DGSCAdvancementProvider::new);
        pack.addProvider(DGSCBiomeTagsProvider::new);

        pack.addProvider((output, registriesFuture) -> new LootTableProvider(output, Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(DGSCBlockLootTableProvider::new, LootContextParamSets.BLOCK))));

        pack.addProvider((output, registriesFuture) -> new DGSCRecipeProvider(output));
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder)
    {
        FishingPropertiesRegistry.register(); //register all entries before anything else
        registryBuilder.add(Starcatcher.FISH_REGISTRY_KEY, FishingPropertiesRegistry::bootstrap);
    }
}
