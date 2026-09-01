package com.wdiscute.starcatcher.registry.items;

import com.wdiscute.starcatcher.fishentity.FishEntity;
import com.wdiscute.starcatcher.io.CaughtFishInfo;
import com.wdiscute.starcatcher.io.SCDataComponents;
import com.wdiscute.starcatcher.io.SingleStackContainer;
import com.wdiscute.starcatcher.registry.FishProperties;
import com.wdiscute.starcatcher.registry.SCEntities;
import com.wdiscute.starcatcher.registry.SCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;

import java.util.function.Supplier;

public class StarcaughtBucket extends BucketItem
{
    Holder<EntityType<FishEntity>> entity;

    public StarcaughtBucket(Fluid fluid)
    {
        super(fluid, new Item.Properties().stacksTo(16));

        entity = SCEntities.FISH;
    }

    @Override
    public void checkExtraContent(@Nullable Player player, Level level, ItemStack containerStack, BlockPos pos)
    {
        if (level instanceof ServerLevel)
        {
            this.spawn((ServerLevel) level, containerStack, pos);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, pos);
        }
    }

    private void spawn(ServerLevel serverLevel, ItemStack bucketedMobStack, BlockPos pos)
    {
        FishEntity fishEntity = this.entity.value().spawn(serverLevel, bucketedMobStack, null, pos, MobSpawnType.BUCKET, true, false);
        if (SCDataComponents.has(bucketedMobStack, SCDataComponents.BUCKETED_FISH))
            fishEntity.setFish(getFish(bucketedMobStack));
        else
            fishEntity.setFish(SCItems.AURORA.toStack());
    }

    private static ItemStack getFish(ItemStack bucket)
    {
        return SCDataComponents.getOrDefault(bucket, SCDataComponents.BUCKETED_FISH, new SingleStackContainer(ItemStack.EMPTY)).stack();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level context, List<Component> tooltipComponents, TooltipFlag tooltipFlag)
    {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        ItemStack fish = getFish(stack);
        if (fish.isEmpty())
        {
            tooltipComponents.add(1, Component.translatable("tooltip.starcatcher.starcaught_bucket.creative.1").withStyle(Style.EMPTY.withColor(0x888888)));
            tooltipComponents.add(1, Component.translatable("tooltip.starcatcher.starcaught_bucket.creative.0").withStyle(Style.EMPTY.withColor(0x888888)));
        }
    }

    @Override
    public Component getName(ItemStack stack)
    {
        SingleStackContainer ssc = SCDataComponents.get(stack, SCDataComponents.BUCKETED_FISH);

        if (ssc == null)
            return super.getName(stack);
        else
        {
            // getHoverName() already falls back custom name -> item's own display name, matching
            // the NeoForge CUSTOM_NAME/ITEM_NAME/description-id fallback chain this used to spell out.
            Component baseName = ssc.stack().getHoverName();

            CaughtFishInfo sw = SCDataComponents.get(ssc.stack(), SCDataComponents.CAUGHT_FISH_INFO);
            if (sw != null)
            {
                FishProperties.Rarity rarity = sw.golden() ? FishProperties.Rarity.GOLDEN : sw.rarity();
                return Component.translatable("tooltip.starcatcher.starcaught_bucket.name", rarity.wrapWithRarityMarkdown(baseName.getString()));
            }
            else
                return Component.translatable("tooltip.starcatcher.starcaught_bucket.name", baseName.getString());
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack)
    {
        return Optional.of(new BucketTooltip(getFish(stack)));
    }

    public record BucketTooltip(ItemStack fish) implements TooltipComponent
    {
    }
}
