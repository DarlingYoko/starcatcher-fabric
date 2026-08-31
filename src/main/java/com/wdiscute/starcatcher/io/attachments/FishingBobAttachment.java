package com.wdiscute.starcatcher.io.attachments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wdiscute.starcatcher.io.SCDataAttachments;
import net.minecraft.world.entity.Entity;
import net.nikdo53.neobackports.io.StreamCodec;
import net.nikdo53.neobackports.io.utils.ByteBufCodecs;

import java.util.UUID;

public class FishingBobAttachment
{
    private String uuid;

    public FishingBobAttachment(String uuid)
    {
        this.uuid = uuid;
    }

    public static final Codec<FishingBobAttachment> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("uuid").forGetter(data -> data.uuid)
            ).apply(instance, FishingBobAttachment::new)
    );

    public static final StreamCodec<FishingBobAttachment> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING, data -> data.uuid,
            FishingBobAttachment::new
    );

    public boolean isEmpty()
    {
        return uuid.isEmpty();
    }

    public void setUuid(Entity holder, UUID uuid) {
        this.uuid = uuid.toString();
        SCDataAttachments.set(holder, SCDataAttachments.FISHING_BOB, new FishingBobAttachment(this.uuid));
    }

    public UUID getUuid()
    {
        if(uuid.isEmpty()) return UUID.randomUUID();
        return UUID.fromString(uuid);
    }

}
