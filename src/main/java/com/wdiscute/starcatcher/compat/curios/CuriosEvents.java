package com.wdiscute.starcatcher.compat.curios;

import com.wdiscute.starcatcher.blocks.SCBlocks;
import com.wdiscute.starcatcher.registry.SCItems;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;

public class CuriosEvents {

    public static void registerRenderers() {
        SCBlocks.HATS.getEntries().forEach(block ->
                TrinketRendererRegistry.registerRenderer(
                        SCItems.ITEMS.getEntries().stream()
                                .filter(i -> i.getId().getPath().equals(block.getId().getPath()))
                                .findFirst().orElseThrow().get(),
                        new CurioHatRenderer()
                )
        );
    }
}
