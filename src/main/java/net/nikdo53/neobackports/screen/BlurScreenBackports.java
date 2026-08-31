package net.nikdo53.neobackports.screen;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Fabric shim for NeoBackports' {@code BlurScreenBackports} — see FABRIC_PORT_PLAN.md §5.8.
 * Dead code in practice: {@link BlurShaderLoader#shouldCancelBackground} always returns
 * {@code false}, so call sites never actually reach this. Kept as a no-op stub purely so
 * those call sites compile.
 */
public class BlurScreenBackports
{
    public static void renderBlurOrPanorama(GuiGraphics guiGraphics, int x, int y, int width, int height)
    {
    }
}
