package com.wdiscute.starcatcher.event;

import com.wdiscute.starcatcher.registry.SCKeymappings;
import com.wdiscute.starcatcher.tournament.TournamentOverlay;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

/**
 * Formerly the `@Mod.EventBusSubscriber(Bus.FORGE, Dist.CLIENT)` raw-`InputEvent.Key` listener —
 * see FABRIC_PORT_PLAN.md §6 (P5). Fabric has no raw GLFW key-event bus; the idiomatic replacement
 * is polling the registered `KeyMapping` for a queued click once per client tick, which vanilla's
 * own input handling already tracks once the mapping is registered (SCClientEvents#registerKeyMappings).
 */
public class SCClientForgeEvents
{
    public static void register()
    {
        ClientTickEvents.END_CLIENT_TICK.register(client ->
        {
            while (SCKeymappings.EXPAND_TOURNAMENT.consumeClick())
            {
                TournamentOverlay.expandedType = TournamentOverlay.expandedType.next();
            }
        });
    }
}
