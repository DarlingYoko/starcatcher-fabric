package com.wdiscute.starcatcher.recipe;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wdiscute.starcatcher.io.SCDataComponents;
import com.wdiscute.starcatcher.registry.SCDataMaps;
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
import java.util.stream.Stream;

public class FishingRodSkinSmithingRecipe implements SmithingRecipe
{
    private ResourceLocation id;
    public final Ingredient template;
    public final Ingredient base;
    public final Ingredient addition;
    public final ItemStack result;

    public FishingRodSkinSmithingRecipe(Ingredient template, Ingredient base, Ingredient addition, ItemStack result)
    {
        this.template = template;
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    @Override
    public ResourceLocation getId()
    {
        return this.id;
    }

    public boolean matches(Container input, Level level)
    {
        return this.template.test(input.getItem(0)) && this.base.test(input.getItem(1)) && this.addition.test(input.getItem(2));
    }

    public ItemStack assemble(Container input, RegistryAccess registries)
    {
        ItemStack resultRod = input.getItem(1).transmuteCopy(this.result.getItem(), this.result.getCount());

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
    public ItemStack getResultItem(RegistryAccess registries)
    {
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return SCRecipes.FISHING_ROD_SKIN_SMITHING.get();
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

    public static class Serializer implements RecipeSerializer<FishingRodSkinSmithingRecipe>
    {
        public static final MapCodec<FishingRodSkinSmithingRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
                BackportCodecs.IngredientCodecs.CODEC.fieldOf("template").forGetter((o) -> o.template),
                BackportCodecs.IngredientCodecs.CODEC.fieldOf("base").forGetter((o) -> o.base),
                BackportCodecs.IngredientCodecs.CODEC.fieldOf("addition").forGetter((o) -> o.addition),
                BackportCodecs.ITEM_STACK_RECIPE.fieldOf("result").forGetter(o -> o.result)
        ).apply(instance, FishingRodSkinSmithingRecipe::new));

        @Override
        public FishingRodSkinSmithingRecipe fromJson(ResourceLocation id, JsonObject json)
        {
            FishingRodSkinSmithingRecipe recipe = CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow(false, s -> {});
            recipe.id = id;
            return recipe;
        }

        @Override
        public FishingRodSkinSmithingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer)
        {
            FishingRodSkinSmithingRecipe recipe = fromNetwork(buffer);
            recipe.id = id;
            return recipe;
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, FishingRodSkinSmithingRecipe recipe)
        {
            toNetworkA(buffer, recipe);
        }

        private static FishingRodSkinSmithingRecipe fromNetwork(FriendlyByteBuf buffer)
        {
            Ingredient template = ByteBufCodecs.INGREDIENT.decode(buffer);
            Ingredient base = ByteBufCodecs.INGREDIENT.decode(buffer);
            Ingredient addition = ByteBufCodecs.INGREDIENT.decode(buffer);
            ItemStack result = ByteBufCodecs.ITEM_STACK.decode(buffer);
            return new FishingRodSkinSmithingRecipe(template, base, addition, result);
        }

        private static void toNetworkA(FriendlyByteBuf buffer, FishingRodSkinSmithingRecipe recipe)
        {
            ByteBufCodecs.INGREDIENT.encode(buffer, recipe.template);
            ByteBufCodecs.INGREDIENT.encode(buffer, recipe.base);
            ByteBufCodecs.INGREDIENT.encode(buffer, recipe.addition);
            ByteBufCodecs.ITEM_STACK.encode(buffer, recipe.result);
        }
    }
}
