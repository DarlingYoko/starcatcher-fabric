package com.wdiscute.starcatcher.registry.catchmodifiers;

import net.minecraft.network.chat.Component;

import java.util.List;

public class AdjustLureTimeModifier extends AbstractCatchModifier
{

    final float minTicks;
    final float maxTicks;
    final float randomness;

    public AdjustLureTimeModifier(float minTicks, float maxTicks, float randomness)
    {
        this.minTicks = minTicks;
        this.maxTicks = maxTicks;
        this.randomness = randomness;
    }

    @Override
    public List<Component> getShiftDescription()
    {
        float averageMultiplier = (minTicks + maxTicks) / 2f;
        String key = averageMultiplier >= 1f
                ? "tooltip.modifier.starcatcher.increase_lure_time.shift"
                : averageMultiplier <= 0.6f
                ? "tooltip.modifier.starcatcher.big_decrease_lure_time.shift"
                : "tooltip.modifier.starcatcher.decrease_lure_time.shift";
        return List.of(Component.translatable(key, Math.round(averageMultiplier * 100)));
    }

    @Override
    public int adjustMinTicksToFish(int minTicksToFish)
    {
        return (int) (minTicksToFish * minTicks);
    }

    @Override
    public int adjustMaxTicksToFish(int maxTicksToFish)
    {
        return (int) (maxTicksToFish * maxTicks);
    }

    @Override
    public float adjustChanceToFishEachTick(float chanceToFishEachTick)
    {
        return chanceToFishEachTick * randomness;
    }
}
