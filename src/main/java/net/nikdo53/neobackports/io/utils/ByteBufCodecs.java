package net.nikdo53.neobackports.io.utils;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.nikdo53.neobackports.io.StreamCodec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Fabric shim for NeoBackports' {@code ByteBufCodecs} — see FABRIC_PORT_PLAN.md §5.3.
 * Exact member set surveyed from real call sites (grep across src/main/java); nothing
 * beyond what's actually used.
 */
public class ByteBufCodecs
{
    public static final StreamCodec<Boolean> BOOL = simple(FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean);
    public static final StreamCodec<Integer> INT = simple(FriendlyByteBuf::writeInt, FriendlyByteBuf::readInt);
    public static final StreamCodec<Integer> VAR_INT = simple(FriendlyByteBuf::writeVarInt, FriendlyByteBuf::readVarInt);
    public static final StreamCodec<Long> VAR_LONG = simple(FriendlyByteBuf::writeVarLong, FriendlyByteBuf::readVarLong);
    public static final StreamCodec<Float> FLOAT = simple(FriendlyByteBuf::writeFloat, FriendlyByteBuf::readFloat);
    public static final StreamCodec<Double> DOUBLE = simple(FriendlyByteBuf::writeDouble, FriendlyByteBuf::readDouble);
    public static final StreamCodec<String> STRING = simple((buf, s) -> buf.writeUtf(s, 32767), buf -> buf.readUtf(32767));
    public static final StreamCodec<String> STRING_UTF8 = STRING;
    public static final StreamCodec<UUID> UUID = simple(FriendlyByteBuf::writeUUID, FriendlyByteBuf::readUUID);
    public static final StreamCodec<ResourceLocation> RESOURCE_LOCATION = simple(FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::readResourceLocation);
    public static final StreamCodec<ItemStack> ITEM_STACK = simple(FriendlyByteBuf::writeItem, FriendlyByteBuf::readItem);
    public static final StreamCodec<Ingredient> INGREDIENT = simple((buf, ingredient) -> ingredient.toNetwork(buf), Ingredient::fromNetwork);
    public static final StreamCodec<BlockPos> BLOCK_POS = simple(FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::readBlockPos);

    private static <V> StreamCodec<V> simple(java.util.function.BiConsumer<FriendlyByteBuf, V> encoder, java.util.function.Function<FriendlyByteBuf, V> decoder)
    {
        return new StreamCodec<V>()
        {
            @Override
            public V decode(FriendlyByteBuf buffer)
            {
                return decoder.apply(buffer);
            }

            @Override
            public void encode(FriendlyByteBuf buffer, V value)
            {
                encoder.accept(buffer, value);
            }
        };
    }

    public static <V> StreamCodec.CodecOperation<V, List<V>> list()
    {
        return base -> new StreamCodec<List<V>>()
        {
            @Override
            public List<V> decode(FriendlyByteBuf buffer)
            {
                int size = buffer.readVarInt();
                List<V> list = new ArrayList<>(size);
                for (int i = 0; i < size; i++)
                    list.add(base.decode(buffer));
                return list;
            }

            @Override
            public void encode(FriendlyByteBuf buffer, List<V> value)
            {
                buffer.writeVarInt(value.size());
                for (V v : value)
                    base.encode(buffer, v);
            }
        };
    }

    public static <K, V, M extends Map<K, V>> StreamCodec<M> map(Supplier<M> mapFactory, StreamCodec<K> keyCodec, StreamCodec<V> valueCodec)
    {
        return new StreamCodec<M>()
        {
            @Override
            public M decode(FriendlyByteBuf buffer)
            {
                int size = buffer.readVarInt();
                M map = mapFactory.get();
                for (int i = 0; i < size; i++)
                    map.put(keyCodec.decode(buffer), valueCodec.decode(buffer));
                return map;
            }

            @Override
            public void encode(FriendlyByteBuf buffer, M value)
            {
                buffer.writeVarInt(value.size());
                for (Map.Entry<K, V> entry : value.entrySet())
                {
                    keyCodec.encode(buffer, entry.getKey());
                    valueCodec.encode(buffer, entry.getValue());
                }
            }
        };
    }

    /**
     * Wraps a DataFixerUpper {@link Codec} into a StreamCodec by round-tripping through NBT.
     * Wrapped in a single-entry CompoundTag since the encoded {@link Tag} isn't always a
     * CompoundTag itself (e.g. {@code Codec.listOf()} produces a ListTag) and
     * {@link FriendlyByteBuf} only exposes a CompoundTag-typed writeNbt/readNbt.
     */
    public static <V> StreamCodec<V> fromCodec(Codec<V> codec)
    {
        return new StreamCodec<V>()
        {
            @Override
            public V decode(FriendlyByteBuf buffer)
            {
                CompoundTag wrapper = buffer.readNbt();
                Tag value = wrapper == null ? null : wrapper.get("v");
                return codec.parse(NbtOps.INSTANCE, value).getOrThrow(false, s -> {
                });
            }

            @Override
            public void encode(FriendlyByteBuf buffer, V value)
            {
                Tag encoded = codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow(false, s -> {
                });
                CompoundTag wrapper = new CompoundTag();
                wrapper.put("v", encoded);
                buffer.writeNbt(wrapper);
            }
        };
    }

    /**
     * Encodes a {@link Holder} of a real vanilla registry entry by its numeric registry id.
     * Only meaningful for registries fully populated by the time this codec runs (all of
     * Starcatcher's usages are for vanilla registries — see FABRIC_PORT_PLAN.md §5.3).
     */
    @SuppressWarnings("unchecked")
    public static <T> StreamCodec<Holder<T>> holderRegistry(ResourceKey<? extends Registry<T>> registryKey)
    {
        return new StreamCodec<Holder<T>>()
        {
            private Registry<T> registry()
            {
                Registry<?> found = BuiltInRegistries.REGISTRY.get(registryKey.location());
                if (found == null)
                    throw new IllegalStateException("Unknown registry " + registryKey.location());
                return (Registry<T>) found;
            }

            @Override
            public Holder<T> decode(FriendlyByteBuf buffer)
            {
                int id = buffer.readVarInt();
                return registry().wrapAsHolder(registry().byId(id));
            }

            @Override
            public void encode(FriendlyByteBuf buffer, Holder<T> value)
            {
                buffer.writeVarInt(registry().getId(value.value()));
            }
        };
    }
}
