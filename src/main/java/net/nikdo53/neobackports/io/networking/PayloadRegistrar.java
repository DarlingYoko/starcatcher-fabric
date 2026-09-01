package net.nikdo53.neobackports.io.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.nikdo53.neobackports.io.StreamCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Fabric shim for NeoBackports' {@code PayloadRegistrar} — see FABRIC_PORT_PLAN.md §5.4.
 * Built directly (no {@code RegisterPayloadHandlersEvent}) and used once from
 * {@code SCNetworking.register()} at mod init. Backed by Fabric Networking API v1's
 * channel-based {@code ServerPlayNetworking}/{@code ClientPlayNetworking} (this Minecraft
 * version predates the payload-type-registry rework), confirmed via {@code javap} against
 * the actual remapped {@code fabric-networking-api-v1} module jar.
 */
public class PayloadRegistrar
{
    static final Map<ResourceLocation, StreamCodec<? extends CustomPacketPayload>> CODECS = new HashMap<>();

    public PayloadRegistrar(String protocolVersion, String modId)
    {
    }

    public <T extends CustomPacketPayload> PayloadRegistrar playToClient(
            CustomPacketPayload.Type<T> type, StreamCodec<T> codec, BiConsumer<T, IPayloadContext> handler)
    {
        CODECS.put(type.id(), codec);

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT)
            registerClientReceiver(type, codec, handler);

        return this;
    }

    /**
     * Split out of {@link #playToClient} so the {@code ClientPlayNetworking}/{@code Minecraft}-typed
     * lambda body never ends up in {@code playToClient}'s own bytecode: {@code playToClient} runs on
     * both sides (called unconditionally from common init), so a client-only lambda inlined directly
     * there would still be present in the class file's constant pool/verification data on a dedicated
     * server even though the runtime `if` guard means it's never invoked — the JVM verifier resolves
     * referenced types (e.g. {@code LocalPlayer}, captured via {@code client.player}) at class-load
     * time regardless of reachability, which crashes server startup with "Cannot load class
     * net.minecraft.client.player.LocalPlayer in environment type SERVER". Method-level
     * {@code @Environment} is Fabric Loader's actual stripping unit, so it must be its own method.
     */
    @Environment(EnvType.CLIENT)
    private static <T extends CustomPacketPayload> void registerClientReceiver(
            CustomPacketPayload.Type<T> type, StreamCodec<T> codec, BiConsumer<T, IPayloadContext> handler)
    {
        ClientPlayNetworking.registerGlobalReceiver(type.id(), (client, listener, buf, sender) ->
        {
            T payload = codec.decode(buf);
            handler.accept(payload, new PayloadContextImpl(client.player, client));
        });
    }

    public <T extends CustomPacketPayload> PayloadRegistrar playToServer(
            CustomPacketPayload.Type<T> type, StreamCodec<T> codec, BiConsumer<T, IPayloadContext> handler)
    {
        CODECS.put(type.id(), codec);

        ServerPlayNetworking.registerGlobalReceiver(type.id(), (server, player, listener, buf, sender) ->
        {
            T payload = codec.decode(buf);
            handler.accept(payload, new PayloadContextImpl(player, server));
        });

        return this;
    }
}
