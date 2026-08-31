package com.wdiscute.starcatcher;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StarcatcherFabric implements ModInitializer
{
    public static final String MOD_ID = "starcatcher";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize()
    {
        // Subsystem registration (items, blocks, data components, custom registries, events, …)
        // is wired in incrementally per FABRIC_PORT_PLAN.md phases P1-P4.
        LOGGER.info("Starcatcher (Fabric) loading");
    }
}
