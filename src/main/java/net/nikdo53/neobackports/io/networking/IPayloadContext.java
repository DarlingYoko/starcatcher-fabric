package net.nikdo53.neobackports.io.networking;

import net.minecraft.world.entity.player.Player;

/**
 * Fabric shim for NeoBackports' {@code IPayloadContext} — see FABRIC_PORT_PLAN.md §5.4.
 * Only {@code enqueueWork}/{@code player} are used by the mod's payload {@code handle} methods.
 */
public interface IPayloadContext
{
    void enqueueWork(Runnable work);

    Player player();
}
