package com.wdiscute.starcatcher.recipe;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wdiscute.starcatcher.io.SCDataComponents;
import com.wdiscute.starcatcher.registry.SCDataMaps;
import com.wdiscute.starcatcher.registry.SCItems;
import com.wdiscute.starcatcher.registry.SCRecipes;
import com.wdiscute.starcatcher.registry.tackleskin.SCTackleSkins;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.nikdo53.neobackports.io.utils.BackportCodecs;
import net.nikdo53.neobackports.io.utils.ByteBufCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public record NetheriteUpgradeSmithingRecipe(ResourceLocation id, Ingredient template, Ingredient base, Ingredient addition) implements SmithingRecipe
{
    public boolean matches(Container input, Level level)
    {
        boolean b = this.template.test(input.getItem(0))
                && this.base.test(input.getItem(1))
                && this.addition.test(input.getItem(2))
                && !SCDataComponents.getOrDefault(input.getItem(1), SCDataComponents.NETHERITE_UPGRADE, false);
        return b;
    }

    public ItemStack assemble(Container input, RegistryAccess registries)
    {
        ItemStack resultRod = input.getItem(1).copy();

        List<ResourceLocation> catchModifiers = new ArrayList<>(SCDataComponents.getOrDefault(input.getItem(1), SCDataComponents.CATCH_MODIFIERS, List.of()));
        catchModifiers.addAll(SCDataComponents.getOrDefault(input.getItem(0), SCDataComponents.CATCH_MODIFIERS, List.of()));
        catchModifiers.addAll(SCDataMaps.getOrDefault(input.getItem(0), SCDataMaps.CATCH_MODIFIERS, List.of()));

        List<ResourceLocation> minigameModifiers = new ArrayList<>(SCDataComponents.getOrDefault(input.getItem(1), SCDataComponents.MINIGAME_MODIFIERS, List.of()));
        minigameModifiers.addAll(SCDataComponents.getOrDefault(input.getItem(0), SCDataComponents.MINIGAME_MODIFIERS, List.of()));
        minigameModifiers.addAll(SCDataMaps.getOrDefault(input.getItem(0), SCDataMaps.MINIGAME_MODIFIERS, List.of()));

        ResourceLocation tackleSkin = SCTackleSkins.getTackleSkin(input.getItem(0));
        if (!tackleSkin.equals(SCTackleSkins.BASE_TACKLE_SKIN))
            SCDataComponents.set(resultRod, SCDataComponents.TACKLE_SKIN, tackleSkin);

        SCDataComponents.set(resultRod, SCDataComponents.MINIGAME_MODIFIERS, minigameModifiers);
        SCDataComponents.set(resultRod, SCDataComponents.CATCH_MODIFIERS, catchModifiers);
        SCDataComponents.set(resultRod, SCDataComponents.NETHERITE_UPGRADE, true);
        return resultRod;
    }

    @Override
    public boolean isTemplateIngredient(ItemStack stack)
    {
        return this.template.test(stack);
    }

    @Override
    public boolean isBaseIngredient(ItemStack stack)
    {
        return this.base.test(stack);
    }

    @Override
    public boolean isAdditionIngredient(ItemStack stack)
    {
        return this.addition.test(stack);
    }

    @Override
    public ResourceLocation getId()
    {
        return this.id;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries)
    {
        ItemStack itemstack = new ItemStack(SCItems.ROD.get());
        SCDataComponents.set(itemstack, SCDataComponents.NETHERITE_UPGRADE, true);
        return itemstack;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return SCRecipes.FISHING_ROD_SMITHING.get();
    }

    @Override
    public RecipeType<?> getType()
    {
        return RecipeType.SMITHING;
    }

    @Override
    public boolean isIncomplete()
    {
        return Stream.of(this.template, this.base, this.addition).anyMatch(Ingredient::hasNoItems);
    }

    public static class Serializer implements RecipeSerializer<NetheriteUpgradeSmithingRecipe>
    {
        public static final MapCodec<Function<ResourceLocation, NetheriteUpgradeSmithingRecipe>> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                BackportCodecs.IngredientCodecs.CODEC.fieldOf("template").forGetter((o) -> o.apply(null).template()),
                BackportCodecs.IngredientCodecs.CODEC.fieldOf("base").forGetter((o) -> o.apply(null).base()),
                BackportCodecs.IngredientCodecs.CODEC.fieldOf("addition").forGetter((o) -> o.apply(null).addition())
        ).apply(instance, (template, base, addition) -> id -> new NetheriteUpgradeSmithingRecipe(id, template, base, addition)));

        @Override
        public NetheriteUpgradeSmithingRecipe fromJson(ResourceLocation id, JsonObject json)
        {
            return CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow(false, s -> {}).apply(id);
        }

        @Override
        public NetheriteUpgradeSmithingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer)
        {
            Ingredient template = ByteBufCodecs.INGREDIENT.decode(buffer);
            Ingredient base = ByteBufCodecs.INGREDIENT.decode(buffer);
            Ingredient addition = ByteBufCodecs.INGREDIENT.decode(buffer);
            return new NetheriteUpgradeSmithingRecipe(id, template, base, addition);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, NetheriteUpgradeSmithingRecipe recipe)
        {
            ByteBufCodecs.INGREDIENT.encode(buffer, recipe.template);
            ByteBufCodecs.INGREDIENT.encode(buffer, recipe.base);
            ByteBufCodecs.INGREDIENT.encode(buffer, recipe.addition);
        }
    }
}
