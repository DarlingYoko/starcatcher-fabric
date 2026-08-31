package com.wdiscute.starcatcher.registry;

import com.wdiscute.starcatcher.Starcatcher;
import com.wdiscute.starcatcher.advancement.MinigameCompletedTrigger;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.function.Supplier;

public interface SCCriterionTriggers
{
    Supplier<MinigameCompletedTrigger> MINIGAME_COMPLETED = MinigameCompletedTrigger::new;

    // Forge deferred this to a FMLCommonSetupEvent listener; Fabric's ModInitializer has no
    // such staging, so it's registered directly (see FABRIC_PORT_PLAN.md §6).
    static void register(IEventBus eventBus)
    {
        CriteriaTriggers.register(MINIGAME_COMPLETED.get());
    }
}
