package com.wdiscute.starcatcher.event;

import com.wdiscute.sellingbin.event.SBevents;
import com.wdiscute.starcatcher.Starcatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Spawn placement (SpawnPlacementRegisterEvent), entity attributes (EntityAttributeCreationEvent),
 * custom registries (NewRegistryEvent), the fish dynamic registry (DataPackRegistryEvent.NewRegistry),
 * and data maps (RegisterDataMapTypesEvent) have all been moved out to plain-method registrars —
 * see FABRIC_PORT_PLAN.md §6/§5.1/§5.6/§5.7:
 * {@link com.wdiscute.starcatcher.registry.SCEntities#registerSpawnPlacements()}/
 * {@link com.wdiscute.starcatcher.registry.SCEntities#registerAttributes()},
 * {@link com.wdiscute.starcatcher.registry.SCCustomRegistries#register},
 * {@link com.wdiscute.starcatcher.registry.SCDynamicRegistries#register}, and
 * {@link com.wdiscute.starcatcher.registry.SCDataMaps#register()}, all wired into
 * {@code StarcatcherFabric.onInitialize()}.
 *
 * What's left here is still broken and out of scope: {@code addPackFinders} depends on the
 * {@code com.wdiscute.sellingbin} companion mod, which doesn't exist anywhere in this Fabric
 * port's source tree yet (§7bis.3/P8 — a separate downport task, not just a missing shim).
 */
@Mod.EventBusSubscriber(modid = Starcatcher.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SCModEvents {

    @SubscribeEvent
    public static void addPackFinders(AddPackFindersEvent event)
    {
        PackSource packSource = new SBevents.DefaultPackSource(){
            @Override
            public boolean shouldAddAutomatically() {
                return true;
            }
        };

        event.addPackFinders(
                Starcatcher.rl("built_in_datapacks/selling_bin_starcatcher_emeralds"),
                PackType.SERVER_DATA,
                Component.literal("Starcatcher - Emeralds"),
                packSource,
                false,
                Pack.Position.TOP
        );

        event.addPackFinders(
                Starcatcher.rl("built_in_datapacks/selling_bin_fishes"),
                PackType.SERVER_DATA,
                Component.literal("Selling Bin - Fishes"),
                packSource,
                false,
                Pack.Position.TOP
        );
    }

}
