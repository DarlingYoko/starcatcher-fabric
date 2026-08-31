package net.nikdo53.neobackports.registry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class DeferredBlock<T extends Block> extends DeferredHolder<Block, T>
{
    DeferredBlock(ResourceKey<Block> key, Supplier<? extends T> factory)
    {
        super(key, factory);
    }
}
