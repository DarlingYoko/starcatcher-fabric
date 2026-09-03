package com.wdiscute.starcatcher.event;

import com.wdiscute.libtooltips.Tooltips;
import com.wdiscute.starcatcher.SCColors;
import com.wdiscute.starcatcher.SCConfig;
import com.wdiscute.starcatcher.Starcatcher;
import com.wdiscute.starcatcher.io.CaughtFishInfo;
import com.wdiscute.starcatcher.io.SCDataComponents;
import com.wdiscute.starcatcher.registry.FishProperties;
import com.wdiscute.starcatcher.registry.catchmodifiers.SCCatchModifiers;
import com.wdiscute.starcatcher.registry.minigamemodifiers.SCMinigameModifiers;
import com.wdiscute.starcatcher.registry.tackleskin.SCTackleSkins;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Formerly the `@Mod.EventBusSubscriber(Bus.FORGE, Dist.CLIENT)` `ItemTooltipEvent` listener —
 * see FABRIC_PORT_PLAN.md §6 (P5). Fabric's `ItemTooltipCallback` doesn't expose the viewing
 * entity (unlike Forge's event), so registry lookups that used `event.getEntity().level()` now
 * go through `Minecraft.getInstance().player`/`.level` directly instead — the only real API-shape
 * difference; the caching/formatting logic itself is unchanged.
 */
public class TooltipEvents
{
    static int cachedTimer = 0;
    static ItemStack cachedItem = ItemStack.EMPTY;
    static List<Component> cachedComps = List.of();
    static boolean cachedShift = false;

    public static void register()
    {
        ItemTooltipCallback.EVENT.register(TooltipEvents::tooltipEvent);
    }

    public static void tooltipEvent(ItemStack stack, TooltipFlag flag, List<Component> tooltipLines)
    {
        List<Component> comp = new ArrayList<>();
        boolean hasShiftDown = Screen.hasShiftDown();

        //cache check
        if (stack == cachedItem && cachedTimer > 0 && hasShiftDown == cachedShift)
        {
            cachedTimer--;
            if (!tooltipLines.isEmpty())
                tooltipLines.addAll(1, cachedComps);
            else
                tooltipLines.addAll(cachedComps);
            return;
        }

        cachedTimer = 100;
        cachedShift = hasShiftDown;
        cachedItem = stack;

        //Netherite Upgrade
        if (SCDataComponents.has(stack, SCDataComponents.NETHERITE_UPGRADE))
        {
            if (Boolean.TRUE.equals(SCDataComponents.get(stack, SCDataComponents.NETHERITE_UPGRADE)))
            {
                cachedTimer = -1;
                comp.add(Tooltips.resolveTagsToComponentFromTranslationKey("tooltip.starcatcher.rod.netherite"));
            }
        }

        //tackle skin
        ResourceLocation rl = SCTackleSkins.getTackleSkin(stack);
        if (!rl.equals(SCTackleSkins.BASE_TACKLE_SKIN))
        {
            comp.add(Tooltips.resolveTagsToComponentFromTranslationKey("tooltip.starcatcher.tackle").withStyle(ChatFormatting.GRAY));
            String s = I18n.get("tooltip.tackle." + rl.toLanguageKey());
            if (!s.isEmpty())
                comp.add(Component.literal(" -").append(Component.literal(s))
                        .withStyle(Style.EMPTY.withColor(SCColors.TOOLTIP_GRAY)));
        }

        //modifiers
        Set<ResourceLocation> minigameModifiersRLs = SCMinigameModifiers.getMinigameModifiersRLs(stack);
        Set<ResourceLocation> catchModifiersRLs = SCCatchModifiers.getCatchModifiersRLs(stack);
        if (!minigameModifiersRLs.isEmpty() || !catchModifiersRLs.isEmpty() && !stack.is(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE))
        {
            List<Component> modComp = new ArrayList<>();

            //add minigame modifiers
            Player entity = Minecraft.getInstance().player;
            if (entity != null)
            {
                minigameModifiersRLs.forEach(o ->
                {
                    var supplier = entity.level().registryAccess().registryOrThrow(Starcatcher.MINIGAME_MODIFIERS).get(o);
                    if (supplier != null)
                    {
                        String s = I18n.get("tooltip.modifier." + o.toLanguageKey());
                        if (!s.isEmpty())
                            modComp.add(Component.literal(" -").append(Component.literal(s))
                                    .withStyle(Style.EMPTY.withColor(SCColors.TOOLTIP_GRAY)));

                        List<Component> shiftDesc = supplier.get().getShiftDescription();
                        if (!shiftDesc.isEmpty())
                        {
                            modComp.add(Component.literal("   ").append(Component.translatable(hasShiftDown ? "tooltip.starcatcher.hold_shift_active" : "tooltip.starcatcher.hold_shift"))
                                    .withStyle(Style.EMPTY.withColor(SCColors.TOOLTIP_GRAY)));

                            if (hasShiftDown)
                                shiftDesc.forEach(shiftLine ->
                                        modComp.add(Component.literal("   ").append(shiftLine)
                                                .withStyle(Style.EMPTY.withColor(SCColors.TOOLTIP_GRAY))));
                        }
                    }
                });

                //add catch modifiers
                catchModifiersRLs.forEach(o ->
                {
                    var supplier = entity.level().registryAccess().registryOrThrow(Starcatcher.CATCH_MODIFIERS).get(o);
                    if (supplier != null)
                    {
                        String s = I18n.get("tooltip.modifier." + o.toLanguageKey());
                        if (!s.isEmpty())
                            modComp.add(Component.literal(" -").append(Component.literal(s))
                                    .withStyle(Style.EMPTY.withColor(SCColors.TOOLTIP_GRAY)));

                        List<Component> shiftDesc = supplier.get().getShiftDescription();
                        if (!shiftDesc.isEmpty())
                        {
                            modComp.add(Component.literal("   ").append(Component.translatable(hasShiftDown ? "tooltip.starcatcher.hold_shift_active" : "tooltip.starcatcher.hold_shift"))
                                    .withStyle(Style.EMPTY.withColor(SCColors.TOOLTIP_GRAY)));

                            if (hasShiftDown)
                                shiftDesc.forEach(shiftLine ->
                                        modComp.add(Component.literal("   ").append(shiftLine)
                                                .withStyle(Style.EMPTY.withColor(SCColors.TOOLTIP_GRAY))));
                        }
                    }
                });

                if (!modComp.isEmpty())
                    comp.add(Tooltips.resolveTagsToComponentFromTranslationKey("tooltip.starcatcher.modifiers").withStyle(ChatFormatting.GRAY));

                comp.addAll(modComp);
            }
        }

        //caught fish info
        if (SCDataComponents.has(stack, SCDataComponents.CAUGHT_FISH_INFO))
        {
            FishProperties.SizeAndWeight.Units units = SCConfig.UNIT.get();
            CaughtFishInfo sw = SCDataComponents.get(stack, SCDataComponents.CAUGHT_FISH_INFO);

            if (sw.golden())
            {
                MutableComponent element = Component.empty().append(Tooltips.resolveTagsToComponentFromTranslationKey("gui.guide.rarity.golden")).withStyle(Style.EMPTY.withColor(0x888888));
                if (hasShiftDown)
                    element.append(Component.literal(" (top 0%)").withStyle(Style.EMPTY.withColor(0x707070)));
                comp.add(element);
            }
            else
            {
                String size = units.getSizeAsString(sw.sizeInCentimeters());
                String weight = units.getWeightAsString(sw.weightInGrams());
                String percentile = " (top " + (int) sw.percentile() + "%)";

                MutableComponent element = Component.literal(size + " - " + weight).withStyle(Style.EMPTY.withColor(0x888888));
                if (hasShiftDown)
                    element.append(Component.literal(percentile).withStyle(Style.EMPTY.withColor(0x707070)));
                comp.add(element);
            }
        }

        cachedComps = comp;
        if (!tooltipLines.isEmpty())
            tooltipLines.addAll(1, comp);
        else
            tooltipLines.addAll(comp);
    }
}
