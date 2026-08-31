package com.wdiscute.starcatcher.io;

import com.wdiscute.starcatcher.Starcatcher;
import com.wdiscute.starcatcher.io.attachments.FishingBobAttachment;
import com.wdiscute.starcatcher.io.attachments.FishingGuideAttachment;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.nikdo53.neobackports.io.attachment.AttachmentComponent;

/**
 * Rewritten on top of Cardinal Components (real per-holder component storage + auto-sync) — see
 * FABRIC_PORT_PLAN.md §5.5. There is no Fabric equivalent of NeoForge's vanilla-patched
 * {@code Entity.getData/setData/removeData} instance methods (same javac-vs-Mixin-timing
 * impossibility as §5.2's {@code ItemStack} data components), so every call site is flattened
 * to this static facade instead. The actual factories (default values, codecs) are registered in
 * {@link com.wdiscute.starcatcher.io.attachments.SCEntityComponents}, a Cardinal Components
 * {@code EntityComponentInitializer} entrypoint — CCA registration doesn't go through the mod's
 * own {@code IEventBus} token at all, so there is no {@code register(IEventBus)} method here
 * (the original had one for {@code DeferredRegister}, called only from the untouched, already
 * out-of-scope Forge {@code Starcatcher} class).
 */
public class SCDataAttachments
{
    @SuppressWarnings("unchecked")
    public static final ComponentKey<AttachmentComponent<FishingBobAttachment>> FISHING_BOB =
            ComponentRegistry.getOrCreate(Starcatcher.rl("fishing_bob"), (Class<AttachmentComponent<FishingBobAttachment>>) (Class<?>) AttachmentComponent.class);

    @SuppressWarnings("unchecked")
    public static final ComponentKey<AttachmentComponent<FishingGuideAttachment>> FISHING_GUIDE =
            ComponentRegistry.getOrCreate(Starcatcher.rl("fishing_guide"), (Class<AttachmentComponent<FishingGuideAttachment>>) (Class<?>) AttachmentComponent.class);

    @SuppressWarnings("unchecked")
    public static final ComponentKey<AttachmentComponent<ResourceLocation>> TACKLE_SKIN =
            ComponentRegistry.getOrCreate(Starcatcher.rl("tackle_skin"), (Class<AttachmentComponent<ResourceLocation>>) (Class<?>) AttachmentComponent.class);

    public static <T> T get(Entity holder, ComponentKey<AttachmentComponent<T>> key)
    {
        if (holder == null) throw new RuntimeException("Called Starcatcher DataAttachments Get() with a null entity");
        return key.get(holder).get();
    }

    // resets the value to default
    public static <T> void remove(Entity holder, ComponentKey<AttachmentComponent<T>> key)
    {
        if (holder == null) return;
        key.get(holder).reset();
        key.sync(holder);
    }

    public static <T> void set(Entity holder, ComponentKey<AttachmentComponent<T>> key, T data)
    {
        if (holder == null) return;
        key.get(holder).set(data);
        key.sync(holder);
    }

    public static void sync(Entity holder, ComponentKey<? extends AutoSyncedComponent> key)
    {
        key.sync(holder);
    }
}
