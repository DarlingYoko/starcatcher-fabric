package net.nikdo53.neobackports.io.attachment;

import com.mojang.serialization.Codec;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.CopyableComponent;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.nikdo53.neobackports.io.StreamCodec;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Generic Cardinal Components component backing NeoBackports-style data attachments — see
 * FABRIC_PORT_PLAN.md §5.5. One instance per attachment type per holder; {@code persistenceCodec}
 * and {@code syncCodec} are both nullable (an attachment may be sync-only, persist-only, or
 * neither) mirroring the original builder's optional {@code .serialize(...)}/{@code .sync(...)}
 * calls. There is no standalone {@code AttachmentType}/{@code DataAttachment} shim class — the
 * builder-chain API was flattened away since {@code SCDataAttachments} is its only caller (same
 * "flatten, don't shim the whole abstraction" approach as §5.2/§5.8).
 */
public class AttachmentComponent<T> implements Component, AutoSyncedComponent, CopyableComponent<AttachmentComponent<T>>
{
    private final Supplier<T> initializer;
    private final Codec<T> persistenceCodec;
    private final StreamCodec<T> syncCodec;
    private T value;

    public AttachmentComponent(Supplier<T> initializer, Codec<T> persistenceCodec, StreamCodec<T> syncCodec)
    {
        this.initializer = initializer;
        this.persistenceCodec = persistenceCodec;
        this.syncCodec = syncCodec;
        this.value = initializer.get();
    }

    public T get()
    {
        return value;
    }

    public void set(T value)
    {
        this.value = value;
    }

    public void reset()
    {
        this.value = initializer.get();
    }

    @Override
    public void readFromNbt(CompoundTag tag)
    {
        if (persistenceCodec == null) return;
        Tag data = tag.get("value");
        if (data == null) return;
        persistenceCodec.parse(NbtOps.INSTANCE, data).result().ifPresent(v -> this.value = v);
    }

    @Override
    public void writeToNbt(CompoundTag tag)
    {
        if (persistenceCodec == null) return;
        persistenceCodec.encodeStart(NbtOps.INSTANCE, value).result().ifPresent(t -> tag.put("value", t));
    }

    @Override
    public void writeSyncPacket(FriendlyByteBuf buf, ServerPlayer recipient)
    {
        if (syncCodec != null) syncCodec.encode(buf, value);
    }

    @Override
    public void applySyncPacket(FriendlyByteBuf buf)
    {
        if (syncCodec != null) this.value = syncCodec.decode(buf);
    }

    @Override
    public void copyFrom(AttachmentComponent<T> other)
    {
        this.value = other.value;
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof AttachmentComponent<?> other && Objects.equals(this.value, other.value);
    }
}
