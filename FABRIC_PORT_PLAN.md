# Starcatcher — Forge 1.20.1 → Fabric 1.20.1 Backport Plan

Status: **P0 (scaffolding) done** — see §13. In progress on branch `fabric-port-1.20.1`.
Target: Minecraft **1.20.1**, loader **Fabric**
Source branch: `v2.3-1.20.1` (Forge, MinecraftForge 47.4.0, LegacyForge ModDev)
Scope: 335 Java files, 719 resource files, 9 lang files, 13 datagen providers, 9 network payloads, 9 screen/menu files.

---

## 0. The central problem: this is not a "plain" Forge mod

Although it runs on Forge 1.20.1, the code is written almost entirely against a **NeoForge-1.21-style API** that is emulated on Forge by the **NeoBackports** library (`net.nikdo53.neobackports.*`, 66+ import sites). Fabric has **no NeoBackports**. So the port is really two migrations at once:

1. Forge loader/runtime → Fabric loader/runtime (metadata, entrypoints, events, registries).
2. NeoBackports' emulated modern APIs (DeferredHolder, StreamCodec/ByteBufCodecs, **DataComponents**, CustomPacketPayload/IPayloadContext, **DataAttachment/AttachmentType**, data maps, custom registries) → Fabric-native or Fabric-shimmed equivalents.

**Strategic decision (recommended):** build a thin **Fabric compatibility shim** package (`com.wdiscute.starcatcher.fabricshim`, or reuse existing package names) that reproduces the *subset* of the NeoBackports/Forge API the mod actually calls, backed by Fabric APIs + a handful of libraries. This keeps most of the 335 call sites unchanged and concentrates the risk. A minority of subsystems (custom registries, data maps, capabilities, loot modifiers, GLM, config, curios) must be genuinely rewritten because no drop-in Fabric analogue exists.

Alternative (not recommended): rewrite every call site to native Fabric APIs. Far more churn across 335 files.

---

## 1. Pre-flight decisions (resolve BEFORE coding)

These gate the whole effort. Several are external-dependency availability questions.

| # | Decision | Options | Notes / recommendation |
|---|----------|---------|------------------------|
| D1 | **Data Components** (1.21 concept, used via NeoBackports on NBT) | (a) port NeoBackports' NBT-backed component impl into a shim; (b) rewrite all component reads/writes to raw NBT | Components are pervasive (`SCDataComponents`, ItemContainerContents, tackle/skin/modifier data on stacks). Recommend (a): a small NBT-backed `DataComponentType`/`get`/`set` shim. |
| D2 | **Attachments** (capability-backed entity/player data, sync + copy-on-death) | Cardinal Components API (Fabric) | 3 attachments: FishingBob (entity+player, synced), FishingGuide (player, serialized+synced+copyOnDeath), TackleSkin (entity, serialized+synced). Cardinal Components maps cleanly. |
| D3 | **Config** (`ForgeConfigSpec`, client + server specs) | **Forge Config API Port (Fabric)** by Fuzs | Provides `ForgeConfigSpec` verbatim on Fabric 1.20.1 → `SCConfig.java` needs almost no change. Recommended for Starcatcher to avoid rewriting `SCConfig`. Note: wd's own Fabric Selling-Bin instead uses **Cloth Config** (§7bis.3) — a valid alternative, but it would require rewriting `SCConfig` from the `ForgeConfigSpec` builder. |
| D4 | **Networking** (CustomPacketPayload/IPayloadContext/PayloadRegistrar) | Fabric Networking API v1 shim | 1.20.1 Fabric has channel+PacketByteBuf networking, not 1.20.5 payloads. Build a payload shim over `Server/ClientPlayNetworking`. 9 payloads. |
| D5 | **Curios compat** | **Trinkets** (or Accessories) for Fabric | Curios has no 1.20.1 Fabric build. `compat/curios/*` (4 files: hat item, renderer, compat, events) must be rewritten against Trinkets API, or gated off. |
| D6 | **Companion mods bundled/depended** — Fabric 1.20.1 status **resolved**, see §7bis | libtooltips, Ember's Text API, wds-selling-bin, tiny-multiblock-lib | tiny-multiblock-lib ✅ Fabric drop-in; Ember's Text API ✅ Fabric 1.20.1 → reimplement libtooltips facade on it; **wds-selling-bin → DECIDED: downport wd's own `1.21.1-fabric` branch to `1.20.1-fabric`** (native rewrite, same package, also a template). |
| D7 | **tiny-multiblock-lib** (aquarium + stand + telescope + trophy multiblocks) | **Use official Fabric build** — RESOLVED | Fabric 1.20.1 build exists by **Nikdo53** (same author, same `net.nikdo53.tinymultiblocklib` package) → expected **drop-in**. Just swap the Modrinth coordinate to the Fabric artifact; verify API parity of `AbstractMultiBlock`/`IMultiBlock`/`IPreviewableMultiblock`/`AbstractMultiBlockEntity`/`IBlockPosOffsetEnum`. |
| D8 | **Mappings** | Mojmap + Parchment via Fabric Loom | Code uses Mojang names already → use `loom` with official mappings layered with Parchment `2023.09.03`. Avoids Yarn renames. |
| D9 | **Selling bin / seasons / JEI / EMI / FTB / Iris** compat | swap to Fabric artifacts, gate by `FabricLoader.isModLoaded` | JEI & EMI have Fabric builds with near-identical APIs. Serene Seasons/Ecliptic/TFC/FTB/Iris(→Iris) all Fabric-available. |

> Action: confirm D1–D7 (especially **D6/D7** — external availability) with the user before implementation, since they can change scope from "large" to "blocked."

---

## 2. Build system & project scaffolding — ✅ DONE (2026-08-31, branch `fabric-port-1.20.1`)

Replaced the LegacyForge ModDev toolchain with **Fabric Loom**.

- [x] `settings.gradle`: added `maven.fabricmc.net` to `pluginManagement`; bumped `foojay-resolver-convention` **0.9.0 → 1.0.0** (0.9.0 references `JvmVendorSpec.IBM_SEMERU`, removed in Gradle 9.x → hard `compileJava` failure; see gotcha below).
- [x] `build.gradle`: replaced `net.neoforged.moddev.legacyforge` with **`net.fabricmc.fabric-loom-remap`** (Loom's plugin id as of 2026 — not the old `fabric-loom` id; confirmed against FabricMC's own live `fabric-example-mod` 1.20.1 branch). Removed `legacyForge {…}`, `obfuscation`/`reobfJar`/`generateModMetadata`.
- [x] Dependencies (all coordinates verified resolvable via `./gradlew dependencies --configuration modImplementation`):
  - `minecraft "com.mojang:minecraft:1.20.1"`, `mappings loom.layered { officialMojangMappings(); parchment("org.parchmentmc.data:parchment-1.20.1:2023.09.03@zip") }`.
  - `fabric-loader:0.19.5`, `fabric-api:0.92.11+1.20.1`.
  - Cardinal Components `5.2.3` (D2), Forge Config API Port `v8.0.3-1.20.1-Fabric` (D3, via `maven.modrinth:forge-config-api-port:…`), Trinkets `3.7.2` (D5), tiny-multiblock-lib `fabric-1.20.1-3.2.0` bundled via `include(...)` (§7bis.2), mixinextras-fabric `0.5.5`.
  - Compat-module libs (JEI/EMI/Seasons/Curios-era/FTB/TFC/Oculus/quality-food/relics) deliberately **deferred to P7** (§8) to keep P0 minimal — repos for them already present from the Forge build.gradle.
- [x] Repositories: FabricMC, Parchment, Ladysnake (Cardinal), Modrinth maven (covers Trinkets/Forge Config API Port/tiny-multiblock-lib), TerraformersMC, BlameJared (JEI), Tyson the Ember (for the future libtooltips facade, §7bis.1), cursemaven.
- [x] `gradle.properties`: retained `mod_*` props; replaced `forge_version*`/`loader_version_range` with `loader_version`/`loom_version`/`fabric_api_version` + library version props; dropped `neobackports_version`.
- [x] Mixin: removed the manual `mixin { add … config … }` block and jar `MixinConfigs` manifest attribute (Loom auto-handles refmap). Split into `starcatcher.mixins.json` (common: `GetNameMixin`, `LecternReturnMixin`, `RemoveFishSizeAndWeightWhenStacking`) + `starcatcher.client.mixins.json` (`RenderTypeHelperMixin`), referenced from `fabric.mod.json`'s `mixins` array with `"environment": "client"` — matches current Fabric convention (no `refmap`/`minVersion` keys needed).
- [x] Kept `withSourcesJar`; dropped `withJavadocJar` (not part of the standard Fabric template, low value). Dropped `jarJar`; tiny-multiblock-lib now bundled via Loom `include(...)`.
- [x] **Gradle wrapper bumped 9.2.1 → 9.7.1** (needed for the current Loom snapshot's `plugin.api-version` requirement).

**Toolchain gotchas hit & fixed (record for next environment refresh):**
1. `org.gradle.toolchains.foojay-resolver-convention:0.9.0` → **`compileJava` fails** with `JvmVendorSpec does not have member field 'IBM_SEMERU'` on Gradle 9.x (field removed). Fix: bump to `1.0.0`+.
2. Loom's run-config DSL uses `vmArg`/`programArg`, **not** `systemProperty`/`programArgs` (those are LegacyForge ModDev names).
3. **Datamaps Refabricated is not a published dependency** — see §5.6, must be vendored from source.

Deliverable **met**: `./gradlew tasks` and `./gradlew dependencies --configuration modImplementation` both succeed (all P0 dependency coordinates resolve). `./gradlew compileJava` fails with exactly the expected ~100+ "package net.minecraftforge.* / net.nikdo53.neobackports.* does not exist" errors from the untouched 335 Forge-API source files (P1+ work) — the two new entrypoint stub files compile cleanly with zero errors, confirming the Loom scaffolding itself is sound.

---

## 3. Mod metadata & manifest — ✅ DONE (2026-08-31)

- [x] Deleted `src/main/templates/META-INF/mods.toml` (and the empty `templates/` dir) and the `generateModMetadata` task; created `src/main/resources/fabric.mod.json`:
  - `id`, `version` (`${version}` via `processResources` `filesMatching("fabric.mod.json") { expand(...) }`), `name`, `description` (full credits ported verbatim from `mods.toml`), `authors`, `license`, `icon`.
  - `environment: "*"`.
  - `entrypoints`: `main` → `com.wdiscute.starcatcher.StarcatcherFabric`, `client` → `…StarcatcherFabricClient` (both new empty stubs, §4). `fabric-datagen`/`emi`/`jei` entrypoints deferred to P6/P7 when those subsystems are actually wired in.
  - `mixins`: `starcatcher.mixins.json` + `{ "config": "starcatcher.client.mixins.json", "environment": "client" }`.
  - `accessWidener`: `starcatcher.accesswidener`.
  - `depends`: kept to only what's certain right now — `fabricloader >=0.19.5`, `fabric-api`, `minecraft ~1.20.1`, `java >=17`. **TODO (P1+):** add exact mod-id entries for `cardinal-components-base`/`trinkets`/`forgeconfigapiport` once those libs are actually wired into code and their real declared ids are confirmed at runtime (didn't want to guess wrong ids and hard-fail loading before they're even used).
- [x] **Access Transformer → Access Widener** (`src/main/resources/starcatcher.accesswidener`, `accessWidener v2 named`). Translated all 6 AT entries using the Mojmap names already recorded as trailing comments in the original `accesstransformer.cfg` (`Gui.overlayMessageTime`, `ShapedRecipe.result`, `ShapelessRecipe.result`, `RegistriesDatapackGenerator.dumpValue`, `RegistryOps.<init>`, `RegistryOps.lookupProvider`) — field/method descriptors written out explicitly. Original `accesstransformer.cfg` left in place (unreferenced by the build) as a historical cross-reference. Datagen-only entries can be pruned later if datagen is rewritten (§9).

---

## 4. Core loader glue (entrypoints & registry bootstrap)

- [x] **Stub only (P0 scope)**: `StarcatcherFabric implements ModInitializer` and `StarcatcherFabricClient implements ClientModInitializer` created as empty entrypoints (logger init only) so the mod jar/manifest is structurally valid. Both compile cleanly against pure Fabric API (no dependency on the still-Forge `Starcatcher.java`).
- [ ] **P1+**: Wire the real subsystem `register()` calls into `StarcatcherFabric.onInitialize()` (Items, Blocks, BlockEntities, DataComponents, Sounds, Entities, Particles, Recipes, MenuTypes, DataAttachments, custom registries, criterion triggers, processors, loot modifiers, data maps, payloads, creative tabs) — replaces the `@Mod`-annotated constructor. On Fabric there is no `IEventBus` — the shim's `DeferredRegister.register(bus)` becomes `DeferredRegister.registerAll()` that flushes into vanilla registries directly.
- [ ] **P5**: Fold `SCClientEvents`/`SCClientForgeEvents` (renderers, layers, screens, particles, keymaps, item properties, color handlers, tooltip processors init from `Starcatcher.Client.init`) into `StarcatcherFabricClient.onInitializeClient()`.
- [ ] **P1+**: Strip `Starcatcher.java` down to a constants/util holder (MOD_ID, `rl()`, resource keys, toast helper) — remove the Forge constructor, `@Mod`, and all `net.minecraftforge`/NeoBackports imports (currently still Forge-shaped; this is the bulk of why `compileJava` fails right now).

---

## 5. The NeoBackports/Forge API shim layer

Build `com.wdiscute.starcatcher.fabricshim` (or shadow the existing package names so imports change minimally). Reproduce only what is used:

### 5.1 Registries & Deferred* (66 sites)
- [ ] `DeferredRegister`, `DeferredRegisterTyped(.Items)`, `DeferredHolder`, `DeferredItem`, `DeferredBlock` shim: collect `(name, supplier)` pairs; on `registerAll()` do `Registry.register(registry, rl, obj)`; the holder returns the created object and implements `Supplier`/`Holder` as the code expects (`.get()`, `.value()`, `.getKey()`).
- [ ] **Custom registries** (`FISH_RESTRICTIONS`, `MINIGAME_MODIFIERS`, `SWEET_SPOT_BEHAVIOUR`, `CATCH_MODIFIERS`, `TACKLE_SKIN`): replace `ForgeRegistryHelper.create(NewRegistryEvent, …)` with `FabricRegistryBuilder.createSimple(key).buildAndRegister()` at init; drop `IForgeRegistry` fields to `Registry<>`. Rewrite `SCModEvents.addRegistry`.
- [ ] `ForgeRegistries`/`NeoForgeRegistries` lookups → vanilla `BuiltInRegistries` / `Registries` keys.

### 5.2 Data Components (D1)
- [ ] Shim `DataComponentType<T>`, `DataComponents`, `ItemContainerContents`, and `SCDataComponents.get/set/remove` over **ItemStack NBT** (namespaced tag). Codec/StreamCodec-driven (de)serialization. Verify stacking/comparison semantics used by `RemoveFishSizeAndWeightWhenStacking` mixin.

### 5.3 Codecs
- [ ] Shim `StreamCodec`, `ByteBufCodecs`, `NeoForgeStreamCodecs`, `BackportCodecs`, `NBTCodecHelper` behavior over `PacketByteBuf`/`FriendlyByteBuf` + DataFixerUpper `Codec`. Most are thin functional wrappers → straightforward.

### 5.4 Networking (D4, 9 payloads)
- [ ] Shim `CustomPacketPayload`, `CustomPacketPayload.Type`, `IPayloadContext`, `PayloadRegistrar`, `PacketDistributorNeo` over Fabric Networking API v1:
  - server→client via `ServerPlayNetworking.send`, registered read on client via `ClientPlayNetworking.registerGlobalReceiver`.
  - client→server via `ClientPlayNetworking.send` + `ServerPlayNetworking.registerGlobalReceiver`.
  - `IPayloadContext.enqueueWork` → run on the client/server thread executor; `player()`/`level()` accessors.
  - Rewrite `SCModEvents.registerPayloads` to call the shim registrar at init (playToClient/playToServer preserved).
- [ ] Payloads themselves (`io/network/*`, `io/network/tournament/*`) keep their record + STREAM_CODEC; only the base interface/context types resolve to the shim.

### 5.5 Attachments (D2, Cardinal Components)
- [ ] Shim `AttachmentType`, `DataAttachment`, `DataAttachmentRegistry`, `AdvancedCapabilityType` over Cardinal Components:
  - Define CCA `Component` classes for FishingBob, FishingGuide, TackleSkin; register component keys with `EntityComponentInitializer` (attach to Player / non-living entities per `canAttachTo`).
  - Map `.sync(streamCodec)` → CCA auto-sync; `.serialize(codec)` → CCA `readFromNbt/writeToNbt`; `.copyOnDeath()` → CCA `RespawnCopyStrategy.ALWAYS_COPY`.
  - `SCDataAttachments.get/set/remove(Entity, type)` → CCA `KEY.get(entity)` getters/setters; drop `Capability`/`CapabilityManager`/`CapabilityToken`.
- [ ] Add `cardinal-components` entrypoints to `fabric.mod.json` (`cardinal-components-entity`).

### 5.6 Data Maps (`SCDataMaps`, `RegisterDataMapTypesEvent`)
- [ ] **Use Datamaps Refabricated** (`1.0.5`, [Nikdo53/DataMaps-Refabricated](https://github.com/Nikdo53/DataMaps-Refabricated), `net.nikdo53` namespace — same author as NeoBackports/tiny-multiblock-lib) — the Fabric analogue to NeoForge data maps, and the very library wd's own Fabric Selling-Bin uses (§7bis.3). **Not published to Modrinth/Maven** — confirmed 2026-08-31 (empty Modrinth search, no GitHub Releases, `has_downloads: false`); wd's own Selling-Bin build.gradle pulls it as a locally-built jar via `flatDir { dirs ".compat" }` + `modImplementation(include("local:datamaps-refabricated-1.0.5"))`. **Action:** clone the repo, build the jar ourselves (same as wd did), and vendor it the same way (`.libs`/`.compat` flatDir + `include(...)`) rather than a remote coordinate. It reproduces the NeoForge data-map API/JSON, so `SCDataMaps` (aquarium interaction, catch/minigame modifiers, tackle skin, treasure) ports with minimal change and its datagen (`DGSCDataMapsProvider`) can emit the same JSON. Register map types via the lib's registration hook in place of `RegisterDataMapTypesEvent`; rewrite `SCDataMaps.getOrDefault` against its lookup API.
- [ ] Fallback only if the lib proves insufficient: a datapack-driven reload listener via `ResourceManagerHelper.registerReloadListener`.
- [ ] The `ItemAttributeModifierEvent` handler (in `SCEvents`) that lazily copies data-map values onto stacks has **no Fabric event** → move this logic to where stacks are built / a tick or an ItemStack mixin, or resolve data-map values on demand in `SCDataComponents.get`.

### 5.7 Datapack registry (`FISH_REGISTRY_KEY`, FishProperties codec)
- [ ] Replace `DataPackRegistryEvent.NewRegistry` with Fabric **Dynamic Registries**: `DynamicRegistries.register(FISH_REGISTRY_KEY, FishProperties.CODEC)` (+ network codec) at init. Verify client sync of the dynamic registry.

### 5.8 Misc NeoBackports utils
- [ ] `FastColorNeo`, `CommonColorsNeo`, `ListReverser`, `TooltipContext`, `ItemInteractionResult`, `IMenuTypeExtension`, `LayeredDraw`, `BlurScreenBackports`/`BlurShaderLoader`, recipe shims (`RecipeHolder`, `*RecipeNeo`, `*RecipeInput`, `SmithingRecipeHolder`, `CraftingInput`) → provide small local equivalents (mostly vanilla 1.20.1 has near-analogues; recipe "holder"/"input" are 1.21 shapes to flatten back to 1.20.1 recipe signatures).

---

## 6. Events: Forge bus → Fabric callbacks (7 `@SubscribeEvent` files)

Rewrite `event/*` and the two client event classes:

| Forge event | Fabric replacement |
|---|---|
| `SpawnPlacementRegisterEvent` | `SpawnPlacements.register(...)` at init |
| `EntityAttributeCreationEvent` | `FabricDefaultAttributeRegistry.register(FISH, …)` |
| `AddPackFindersEvent` (2 built-in datapacks) | `ResourceManagerHelper.registerBuiltinResourcePack(id, container, ...)` |
| `RegisterCommandsEvent` | `CommandRegistrationCallback.EVENT` |
| `ServerStartedEvent` / `ServerStoppingEvent` | `ServerLifecycleEvents.SERVER_STARTED` / `SERVER_STOPPING` |
| `TickEvent.ServerTickEvent` (END) | `ServerTickEvents.END_SERVER_TICK` |
| `PlayerEvent.PlayerLoggedInEvent` | `ServerPlayConnectionEvents.JOIN` |
| `PlayerInteractEvent.RightClickBlock` (bonemeal→worms) | `UseBlockCallback.EVENT` |
| `ItemAttributeModifierEvent` | see §5.6 (no direct event) |
| `RegisterDataMapTypesEvent` | §5.6 custom loader |
| `RegisterPayloadHandlersEvent` | §5.4 shim registrar at init |
| `NewRegistryEvent` / `DataPackRegistryEvent` | §5.1 / §5.7 |
| Client `EntityRenderersEvent.RegisterRenderers` | `EntityRendererRegistry.register`, `BlockEntityRendererFactories.register` |
| `RegisterMenuScreensEvent` | `HandledScreens.register` (a.k.a. `MenuScreens`) |
| `EntityRenderersEvent.RegisterLayerDefinitions` | `EntityModelLayerRegistry.registerModelLayer` |
| Key mappings (`SCKeymappings`) | `KeyBindingHelper.registerKeyBinding` + `ClientTickEvents` |
| Particle providers (`SCParticles`) | `ParticleFactoryRegistry.getInstance().register` |
| Item properties (`SCItemProperties`) | `ItemProperties.register` in client init |
| Block/render layers, color handlers (`SCRenderTypes`, `SCColors`) | `BlockRenderLayerMap.INSTANCE.putBlock`, `ColorProviderRegistry` |
| `DistExecutor`/`@OnlyIn`/`Dist` (12 files) | `@Environment(EnvType.CLIENT)`, `FabricLoader.getEnvironmentType()`, or Porting Lib `EnvExecutor` |
| `ModList.get().isLoaded(id)` (14 files) | `FabricLoader.getInstance().isModLoaded(id)` (map mod-ids where they differ, e.g. `oculus`→`iris`) |

---

## 7. Creative tabs, loot modifiers, recipes, menus

- [ ] **Creative tabs** (`SCCreativeModeTabs`): build with `FabricItemGroup.builder()` + register to `Registries.CREATIVE_MODE_TAB`; populate via `ItemGroupEvents.modifyEntriesEvent` (and for adding to vanilla tabs).
- [ ] **Loot modifiers** (`SCLootModifiers`, `IGlobalLootModifier`, `DGSCLootModifiers`, `data/…/loot_modifiers`): Forge GLM has **no Fabric equivalent** → reimplement each modifier via `LootTableEvents.MODIFY` (add fishing/treasure drops by inspecting the table id and conditions). Delete `forge/loot_modifiers` JSON; port logic to code.
- [ ] **Menus** (`SCMenuTypes`, 9 screen/menu files): `MenuType` via `new MenuType<>(factory, FeatureFlags…)`; the `IMenuTypeExtension` (extended-data menu open with extra buf) → `ExtendedScreenHandlerType` (Fabric API). Screens open via `player.openMenu` → Fabric `screenHandler` opening with `ExtendedScreenHandlerFactory`.
- [ ] **Recipes** (`SCRecipes`, recipe/*): flatten NeoBackports `RecipeHolder`/`RecipeInput`/`SmithingRecipeNeo`/`CraftingRecipeNeo` back to vanilla 1.20.1 recipe signatures (`Container`, no `RecipeHolder`). Custom serializers register to `BuiltInRegistries.RECIPE_SERIALIZER`.
- [ ] **Inventories** (`ItemStackHandler`, `SlotItemHandler`, `SingleStackContainer`, `SingleStackContainer.java`): replace Forge `IItemHandler` with vanilla `Container`/`SimpleContainer` (or Fabric Transfer API if needed by tackle box block). 4 `ItemStackHandler` sites.

---

## 7bis. Companion-library resolution (investigated — findings)

Investigated the three Fabric analogs the author suggested. Verdicts:

### 7bis.1 `libtooltips` → **custom-tooltip-api does NOT fit; reimplement on Ember's Text API (Fabric)**
- Actual API the mod calls (small surface): `Tooltips.registerProcessor(name, (t,s,e)->Component)` ×9, `Tooltips.resolveTagsToComponentFromTranslationKey(key)` ×5, `Tooltips.resolveTagsToComponent(markdownString)` ×2, and `ExampleRGBEffect` (1 import in `Starcatcher.java`, appears unused → drop).
- `libtooltips` is wd's **thin wrapper around Ember's Text API** that: (a) registers named custom text-effect tag processors (`scgolden`, `sclegendary`, `scepic`, `scrare`, `scuncommon`, `sclava`, …), and (b) resolves an XML/markdown-tagged string (or translation key) into a styled `Component`.
- **`custom-tooltip-api` (Stalemated)** is a *different kind* of library — a fluent per-item tooltip **builder** (`CustomTooltipApi.builder(itemId).dynamicText(...).register()`, `registerPlaceholder`). It has **no** `registerProcessor` / `resolveTagsToComponent*` markdown-resolution API. ❌ Not a fit.
- **Ember's Text API has a Fabric 1.20.1 build** (v2.5.0–2.9.4, TysonTheEmber) with the same XML-style markup parser + effects (rainbow/gradient/etc.). ✅
- **Plan:** add Ember's Text API (Fabric 1.20.1) as a dependency and **reimplement the ~3-method `libtooltips` facade** (`Tooltips.registerProcessor` / `resolveTagsToComponent` / `resolveTagsToComponentFromTranslationKey`) in-repo (package `com.wdiscute.libtooltips`) delegating to Ember's Fabric API. Verify Ember's Fabric API exposes **custom effect/tag registration** for the mod's named gradients; if not, implement those gradients directly (the mod already has the gradient math in `tooltips/SCTooltipGradient`, `SCLegendary`). Confirm bundling is license-permitted (README says Ember jar-in-jar was approved for this mod).

### 7bis.2 `tiny-multiblock-lib` → **official Fabric build (drop-in)**
- Fabric 1.20.1 build exists by **Nikdo53**, same `net.nikdo53.tinymultiblocklib` package. Used by `blocks/aquarium`, `blocks/stand`, `blocks/Telescope`, `TrophyOfTheOlderAngler` (`AbstractMultiBlock`, `IMultiBlock`, `IPreviewableMultiblock`, `AbstractMultiBlockEntity`, `IBlockPosOffsetEnum`). ✅
- **Plan:** swap the build.gradle coordinate `maven.modrinth:tiny-multiblock-lib:forge-1.20.1-…` → the **fabric-1.20.1** artifact; `include(...)` if bundling. Verify the five types' signatures match (high confidence, same author). This **removes the earlier "reimplement multiblock" risk**.

### 7bis.3 `wds-selling-bin` → **DECIDED: downport wd's own Selling-Bin `1.21.1-fabric` → `1.20.1-fabric`** (sub-project)

**Reference found:** [wdiscute/Selling-Bin @ `1.21.1-fabric`](https://github.com/wdiscute/Selling-Bin/tree/1.21.1-fabric) is a **native Fabric rewrite** (~40 classes) in the **exact `com.wdiscute.sellingbin` package** Starcatcher consumes: `SellingBin`, `processors.AbstractProcessor` (+ `SBProcessors`, Food/Enchantment/Durability/QualityFoods/etc.), `registry.SBBlocks`/`SBDataMaps`/`SBMenuTypes`/`SBBlockEntities`/`SBItemPredicate`, `bin.*` (block/BE/menu/screen/slots), `jei.SellingBinJeiPlugin`, `emi.SellingBinEmiPlugin`, `event.SBvents`/`SBClientEvents`, `SBConfig`, `datagen.*`. This is a **bounded, well-scoped sub-project** and doubles as a **reference template** for Starcatcher's own port.

**wd's Fabric tooling choices in that repo (adopt for consistency where sensible):**
- **Fabric Loom** (`fabric-loom` / `-remap` variant — verify exact id), Fabric API.
- **Datamaps Refabricated** (`1.0.5`) for data maps — the Fabric analogue to NeoForge data maps. ⇒ **revises §5.6** (use this lib instead of a hand-rolled loader for Starcatcher's `SCDataMaps` too).
- **Cloth Config** for config (not Forge Config API Port).
- **tiny-multiblock-lib (Fabric)**, **Architectury API** (compile-only), JEI-fabric, EMI-fabric.
- **No** Cardinal Components, **no** Forge Config API Port, **no** NeoBackports (it's a from-scratch Fabric rewrite, not a shim port).

**Downport gap (1.21.1-fabric → 1.20.1-fabric):** the reference targets 1.21.1, where **data components are native**; at 1.20.1 they are not, so the selling-bin's component reads/writes must drop to NBT — the **same DataComponents shim from §5.2 can be shared** between this sub-project and Starcatcher. Also reconcile 1.21↔1.20 API deltas (enchantment/food/item-predicate APIs, `SBItemPredicate`, menu/slot signatures). Confirm the `event.SBvents` built-in-datapack path (Fabric uses `ResourceManagerHelper.registerBuiltinResourcePack`) so Starcatcher's `SCModEvents` pack-finder glue and `SCProcessors` line up with the Fabric selling-bin API (note the Forge branch's `SBevents.DefaultPackSource` may not exist verbatim on Fabric).

**Plan:** (1) fork/branch wd's Selling-Bin to `1.20.1-fabric`, downport using §5.2 shim + version deltas; (2) publish it (Modrinth maven or local `.libs`/`include`); (3) depend on it from Starcatcher and adapt `SCProcessors`/selling-bin glue to the Fabric API. Sequence this **before** Starcatcher's P7/P8 (or in parallel, since it's independent).

---

_(superseded — retained for history)_ Prior "no API-compatible Fabric build" concern:
- Actual API used (specific to **wd's** selling bin): `SellingBin.SELLING_BIN` (a custom `DeferredRegisterTyped` registry) + `SellingBin.rl` ×8, `processors.AbstractProcessor` ×13 (SCProcessors registers `FishProcessor` into it), `SBDataMaps.SELLING_BIN_VALUE`/`SELLING_BIN_CURRENCIES`/`ItemValue` ×7, `SBBlocks.SELLING_BIN`, `SBevents.DefaultPackSource` (built-in datapacks), `jei/StarcatcherJeiPlugin`/`emi` use `SellingBinJeiPlugin.SLOT` ×5, plus two built-in datapacks (`selling_bin_fishes`, `selling_bin_starcatcher_emeralds`).
- The suggested Fabric options are **not** API-compatible: **i5onad3/selling-bin-fabric** is **1.21.1-only**, a different author, GPLv3 independent fork, **different package** (not `com.wdiscute.sellingbin`), simplified block-bound design. The CurseForge "Selling Bin" is likewise not wd's `com.wdiscute.sellingbin` codebase. ❌ Neither is a drop-in.
- **Plan (pick one, confirm with author):**
  - **(a) Port wd's own `wds-selling-bin` to Fabric 1.20.1** (best fidelity; it's the author's mod and itself uses NeoBackports, so it rides the same shim). Preferred if the author will ship a Fabric selling-bin.
  - **(b) Feature-gate the entire selling-bin subsystem** behind `isModLoaded`: drop `SCProcessors`, the `sellingbin/` package, the JEI/EMI selling-bin recipe/slot code, the two built-in datapacks, and the `SBevents.DefaultPackSource` pack finders. Mod ships without selling support on Fabric.
  - **(c) Thin re-facade** of just the used surface against a Fabric selling-bin fork — **not viable** given the fork's different package/design.
- This is the **remaining hard blocker** (§14 R1).

## 8. Compatibility modules (`compat/*`, gated)

Each is `modCompileOnly` + `isModLoaded`-guarded, so port or gate individually:

- [ ] **JEI** (`compat/jei/*`, 4 files): swap to `mezz.jei:jei-1.20.1-fabric`; `@JeiPlugin` + API are largely loader-agnostic. Minor import/registration tweaks.
- [ ] **EMI** (`compat/emi/*`, 5 files): swap to `dev.emi:emi-fabric`; EMI API is loader-agnostic. Should port with minimal change.
- [ ] **Curios → Trinkets** (`compat/curios/*`, 4 files) (D5): rewrite `ICurioItem`/`ICurioRenderer`/`CuriosApi`/`SlotContext` against Trinkets API, or feature-gate the fisherman-hat cosmetic slot behind `isModLoaded("trinkets")`. Update `data/curios/tags` + `data/starcatcher/curios` to Trinkets slot JSON.
- [ ] **Serene Seasons** (`SereneSeasonsCompat`): Fabric build exists → swap artifact, gate.
- [ ] **Ecliptic Seasons** (`EclipticSeasonsCompat`): confirm Fabric build; gate.
- [ ] **TerraFirmaCraft** (`TerraFirmaCraftSeasonsCompat`): TFC is Forge-only historically → likely **gate off** on Fabric.
- [ ] **FTB Teams** (`FTBTeamsCompat`): FTB has Fabric builds → swap, gate.
- [ ] **Iris/Oculus** (`IrisShadersCompat`): on Fabric the mod id is `iris` (Oculus is the Forge port) → gate on `iris`.
- [ ] **Quality Food / Reliquified-Relics** (`QualityFoodCompat`, `ReliquifiedArtifactsCompat`): confirm Fabric availability; gate or drop.
- [ ] **Selling bin** (`sellingbin`/`SCProcessors`, built-in datapacks `selling_bin_*`, `data_maps`): see **§7bis.3** — no drop-in Fabric build; port wd's own selling-bin to Fabric **or** gate the whole subsystem (drop built-in datapacks + `SBevents.DefaultPackSource`, and the selling-bin JEI/EMI slot code).

---

## 9. Data generation (13 providers)

Forge datagen (`GatherDataEvent`, `ExistingFileHelper`, `DatapackBuiltinEntriesProvider`, `BlockTagsProvider`, `IGlobalLootModifier` provider) → **Fabric Data Generation API**:

- [ ] Replace `DataGenerators` (Forge `@SubscribeEvent GatherDataEvent`) with a `DataGeneratorEntrypoint` (`fabric-datagen` entrypoint) registering `FabricTagProvider`, `FabricModelProvider`, `FabricRecipeProvider`, `FabricBlockLootTableProvider`, `FabricDynamicRegistryProvider` (for FishProperties + biome modifiers), advancement provider.
- [ ] Rewrite each `DGSC*Provider` against the Fabric provider base classes; drop `ExistingFileHelper`.
- [ ] **Biome modifiers** (`DGSCBiomeModifierProvider`, `data/…/biome`): Forge biome modifiers → Fabric **Biome Modification API** (`BiomeModifications.create(...)` in code) rather than JSON, or keep as data via Fabric's dynamic registry if applicable.
- [ ] `DGSCDataMapsProvider` → emit the custom data-map JSON format defined in §5.6.
- [ ] `DGSCLootModifiers` → removed (logic moved to `LootTableEvents`, §7).
- [ ] Keep the already-generated `src/generated/resources` as a fallback, but regenerate to catch format differences (Forge vs Fabric loot/tag output).

Note: much of `src/generated/resources` (fish JSON, tags, models) is **loader-neutral data** and can be reused as-is; only Forge-namespaced files (`data/forge/**`, `data/neoforge/**`, `loot_modifiers`, `data_maps` in the Forge format) need conversion (`forge:` tags → `c:` conventional tags / vanilla; `neoforge:` → removed/reworked).

---

## 10. Mixins (4 mixins)

- [ ] `starcatcher.mixins.json` works on Fabric; ensure `package`, `compatibilityLevel: JAVA_17`, add `refmap`. Split `RenderTypeHelperMixin` (client) into a `client` array (already present).
- [ ] Verify each mixin target resolves under Mojmap+Parchment: `GetNameMixin`, `LecternReturnMixin`, `RemoveFishSizeAndWeightWhenStacking` (must match the NBT-backed component semantics from §5.2), `RenderTypeHelperMixin`.
- [ ] `overlayMessageTime` write (used by the toast helper) now via access widener instead of AT.

---

## 11. Resources & assets

- [ ] Assets (textures, models, blockstates, sounds, particles, shaders, sprites, lang×9) are **loader-neutral** — copy unchanged.
- [ ] `data/forge/**` and `data/neoforge/**` → convert to `c:`/vanilla tags or code (§9). Item/block tag files using `forge:` namespaces → `c:` conventional equivalents.
- [ ] `data/curios/**` → Trinkets slot data (§8, D5) or gate.
- [ ] Built-in datapacks (`built_in_datapacks/selling_bin_*`) → registered via Fabric builtin resource pack API (§6) or dropped with selling-bin gating (§8).
- [ ] `pack.mcmeta` present/valid for 1.20.1 pack format.

---

## 12. Build, run, verify

- [ ] `./gradlew build` compiles; `runClient`/`runServer`/`runDatagen` configs via Loom.
- [ ] Smoke test matrix: world load; fishing minigame start→complete→catch payload roundtrip; toast + overlay message; guide book screen; tackle box menu (extended screen data); aquarium multiblock place/preview (D7); tournament stand (saved data + sync payloads); creative tabs populated; config screen values; message-in-a-bottle set/read payloads.
- [ ] Attachment persistence: guide received flag copy-on-death/respawn; tackle skin on bobber synced to client; fishing-bob state.
- [ ] Data-driven: a dynamic FishProperties datapack entry loads and syncs to client.
- [ ] Each compat module: load with the corresponding Fabric mod present and absent (gating correctness).
- [ ] Loot: fishing/treasure drops from the reimplemented `LootTableEvents` modifiers.

---

## 13. Suggested phase ordering (each phase should compile before the next)

1. **P0 Scaffolding** — ✅ done (2026-08-31, branch `fabric-port-1.20.1`). §2 build, §3 metadata/AW, §4 empty entrypoints. Project imports on Fabric; `compileJava` fails only on the untouched Forge-API source files, as expected.
2. **P1 Shim core** — §5.1 registries/Deferred*, §5.3 codecs, §5.8 utils. Get `SCItems/SCBlocks/SCEntities/…` registering.
3. **P2 Components & stacks** — §5.2 (D1), §7 recipes/menus/inventories, relevant mixins (§10).
4. **P3 Networking & attachments** — §5.4 (D4), §5.5 (D2). Payload + attachment roundtrips.
5. **P4 Events & world systems** — §6, custom registries (§5.1), dynamic registry (§5.7), data maps (§5.6), loot (§7).
6. **P5 Client** — renderers, screens, layers, particles, keymaps, item props, colors, tooltips (§4/§6).
7. **P6 Datagen** — §9 (regenerate & diff data).
8. **P7 Compat** — §8 modules one by one, gated.
9. **P8 Companion-mod resolution** — §7bis: swap tiny-multiblock-lib (Fabric); reimplement libtooltips facade on Ember's Text API (Fabric); **downport wd's Selling-Bin `1.21.1-fabric`→`1.20.1-fabric` as a parallel sub-project** (own repo/branch, shares the §5.2 component shim), then depend on it. Start the selling-bin downport early — it is independent of Starcatcher P1–P6.
10. **P9 QA** — §12 full test matrix; fix mapping/behavior gaps.

---

## 14. Top risks / likely blockers

- **R1 (retired → now a scoped sub-task, see §7bis):** tiny-multiblock-lib ✅ Fabric drop-in; libtooltips ✅ facade on Ember's Text API (Fabric 1.20.1); **wds-selling-bin ✅ decided** — downport wd's own `1.21.1-fabric` branch to `1.20.1-fabric` (same `com.wdiscute.sellingbin` package; native Fabric rewrite that also serves as the port template). Residual risk is the **1.21.1→1.20.1 downport gap** (native data components → NBT shim, API deltas), tracked in §7bis.3 and shared with §5.2.
- **R2 (high):** Data Components (D1) are emulated on NBT by NeoBackports; the Fabric shim must exactly reproduce stacking/equality/serialization semantics or items will mis-stack and the `RemoveFishSizeAndWeightWhenStacking` mixin will misbehave.
- **R3 (med):** NeoForge **data maps** and the `ItemAttributeModifierEvent` lazy-copy pattern have no Fabric analogue → custom loader + relocation of the copy logic (§5.6).
- **R4 (med):** Global Loot Modifiers → hand-reimplement as `LootTableEvents.MODIFY`; parity of conditions/rolls needs care (§7).
- **R5 (med):** Curios→Trinkets slot/renderer semantics differ (D5); cosmetic hat behavior may change.
- **R6 (low/med):** Dynamic registry client-sync for `FishProperties` under Fabric; verify large registry syncs.
- **R7 (low):** Mapping mismatches in mixins/AW under Parchment; resolve per-target.

---

## 15. Out of scope / to confirm with author

- Whether to **bundle** (Loom `include`) the Fabric companion libs or hard-depend.
- Whether TFC/QualityFood/Relics/Ecliptic compat is **dropped** on Fabric if no Fabric build exists.
- Version string scheme (`…-FABRIC-1.20.1`) and separate branch (`v2.3-fabric-1.20.1`).
