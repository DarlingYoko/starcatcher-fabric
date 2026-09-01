package com.wdiscute.starcatcher;

import com.wdiscute.starcatcher.Starcatcher;
import com.wdiscute.starcatcher.event.SCClientEvents;
import com.wdiscute.starcatcher.event.SCClientForgeEvents;
import com.wdiscute.starcatcher.event.TooltipEvents;
import com.wdiscute.starcatcher.registry.SCItemProperties;
import com.wdiscute.starcatcher.registry.SCRenderTypes;
import net.fabricmc.api.ClientModInitializer;

public class StarcatcherFabricClient implements ClientModInitializer
{
    @Override
    public void onInitializeClient()
    {
        // Renderers, layers, screens, particles, keymaps, item properties, color handlers,
        // tooltip processors — see FABRIC_PORT_PLAN.md §4/§6 (P5).
        Starcatcher.Client.init();
        SCClientEvents.registerRenderers();
        SCClientEvents.registerLayers();
        SCClientEvents.registerParticleFactories();
        SCClientEvents.registerScreens();
        SCClientEvents.registerKeyMappings();
        SCClientEvents.registerTooltipComponents();
        SCClientEvents.registerHudLayers();

        SCItemProperties.addCustomItemProperties();
        SCRenderTypes.register();

        SCClientForgeEvents.register();
        TooltipEvents.register();
    }
}
