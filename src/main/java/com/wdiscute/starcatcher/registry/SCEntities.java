package com.wdiscute.starcatcher.registry;

import com.wdiscute.starcatcher.Starcatcher;
import com.wdiscute.starcatcher.secretnotes.BottledLetterEntity;
import com.wdiscute.starcatcher.secretnotes.BrokenBottleEntity;
import com.wdiscute.starcatcher.fishentity.FishEntity;
import com.wdiscute.starcatcher.bobberentity.FishingBobEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.nikdo53.neobackports.eventbus.IEventBus;
import net.nikdo53.neobackports.registry.DeferredHolder;
import net.nikdo53.neobackports.registry.DeferredRegisterTyped;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class SCEntities
{
    public static final DeferredRegisterTyped<EntityType<?>> ENTITY_TYPES =
            DeferredRegisterTyped.create(BuiltInRegistries.ENTITY_TYPE, Starcatcher.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<FishingBobEntity>> FISHING_BOB =
            register("fishing_bob", FishingBobEntity::new, MobCategory.MISC,
                    b -> b.noSummon().noSave().sized(0.3f, 0.3f));

    public static final Holder<EntityType<FishEntity>> FISH =
            register("fish", FishEntity::new, MobCategory.WATER_AMBIENT,
                    b -> b.sized(0.5f, 0.5f));

    public static final DeferredHolder<EntityType<?>,EntityType<BrokenBottleEntity>> BROKEN_BOTTLE =
            register("broken_bottle", BrokenBottleEntity::new, MobCategory.MISC,
                    b -> b.sized(0.25f, 0.25f)
                            .clientTrackingRange(4).updateInterval(10));

    public static final DeferredHolder<EntityType<?>,EntityType<BottledLetterEntity>> BOTTLED_LETTER =
            register("bottled_letter", BottledLetterEntity::new, MobCategory.MISC,
                    b -> b.sized(0.25f, 0.25f)
                            .clientTrackingRange(4).updateInterval(10));

    public static void register(IEventBus eventBus)
    {
        ENTITY_TYPES.register(eventBus);
    }

    /**
     * Replaces {@code SCModEvents.registerAttributed(EntityAttributeCreationEvent)} — see
     * FABRIC_PORT_PLAN.md §6. Must run after {@link #register(IEventBus)} (needs {@code FISH}
     * bound).
     */
    public static void registerAttributes()
    {
        FabricDefaultAttributeRegistry.register(FISH.value(), FishEntity.createAttributes());
    }

    /**
     * Replaces {@code SCModEvents.serverStarted(SpawnPlacementRegisterEvent)} — see
     * FABRIC_PORT_PLAN.md §6. Vanilla {@code SpawnPlacements.register} is private and Fabric API
     * has no equivalent helper (confirmed absent from every fabric-api module jar), so it's
     * widened directly via the access widener instead. Must run after {@link #register(IEventBus)}.
     */
    public static void registerSpawnPlacements()
    {
        SpawnPlacements.register(
                FISH.value(), SpawnPlacements.Type.IN_WATER,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                FishEntity::validSpawnPlacement);
    }

    static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String name, EntityType.EntityFactory<T> factory, MobCategory category, UnaryOperator<EntityType.Builder<T>> provider) {
        return ENTITY_TYPES.register(name, () -> provider.apply(EntityType.Builder.of(factory, category)).build(name));
    }

}
