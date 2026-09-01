package com.wdiscute.starcatcher.registry;

import com.wdiscute.starcatcher.Starcatcher;
import com.wdiscute.starcatcher.io.network.*;
import com.wdiscute.starcatcher.io.network.tournament.CBActiveTournamentUpdatePayload;
import com.wdiscute.starcatcher.io.network.tournament.CBClearTournamentPayload;
import com.wdiscute.starcatcher.io.network.tournament.SBStandTournamentNameChangePayload;
import net.nikdo53.neobackports.eventbus.IEventBus;
import net.nikdo53.neobackports.io.networking.PayloadRegistrar;

/**
 * Moved out of {@code SCModEvents.registerPayloads} (a {@code @SubscribeEvent} method on
 * {@code RegisterPayloadHandlersEvent}) into its own plain-method class — see
 * FABRIC_PORT_PLAN.md §5.4. {@code SCModEvents} itself still mixes in several other
 * unshimmed Forge event types (data maps, datapack registry, entity attributes, pack
 * finders — P4/§5.6/§5.7 scope) and is left broken for now; {@code bus} is the same no-op
 * {@code IEventBus} token used across the other {@code SCXxx.register(bus)} calls.
 */
public class SCNetworking
{
    public static void register(IEventBus bus)
    {
        PayloadRegistrar registrar = new PayloadRegistrar("1", Starcatcher.MOD_ID);

        registrar.playToClient(
                FishingStartedPayload.TYPE,
                FishingStartedPayload.STREAM_CODEC,
                FishingStartedPayload::handle
        );

        registrar.playToServer(
                FishingCompletedPayload.TYPE,
                FishingCompletedPayload.STREAM_CODEC,
                FishingCompletedPayload::handle
        );

        registrar.playToClient(
                FishCaughtPayload.TYPE,
                FishCaughtPayload.STREAM_CODEC,
                FishCaughtPayload::handle
        );

        registrar.playToServer(
                FPsSeenPayload.TYPE,
                FPsSeenPayload.STREAM_CODEC,
                FPsSeenPayload::handle
        );

        registrar.playToServer(
                SBStandTournamentNameChangePayload.TYPE,
                SBStandTournamentNameChangePayload.STREAM_CODEC,
                SBStandTournamentNameChangePayload::handle
        );

        registrar.playToClient(
                CBActiveTournamentUpdatePayload.TYPE,
                CBActiveTournamentUpdatePayload.STREAM_CODEC,
                CBActiveTournamentUpdatePayload::handle
        );

        registrar.playToClient(
                CBClearTournamentPayload.TYPE,
                CBClearTournamentPayload.STREAM_CODEC,
                CBClearTournamentPayload::handle
        );

        registrar.playToServer(
                SetMessagePayload.TYPE,
                SetMessagePayload.STREAM_CODEC,
                SetMessagePayload::handle
        );

        registrar.playToServer(
                SignGuidePayload.TYPE,
                SignGuidePayload.STREAM_CODEC,
                SignGuidePayload::handle
        );
    }
}
