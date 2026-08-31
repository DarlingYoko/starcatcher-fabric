package net.nikdo53.neobackports.registry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class DeferredItem<T extends Item> extends DeferredHolder<Item, T>
{
    DeferredItem(ResourceKey<Item> key, Supplier<? extends T> factory)
    {
        super(key, factory);
    }
}
