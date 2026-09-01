package com.wdiscute.starcatcher.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.wdiscute.starcatcher.Starcatcher;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.Map;

/**
 * Reinstates the original Forge behavior (ConditionalOps + a `forge:mod_loaded` condition) for
 * Starcatcher's own {@link Starcatcher#FISH_REGISTRY_KEY} dynamic registry: cross-mod fish compat
 * entries (data/&lt;othermod&gt;/starcatcher/fish/*.json, ~310 of them) reference items from
 * fishing/aquatic mods this project doesn't depend on, so on a machine without that mod their
 * `item`/`entity` fields can't resolve and {@code RegistryDataLoader.loadRegistryContents} records
 * a per-file parse failure. Vanilla's own `RegistryDataLoader.load` treats ANY registry parse
 * failure as fatal for the whole game (by design, for its own worldgen registries where that's the
 * right call) — Fabric has no equivalent to Forge's conditional-loading system for this
 * (fabric-resource-conditions-api-v1 only patches JsonDataLoader-family loaders, not
 * RegistryDataLoader). So without this, the client crashes to an error screen the moment registries
 * are loaded (e.g. opening Singleplayer), for every user, regardless of which compat mods they have.
 * <p>
 * This strips only FISH_REGISTRY_KEY's own failures from the error map right before vanilla's
 * "any errors = throw" check, so an unresolvable compat entry is silently dropped from the registry
 * (exactly the old ConditionalOps outcome) instead of aborting the whole registry load. Real vanilla
 * registries (biomes, worldgen, etc.) are untouched and stay just as strict as before.
 */
@Mixin(RegistryDataLoader.class)
public class RegistryDataLoaderMixin
{
    @Inject(method = "load", at = @At(value = "INVOKE", target = "Ljava/util/Map;isEmpty()Z"))
    private static void starcatcher$tolerateMissingCompatFish(net.minecraft.server.packs.resources.ResourceManager resourceManager, net.minecraft.core.RegistryAccess registryAccess, java.util.List<RegistryDataLoader.RegistryData<?>> registryData, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<net.minecraft.core.RegistryAccess.Frozen> cir, @Local Map<ResourceKey<?>, Exception> errors)
    {
        errors.entrySet().removeIf(entry -> entry.getKey().isFor(Starcatcher.FISH_REGISTRY_KEY));
    }
}
