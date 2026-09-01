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
            ClientReceiver.register(type, codec, handler);

        return this;
    }

    /**
     * A genuinely separate class, not just an {@code @Environment}-annotated method on
     * {@link PayloadRegistrar} — javac desugars the {@code ClientPlayNetworking.registerGlobalReceiver}
     * lambda below into its own synthetic method (verified via {@code javap}:
     * {@code lambda$registerClientReceiver$0(..., Minecraft, ...)}), and that synthetic method does
     * NOT itself inherit the enclosing method's {@code @Environment} annotation. Fabric Loader's
     * stripper only strips annotated methods, so the orphaned, un-annotated lambda body (still
     * {@code Minecraft}/{@code LocalPlayer}-typed) was left behind in {@code PayloadRegistrar}'s own
     * class file and the JVM verifier choked on IT instead — confirmed via runServer, crashing at the
     * exact same "Cannot load class net.minecraft.client.player.LocalPlayer in environment type
     * SERVER" even after extracting a same-class method (see git history on this file).
     * <p>
     * With the lambda in its OWN class instead, {@link PayloadRegistrar}'s class file (verified
     * whenever {@code new PayloadRegistrar(...)} runs, unconditionally on both sides) never contains
     * any reference to a client-only type at all — {@link #playToClient}'s call to
     * {@code ClientReceiver.register(...)} only exposes {@code CustomPacketPayload.Type}/
     * {@code StreamCodec}/{@code BiConsumer} in its descriptor. {@code ClientReceiver} itself is only
     * ever actually loaded when that call executes, which the runtime environment check in
     * {@link #playToClient} guarantees never happens on a dedicated server.
     */
    @Environment(EnvType.CLIENT)
    private static class ClientReceiver
    {
        private static <T extends CustomPacketPayload> void register(
                CustomPacketPayload.Type<T> type, StreamCodec<T> codec, BiConsumer<T, IPayloadContext> handler)
        {
            ClientPlayNetworking.registerGlobalReceiver(type.id(), (client, listener, buf, sender) ->
            {
                T payload = codec.decode(buf);
                handler.accept(payload, new PayloadContextImpl(client.player, client));
            });
        }
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
