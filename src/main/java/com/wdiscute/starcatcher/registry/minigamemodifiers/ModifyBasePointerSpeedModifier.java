package com.wdiscute.starcatcher.registry.minigamemodifiers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wdiscute.starcatcher.minigame.FishingMinigameScreen;
import net.minecraft.network.chat.Component;
import net.nikdo53.neobackports.registry.DeferredHolder;

import java.util.List;
import java.util.function.Supplier;

public class ModifyBasePointerSpeedModifier extends AbstractMinigameModifier
{
    public float baseSpeedRatio;

    public static final MapCodec<ModifyBasePointerSpeedModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.FLOAT.optionalFieldOf("base_speed_ratio", 0f).forGetter(o -> o.baseSpeedRatio)
                    ).apply(instance, ModifyBasePointerSpeedModifier::new));

    public ModifyBasePointerSpeedModifier(float baseSpeed)
    {
        this.baseSpeedRatio = baseSpeed;
    }

    @Override
    public void onAdd(FishingMinigameScreen instance)
    {
        super.onAdd(instance);
        instance.pointerBaseSpeed = instance.pointerBaseSpeed * baseSpeedRatio;
        instance.pointerSpeed = instance.pointerBaseSpeed;
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
        String key = baseSpeedRatio >= 1.4f
                ? "tooltip.modifier.starcatcher.faster_handle_speed.shift"
                : baseSpeedRatio >= 1f
                ? "tooltip.modifier.starcatcher.slightly_faster_handle_speed.shift"
                : baseSpeedRatio >= 0.6f
                ? "tooltip.modifier.starcatcher.slightly_slower_handle_speed.shift"
                : "tooltip.modifier.starcatcher.slower_handle_speed.shift";
        return List.of(Component.translatable(key, Math.round(baseSpeedRatio * 100)));
    }

}
