package net.nikdo53.neobackports.io.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Map;

/**
 * Fabric shim for NeoBackports' {@code BackportCodecs} — see FABRIC_PORT_PLAN.md §5.3.
 * Vanilla 1.20.1 has no native {@code Codec<Ingredient>} (that's a 1.20.5+ component-era
 * addition); {@link IngredientCodecs#CODEC} round-trips through Ingredient's existing
 * JSON (de)serialization via {@link Codec#PASSTHROUGH}.
 */
public class BackportCodecs
{
    public static class IngredientCodecs
    {
        public static final Codec<Ingredient> CODEC = Codec.PASSTHROUGH.comapFlatMap(
                dynamic -> {
                    try
                    {
                        return DataResult.success(Ingredient.fromJson(dynamic.convert(JsonOps.INSTANCE).getValue()));
                    }
                    catch (Exception e)
                    {
                        return DataResult.error(() -> "Failed to parse Ingredient: " + e.getMessage());
                    }
                },
                ingredient -> new Dynamic<>(JsonOps.INSTANCE, ingredient.toJson())
        );
    }

    public static final Codec<ItemStack> ITEM_STACK_RECIPE = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(ItemStack::getItem),
            Codec.INT.optionalFieldOf("count", 1).forGetter(ItemStack::getCount)
    ).apply(instance, ItemStack::new));

    public static <K, V> Codec<Map<K, V>> strictUnboundedMap(Codec<K> keyCodec, Codec<V> valueCodec)
    {
        return Codec.unboundedMap(keyCodec, valueCodec);
    }
}
