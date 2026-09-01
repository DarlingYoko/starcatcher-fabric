package com.wdiscute.starcatcher.mixin;

import com.wdiscute.starcatcher.registry.SCRenderTypes;
import com.wdiscute.starcatcher.registry.FishProperties;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemBlockRenderTypes.class)
public class RenderTypeHelperMixin {

    @Inject(method = "getRenderType(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/client/renderer/RenderType;", at = @At("HEAD"), cancellable = true)
    private static void getRenderType(ItemStack stack, boolean fabulous, CallbackInfoReturnable<RenderType> cir) {
        if (FishProperties.Rarity.isGolden(stack)) {
            cir.setReturnValue(SCRenderTypes.RENDER_TYPE_GOLD_ITEM);
        }
    }

}
