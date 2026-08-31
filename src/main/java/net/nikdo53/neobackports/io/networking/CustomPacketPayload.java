package net.nikdo53.neobackports.io.networking;

import net.minecraft.resources.ResourceLocation;

/**
 * Fabric shim for NeoBackports' {@code CustomPacketPayload} — see FABRIC_PORT_PLAN.md §5.4.
 * {@link Type} carries the payload's {@code Class} alongside its id (unlike real upstream
 * NeoForge's single-arg {@code Type(ResourceLocation)}) since it's used by
 * {@link PacketDistributorNeo} to look up the registered {@link net.nikdo53.neobackports.io.StreamCodec}
 * purely from a payload instance, without a network-thread registrar handle.
 */
public interface CustomPacketPayload
{
    Type<? extends CustomPacketPayload> type();

    class Type<T extends CustomPacketPayload>
    {
        private final ResourceLocation id;
        private final Class<T> payloadClass;

        public Type(ResourceLocation id, Class<T> payloadClass)
        {
            this.id = id;
            this.payloadClass = payloadClass;
        }

        public ResourceLocation id()
        {
            return id;
        }

        public Class<T> payloadClass()
        {
            return payloadClass;
        }
    }
}
