package com.wdiscute.starcatcher.registry;

import com.wdiscute.starcatcher.blocks.SCBlocks;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.nikdo53.neobackports.eventbus.IEventBus;

/**
 * Forge's {@code IGlobalLootModifier} has no Fabric equivalent (see FABRIC_PORT_PLAN.md §7) —
 * reimplemented directly as a {@link LootTableEvents#MODIFY} listener instead of a datapack-JSON
 * -driven modifier registry. Replaces the old {@code DGSCLootModifiers}/{@code AddItemModifier}
 * pair; {@code eventBus} is unused, kept only so existing call sites don't need to change.
 */
public class SCLootModifiers
{
    public static void register(IEventBus eventBus)
    {
        LootTableEvents.MODIFY.register(SCLootModifiers::modifyLootTables);
    }

    private static void modifyLootTables(ResourceManager resourceManager, LootDataManager lootManager,
                                          ResourceLocation id, LootTable.Builder tableBuilder, LootTableSource source)
    {
        //thank you kaupen my goat
        if (id.equals(BuiltInLootTables.SHIPWRECK_MAP) && source.isBuiltin())
        {
            LootPool.Builder pool = LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1))
                    .when(LootItemRandomChanceCondition.randomChance(0.1f));

            for (var hat : SCBlocks.HATS.getEntries())
                pool.add(LootItem.lootTableItem(hat.get().asItem()));

            tableBuilder.withPool(pool);
        }
    }
}
