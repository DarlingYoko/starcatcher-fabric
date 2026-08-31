package net.nikdo53.neobackports.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fabric shim for NeoBackports' {@code ListReverser} — see FABRIC_PORT_PLAN.md §5.8.
 */
public class ListReverser
{
    public static <T> List<T> reverse(List<T> list)
    {
        List<T> copy = new ArrayList<>(list);
        Collections.reverse(copy);
        return copy;
    }
}
