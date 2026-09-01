package com.wdiscute.starcatcher.event;

import com.wdiscute.starcatcher.bobberentity.FishingBobRenderer;
import com.wdiscute.starcatcher.fishentity.FishRenderer;
import com.wdiscute.starcatcher.fishentity.fishmodels.*;
import com.wdiscute.starcatcher.fishspotter.FishRadarLayer;
import com.wdiscute.starcatcher.blocks.SCBlockEntities;
import com.wdiscute.starcatcher.blocks.SCBlocks;
import com.wdiscute.starcatcher.blocks.aquarium.AquariumRenderer;
import com.wdiscute.starcatcher.blocks.display.DisplayBlockRenderer;
import com.wdiscute.starcatcher.blocks.display.DisplayBookModel;
import com.wdiscute.starcatcher.blocks.tacklebox.TackleBoxRenderer;
import com.wdiscute.starcatcher.blocks.tacklebox.TackleBoxScreen;
import com.wdiscute.starcatcher.registry.items.BucketTooltipRenderer;
import com.wdiscute.starcatcher.registry.items.RodSlotTooltipRenderer;
import com.wdiscute.starcatcher.registry.items.StarcaughtBucket;
import com.wdiscute.starcatcher.particles.FishingBitingLavaParticles;
import com.wdiscute.starcatcher.particles.FishingBitingParticles;
import com.wdiscute.starcatcher.particles.FishingNotificationParticles;
import com.wdiscute.starcatcher.registry.*;
import com.wdiscute.starcatcher.compat.curios.CuriosEvents;
import com.wdiscute.starcatcher.registry.items.rod.StarcatcherFishingRodItem;
import com.wdiscute.starcatcher.registry.tackleskin.*;
import com.wdiscute.starcatcher.registry.items.rod.FishingRodScreen;
import com.wdiscute.starcatcher.tournament.StandScreen;
import com.wdiscute.starcatcher.tournament.TournamentOverlay;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

/**
 * Client-side rendering/registration, formerly the `@Mod.EventBusSubscriber(Bus.MOD, Dist.CLIENT)`
 * `SCClientEvents` — see FABRIC_PORT_PLAN.md §4/§6 (P5). Entity renderers go through Fabric API's
 * `EntityRendererRegistry` since vanilla `EntityRenderers.register` is private in 1.20.1.
 */
public class SCClientEvents
{
    public static void registerRenderers()
    {
        BlockEntityRendererRegistry.register(SCBlockEntities.DISPLAY.get(), DisplayBlockRenderer::new);
        BlockEntityRendererRegistry.register(SCBlockEntities.AQUARIUM.get(), AquariumRenderer::new);
        //BlockEntityRendererRegistry.register(SCBlockEntities.TACKLE_BOX.get(), TackleBoxRenderer::new);

        // aquarium.json declares a Forge/NeoForge-only top-level "render_type": "minecraft:translucent"
        // model key, which vanilla's (and Fabric's) own model parser silently ignores -- without
        // this, the block falls back to the default solid render type, so its glass/water quads
        // render fully opaque instead of alpha-blended (confirmed via runClient: solid black instead
        // of translucent glass/water).
        BlockRenderLayerMap.INSTANCE.putBlock(SCBlocks.AQUARIUM.get(), RenderType.translucent());

        EntityRendererRegistry.register(SCEntities.FISHING_BOB.get(), FishingBobRenderer::new);
        EntityRendererRegistry.register(SCEntities.BROKEN_BOTTLE.get(), ThrownItemRenderer::new);
        EntityRendererRegistry.register(SCEntities.BOTTLED_LETTER.get(), ThrownItemRenderer::new);
        EntityRendererRegistry.register(SCEntities.FISH.value(), FishRenderer::new);

        //Trinkets (curios' Fabric replacement, D5) is a hard dependency, always present
        CuriosEvents.registerRenderers();
    }

    public static void registerHudLayers()
    {
        FishRadarLayer fishRadarLayer = new FishRadarLayer();
        TournamentOverlay tournamentOverlay = new TournamentOverlay();
        HudRenderCallback.EVENT.register(fishRadarLayer::render);
        HudRenderCallback.EVENT.register(tournamentOverlay::render);
    }

    public static void registerParticleFactories()
    {
        ParticleFactoryRegistry.getInstance().register(SCParticles.FISHING_NOTIFICATION.get(), FishingNotificationParticles.Provider::new);
        ParticleFactoryRegistry.getInstance().register(SCParticles.FISHING_BITING.get(), FishingBitingParticles.Provider::new);
        ParticleFactoryRegistry.getInstance().register(SCParticles.FISHING_BITING_LAVA.get(), FishingBitingLavaParticles.Provider::new);
    }

    public static void registerScreens()
    {
        MenuScreens.register(SCMenuTypes.FISHING_ROD_MENU.get(), FishingRodScreen::new);
        MenuScreens.register(SCMenuTypes.STAND_MENU.get(), StandScreen::new);
        MenuScreens.register(SCMenuTypes.TACKLE_BOX.get(), TackleBoxScreen::new);
    }

    public static void registerLayers()
    {
        //tackle skins
        EntityModelLayerRegistry.registerModelLayer(new BaseTackleSkin().getLayerLocation(), BaseTackleSkin::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(new PearlTackleSkin().getLayerLocation(), PearlTackleSkin::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(new KimbeTackleSkin().getLayerLocation(), KimbeTackleSkin::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(new KingTackleSkin().getLayerLocation(), KingTackleSkin::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(new FrogTackleSkin().getLayerLocation(), FrogTackleSkin::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(new ColorfulTackleSkin().getLayerLocation(), ColorfulTackleSkin::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(new ClearTackleSkin().getLayerLocation(), ClearTackleSkin::createBodyLayer);

        //tackle box
        EntityModelLayerRegistry.registerModelLayer(TackleBoxRenderer.LAYER_LOCATION, TackleBoxRenderer::createBodyLayer);

        //book model
        EntityModelLayerRegistry.registerModelLayer(DisplayBookModel.LAYER_LOCATION, DisplayBookModel::createBodyLayer);

        //fishes
        EntityModelLayerRegistry.registerModelLayer(AgaveBream.LAYER_LOCATION, AgaveBream::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(BigeyeTuna.LAYER_LOCATION, BigeyeTuna::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(Boreal.LAYER_LOCATION, Boreal::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(CactiFish.LAYER_LOCATION, CactiFish::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(Charfish.LAYER_LOCATION, Charfish::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(CrystalbackBoreal.LAYER_LOCATION, CrystalbackBoreal::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(CrystalbackMinnow.LAYER_LOCATION, CrystalbackMinnow::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(DeepjawHerring.LAYER_LOCATION, DeepjawHerring::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(DownfallBream.LAYER_LOCATION, DownfallBream::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(Driftfin.LAYER_LOCATION, Driftfin::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(DriftingBream.LAYER_LOCATION, DriftingBream::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(DusktailSnapper.LAYER_LOCATION, DusktailSnapper::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(LilySnapper.LAYER_LOCATION, LilySnapper::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(PinkKoi.LAYER_LOCATION, PinkKoi::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(SilverveilPerch.LAYER_LOCATION, SilverveilPerch::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(SludgeCatfish.LAYER_LOCATION, SludgeCatfish::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(Whiteveil.LAYER_LOCATION, Whiteveil::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(WinteryPike.LAYER_LOCATION, WinteryPike::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(CrystalbackTrout.LAYER_LOCATION, CrystalbackTrout::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(Embergill.LAYER_LOCATION, Embergill::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(FrostgillChub.LAYER_LOCATION, FrostgillChub::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(FrostjawTrout.LAYER_LOCATION, FrostjawTrout::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(HollowbellyDarter.LAYER_LOCATION, HollowbellyDarter::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(IcetoothSturgeon.LAYER_LOCATION, IcetoothSturgeon::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(MistbackChub.LAYER_LOCATION, MistbackChub::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(BlueCrystalFin.LAYER_LOCATION, BlueCrystalFin::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(Carpenjoe.LAYER_LOCATION, Carpenjoe::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(Elderscale.LAYER_LOCATION, Elderscale::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(GhostlyPike.LAYER_LOCATION, GhostlyPike::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(IronjarHerring.LAYER_LOCATION, IronjarHerring::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(MirageCarp.LAYER_LOCATION, MirageCarp::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(PetaldriftCarp.LAYER_LOCATION, PetaldriftCarp::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(BlueHerring.LAYER_LOCATION, BlueHerring::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(LightningBass.LAYER_LOCATION, LightningBass::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(LushPike.LAYER_LOCATION, LushPike::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(MagmaFish.LAYER_LOCATION, MagmaFish::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(Morganite.LAYER_LOCATION, Morganite::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(PalePinfish.LAYER_LOCATION, PalePinfish::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(Pinfish.LAYER_LOCATION, Pinfish::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(Pyrotrout.LAYER_LOCATION, Pyrotrout::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(Sculkfish.LAYER_LOCATION, Sculkfish::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(SilverfinPike.LAYER_LOCATION, SilverfinPike::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(VividMoss.LAYER_LOCATION, VividMoss::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(Willish.LAYER_LOCATION, Willish::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(YellowstoneFish.LAYER_LOCATION, YellowstoneFish::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(Voidbiter.LAYER_LOCATION, Voidbiter::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(Obidontiee.LAYER_LOCATION, Obidontiee::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(RedscaledTuna.LAYER_LOCATION, RedscaledTuna::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(SunSeekingCarp.LAYER_LOCATION, SunSeekingCarp::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(Suneater.LAYER_LOCATION, Suneater::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(SunnySturgeon.LAYER_LOCATION, SunnySturgeon::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(TheQuarrish.LAYER_LOCATION, TheQuarrish::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(ThunderBass.LAYER_LOCATION, ThunderBass::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(TwilightKoi.LAYER_LOCATION, TwilightKoi::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(WillowBream.LAYER_LOCATION, WillowBream::createBodyLayer);
    }

    public static void registerKeyMappings()
    {
        KeyBindingHelper.registerKeyBinding(SCKeymappings.MINIGAME_HIT);
        KeyBindingHelper.registerKeyBinding(SCKeymappings.EXPAND_TOURNAMENT);
    }

    public static void registerTooltipComponents()
    {
        TooltipComponentCallback.EVENT.register(data ->
        {
            if (data instanceof StarcaughtBucket.BucketTooltip bucketTooltip)
                return new BucketTooltipRenderer(bucketTooltip);
            if (data instanceof StarcatcherFishingRodItem.RodSlotTooltip rodSlotTooltip)
                return new RodSlotTooltipRenderer(rodSlotTooltip);
            return null;
        });
    }
}
