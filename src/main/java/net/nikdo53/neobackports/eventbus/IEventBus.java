package net.nikdo53.neobackports.eventbus;

import java.util.function.Consumer;

/**
 * Fabric shim. On Forge this is a real pub/sub bus with staged FML lifecycle events
 * (FMLCommonSetupEvent, FMLClientSetupEvent, ...); Fabric's ModInitializer has no such
 * staging. {@code DeferredRegisterTyped.register(IEventBus)} ignores the bus entirely and
 * flushes synchronously (see FABRIC_PORT_PLAN.md §5.1) — callers just need to invoke the
 * various {@code SCXxx.register(bus)} methods in dependency order from StarcatcherFabric.
 * A single shared instance is passed through since call sites already expect the parameter.
 * {@link #addListener} is a deliberate no-op: any remaining lifecycle-event listener
 * (there is exactly one, in SCCriterionTriggers) is rewritten to call its logic directly
 * instead — see FABRIC_PORT_PLAN.md §6.
 * <p>
 * Deliberately NOT in the real {@code net.minecraftforge.eventbus.api} package: several
 * common mods (e.g. CreativeCore) jar-in-jar the actual upstream {@code eventbus} library,
 * which ships its own {@code net.minecraftforge.eventbus.api.IEventBus} (a plain interface,
 * no {@code INSTANCE} field). Fabric Loader puts jar-in-jar'd classes on the same shared mod
 * classpath, so whichever same-named class loads first wins — this collided in practice and
 * crashed dedicated-server startup with {@code NoSuchFieldError: IEventBus.INSTANCE}.
 */
public class IEventBus
{
    public static final IEventBus INSTANCE = new IEventBus();

    public <T> void addListener(Consumer<T> listener)
    {
    }
}
