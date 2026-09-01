package com.wdiscute.starcatcher.event;

import com.wdiscute.starcatcher.SCConfig;
import com.wdiscute.starcatcher.SCTags;
import com.wdiscute.starcatcher.registry.SCCommands;
import com.wdiscute.starcatcher.io.SCDataAttachments;
import com.wdiscute.starcatcher.io.TournamentSavedData;
import com.wdiscute.starcatcher.io.attachments.FishingGuideAttachment;
import com.wdiscute.starcatcher.registry.SCItems;
import com.wdiscute.starcatcher.tournament.TournamentHandler;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Rewritten from a {@code @Mod.EventBusSubscriber(bus = FORGE)} class to plain methods called
 * from a {@link #register()} wired into {@code StarcatcherFabric.onInitialize()} — see
 * FABRIC_PORT_PLAN.md §6. The data-map lazy-copy this class used to do in
 * {@code modifyItemAttribute} (fired by Forge's {@code ItemAttributeModifierEvent}, which has no
 * Fabric equivalent) now happens directly in {@code SCDataComponents.get}'s data-map fallback
 * instead — see FABRIC_PORT_PLAN.md §5.6.
 */
public class SCEvents
{
    public static void register()
    {
        ServerLifecycleEvents.SERVER_STARTED.register(SCEvents::serverStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(SCEvents::serverStopping);
        ServerTickEvents.END_SERVER_TICK.register(TournamentHandler::tick);
        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> SCCommands.register(dispatcher, buildContext));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerLoggedIn(handler.player));
        UseBlockCallback.EVENT.register(SCEvents::dropWormsWhenBonemealing);
    }

    public static void serverStarted(net.minecraft.server.MinecraftServer server)
    {
        TournamentHandler.setAll(TournamentSavedData.get(server.overworld()).getTournaments());
        registerCompostables();
    }

    /**
     * Real vanilla {@code ComposterBlock.COMPOSTABLES} has no data-driven equivalent to NeoForge's
     * data-map-based compostables (see FABRIC_PORT_PLAN.md §9, {@code DGSCDataMapsProvider}) — the
     * two tags are just resolved directly against the loaded registry here instead, once tags are
     * guaranteed to be bound (server start).
     */
    private static void registerCompostables()
    {
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(SCTags.WORMS))
            ComposterBlock.COMPOSTABLES.put(holder.value(), 0.65F);

        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(SCTags.BUCKETABLE_FISHES))
            ComposterBlock.COMPOSTABLES.put(holder.value(), 0.9F);
    }

    public static void serverStopping(net.minecraft.server.MinecraftServer server)
    {
        TournamentSavedData.get(server.overworld()).setTournaments(TournamentHandler.getAll());
    }

    public static void onPlayerLoggedIn(ServerPlayer sp)
    {
        //tournament
        var tournament = TournamentHandler.getTournamentForPlayer(sp);
        if (tournament != null)
            TournamentHandler.sendActiveTournamentUpdateToClient(sp, tournament);
        else
            TournamentHandler.clearTournamentToClient(sp);

        //guide
        FishingGuideAttachment fishingGuideAttachment = SCDataAttachments.get(sp, SCDataAttachments.FISHING_GUIDE);

        if (SCConfig.GIVE_GUIDE.get() && !FishingGuideAttachment.getReceivedGuide(sp))
        {
            sp.addItem(new ItemStack(SCItems.GUIDE.get()));
            fishingGuideAttachment.setReceivedGuide(sp, true);
        }
    }


    public static InteractionResult dropWormsWhenBonemealing(Player player, Level level, InteractionHand hand, BlockHitResult hitResult)
    {
        BlockPos pos = hitResult.getBlockPos();
        ItemStack heldItem = player.getItemInHand(hand);

        if (heldItem.is(Items.BONE_MEAL) && level.getBlockState(pos).getBlock() instanceof FarmBlock)
        {
            if (!level.isClientSide && SCConfig.ENABLE_BONE_MEAL_ON_FARMLAND_FOR_WORMS.get())
            {
                ItemStack is;
                float i = level.getRandom().nextFloat();
                if (i < 0.8f)
                    is = new ItemStack(SCItems.WORM.get());
                else if (i < 0.99f)
                    is = new ItemStack(SCItems.ALMIGHTY_WORM.get());
                else
                    is = new ItemStack(SCItems.SEEKING_WORM.get());

                Vec3 vec3 = Vec3.atLowerCornerWithOffset(pos, 0.5F, 1.01, 0.5F).offsetRandom(level.random, 0.7F);
                ItemEntity itementity = new ItemEntity(level, vec3.x(), vec3.y(), vec3.z(), is);
                itementity.setDefaultPickUpDelay();
                level.addFreshEntity(itementity);

                level.playSound(null, pos, SoundEvents.COMPOSTER_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);

                if (player instanceof ServerPlayer sp)
                {
                    sp.swing(hand, true);
                    if (!sp.isCreative())
                        heldItem.shrink(1);
                }
            }
        }

        return InteractionResult.PASS;
    }


}
