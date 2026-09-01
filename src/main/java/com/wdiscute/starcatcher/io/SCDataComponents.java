package com.wdiscute.starcatcher.io;

import com.mojang.serialization.Codec;
import com.wdiscute.starcatcher.Starcatcher;
import com.wdiscute.starcatcher.io.attachments.FishingGuideAttachment;
import com.wdiscute.starcatcher.registry.SignedGuide;
import com.wdiscute.starcatcher.registry.catchmodifiers.SCCatchModifiers;
import com.wdiscute.starcatcher.registry.tackleskin.SCTackleSkins;
import com.wdiscute.starcatcher.secretnotes.LetterItem;
import com.wdiscute.starcatcher.secretnotes.SecretNote;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.nikdo53.neobackports.io.components.DataComponentType;
import net.nikdo53.neobackports.io.components.ItemContainerContents;
import net.nikdo53.neobackports.registry.DeferredHolder;
import net.nikdo53.neobackports.registry.DeferredRegisterTyped;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class SCDataComponents
{
    public static final DeferredRegisterTyped.DataComponents DATA_COMPONENT_TYPES =
            DeferredRegisterTyped.createDataComponents(Starcatcher.MOD_ID);

    //bucketed fish
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SingleStackContainer>> BUCKETED_FISH = register(
            "bucketed_fish",
            builder -> builder.persistent(SingleStackContainer.CODEC));

    //signed book system
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SignedGuide>> SIGNED_GUIDE = register(
            "signed_guide",
            builder -> builder.persistent(SignedGuide.CODEC));

    //rod menu
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SingleStackContainer>> BOBBER = register(
            "bobber",
            builder -> builder.persistent(SingleStackContainer.CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SingleStackContainer>> BAIT = register(
            "bait", builder -> builder.persistent(SingleStackContainer.CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SingleStackContainer>> HOOK = register(
            "hook", builder -> builder.persistent(SingleStackContainer.CODEC));

    //storing data on itemstack
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SecretNote.Note>> SECRET_NOTE = register(
            "secret_note", builder -> builder.persistent(SecretNote.Note.CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LetterItem.Message>> MESSAGE = register(
            "message", builder -> builder.persistent(LetterItem.Message.CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CaughtFishInfo>> CAUGHT_FISH_INFO = register(
            "caught_fish_info", builder -> builder.persistent(CaughtFishInfo.CODEC));


    //modifiers
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ResourceLocation>>> MINIGAME_MODIFIERS = register(
            "minigame_modifiers",
            builder -> builder.persistent(ResourceLocation.CODEC.listOf()));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ResourceLocation>>> CATCH_MODIFIERS = register(
            "catch_modifiers",
            builder -> builder.persistent(ResourceLocation.CODEC.listOf()));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceLocation>> TACKLE_SKIN = register(
            "tackle_skin",
            builder -> builder.persistent(ResourceLocation.CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> NETHERITE_UPGRADE = register(
            "netherite_upgraded",
            builder -> builder.persistent(Codec.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<ItemStack>>> TACKLE_BOX_FISHES = register(
            "tackle_box_fishes",
            builder -> builder.persistent(ItemStack.CODEC.listOf()));

    //tackle box contents (no vanilla equivalent in 1.20.1 — see FABRIC_PORT_PLAN.md §5.2)
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> CONTAINER = register(
            "container", builder -> builder.persistent(ItemContainerContents.CODEC));

    private static final String STORAGE_KEY = "StarcatcherComponents";

    public static <T> void set(ItemStack stack, Supplier<DataComponentType<T>> component, T data)
    {
        DataComponentType<T> type = component.get();
        CompoundTag components = stack.getOrCreateTagElement(STORAGE_KEY);
        components.put(type.id().toString(), type.codec().encodeStart(NbtOps.INSTANCE, data).getOrThrow(false, s -> {}));
    }

    @Nullable
    public static <T> T get(ItemStack stack, Supplier<DataComponentType<T>> component)
    {
        DataComponentType<T> type = component.get();
        CompoundTag components = stack.getTagElement(STORAGE_KEY);
        String key = type.id().toString();
        if (components != null && components.contains(key))
            return type.codec().parse(NbtOps.INSTANCE, components.get(key)).result().orElse(null);

        return getDataMapFallback(stack, component);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static <T> T getDataMapFallback(ItemStack stack, Supplier<DataComponentType<T>> component)
    {
        if (component == CATCH_MODIFIERS)
            return (T) com.wdiscute.starcatcher.registry.SCDataMaps.getOrDefault(stack, com.wdiscute.starcatcher.registry.SCDataMaps.CATCH_MODIFIERS, null);
        if (component == MINIGAME_MODIFIERS)
            return (T) com.wdiscute.starcatcher.registry.SCDataMaps.getOrDefault(stack, com.wdiscute.starcatcher.registry.SCDataMaps.MINIGAME_MODIFIERS, null);
        if (component == TACKLE_SKIN)
            return (T) com.wdiscute.starcatcher.registry.SCDataMaps.getOrDefault(stack, com.wdiscute.starcatcher.registry.SCDataMaps.TACKLE_SKIN, null);
        return null;
    }

    public static <T> boolean has(ItemStack stack, Supplier<DataComponentType<T>> component)
    {
        CompoundTag components = stack.getTagElement(STORAGE_KEY);
        return components != null && components.contains(component.get().id().toString());
    }

    public static <T> void remove(ItemStack stack, Supplier<DataComponentType<T>> component)
    {
        CompoundTag components = stack.getTagElement(STORAGE_KEY);
        if (components != null)
            components.remove(component.get().id().toString());
    }

    @Nonnull
    public static <T> T getOrDefault(ItemStack stack, Supplier<DataComponentType<T>> component, T defaultValue)
    {
        T value = get(stack, component);
        return value != null ? value : defaultValue;
    }

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name,
                                                                                           UnaryOperator<DataComponentType.Builder<T>> builderOperator)
    {
        return DATA_COMPONENT_TYPES.registerComponentType(name, builderOperator);
    }

    public static void register(IEventBus eventBus)
    {
        DATA_COMPONENT_TYPES.register(eventBus);
    }

}
