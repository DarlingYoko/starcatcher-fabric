package net.nikdo53.neobackports.utils;

import net.minecraft.util.FastColor;

/**
 * Fabric shim for NeoBackports' {@code FastColorNeo} — see FABRIC_PORT_PLAN.md §5.8.
 * Vanilla 1.20.1 already has {@link FastColor.ARGB32} with red/green/blue/alpha and a
 * 4-arg {@code color(a, r, g, b)}; the only addition NeoBackports makes is a 2-arg
 * {@code color(alpha, rgb)} that stamps an alpha byte onto an existing packed color.
 */
public class FastColorNeo
{
    public static class ARGB32
    {
        public static int color(int alpha, int rgb)
        {
            return (alpha << 24) | (rgb & 0xFFFFFF);
        }

        public static int alpha(int color)
        {
            return FastColor.ARGB32.alpha(color);
        }

        public static int red(int color)
        {
            return FastColor.ARGB32.red(color);
        }

        public static int green(int color)
        {
            return FastColor.ARGB32.green(color);
        }

        public static int blue(int color)
        {
            return FastColor.ARGB32.blue(color);
        }
    }
}
