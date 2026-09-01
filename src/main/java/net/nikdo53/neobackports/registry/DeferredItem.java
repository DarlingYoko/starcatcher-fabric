package net.nikdo53.neobackports.registry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.function.Supplier;

public class DeferredItem<T extends Item> extends DeferredHolder<Item, T> implements ItemLike
{
    DeferredItem(ResourceKey<Item> key, Supplier<? extends T> factory)
    {
        super(key, factory);
    }

    @Override
    public Item asItem()
    {
        return get();
    }

    public ItemStack toStack()
    {
        return new ItemStack(this);
    }
}
