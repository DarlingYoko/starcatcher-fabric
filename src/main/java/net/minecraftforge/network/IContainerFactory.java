package net.minecraftforge.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Fabric shim for Forge's {@code IContainerFactory} — see FABRIC_PORT_PLAN.md §5.8.
 * Shape matches Fabric API's {@code ExtendedScreenHandlerType.ExtendedFactory} exactly,
 * so {@link net.nikdo53.neobackports.utils.IMenuTypeExtension} can delegate to it directly.
 */
@FunctionalInterface
public interface IContainerFactory<T extends AbstractContainerMenu>
{
    T create(int windowId, Inventory inv, FriendlyByteBuf data);
}
