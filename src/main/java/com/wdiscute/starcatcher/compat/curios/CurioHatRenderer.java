package com.wdiscute.starcatcher.compat.curios;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CurioHatRenderer implements TrinketRenderer {

    @Override
    public void render(ItemStack stack, SlotReference slotReference, EntityModel<? extends LivingEntity> entityModel,
                        PoseStack poseStack, MultiBufferSource bufferSource, int light, LivingEntity entity,
                        float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                        float netHeadYaw, float headPitch) {

        poseStack.pushPose();

        float yOffset = entity.isCrouching() ? 25 : 0F;
        poseStack.translate(0.0, yOffset, 0.0); //sneak offset is off by like the tiniest bit.

        poseStack.mulPose(Axis.YP.rotationDegrees(netHeadYaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(headPitch));

        poseStack.mulPose(Axis.XP.rotationDegrees(180f));
        poseStack.mulPose(Axis.YP.rotationDegrees(180f));

        poseStack.translate(0.0, 0.25, 0.0);
        poseStack.scale(0.62f, 0.62f, 0.62f);

        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.HEAD, light,
                OverlayTexture.NO_OVERLAY, poseStack, bufferSource, Minecraft.getInstance().level, 0
        );

        poseStack.popPose();
    }
}
