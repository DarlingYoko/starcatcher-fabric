package com.wdiscute.starcatcher.io.attachments;

import com.wdiscute.starcatcher.Starcatcher;
import com.wdiscute.starcatcher.bobberentity.FishingBobEntity;
import com.wdiscute.starcatcher.io.SCDataAttachments;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.resources.ResourceLocation;
import net.nikdo53.neobackports.io.attachment.AttachmentComponent;
import net.nikdo53.neobackports.io.utils.ByteBufCodecs;

/**
 * Cardinal Components entrypoint (registered via {@code cardinal-components-entity} in
 * fabric.mod.json, not the mod's own {@code IEventBus} token) that actually creates the
 * component instances {@link SCDataAttachments}' keys look up — see FABRIC_PORT_PLAN.md §5.5.
 * {@code FISHING_BOB}/{@code FISHING_GUIDE} are only ever attached to {@code Player}s at call
 * sites (grepped across the whole 335-file tree); {@code TACKLE_SKIN} is only ever attached to
 * {@link FishingBobEntity}, so it's registered narrowly for that class rather than a broad
 * "non-living entity" predicate the original NeoForge-style {@code canAttachTo} declared but
 * nothing actually used.
 */
public class SCEntityComponents implements EntityComponentInitializer
{
    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry)
    {
        registry.registerForPlayers(
                SCDataAttachments.FISHING_BOB,
                player -> new AttachmentComponent<>(() -> new FishingBobAttachment(""), FishingBobAttachment.CODEC, FishingBobAttachment.STREAM_CODEC),
                RespawnCopyStrategy.NEVER_COPY
        );

        registry.registerForPlayers(
                SCDataAttachments.FISHING_GUIDE,
                player -> new AttachmentComponent<>(FishingGuideAttachment::createDefault, FishingGuideAttachment.CODEC, FishingGuideAttachment.STREAM_CODEC),
                RespawnCopyStrategy.ALWAYS_COPY
        );

        registry.registerFor(
                FishingBobEntity.class,
                SCDataAttachments.TACKLE_SKIN,
                entity -> new AttachmentComponent<>(() -> Starcatcher.rl("base"), ResourceLocation.CODEC, ByteBufCodecs.RESOURCE_LOCATION)
        );
    }
}
