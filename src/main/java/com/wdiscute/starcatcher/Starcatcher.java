package com.wdiscute.starcatcher;

import com.mojang.logging.LogUtils;
import com.wdiscute.libtooltips.Tooltips;
import com.wdiscute.starcatcher.registry.FishProperties.SizeAndWeight.Units;
import com.wdiscute.starcatcher.registry.fishrestrictions.AbstractFishRestriction;
import com.wdiscute.starcatcher.registry.tackleskin.AbstractTackleSkin;
import com.wdiscute.starcatcher.registry.catchmodifiers.AbstractCatchModifier;
import com.wdiscute.starcatcher.guide.FishCaughtToast;
import com.wdiscute.starcatcher.registry.minigamemodifiers.AbstractMinigameModifier;
import com.wdiscute.starcatcher.registry.sweetspotbehaviour.AbstractSweetSpotBehaviour;
import com.wdiscute.starcatcher.registry.FishProperties;
import com.wdiscute.starcatcher.tooltips.SCLegendary;
import com.wdiscute.starcatcher.tooltips.SCTooltipGradient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;

import java.util.function.Supplier;

public class Starcatcher
{
    public static final String MOD_ID = "starcatcher";
    public static final Logger LOGGER = LogUtils.getLogger();

    //resource keys
    public static final ResourceKey<Registry<FishProperties>> FISH_REGISTRY_KEY =
            ResourceKey.createRegistryKey(Starcatcher.rl("fish"));

    public static final ResourceKey<Registry<AbstractFishRestriction>> FISH_RESTRICTIONS =
            ResourceKey.createRegistryKey(Starcatcher.rl("fish_restrictions"));

    public static final ResourceKey<Registry<Supplier<AbstractMinigameModifier>>> MINIGAME_MODIFIERS =
            ResourceKey.createRegistryKey(Starcatcher.rl("minigame_modifiers"));

    public static final ResourceKey<Registry<Supplier<? extends AbstractSweetSpotBehaviour>>> SWEET_SPOT_BEHAVIOUR =
            ResourceKey.createRegistryKey(Starcatcher.rl("sweet_spot_behaviour"));

    public static final ResourceKey<Registry<Supplier<AbstractCatchModifier>>> CATCH_MODIFIERS =
            ResourceKey.createRegistryKey(Starcatcher.rl("catch_modifiers"));

    public static final ResourceKey<Registry<Supplier<AbstractTackleSkin>>> TACKLE_SKIN =
            ResourceKey.createRegistryKey(Starcatcher.rl("bobber_skin"));

    //registry
    public static Registry<AbstractFishRestriction> FISH_RESTRICTIONS_REGISTRY;

    public static Registry<Supplier<AbstractMinigameModifier>> MINIGAME_MODIFIERS_REGISTRY;

    public static Registry<Supplier<? extends AbstractSweetSpotBehaviour>> SWEET_SPOT_BEHAVIOUR_REGISTRY;

    public static Registry<Supplier<AbstractCatchModifier>> CATCH_MODIFIERS_REGISTRY;

    public static Registry<Supplier<AbstractTackleSkin>> TACKLE_SKIN_REGISTRY;

    public static ResourceLocation rl(String s)
    {
        return new ResourceLocation(Starcatcher.MOD_ID, s);
    }

    //shitty fix for double toast because its caused by nikdos payload sender thingy
    static Holder<Item> lastToast = null;

    @Environment(EnvType.CLIENT)
    public static void fishCaughtToast(FishProperties fp, boolean newFish, int sizeCM, int weightCM)
    {
        if (newFish && !fp.catchInfo().fish().equals(lastToast)) Minecraft.getInstance().getToasts().addToast(new FishCaughtToast(fp));
        lastToast = fp.catchInfo().fish();
        Units units = SCConfig.UNIT.get();

        String size = units.getSizeAsString(sizeCM);
        String weight = units.getWeightAsString(weightCM);

        Minecraft.getInstance().player.displayClientMessage(
                Component.literal("")
                        .append(Component.translatable(fp.catchInfo().fish().value().getDescriptionId()))
                        .append(Component.literal(" - " + size + " - " + weight))
                , true);

        Minecraft.getInstance().gui.overlayMessageTime = 180;
    }


    //Registration itself now happens in StarcatcherFabric/StarcatcherFabricClient (the real Fabric
    //entrypoints, see FABRIC_PORT_PLAN.md §4) — this class only keeps the fields/helpers other files
    //still read (registries, rl(), fishCaughtToast) and the client-only tooltip processor registration
    //below, called from StarcatcherFabricClient.onInitializeClient() now that libtooltips (§7bis.1) has
    //a real Fabric facade.
    public static class Client
    {
        @Environment(EnvType.CLIENT)
        public static void init()
        {
            //register tooltip tag processors
            Tooltips.registerProcessor("scgolden",
                    (t, s, e) -> SCTooltipGradient.process(t,
                            Triple.of(202, 93, 5),
                            Triple.of(230, 204, 9)
                    ));

            Tooltips.registerProcessor("sclegendary", SCLegendary::process);

            Tooltips.registerProcessor("scepic",
                    (t, s, e) -> SCTooltipGradient.process(t,
                            Triple.of(61, 0, 255),
                            Triple.of(255, 0, 224)
                    ));

            Tooltips.registerProcessor("scrare",
                    (t, s, e) -> SCTooltipGradient.process(t,
                            Triple.of(20, 40, 120),
                            Triple.of(100, 180, 255)
                    ));

            Tooltips.registerProcessor("scuncommon",
                    (t, s, e) -> SCTooltipGradient.process(t,
                            Triple.of(11, 185, 2),
                            Triple.of(2, 185, 69)
                    ));

            Tooltips.registerProcessor("sccommon",
                    (t, s, e) -> Component.literal(t));

            Tooltips.registerProcessor("sctrash",
                    (t, s, e) -> Component.literal(t));

            Tooltips.registerProcessor("sclava",
                    (t, s, e) -> SCTooltipGradient.process(t,
                            Triple.of(197, 11, 11),
                            Triple.of(197, 64, 11)
                    ));

            Tooltips.registerProcessor("scnone",
                    (t, s, e) -> Component.literal(t));
        }
    }

}
