package com.wdiscute.starcatcher;

import com.wdiscute.starcatcher.blocks.SCBlockEntities;
import com.wdiscute.starcatcher.blocks.SCBlocks;
import com.wdiscute.starcatcher.event.SCEvents;
import com.wdiscute.starcatcher.io.SCDataComponents;
import com.wdiscute.starcatcher.registry.SCCommands;
import com.wdiscute.starcatcher.registry.SCCreativeModeTabs;
import com.wdiscute.starcatcher.registry.SCCriterionTriggers;
import com.wdiscute.starcatcher.registry.SCCustomRegistries;
import com.wdiscute.starcatcher.registry.SCDataMaps;
import com.wdiscute.starcatcher.registry.SCDynamicRegistries;
import com.wdiscute.starcatcher.registry.SCEntities;
import com.wdiscute.starcatcher.registry.SCItems;
import com.wdiscute.starcatcher.registry.SCLootModifiers;
import com.wdiscute.starcatcher.registry.SCMenuTypes;
import com.wdiscute.starcatcher.registry.SCNetworking;
import com.wdiscute.starcatcher.registry.SCParticles;
import com.wdiscute.starcatcher.registry.SCRecipes;
import com.wdiscute.starcatcher.registry.SCSounds;
import com.wdiscute.starcatcher.registry.catchmodifiers.SCCatchModifiers;
import com.wdiscute.starcatcher.registry.fishrestrictions.SCFishRestrictions;
import com.wdiscute.starcatcher.registry.minigamemodifiers.SCMinigameModifiers;
import com.wdiscute.starcatcher.registry.sweetspotbehaviour.SCSweetSpotsBehaviour;
import com.wdiscute.starcatcher.registry.tackleskin.SCTackleSkins;
import net.fabricmc.api.ModInitializer;
import net.nikdo53.neobackports.eventbus.IEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StarcatcherFabric implements ModInitializer
{
    public static final String MOD_ID = "starcatcher";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize()
    {
        IEventBus bus = IEventBus.INSTANCE;

        // Must run before vanilla's BuiltInRegistries.bootStrap() freezes COMMAND_ARGUMENT_TYPE —
        // see SCCommands.registerArgumentTypes()'s own doc comment.
        SCCommands.registerArgumentTypes();

        // Dependency order matters: DeferredRegisterTyped.register(bus) flushes synchronously
        // (see FABRIC_PORT_PLAN.md §5.1), and several of these reference the previous ones'
        // DeferredHolder.get() inside their own registration suppliers (e.g. block items need
        // the block already bound, block entities need their block already bound).
        SCDataComponents.register(bus);
        SCBlocks.register(bus);
        SCItems.register(bus);
        SCBlockEntities.register(bus);
        SCEntities.register(bus);
        SCEntities.registerAttributes();
        SCEntities.registerSpawnPlacements();
        SCSounds.register(bus);
        SCParticles.register(bus);
        SCMenuTypes.register(bus);
        SCCreativeModeTabs.register(bus);
        SCCriterionTriggers.register(bus);
        SCRecipes.register(bus);
        SCLootModifiers.register(bus);
        SCNetworking.register(bus);

        // Custom registries (§5.1) must exist (FabricRegistryBuilder) before the
        // DeferredRegisterTyped-backed registries below try to resolve them.
        SCCustomRegistries.register(bus);
        SCFishRestrictions.register(bus);
        SCMinigameModifiers.register(bus);
        SCSweetSpotsBehaviour.register(bus);
        SCCatchModifiers.register(bus);
        SCTackleSkins.register(bus);

        // Dynamic (datapack-driven) fish registry, §5.7.
        SCDynamicRegistries.register(bus);

        // Data maps (§5.6) — reload listeners registered as each DataMapType is built.
        SCDataMaps.register();

        // Server-side events, §6.
        SCEvents.register();

        // Config (Forge Config API Port, D3) — SCConfig.SPEC/SPEC_SERVER built at class-load time.
        SCConfig.register();

        // Attachments (§5.5) register themselves via the cardinal-components-entity
        // entrypoint (SCEntityComponents), not through this bus.
        // Remaining subsystems (selling-bin processors) come in P4+ as their shim layers land.
        LOGGER.info("Starcatcher (Fabric) loading");
    }
}
