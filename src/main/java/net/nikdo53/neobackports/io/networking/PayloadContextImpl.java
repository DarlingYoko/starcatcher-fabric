package net.nikdo53.neobackports.io.networking;

import net.minecraft.world.entity.player.Player;

import java.util.concurrent.Executor;

class PayloadContextImpl implements IPayloadContext
{
    private final Player player;
    private final Executor executor;

    PayloadContextImpl(Player player, Executor executor)
    {
        this.player = player;
        this.executor = executor;
    }

    @Override
    public void enqueueWork(Runnable work)
    {
        executor.execute(work);
    }

    @Override
    public Player player()
    {
        return player;
    }
}
