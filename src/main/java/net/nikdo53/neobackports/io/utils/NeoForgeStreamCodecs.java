package net.nikdo53.neobackports.io.utils;

import net.minecraft.network.FriendlyByteBuf;
import net.nikdo53.neobackports.io.StreamCodec;

public class NeoForgeStreamCodecs
{
    public static <E extends Enum<E>> StreamCodec<E> enumCodec(Class<E> enumClass)
    {
        E[] values = enumClass.getEnumConstants();
        return new StreamCodec<E>()
        {
            @Override
            public E decode(FriendlyByteBuf buffer)
            {
                return values[buffer.readVarInt()];
            }

            @Override
            public void encode(FriendlyByteBuf buffer, E value)
            {
                buffer.writeVarInt(value.ordinal());
            }
        };
    }
}
