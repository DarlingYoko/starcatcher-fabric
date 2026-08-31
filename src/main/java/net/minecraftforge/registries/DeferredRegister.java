package net.minecraftforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.nikdo53.neobackports.registry.DeferredRegisterTyped;

/**
 * Fabric shim. Forge 47.x's own {@code DeferredRegister} and NeoBackports'
 * {@code DeferredRegisterTyped} have the same shape in this codebase's usage — see
 * FABRIC_PORT_PLAN.md §5.1. Kept as a separate class only so the original
 * {@code net.minecraftforge.registries.DeferredRegister} import path still resolves.
 */
public class DeferredRegister<T> extends DeferredRegisterTyped<T>
{
    protected DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, Registry<T> resolvedRegistry, String modid)
    {
        super(registryKey, resolvedRegistry, modid);
    }

    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> registryKey, String modid)
    {
        return new DeferredRegister<>(registryKey, null, modid);
    }

    public static <T> DeferredRegister<T> create(Registry<T> registry, String modid)
    {
        return new DeferredRegister<>(registry.key(), registry, modid);
    }
}
