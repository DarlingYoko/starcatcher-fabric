package net.nikdo53.neobackports.io.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.nikdo53.neobackports.io.StreamCodec;

/**
 * Fabric shim for NeoBackports' {@code PacketDistributorNeo} — see FABRIC_PORT_PLAN.md §5.4.
 * Only the two shapes actually used by call sites in this codebase are provided:
 * server→player and client→server, unlike upstream NeoForge's much larger distributor surface
 * (ALL, TRACKING_ENTITY, NEAR, etc. — none of which this mod calls).
 */
public class PacketDistributorNeo
{
    @SuppressWarnings("unchecked")
    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload)
    {
        StreamCodec<CustomPacketPayload> codec =
                (StreamCodec<CustomPacketPayload>) PayloadRegistrar.CODECS.get(payload.type().id());
        FriendlyByteBuf buf = PacketByteBufs.create();
        codec.encode(buf, payload);
        ServerPlayNetworking.send(player, payload.type().id(), buf);
    }

    @SuppressWarnings("unchecked")
    public static void sendToServer(CustomPacketPayload payload)
    {
        StreamCodec<CustomPacketPayload> codec =
                (StreamCodec<CustomPacketPayload>) PayloadRegistrar.CODECS.get(payload.type().id());
        FriendlyByteBuf buf = PacketByteBufs.create();
        codec.encode(buf, payload);
        ClientPlayNetworking.send(payload.type().id(), buf);
    }
}
