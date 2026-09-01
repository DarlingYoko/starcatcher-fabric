package com.wdiscute.starcatcher.datagen;

import com.wdiscute.starcatcher.SCTags;
import com.wdiscute.starcatcher.blocks.SCBlocks;
import com.wdiscute.starcatcher.registry.SCItems;
import com.wdiscute.starcatcher.registry.fishing.DGStarcatcherFishes;
import com.wdiscute.starcatcher.registry.fishing.FishingPropertiesRegistry;
import com.wdiscute.starcatcher.registry.FishProperties;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.nikdo53.neobackports.registry.DeferredBlock;

import java.util.concurrent.CompletableFuture;

import static com.wdiscute.starcatcher.registry.SCItems.*;
import static com.wdiscute.starcatcher.blocks.SCBlocks.*;

public class DGSCItemsTagsProvider extends FabricTagProvider.ItemTagProvider
{

    public DGSCItemsTagsProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                 FabricTagProvider.BlockTagProvider blockTags)
    {
        super(output, lookupProvider, blockTags);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider)
    {
        //fishes, cat_food, foods/raw_fish
        for (var item : BUCKETABLE_FISHES_REGISTRY.getEntries())
        {
            getOrCreateTagBuilder(ItemTags.FISHES).add(item.get());
            //   getOrCreateTagBuilder(ItemTags.CAT_FOOD).add(item.get());
            //   getOrCreateTagBuilder(Tags.Items.FOODS_RAW_FISH).add(item.get());
            getOrCreateTagBuilder(SCTags.BUCKETABLE_FISHES).add(item.get());
            getOrCreateTagBuilder(SCTags.STARCAUGHT_FISHES).add(item.get());
        }

        //todo figure out what to do with crabs/eels tags?

        //rarity tags
        FishingPropertiesRegistry.PROPERTIES.forEach(p ->
        {
            //return if not a fish or alwaysSpawnEntity
            FishProperties fp = p.getSecond();
            if (!fp.catchInfo().fishEntryType().equals(FishProperties.CatchInfo.FishEntryType.FISH)) return;
            if (fp.catchInfo().alwaysSpawnEntity()) return;

            getOrCreateTagBuilder(SCTags.FISHABLE)
                    .addOptional(fp.catchInfo().fish().unwrapKey().orElseThrow().location());

            switch (p.getSecond().rarity())
            {
                case TRASH -> getOrCreateTagBuilder(SCTags.TRASH).addOptional(fp.catchInfo().fish().unwrapKey().orElseThrow().location());
                case COMMON -> getOrCreateTagBuilder(SCTags.COMMON_FISHES).addOptional(fp.catchInfo().fish().unwrapKey().orElseThrow().location());
                case UNCOMMON -> getOrCreateTagBuilder(SCTags.UNCOMMON_FISHES).addOptional(fp.catchInfo().fish().unwrapKey().orElseThrow().location());
                case RARE -> getOrCreateTagBuilder(SCTags.RARE_FISHES).addOptional(fp.catchInfo().fish().unwrapKey().orElseThrow().location());
                case EPIC -> getOrCreateTagBuilder(SCTags.EPIC_FISHES).addOptional(fp.catchInfo().fish().unwrapKey().orElseThrow().location());
                case LEGENDARY -> getOrCreateTagBuilder(SCTags.LEGENDARY_FISHES).addOptional(fp.catchInfo().fish().unwrapKey().orElseThrow().location());
            }
        });

        for (FishProperties fp : DGStarcatcherFishes.STARCATCHER_FISHES)
        {
            switch (fp.rarity())
            {
                case COMMON -> getOrCreateTagBuilder(SCTags.COMMON_FISHES).add(fp.catchInfo().fish().value());
                case UNCOMMON -> getOrCreateTagBuilder(SCTags.UNCOMMON_FISHES).add(fp.catchInfo().fish().value());
                case RARE -> getOrCreateTagBuilder(SCTags.RARE_FISHES).add(fp.catchInfo().fish().value());
                case EPIC -> getOrCreateTagBuilder(SCTags.EPIC_FISHES).add(fp.catchInfo().fish().value());
                case LEGENDARY -> getOrCreateTagBuilder(SCTags.LEGENDARY_FISHES).add(fp.catchInfo().fish().value());
            }
        }

        //worms
        getOrCreateTagBuilder(SCTags.WORMS)
                .add(WORM.get())
                .add(ALMIGHTY_WORM.get())
                .add(SEEKING_WORM.get());

        //baits tag
        getOrCreateTagBuilder(SCTags.BAITS)
                .add(WORM.get())
                .add(ALMIGHTY_WORM.get())
                .add(SEEKING_WORM.get())
                .add(DEV_WORM.get())
                .add(GUNPOWDER_BAIT.get())
                .add(CHERRY_BAIT.get())
                .add(LUSH_BAIT.get())
                .add(SCULK_BAIT.get())
                .add(DRIPSTONE_BAIT.get())
                .add(MURKWATER_BAIT.get())
                .add(LEGENDARY_BAIT.get())
                .add(METEOROLOGICAL_BAIT.get())
                .add(Items.WITHER_SKELETON_SKULL)
                .add(Items.BUCKET)

                .addOptional(rl("fishofthieves", "earthworms"))
                .addOptional(rl("fishofthieves", "grubs"))
                .addOptional(rl("fishofthieves", "leeches"))

                .addOptional(rl("tfc", "food/bluegill"))
                .addOptional(rl("tfc", "food/cod"))
                .addOptional(rl("tfc", "food/salmon"))
                .addOptional(rl("tfc", "food/tropical_fish"))
        ;

        //templates tag
        TEMPLATES_REGISTRY.getEntries().forEach(o -> getOrCreateTagBuilder(SCTags.TEMPLATES).add(o.get()));

        //tackle skins
        getOrCreateTagBuilder(SCTags.TACKLE_SKINS)
                .add(PEARL_SMITHING_TEMPLATE.get())
                .add(KING_SMITHING_TEMPLATE.get())
                .add(COLORFUL_SMITHING_TEMPLATE.get())
                .add(CLEAR_SMITHING_TEMPLATE.get())
                .add(FROG_SMITHING_TEMPLATE.get())
                .add(PEARL_SMITHING_TEMPLATE.get())
        ;

        //Equipment tag
        RODS_REGISTRY.getEntries().forEach(o -> getOrCreateTagBuilder(SCTags.EQUIPMENTS).add(o.get()));
        //ModItems.HATS_REGISTRY.getEntries().stream().forEach(o -> getOrCreateTagBuilder(StarcatcherTags.EQUIPMENTS).add(o.get()));

        //gadgets
        getOrCreateTagBuilder(SCTags.GADGETS).add(FISH_RADAR.get());

        //hooks tag
        HOOKS_REGISTRY.getEntries().forEach(o -> getOrCreateTagBuilder(SCTags.HOOKS).add(o.get()));
        getOrCreateTagBuilder(SCTags.HOOKS).addOptional(rl("tide", "void_hook"));

        //bobbers tag
        BOBBERS_REGISTRY.getEntries().forEach(o -> getOrCreateTagBuilder(SCTags.BOBBERS).add(o.get()));

        //rods and tools/fishing_rod
        RODS_REGISTRY.getEntries().forEach(o -> getOrCreateTagBuilder(SCTags.RODS).add(o.get()));
        //  RODS_REGISTRY.getEntries().forEach(o -> getOrCreateTagBuilder(Tags.Items.TOOLS_FISHING_ROD).add(o.get()));

        getOrCreateTagBuilder(SCTags.AQUARIUM_INTERACTIONS)
                .add(Items.DIAMOND_PICKAXE)
                .add(Items.DIAMOND_SHOVEL)
                .add(Items.STONE)
                .add(Items.GRAVEL)
                .add(Items.SAND)
                .add(Items.RED_SAND)
                .add(Items.KELP)
                .add(Items.SEAGRASS)
                .add(Items.BUCKET)
                .add(AURORA.get())
                .add(CONCH.asItem())
                .add(CLAM.asItem())
        ;

        //hats
        HATS.getEntries().forEach(o -> getOrCreateTagBuilder(SCTags.HATS).add(((DeferredBlock<?>) o).asItem()));

        //equippable hats
        // getOrCreateTagBuilder(ItemTags.EQUIPPABLE_ENCHANTABLE)
        //         .addTag(SCTags.HATS);

        getOrCreateTagBuilder(SCTags.PLACEABLE_IN_DISPLAY)
                .addTag(SCTags.BUCKETABLE_FISHES)
                .add(GUIDE.get())
        ;

        getOrCreateTagBuilder(SCTags.PLACEABLE_IN_TACKLE_BOX)
                .addTag(SCTags.BAITS)
                .addTag(SCTags.HOOKS)
                .addTag(SCTags.BOBBERS)
                .addTag(ItemTags.FISHES)
                .addTag(SCTags.COMMON_FISHES)
                .addTag(SCTags.UNCOMMON_FISHES)
                .addTag(SCTags.RARE_FISHES)
                .addTag(SCTags.EPIC_FISHES)
                .addTag(SCTags.LEGENDARY_FISHES)
        ;

        getOrCreateTagBuilder(SCTags.PLACEABLE_IN_TACKLE_BOX_FISH_SLOT)
                .addTag(ItemTags.FISHES)
        ;


        //tackle boxes
        getOrCreateTagBuilder(SCTags.TACKLE_BOXES)
                .add(TACKLE_BOX.asItem())
                .add(TACKLE_BOX_BLACK.asItem())
                .add(TACKLE_BOX_BLUE.asItem())
                .add(TACKLE_BOX_LIGHT_BLUE.asItem())
                .add(TACKLE_BOX_ORANGE.asItem())
                .add(TACKLE_BOX_YELLOW.asItem())
                .add(TACKLE_BOX_RED.asItem())
                .add(TACKLE_BOX_BROWN.asItem())
                .add(TACKLE_BOX_CYAN.asItem())
                .add(TACKLE_BOX_GREEN.asItem())
                .add(TACKLE_BOX_LIME.asItem())
                .add(TACKLE_BOX_GRAY.asItem())
                .add(TACKLE_BOX_LIGHT_GRAY.asItem())
                .add(TACKLE_BOX_PINK.asItem())
                .add(TACKLE_BOX_MAGENTA.asItem())
                .add(TACKLE_BOX_PURPLE.asItem())
                .add(TACKLE_BOX_WHITE.asItem())
        ;

    }


    public static ResourceLocation rl(String ns, String path)
    {
        return new ResourceLocation(ns, path);
    }
}
