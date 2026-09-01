package com.wdiscute.starcatcher.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

import static com.wdiscute.starcatcher.blocks.SCBlocks.*;

public class DGSCBlockLootTableProvider extends FabricBlockLootTableProvider
{
    protected DGSCBlockLootTableProvider(FabricDataOutput output)
    {
        super(output);
    }

    @Override
    public void generate()
    {
        HATS.getEntries().forEach(o -> dropSelf(o.get()));
        TACKLE_BOXES.getEntries().forEach(o -> add(o.get(), noDrop()));

        dropSelf(AQUARIUM.get());

        dropSelf(DISPLAY.get());

        dropSelf(TROPHY_COPPER.get());
        dropSelf(TROPHY_IRON.get());
        dropSelf(TROPHY_GOLD.get());
        dropSelf(TROPHY_EMERALD.get());
        dropSelf(TROPHY_DIAMOND.get());

        dropSelf(CLAM.get());
        dropSelf(CONCH.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks()
    {
        List<Block> list = new ArrayList<>();
        list.addAll(HATS.getEntries().stream().map(Holder::value).toList());
        list.addAll(TACKLE_BOXES.getEntries().stream().map(Holder::value).toList());

        list.add(TROPHY_COPPER.get());
        list.add(TROPHY_IRON.get());
        list.add(TROPHY_GOLD.get());
        list.add(TROPHY_EMERALD.get());
        list.add(TROPHY_DIAMOND.get());

        list.add(AQUARIUM.get());
        list.add(DISPLAY.get());

        list.add(CLAM.get());
        list.add(CONCH.get());
        return list::iterator;
    }
}
