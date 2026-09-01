package com.wdiscute.starcatcher.datagen;

import com.wdiscute.starcatcher.SCTags;
import com.wdiscute.starcatcher.U;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.concurrent.CompletableFuture;

/**
 * Was `BlockTagsProvider` (Forge convenience subclass, modid+ExistingFileHelper ctor, `addOptional`/
 * `addOptionalTag` sugar) — vanilla has no such subclass at all for biome tags with that sugar, so
 * this now extends Fabric's base `FabricTagProvider<Biome>` directly (there's no `BiomeTagProvider`
 * inner specialization the way there is for Item/Block), using `getOrCreateTagBuilder` for the same
 * optional-add semantics. `Tags.Biomes.IS_MUSHROOM/IS_SWAMP/IS_DESERT` (Forge conventional tags)
 * became Fabric's `ConventionalBiomeTags.MUSHROOM/SWAMP/DESERT` — same `c:` convention, different name.
 */
public class DGSCBiomeTagsProvider extends FabricTagProvider<Biome>
{
    public DGSCBiomeTagsProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        super(output, Registries.BIOME, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider)
    {
        getOrCreateTagBuilder(create(SCTags.IS_BEACH))
                .addOptional(BiomeTags.IS_BEACH.location())
        ;

        getOrCreateTagBuilder(create(SCTags.IS_WARPED_FOREST))
                .addOptional(Biomes.WARPED_FOREST.location())
        ;

        getOrCreateTagBuilder(create(SCTags.IS_SOUL_SAND_VALLEY))
                .addOptional(Biomes.SOUL_SAND_VALLEY.location())
        ;

        getOrCreateTagBuilder(create(SCTags.IS_BASALT_DELTAS))
                .addOptional(Biomes.BASALT_DELTAS.location())
        ;

        getOrCreateTagBuilder(create(SCTags.IS_CRIMSON_FOREST))
                .addOptional(Biomes.CRIMSON_FOREST.location())
        ;

        getOrCreateTagBuilder(create(SCTags.IS_BIRCH_FOREST))
                .addOptional(Biomes.BIRCH_FOREST.location())
                .addOptional(Biomes.OLD_GROWTH_BIRCH_FOREST.location())
        ;

        getOrCreateTagBuilder(create(SCTags.IS_CHERRY_GROVE))
                .addOptional(Biomes.CHERRY_GROVE.location());

        getOrCreateTagBuilder(create(SCTags.IS_COLD_LAKE))
                .addOptional(Biomes.SNOWY_TAIGA.location())
                .addOptional(Biomes.SNOWY_BEACH.location())
                .addOptional(Biomes.SNOWY_PLAINS.location())
                .addOptional(Biomes.SNOWY_SLOPES.location())
                .addOptional(Biomes.ICE_SPIKES.location())
                .addOptional(Biomes.FROZEN_PEAKS.location())
                .addOptional(Biomes.JAGGED_PEAKS.location())
        ;

        getOrCreateTagBuilder(create(SCTags.IS_COLD_OCEAN))
                .addOptional(Biomes.COLD_OCEAN.location())
                .addOptional(Biomes.DEEP_COLD_OCEAN.location())
                .addOptional(Biomes.FROZEN_OCEAN.location())
                .addOptional(Biomes.DEEP_FROZEN_OCEAN.location())
        ;

        getOrCreateTagBuilder(create(SCTags.IS_COLD_RIVER))
                .addOptional(Biomes.FROZEN_RIVER.location())
                .addOptional(Biomes.SNOWY_BEACH.location())
        ;

        getOrCreateTagBuilder(create(SCTags.IS_DARK_FOREST))
                .addOptional(Biomes.DARK_FOREST.location())
        ;

        getOrCreateTagBuilder(create(SCTags.IS_DEEP_OCEAN))
                .addOptional(Biomes.DEEP_COLD_OCEAN.location())
                .addOptional(Biomes.DEEP_FROZEN_OCEAN.location())
                .addOptional(Biomes.DEEP_LUKEWARM_OCEAN.location())
                .addOptional(Biomes.DEEP_OCEAN.location())
        ;

        getOrCreateTagBuilder(create(SCTags.IS_LUKEWARM_OCEAN))
                .addOptional(Biomes.LUKEWARM_OCEAN.location())
                .addOptional(Biomes.DEEP_LUKEWARM_OCEAN.location())
        ;

        getOrCreateTagBuilder(create(SCTags.IS_MUSHROOM_FIELDS))
                .addOptional(Biomes.MUSHROOM_FIELDS.location())
                .addOptionalTag(ConventionalBiomeTags.MUSHROOM)
        ;

        getOrCreateTagBuilder(create(SCTags.IS_NORMAL_OCEAN))
                .addOptional(Biomes.OCEAN.location())
                .addOptional(Biomes.DEEP_OCEAN.location())
        ;

        getOrCreateTagBuilder(create(SCTags.IS_OCEAN))
                .addOptionalTag(BiomeTags.IS_OCEAN)
                .addOptional(U.rl("tfc", "deep_ocean"))
                .addOptional(U.rl("tfc", "deep_ocean_trench"))
                .addOptional(U.rl("tfc", "ocean"))
                .addOptional(U.rl("tfc", "ocean_reef"))
        ;

        getOrCreateTagBuilder(create(SCTags.IS_RIVER))
                .addOptionalTag(BiomeTags.IS_RIVER)
                .addOptionalTag(U.rl("tfc", "river"))
        ;

        getOrCreateTagBuilder(create(SCTags.IS_SWAMP))
                .addOptional(Biomes.SWAMP.location())
                .addOptional(Biomes.MANGROVE_SWAMP.location())
                .addOptionalTag(ConventionalBiomeTags.SWAMP)
        ;

        getOrCreateTagBuilder(create(SCTags.IS_WARM_LAKE))
                .addOptionalTag(BiomeTags.IS_SAVANNA)
                .addOptionalTag(BiomeTags.HAS_DESERT_PYRAMID)
                .addOptionalTag(ConventionalBiomeTags.DESERT)
        ;

        getOrCreateTagBuilder(create(SCTags.IS_WARM_OCEAN))
                .addOptional(Biomes.WARM_OCEAN.location())
                .addOptional(Biomes.LUKEWARM_OCEAN.location())
                .addOptional(Biomes.DEEP_LUKEWARM_OCEAN.location())
        ;

        getOrCreateTagBuilder(create(SCTags.IS_WARM_RIVER))
                .addOptional(Biomes.RIVER.location())
        ;

    }

    private static TagKey<Biome> create(ResourceLocation rl)
    {
        return TagKey.create(Registries.BIOME, rl);
    }
}
