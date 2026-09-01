package com.wdiscute.starcatcher.recipe;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.wdiscute.starcatcher.io.SCDataComponents;
import com.wdiscute.starcatcher.registry.SCRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.RecipeMatcher;
import net.nikdo53.neobackports.io.utils.BackportCodecs;
import net.nikdo53.neobackports.io.utils.ByteBufCodecs;

public class BottledLetterRecipe implements CraftingRecipe
{
    private ResourceLocation id;
    final String group;
    final CraftingBookCategory category;
    final ItemStack result;
    final NonNullList<Ingredient> ingredients;
    private final boolean isSimple;

    public BottledLetterRecipe(String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients)
    {
        this.group = group;
        this.category = category;
        this.result = result;
        this.ingredients = ingredients;
        //Ingredient.isSimple() doesn't exist in 1.20.1 — vanilla Ingredient here has no complex/compound
        //variant to distinguish (that's a later NeoForge custom-ingredient concept), so every ingredient
        //is effectively "simple", matching how real vanilla ShapelessRecipe behaves in this version.
        this.isSimple = true;
    }

    @Override
    public ResourceLocation getId()
    {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return SCRecipes.BOTTLED_LETTER.get();
    }

    @Override
    public String getGroup()
    {
        return this.group;
    }


    @Override
    public CraftingBookCategory category()
    {
        return this.category;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries)
    {
        return this.result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients()
    {
        return this.ingredients;
    }

    public boolean matches(CraftingContainer container, Level level)
    {
        var nonEmptyItems = new java.util.ArrayList<ItemStack>();
        StackedContents stackedContents = new StackedContents();
        int nonEmptyCount = 0;
        for (int i = 0; i < container.getContainerSize(); i++)
        {
            ItemStack item = container.getItem(i);
            if (!item.isEmpty())
            {
                nonEmptyCount++;
                if (isSimple)
                    stackedContents.accountStack(item, 1);
                else
                    nonEmptyItems.add(item);
            }
        }

        if (nonEmptyCount != this.ingredients.size())
            return false;

        return isSimple
                ? stackedContents.canCraft(this, null)
                : RecipeMatcher.findMatches(nonEmptyItems, this.ingredients) != null;
    }

    public ItemStack assemble(CraftingContainer container, RegistryAccess registries)
    {
        ItemStack is = this.result.copy();
        for (int i = 0; i < container.getContainerSize(); i++)
        {
            if(SCDataComponents.has(container.getItem(i), SCDataComponents.MESSAGE))
            {
                SCDataComponents.set(is, SCDataComponents.MESSAGE, SCDataComponents.get(container.getItem(i), SCDataComponents.MESSAGE));
                break;
            }
        }
        return is;
    }

    /**
     * Used to determine if this recipe can fit in a grid of the given width/height
     */
    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return width * height >= this.ingredients.size();
    }

    public static class Serializer implements RecipeSerializer<BottledLetterRecipe>
    {
        private static final MapCodec<BottledLetterRecipe> CODEC = RecordCodecBuilder.mapCodec(
                p_340779_ -> p_340779_.group(
                                Codec.STRING.optionalFieldOf("group", "").forGetter(p_301127_ -> p_301127_.group),
                                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(p_301133_ -> p_301133_.category),
                                BackportCodecs.ITEM_STACK_RECIPE.fieldOf("result").forGetter(p_301142_ -> p_301142_.result),
                                BackportCodecs.IngredientCodecs.CODEC
                                        .listOf()
                                        .fieldOf("ingredients")
                                        .flatXmap(
                                                p_301021_ ->
                                                {
                                                    Ingredient[] aingredient = p_301021_.toArray(Ingredient[]::new);
                                                    if (aingredient.length == 0)
                                                    {
                                                        return DataResult.error(() -> "No ingredients for shapeless recipe");
                                                    }
                                                    else
                                                    {
                                                        return aingredient.length > 3 * 3
                                                                ? DataResult.error(() -> "Too many ingredients for shapeless recipe. The maximum is: %s".formatted(3 * 3))
                                                                : DataResult.success(NonNullList.of(Ingredient.EMPTY, aingredient));
                                                    }
                                                },
                                                DataResult::success
                                        )
                                        .forGetter(p_300975_ -> p_300975_.ingredients)
                        )
                        .apply(p_340779_, BottledLetterRecipe::new)
        );

        @Override
        public BottledLetterRecipe fromJson(ResourceLocation id, JsonObject json)
        {
            BottledLetterRecipe recipe = CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow(false, s -> {});
            recipe.id = id;
            return recipe;
        }

        @Override
        public BottledLetterRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer)
        {
            BottledLetterRecipe recipe = fromNetwork1(buffer);
            recipe.id = id;
            return recipe;
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, BottledLetterRecipe recipe)
        {
            toNetwork1(buffer, recipe);
        }

        private static BottledLetterRecipe fromNetwork1(FriendlyByteBuf buffer)
        {
            String s = buffer.readUtf();
            CraftingBookCategory craftingbookcategory = buffer.readEnum(CraftingBookCategory.class);
            int i = buffer.readVarInt();
            NonNullList<Ingredient> nonnulllist = NonNullList.withSize(i, Ingredient.EMPTY);
            nonnulllist.replaceAll(p_319735_ -> ByteBufCodecs.INGREDIENT.decode(buffer));
            ItemStack itemstack = ByteBufCodecs.ITEM_STACK.decode(buffer);
            return new BottledLetterRecipe(s, craftingbookcategory, itemstack, nonnulllist);
        }

        private static void toNetwork1(FriendlyByteBuf buffer, BottledLetterRecipe recipe)
        {
            buffer.writeUtf(recipe.group);
            buffer.writeEnum(recipe.category);
            buffer.writeVarInt(recipe.ingredients.size());

            for (Ingredient ingredient : recipe.ingredients)
            {
                ByteBufCodecs.INGREDIENT.encode(buffer, ingredient);
            }

            ByteBufCodecs.ITEM_STACK.encode(buffer, recipe.result);
        }
    }
}
