package com.wdiscute.starcatcher.compat.curios;

import com.wdiscute.starcatcher.registry.items.HatItem;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class CurioHatItem extends HatItem implements Trinket {
    public CurioHatItem(Block block, ResourceLocation... modifiers) {
        super(block, modifiers);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (TrinketItem.equipItem(player, stack))
            return InteractionResultHolder.consume(stack);
        return super.use(level, player, hand);
    }
}
