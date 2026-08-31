package net.nikdo53.neobackports.io.components;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Fabric shim for NeoBackports' {@code ItemContainerContents} — see FABRIC_PORT_PLAN.md §5.2.
 * An immutable, fixed-order snapshot of item stacks (1.20.5+ concept, no vanilla 1.20.1
 * equivalent). Callers in this mod use it purely as their own custom item-carried inventory
 * (e.g. tackle box contents), not to interop with any vanilla system.
 */
public final class ItemContainerContents
{
    public static final ItemContainerContents EMPTY = new ItemContainerContents(List.of());

    public static final Codec<ItemContainerContents> CODEC =
            ItemStack.CODEC.listOf().xmap(ItemContainerContents::fromItems, c -> c.items);

    private final List<ItemStack> items;

    private ItemContainerContents(List<ItemStack> items)
    {
        this.items = items;
    }

    public static ItemContainerContents fromItems(List<ItemStack> items)
    {
        List<ItemStack> copy = new ArrayList<>(items.size());
        for (ItemStack stack : items)
            copy.add(stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
        return new ItemContainerContents(List.copyOf(copy));
    }

    public List<ItemStack> nonEmptyItems()
    {
        List<ItemStack> result = new ArrayList<>();
        for (ItemStack stack : items)
            if (!stack.isEmpty())
                result.add(stack);
        return result;
    }

    public void copyInto(List<ItemStack> into)
    {
        for (int i = 0; i < into.size(); i++)
            into.set(i, i < items.size() ? items.get(i).copy() : ItemStack.EMPTY);
    }
}
