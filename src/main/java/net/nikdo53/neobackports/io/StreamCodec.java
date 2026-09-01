package net.nikdo53.neobackports.io;

import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Function;

/**
 * Fabric shim for NeoBackports' {@code StreamCodec} — see FABRIC_PORT_PLAN.md §5.3.
 * Deliberately single-type-param (unlike real upstream NeoForge's 2-param
 * {@code StreamCodec<B, V>}) — confirmed from actual call sites in this codebase
 * (e.g. {@code FishCaughtPayload.STREAM_CODEC : StreamCodec<FishCaughtPayload>}).
 * The buffer type is fixed to vanilla {@link FriendlyByteBuf} (1.20.1 has no
 * RegistryFriendlyByteBuf). Higher-arity composites (7+ fields) are handled by the
 * mod's own {@code com.wdiscute.starcatcher.io.ExtraComposites}, which already assumes
 * exactly this shape.
 */
public interface StreamCodec<V>
{
    V decode(FriendlyByteBuf buffer);

    void encode(FriendlyByteBuf buffer, V value);

    default <C> StreamCodec<C> apply(CodecOperation<V, C> operation)
    {
        return operation.apply(this);
    }

    default <O> StreamCodec<O> map(Function<V, O> to, Function<O, V> from)
    {
        StreamCodec<V> self = this;
        return new StreamCodec<O>()
        {
            @Override
            public O decode(FriendlyByteBuf buffer)
            {
                return to.apply(self.decode(buffer));
            }

            @Override
            public void encode(FriendlyByteBuf buffer, O value)
            {
                self.encode(buffer, from.apply(value));
            }
        };
    }

    interface CodecOperation<V, C>
    {
        StreamCodec<C> apply(StreamCodec<V> base);
    }

    static <C, T1> StreamCodec<C> composite(
            StreamCodec<T1> codec1, Function<C, T1> getter1,
            Function<T1, C> factory)
    {
        return new StreamCodec<C>()
        {
            @Override
            public C decode(FriendlyByteBuf buffer)
            {
                return factory.apply(codec1.decode(buffer));
            }

            @Override
            public void encode(FriendlyByteBuf buffer, C value)
            {
                codec1.encode(buffer, getter1.apply(value));
            }
        };
    }

    static <C, T1, T2> StreamCodec<C> composite(
            StreamCodec<T1> codec1, Function<C, T1> getter1,
            StreamCodec<T2> codec2, Function<C, T2> getter2,
            java.util.function.BiFunction<T1, T2, C> factory)
    {
        return new StreamCodec<C>()
        {
            @Override
            public C decode(FriendlyByteBuf buffer)
            {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                return factory.apply(t1, t2);
            }

            @Override
            public void encode(FriendlyByteBuf buffer, C value)
            {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
            }
        };
    }

    static <C, T1, T2, T3> StreamCodec<C> composite(
            StreamCodec<T1> codec1, Function<C, T1> getter1,
            StreamCodec<T2> codec2, Function<C, T2> getter2,
            StreamCodec<T3> codec3, Function<C, T3> getter3,
            com.mojang.datafixers.util.Function3<T1, T2, T3, C> factory)
    {
        return new StreamCodec<C>()
        {
            @Override
            public C decode(FriendlyByteBuf buffer)
            {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                return factory.apply(t1, t2, t3);
            }

            @Override
            public void encode(FriendlyByteBuf buffer, C value)
            {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
            }
        };
    }

    static <C, T1, T2, T3, T4> StreamCodec<C> composite(
            StreamCodec<T1> codec1, Function<C, T1> getter1,
            StreamCodec<T2> codec2, Function<C, T2> getter2,
            StreamCodec<T3> codec3, Function<C, T3> getter3,
            StreamCodec<T4> codec4, Function<C, T4> getter4,
            com.mojang.datafixers.util.Function4<T1, T2, T3, T4, C> factory)
    {
        return new StreamCodec<C>()
        {
            @Override
            public C decode(FriendlyByteBuf buffer)
            {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                return factory.apply(t1, t2, t3, t4);
            }

            @Override
            public void encode(FriendlyByteBuf buffer, C value)
            {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
            }
        };
    }

    static <C, T1, T2, T3, T4, T5> StreamCodec<C> composite(
            StreamCodec<T1> codec1, Function<C, T1> getter1,
            StreamCodec<T2> codec2, Function<C, T2> getter2,
            StreamCodec<T3> codec3, Function<C, T3> getter3,
            StreamCodec<T4> codec4, Function<C, T4> getter4,
            StreamCodec<T5> codec5, Function<C, T5> getter5,
            com.mojang.datafixers.util.Function5<T1, T2, T3, T4, T5, C> factory)
    {
        return new StreamCodec<C>()
        {
            @Override
            public C decode(FriendlyByteBuf buffer)
            {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                return factory.apply(t1, t2, t3, t4, t5);
            }

            @Override
            public void encode(FriendlyByteBuf buffer, C value)
            {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
                codec5.encode(buffer, getter5.apply(value));
            }
        };
    }

    static <C, T1, T2, T3, T4, T5, T6> StreamCodec<C> composite(
            StreamCodec<T1> codec1, Function<C, T1> getter1,
            StreamCodec<T2> codec2, Function<C, T2> getter2,
            StreamCodec<T3> codec3, Function<C, T3> getter3,
            StreamCodec<T4> codec4, Function<C, T4> getter4,
            StreamCodec<T5> codec5, Function<C, T5> getter5,
            StreamCodec<T6> codec6, Function<C, T6> getter6,
            com.mojang.datafixers.util.Function6<T1, T2, T3, T4, T5, T6, C> factory)
    {
        return new StreamCodec<C>()
        {
            @Override
            public C decode(FriendlyByteBuf buffer)
            {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                return factory.apply(t1, t2, t3, t4, t5, t6);
            }

            @Override
            public void encode(FriendlyByteBuf buffer, C value)
            {
                codec1.encode(buffer, getter1.apply(value));
                codec2.encode(buffer, getter2.apply(value));
                codec3.encode(buffer, getter3.apply(value));
                codec4.encode(buffer, getter4.apply(value));
                codec5.encode(buffer, getter5.apply(value));
                codec6.encode(buffer, getter6.apply(value));
            }
        };
    }
}
