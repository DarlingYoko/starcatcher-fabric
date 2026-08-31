package net.nikdo53.neobackports.screen;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Fabric shim for NeoBackports' {@code LayeredDraw} — see FABRIC_PORT_PLAN.md §5.8.
 * Vanilla 1.20.1 has no layered-HUD system yet (added upstream in 1.20.5+); only the
 * {@code Layer} shape is needed here so the mod's own overlay classes compile. Actually
 * hooking layers into HUD rendering (via Fabric API's HudRenderCallback, replacing the
 * Forge RegisterGuiOverlaysEvent call site) is §6 (Events) work, not done here.
 */
public class LayeredDraw
{
    @FunctionalInterface
    public interface Layer
    {
        void render(GuiGraphics guiGraphics, float deltaTracker);
    }
}
