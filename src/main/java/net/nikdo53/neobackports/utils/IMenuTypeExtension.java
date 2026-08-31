package net.nikdo53.neobackports.utils;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.network.IContainerFactory;

/**
 * Fabric shim for NeoBackports' {@code IMenuTypeExtension} — see FABRIC_PORT_PLAN.md §5.8.
 * NeoForge's "menu type with extra data" concept maps directly onto Fabric API's
 * {@link ExtendedScreenHandlerType}, whose factory has the identical
 * {@code (windowId, Inventory, FriendlyByteBuf) -> T} shape as {@link IContainerFactory}.
 */
public class IMenuTypeExtension
{
    public static <T extends AbstractContainerMenu> MenuType<T> create(IContainerFactory<T> factory)
    {
        return new ExtendedScreenHandlerType<>(factory::create);
    }
}
