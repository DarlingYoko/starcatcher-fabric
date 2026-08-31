package com.wdiscute.starcatcher;

import com.wdiscute.starcatcher.blocks.SCBlockEntities;
import com.wdiscute.starcatcher.blocks.SCBlocks;
import com.wdiscute.starcatcher.io.SCDataComponents;
import com.wdiscute.starcatcher.registry.SCCreativeModeTabs;
import com.wdiscute.starcatcher.registry.SCCriterionTriggers;
import com.wdiscute.starcatcher.registry.SCEntities;
import com.wdiscute.starcatcher.registry.SCItems;
import com.wdiscute.starcatcher.registry.SCLootModifiers;
import com.wdiscute.starcatcher.registry.SCMenuTypes;
import com.wdiscute.starcatcher.registry.SCNetworking;
import com.wdiscute.starcatcher.registry.SCParticles;
import com.wdiscute.starcatcher.registry.SCRecipes;
import com.wdiscute.starcatcher.registry.SCSounds;
import net.fabricmc.api.ModInitializer;
import net.minecraftforge.eventbus.api.IEventBus;
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

        // Dependency order matters: DeferredRegisterTyped.register(bus) flushes synchronously
        // (see FABRIC_PORT_PLAN.md §5.1), and several of these reference the previous ones'
        // DeferredHolder.get() inside their own registration suppliers (e.g. block items need
        // the block already bound, block entities need their block already bound).
        SCDataComponents.register(bus);
        SCBlocks.register(bus);
        SCItems.register(bus);
        SCBlockEntities.register(bus);
        SCEntities.register(bus);
        SCSounds.register(bus);
        SCParticles.register(bus);
        SCMenuTypes.register(bus);
        SCCreativeModeTabs.register(bus);
        SCCriterionTriggers.register(bus);
        SCRecipes.register(bus);
        SCLootModifiers.register(bus);
        SCNetworking.register(bus);

        // Attachments (§5.5) register themselves via the cardinal-components-entity
        // entrypoint (SCEntityComponents), not through this bus.
        // Remaining subsystems (custom registries, data maps, selling-bin processors)
        // come in P4+ as their shim layers land.
        LOGGER.info("Starcatcher (Fabric) loading");
    }
}
