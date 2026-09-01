package com.wdiscute.starcatcher.registry;

import com.wdiscute.starcatcher.Starcatcher;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.nikdo53.neobackports.eventbus.IEventBus;

/**
 * Replaces {@code SCModEvents.addRegistry} (a {@code NewRegistryEvent} handler) — see
 * FABRIC_PORT_PLAN.md §5.1's "custom registries" remainder. Must run before
 * {@code SCFishRestrictions}/{@code SCMinigameModifiers}/{@code SCSweetSpotsBehaviour}/
 * {@code SCCatchModifiers}/{@code SCTackleSkins}' own {@code .register(bus)} calls, since
 * {@code DeferredRegisterTyped.resolveRegistry()} looks the registry up from
 * {@code BuiltInRegistries.REGISTRY} by the key's location and throws if it isn't there yet —
 * {@code FabricRegistryBuilder.buildAndRegister()} is what actually puts it there.
 */
public class SCCustomRegistries
{
    public static void register(IEventBus bus)
    {
        Starcatcher.FISH_RESTRICTIONS_REGISTRY =
                FabricRegistryBuilder.createSimple(Starcatcher.FISH_RESTRICTIONS).buildAndRegister();

        Starcatcher.MINIGAME_MODIFIERS_REGISTRY =
                FabricRegistryBuilder.createSimple(Starcatcher.MINIGAME_MODIFIERS).buildAndRegister();

        Starcatcher.SWEET_SPOT_BEHAVIOUR_REGISTRY =
                FabricRegistryBuilder.createSimple(Starcatcher.SWEET_SPOT_BEHAVIOUR).buildAndRegister();

        Starcatcher.CATCH_MODIFIERS_REGISTRY =
                FabricRegistryBuilder.createSimple(Starcatcher.CATCH_MODIFIERS).buildAndRegister();

        Starcatcher.TACKLE_SKIN_REGISTRY =
                FabricRegistryBuilder.createSimple(Starcatcher.TACKLE_SKIN).buildAndRegister();
    }
}
