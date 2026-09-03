package com.wdiscute.starcatcher.registry.catchmodifiers;

import com.wdiscute.starcatcher.U;
import com.wdiscute.starcatcher.io.FishCaughtCounter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class IncreaseGoldenChance extends AbstractCatchModifier
{
    private final float increase;

    public IncreaseGoldenChance(float increase)
    {
        this.increase = increase;
    }


    @Override
    public boolean shouldBeGolden()
    {
        return FishCaughtCounter.canCatchGolden(instance.fpToFish, (ServerPlayer) instance.player) && U.r.nextFloat() < increase;
    }

    @Override
    public List<Component> getShiftDescription()
    {
        String key = increase >= 0.5f
                ? "tooltip.modifier.starcatcher.add_50_golden_chance.shift"
                : "tooltip.modifier.starcatcher.add_5_golden_chance.shift";
        return List.of(Component.translatable(key, Math.round(increase * 100)));
    }
}
