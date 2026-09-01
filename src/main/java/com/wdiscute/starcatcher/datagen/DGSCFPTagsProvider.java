package com.wdiscute.starcatcher.datagen;

import com.wdiscute.starcatcher.SCTags;
import com.wdiscute.starcatcher.Starcatcher;
import com.wdiscute.starcatcher.registry.FishProperties;
import com.wdiscute.starcatcher.registry.fishing.FishingPropertiesRegistry;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class DGSCFPTagsProvider extends FabricTagProvider<FishProperties>
{
    public DGSCFPTagsProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        super(output, Starcatcher.FISH_REGISTRY_KEY, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider)
    {
        FishingPropertiesRegistry.PROPERTIES.forEach(o ->
        {
            FishProperties fp = o.getSecond();
            ResourceLocation location = o.getFirst().location();
            if(fp.catchInfo().fishEntryType().equals(FishProperties.CatchInfo.FishEntryType.FISH))
            {
                if(fp.rarity().equals(FishProperties.Rarity.TRASH)) getOrCreateTagBuilder(SCTags.TRASH_FISHES_FP).addOptional(location);
                if(fp.rarity().equals(FishProperties.Rarity.COMMON)) getOrCreateTagBuilder(SCTags.COMMON_FISHES_FP).addOptional(location);
                if(fp.rarity().equals(FishProperties.Rarity.UNCOMMON)) getOrCreateTagBuilder(SCTags.UNCOMMON_FISHES_FP).addOptional(location);
                if(fp.rarity().equals(FishProperties.Rarity.RARE)) getOrCreateTagBuilder(SCTags.RARE_FISHES_FP).addOptional(location);
                if(fp.rarity().equals(FishProperties.Rarity.EPIC)) getOrCreateTagBuilder(SCTags.EPIC_FISHES_FP).addOptional(location);
                if(fp.rarity().equals(FishProperties.Rarity.LEGENDARY)) getOrCreateTagBuilder(SCTags.LEGENDARY_FISHES_FP).addOptional(location);
            }
        });
    }
}
