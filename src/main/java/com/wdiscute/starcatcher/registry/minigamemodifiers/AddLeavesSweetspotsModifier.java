package com.wdiscute.starcatcher.registry.minigamemodifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wdiscute.starcatcher.U;
import com.wdiscute.starcatcher.minigame.ActiveSweetSpot;
import com.wdiscute.starcatcher.minigame.FishingMinigameScreen;
import com.wdiscute.starcatcher.registry.FishProperties;
import net.minecraft.network.chat.Component;
import net.nikdo53.neobackports.registry.DeferredHolder;


import java.util.List;
import java.util.function.Supplier;

public class AddLeavesSweetspotsModifier extends AbstractMinigameModifier
{
    public float chancePerTick;

    public static final MapCodec<AddLeavesSweetspotsModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.FLOAT.optionalFieldOf("chance_per_tick", 0.025f).forGetter(o -> o.chancePerTick)
                    ).apply(instance, AddLeavesSweetspotsModifier::new));

    public AddLeavesSweetspotsModifier(float chancePerTick)
    {
        this.chancePerTick = chancePerTick;
    }

    @Override
    public void tick()
    {
        super.tick();

        if(U.r.nextFloat() < chancePerTick)
        {
            ActiveSweetSpot activeSweetSpot = new ActiveSweetSpot(instance, FishProperties.SweetSpot.LEAF);
            instance.addSweetSpot(activeSweetSpot);
        }
    }

    @Override
    public MapCodec<? extends AbstractMinigameModifier> codec()
    {
        return CODEC;
    }

    @Override
    public DeferredHolder<Supplier<AbstractMinigameModifier>, Supplier<AbstractMinigameModifier>> getRegistryHolder()
    {
        return SCMinigameModifiers.SPAWN_SWEET_SPOTS;
    }

    @Override
    public List<Component> getShiftDescription()
    {
        return List.of(Component.translatable("tooltip.modifier.starcatcher.add_leaves_spots.shift",
                Math.round(chancePerTick * 1000) / 10f));
    }

}
