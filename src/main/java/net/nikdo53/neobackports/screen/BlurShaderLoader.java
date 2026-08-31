package net.nikdo53.neobackports.screen;

/**
 * Fabric shim for NeoBackports' {@code BlurShaderLoader} — see FABRIC_PORT_PLAN.md §5.8.
 * The real NeoForge/NeoBackports version renders a blurred-background/panorama effect
 * behind menu screens (a later-version option not present in vanilla 1.20.1). Rather than
 * porting the blur shader pipeline, this always declines it — screens fall through to
 * their normal {@code renderBackground}, matching stock 1.20.1 behaviour.
 */
public class BlurShaderLoader
{
    public static boolean shouldCancelBackground(boolean menuBackgroundBlurriness)
    {
        return false;
    }
}
