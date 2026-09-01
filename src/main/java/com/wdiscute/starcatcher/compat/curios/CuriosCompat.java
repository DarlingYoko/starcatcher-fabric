package com.wdiscute.starcatcher.compat.curios;

import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CuriosCompat
{
    public static List<ItemStack> getItems(Player player) {
        List<ItemStack> items = new ArrayList<>();

        TrinketsApi.getTrinketComponent(player).ifPresent(component ->
                component.getAllEquipped().forEach(tuple -> items.add(tuple.getB())));

        return items;
    }
}
