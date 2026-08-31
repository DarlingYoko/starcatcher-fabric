package net.nikdo53.neobackports.io.components;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

/**
 * Fabric shim for NeoBackports' {@code DataComponentType} — see FABRIC_PORT_PLAN.md §5.2.
 * Vanilla 1.20.1 predates the real Data Components system (added 1.20.5+), so there is no
 * registry to back this with. Each instance just pairs an id with the {@link Codec} used to
 * (de)serialize it; storage itself lives in {@link SCDataComponents}, which encodes values
 * into a namespaced NBT compound on the {@code ItemStack} keyed by {@link #id()}.
 */
public class DataComponentType<T>
{
    private final ResourceLocation id;
    private final Codec<T> codec;

    private DataComponentType(ResourceLocation id, Codec<T> codec)
    {
        this.id = id;
        this.codec = codec;
    }

    public ResourceLocation id()
    {
        return id;
    }

    public Codec<T> codec()
    {
        return codec;
    }

    public static <T> Builder<T> builder()
    {
        return new Builder<>();
    }

    public static class Builder<T>
    {
        private Codec<T> codec;

        public Builder<T> persistent(Codec<T> codec)
        {
            this.codec = codec;
            return this;
        }

        public DataComponentType<T> build(ResourceLocation id)
        {
            return new DataComponentType<>(id, codec);
        }
    }
}
