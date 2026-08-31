package net.minecraftforge.common.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Fabric shim for Forge's {@code RecipeMatcher} — see FABRIC_PORT_PLAN.md §5.8.
 * No vanilla or Fabric equivalent exists; this is a standard bipartite-matching
 * (Kuhn's algorithm) implementation rather than a port of Forge's exact bytecode —
 * behaviourally equivalent for the one thing callers check: whether every input can
 * be assigned to a distinct ingredient predicate.
 */
public class RecipeMatcher
{
    public static <T> int[] findMatches(List<T> inputs, List<? extends Predicate<T>> ingredients)
    {
        int size = inputs.size();
        if (size != ingredients.size())
            return null;

        int[] itemToIngredient = new int[size];
        Arrays.fill(itemToIngredient, -1);

        for (int i = 0; i < size; i++)
        {
            boolean[] visited = new boolean[size];
            if (!augment(i, inputs, ingredients, itemToIngredient, visited))
                return null;
        }

        int[] result = new int[size];
        for (int j = 0; j < size; j++)
            result[itemToIngredient[j]] = j;
        return result;
    }

    private static <T> boolean augment(int ingredientIndex, List<T> inputs, List<? extends Predicate<T>> ingredients,
                                        int[] itemToIngredient, boolean[] visited)
    {
        Predicate<T> predicate = ingredients.get(ingredientIndex);
        for (int j = 0; j < inputs.size(); j++)
        {
            if (visited[j] || !predicate.test(inputs.get(j)))
                continue;
            visited[j] = true;
            if (itemToIngredient[j] == -1 || augment(itemToIngredient[j], inputs, ingredients, itemToIngredient, visited))
            {
                itemToIngredient[j] = ingredientIndex;
                return true;
            }
        }
        return false;
    }
}
