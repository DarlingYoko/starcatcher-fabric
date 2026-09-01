package com.wdiscute.starcatcher.datagen;

import com.wdiscute.starcatcher.Starcatcher;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.world.item.Item;
import net.nikdo53.neobackports.registry.DeferredHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.nikdo53.neobackports.registry.DeferredItem;

import static com.wdiscute.starcatcher.registry.SCItems.*;
import static com.wdiscute.starcatcher.blocks.SCBlocks.*;

/**
 * Was Forge's `ItemModelProvider`/`ItemModelBuilder` (`withExistingParent(...).texture(...)`) — no
 * Fabric equivalent exists for that abstraction; vanilla's own datagen already covers this via
 * `ItemModelGenerators.generateFlatItem(Item, ModelTemplate)`, which produces the exact same
 * `item/generated` + `layer0` shape `simpleItem` used to hand-build. See FABRIC_PORT_PLAN.md §9 (P6).
 *
 * The block-item ("parent to the block's own model") half of the original file — every
 * `simpleBlockItem(...)` call — is intentionally NOT reproduced here. This project has no block-state
 * datagen provider at all (block states/models are hand-authored static resources), so those item
 * model JSONs were never really "generated" so much as written once and committed; they already exist
 * under `src/generated/resources`/`src/main/resources` and are loader-neutral static assets per §9/§11.
 */
public class DGSCItemModelProvider extends FabricModelProvider
{
    public DGSCItemModelProvider(FabricDataOutput output)
    {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator)
    {
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerators)
    {
        //bucket fishes
        for (DeferredHolder<Item, ? extends Item> item : BUCKETABLE_FISHES_REGISTRY.getEntries())
            simpleItem(itemModelGenerators, item);

        //non bucket fishes
        simpleItem(itemModelGenerators, BLACK_EEL);
        simpleItem(itemModelGenerators, GEODE_EEL);
        simpleItem(itemModelGenerators, OBSIDIAN_EEL);
        simpleItem(itemModelGenerators, MOLTEN_SHRIMP);
        simpleItem(itemModelGenerators, OBSIDIAN_CRAB);
        simpleItem(itemModelGenerators, SCORCHED_BLOODSUCKER);
        simpleItem(itemModelGenerators, MOLTEN_DEEPSLATE_CRAB);
        simpleItem(itemModelGenerators, LAVA_CRAB);
        simpleItem(itemModelGenerators, CINDER_SQUID);
        simpleItem(itemModelGenerators, CHORUS_CRAB);

        //trash
        simpleItem(itemModelGenerators, BOOT);
        simpleItem(itemModelGenerators, DRIED_SEAWEED);
        simpleItem(itemModelGenerators, LAVA_CRAB_CLAW);
        simpleItem(itemModelGenerators, MOSSY_BOOT);

        //items
        simpleItem(itemModelGenerators, MISSINGNO);
        simpleItem(itemModelGenerators, UNKNOWN_FISH);
        simpleItem(itemModelGenerators, GUIDE);
        simpleItem(itemModelGenerators, FISH_RADAR);
        simpleItem(itemModelGenerators, STARCATCHER_TWINE);
        simpleItem(itemModelGenerators, WATERLOGGED_SATCHEL);
        simpleItem(itemModelGenerators, FISH_BONES);
        simpleItem(itemModelGenerators, PEARL);
        simpleItem(itemModelGenerators, STARCAUGHT_BUCKET);
        simpleItem(itemModelGenerators, COOKED_STARCAUGHT_FISH);
        simpleItem(itemModelGenerators, SETTINGS);

        //notes & messages
        simpleItem(itemModelGenerators, LETTER);
        simpleItem(itemModelGenerators, BOTTLED_LETTER);

        simpleItem(itemModelGenerators, MESSAGE_IN_A_BOTTLE);
        simpleItem(itemModelGenerators, MESSAGE);

        simpleItem(itemModelGenerators, BROKEN_BOTTLE);

        simpleItem(itemModelGenerators, SECRET_NOTE);
        simpleItem(itemModelGenerators, DRIFTING_WATERLOGGED_BOTTLE);
        simpleItem(itemModelGenerators, SCALDING_BOTTLE);
        simpleItem(itemModelGenerators, BURNING_BOTTLE);
        simpleItem(itemModelGenerators, HOPEFUL_BOTTLE);
        simpleItem(itemModelGenerators, HOPELESS_BOTTLE);
        simpleItem(itemModelGenerators, TRUE_BLUE_BOTTLE);
        simpleItem(itemModelGenerators, WITHERED_BOTTLE);

        //hooks
        simpleItem(itemModelGenerators, HOOK);
        simpleItem(itemModelGenerators, AMETHYST_HOOK);
        simpleItem(itemModelGenerators, SHINY_HOOK);
        simpleItem(itemModelGenerators, GOLD_HOOK);
        simpleItem(itemModelGenerators, MOSSY_HOOK);
        simpleItem(itemModelGenerators, STONE_HOOK);
        simpleItem(itemModelGenerators, SPLIT_HOOK);
        simpleItem(itemModelGenerators, HEAVY_HOOK);
        simpleItem(itemModelGenerators, VANILLA_HOOK);
        simpleItem(itemModelGenerators, COPPER_HOOK);
        simpleItem(itemModelGenerators, EXPOSED_COPPER_HOOK);
        simpleItem(itemModelGenerators, WEATHERED_COPPER_HOOK);
        simpleItem(itemModelGenerators, OXIDISED_COPPER_HOOK);
        simpleItem(itemModelGenerators, FROZEN_HOOK);
        simpleItem(itemModelGenerators, ECHOING_HOOK);

        //bobbers
        simpleItem(itemModelGenerators, BOBBER);
        simpleItem(itemModelGenerators, STEADY_BOBBER);
        simpleItem(itemModelGenerators, CLEAR_BOBBER);
        simpleItem(itemModelGenerators, AQUA_BOBBER);
        simpleItem(itemModelGenerators, VANILLA_BOBBER);
        simpleItem(itemModelGenerators, LEAF_BOBBER);
        simpleItem(itemModelGenerators, SLIMEY_BOBBER);

        //baits
        simpleItem(itemModelGenerators, WORM);
        simpleItem(itemModelGenerators, ALMIGHTY_WORM);
        simpleItem(itemModelGenerators, SEEKING_WORM);
        simpleItem(itemModelGenerators, DEV_WORM);
        simpleItem(itemModelGenerators, GUNPOWDER_BAIT);
        simpleItem(itemModelGenerators, CHERRY_BAIT);
        simpleItem(itemModelGenerators, LUSH_BAIT);
        simpleItem(itemModelGenerators, SCULK_BAIT);
        simpleItem(itemModelGenerators, DRIPSTONE_BAIT);
        simpleItem(itemModelGenerators, MURKWATER_BAIT);
        simpleItem(itemModelGenerators, LEGENDARY_BAIT);
        simpleItem(itemModelGenerators, METEOROLOGICAL_BAIT);

        //templates
        TEMPLATES_REGISTRY.getEntries().forEach(item -> simpleItem(itemModelGenerators, item));

        //rods
        //custom model

        simpleItem(itemModelGenerators, BuiltInRegistries.ITEM.get(Starcatcher.rl("clam")));
        simpleItem(itemModelGenerators, BuiltInRegistries.ITEM.get(Starcatcher.rl("conch")));

        //trophies, hats, tackle boxes: block-item models parenting to the block's own model —
        //already exist as static resources, see class javadoc.
    }

    private void simpleItem(ItemModelGenerators itemModelGenerators, DeferredHolder<Item, ? extends Item> item)
    {
        itemModelGenerators.generateFlatItem(item.get(), ModelTemplates.FLAT_ITEM);
    }

    private void simpleItem(ItemModelGenerators itemModelGenerators, Item item)
    {
        itemModelGenerators.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
    }
}
