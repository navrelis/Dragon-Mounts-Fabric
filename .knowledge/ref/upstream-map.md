# Dragon Mounts: Legacy - NeoForge 1.21.1 -> Fabric 1.21.1 porting map

Source repo: `scratchpad/upstream` (branch `neoforge`, HEAD `d055255`), upstream https://github.com/MWall541/Dragon-Mounts-Legacy, GPLv3, mod id `dragonmounts`, root package `com.github.kay9.dragonmounts` (abbreviated `dm.` below; all paths are relative to `src/main/java/com/github/kay9/dragonmounts/` unless stated). Mojang mappings + Parchment (`2024.11.17`), Java 21, NeoForge `21.1.1`, NeoGradle 7.0.145.

How this map was verified (so implementers can trust "vanilla vs NeoForge-only" claims):
- All 54 `.java` files were compiled with `javac` against the **vanilla Mojmap 1.21.1 merged jar** (from the Loom cache) + libraries. Every "cannot find symbol / does not override / has private access" error is a symbol that exists only because of NeoForge (patches, event API, or NeoForge's own access transformers). That list is section 5.
- Fabric API 0.116.17+1.21.1 jars (in the Gradle cache) were inspected with `javap` to confirm the replacement APIs named below actually exist, and their built-in access wideners were checked (section 3.10).
- NeoForge 21.1.219 sources jar was used to confirm what `EventHooks.canEntityGrief`, `EventHooks.onBlockPlace`, `ClientHooks.registerLayerDefinition`, `LootModifier` really do.

---------------------------------------------------------------------------------------------------

## 1. Overview

### 1.1 What the mod does (feature list)

- **Dragon entity** `dragonmounts:dragon` (`TameableDragon extends TamableAnimal implements Saddleable, FlyingAnimal, PlayerRideable, VariantHolder<Holder<DragonBreed>>`). One entity class; the "breed" is data, not a class.
- **Dragon breeds are a data-driven (datapack) registry** `dragonmounts:dragon_breeds`, 8 built in: `aether, end, fire, forest, ghost, ice, nether, water`. A breed defines colours, hatch particle, attribute overrides, abilities, egg habitats, damage-type immunities, ambient sound, growth time, hatch chance, size modifier, taming items, breeding items, reproduction limit. Addons add breeds with JSON only (see `example_addon/`).
- **Eggs**: block `dragonmounts:dragon_egg` (`HatchableEggBlock extends DragonEggBlock`, with a `HatchableEggBlockEntity` storing the breed) and its BlockItem (`HatchableEggBlock.Item`), carrying data component `dragonmounts:dragon_breed`. Hatch stages 0-3 (`hatch_stage` blockstate property) + `hatching` boolean + `waterlogged`. Right-click starts hatching; random ticks advance stages and eventually spawn a baby dragon.
- **Vanilla ender-dragon-egg override** (config `allow_egg_override`): right-clicking a vanilla `minecraft:dragon_egg` replaces it with a hatching `dragonmounts:end` egg. The `end` egg keeps the vanilla "teleport when punched/mined" behaviour until it is set to hatch.
- **Egg replenish** (config `replenish_eggs`): mixin lets the ender dragon drop a new egg after every kill, not only the first.
- **Habitats**: while hatching (stages 0-2, config `update_habitats`) an egg randomly samples its surroundings; each breed scores "habitat points" (biome tag, fluid nearby, nearby block tag, world height, light level, dragon-breath cloud, or a "picky" AND of several). Winning breed (>2 points) triggers a 200-tick transition that swaps the egg's breed (dust particles during transition).
- **Abilities** (per breed, data-driven, registry `dragonmounts:ability_type`): `frost_walker` (frosted ice under dragon, param `radius_multiplier`), `snow_stepper` (snow layers in cold biomes), `hot_feet` (burns plants in tag `hot_feet_burnables`), `green_toes` (bone-meal/flowers/mushrooms/grass), `reaper_step` (kills plants, dirt->soul soil, sand->soul sand), `hydro_step` (wets farmland/sponge, magma->blackstone, copper weathering, extinguish fire). Footprint abilities are gated by mob-griefing.
- **Taming/riding/flight**: tame with the breed's `taming_items` (default tag `#minecraft:fishes`, 1/5 chance per item), saddle (vanilla saddle), unsaddle with shears, sit via bone or sneak+use, heal with `#minecraft:meat`, ride (owner only). Flight is automatic: the dragon flies when more than 3 blocks (scaled by size) above ground; takes off by jumping while ridden. Vertical flight: jump = up, keybind Z = down, or camera pitch ("camera driven flight").
- **Growth**: baby (hatchling) -> juvenile -> adult via the age timer (`growth_time`, default 72000 ticks). Size, attributes (max health, attack) and step height scale with age. Hatchlings can't fly/be saddled/attack.
- **Breeding**: feed `breeding_items` (default fish) to two tamed adults -> an egg block is placed (hatching) with the breed of a random parent, or the **cross-breeding** table result (datapack `data/<ns>/dragonmounts/cross_breeding/*.json`). Reproduction limit per dragon (config for built-ins, `config:<breed>` string, or int in JSON). Parent custom names are mixed for the egg's name.
- **Loot**: optional chest-loot eggs (config `use_loot_tables`, default off) via NeoForge global loot modifiers (8 built in), with per-target chance config; plus a custom loot condition `dragonmounts:random_chance_by_config`. Breed `death_loot` field (loot table key) on this branch.
- **Spawn eggs** (`dragonmounts:spawn_egg`, one item, breed in components; tinted by breed colours) and **creative tab entries** (all breeds' spawn eggs in Spawn Eggs tab, all breeds' eggs in Functional Blocks tab; only populated when a client world exists).
- **Rendering**: custom procedural multi-part dragon model (`DragonModel` + `DragonAnimator`, ported from the original ata4 Dragon Mounts), per-breed textures (`body/glow/saddle`), per-breed model "properties" (tail scales/horns/thin legs) loaded from resource JSON, glow layer, death dissolve layer. Eggs rendered by a dynamic baked model that swaps per-breed block models by block-entity data.
- **Camera & controls**: auto switch to third person on mount/dismount (config), configurable third-person camera offsets while riding a dragon, hint message 60 ticks after mount, F6 toggles camera-driven flight, keybind Z = descend.
- **Config**: common + client TOML (section 3.6).
- **No commands, no custom networking/payloads, no attachments/capabilities, no JEI plugin, no recipes, no worldgen.** (JEI is only a `localRuntime` dev dependency in `build.gradle`.)
- Sounds: `entity.dragon.ambient|step|death|ambient.ghost` (4 `SoundEvent`s).
- Lang: en_us, ja_jp, uk_ua.

### 1.2 Entry points and lifecycle

There is only one `@Mod` class, no separate client entry class:

```
@Mod("dragonmounts")  dm.NeoModImpl(IEventBus modBus, ModContainer container)        [NeoModImpl.java:26-37]
 |-- DMLRegistry.init(modBus)           register every DeferredRegister (static initializer of DMLRegistry builds them)
 |-- container.registerConfig(COMMON, DMLConfig.COMMON_SPEC) / (CLIENT, DMLConfig.CLIENT_SPEC)
 '-- setupEvents(modBus)                 [NeoModImpl.java:39-68]
       NeoForge bus : RightClickBlock, AddReloadListenerEvent
                      (client only) ClientTickEvent.Post, ViewportEvent.ComputeCameraAngles, InputEvent.Key
       mod bus      : EntityAttributeCreationEvent, DataPackRegistryEvent.NewRegistry, NewRegistryEvent, RegisterEvent
                      (client only) ModelEvent.RegisterGeometryLoaders, BuildCreativeModeTabContentsEvent,
                      EntityRenderersEvent.RegisterRenderers, RegisterColorHandlersEvent.Item,
                      FMLConstructModEvent, RegisterKeyMappingsEvent
```

`dm.DragonMountsLegacy` is the loader-agnostic "event method" class: `MOD_ID`, `LOG`, `id(path)`, and one static method per event (`registerRenderers`, `registerEggModelLoader`, `registerItemColors`, `registerReloadListenersEarly`, `registerKeyBindings`, `registerReloadListeners`, `registerDatapacks`, `registerCreativeTabItems`, `registerEntityAttributes`, `registerOtherObjects`, `overrideVanillaDragonEgg`, `clientTick`, `onKeyPress`). `NeoModImpl` is a thin dispatcher that calls these via lambdas. (The class comment says mixins could also call into it.) `dm.DMLRegistry` holds every `Supplier<...>` for registered content. `dm.DMLConfig` holds both config specs.

Connection summary: `NeoModImpl` -> `DMLRegistry` (content), `DMLConfig` (config), `DragonMountsLegacy` (event logic) -> `client.*` (rendering/input/camera), `data.*` (reload listeners, loot), `dragon.*` (entity, breed, eggs, abilities, habitats, AI). `TameableDragon` calls `client.MountCameraManager/MountControlsMessenger/KeyMappings/DragonAnimator` directly (client classes referenced from the common entity; only executed on the client, same as upstream on dedicated servers).

---------------------------------------------------------------------------------------------------

## 2. Per-file table (every .java under src/main/java; 54 files, 6716 lines)

Difficulty scale: none = copy verbatim; easy = mechanical swap of 1-3 calls; medium = needs a Fabric design decision / small rewrite; hard = no direct equivalent, redesign.
"NF APIs" = NeoForge/FML-specific APIs used, with line numbers. "(AT)" = compiles upstream only because of an access transformer (own AT or NeoForge's built-in ATs).

### Root

| Path | Lines | Purpose | NeoForge/FML-specific APIs (line) | Difficulty |
|---|---|---|---|---|
| `NeoModImpl.java` | 69 | `@Mod` entry; wires events | `@Mod` 26; `IEventBus`/`ModContainer` ctor 29; `ModConfig.Type` + `registerConfig` 33-34; `NeoForge.EVENT_BUS` 41; `PlayerInteractEvent.RightClickBlock` 43; `AddReloadListenerEvent` 44; `EntityAttributeCreationEvent` 46; `DataPackRegistryEvent.NewRegistry` 47; `NewRegistryEvent` + `RegistryBuilder` (`sync(true)`) 48-52; `RegisterEvent` 53; `FMLLoader.getDist()`/`Dist` 55; `ClientTickEvent.Post` 57; `ViewportEvent.ComputeCameraAngles` 58; `InputEvent.Key` 59; `ModelEvent.RegisterGeometryLoaders` 61; `BuildCreativeModeTabContentsEvent` 62; `EntityRenderersEvent.RegisterRenderers` 63; `RegisterColorHandlersEvent.Item` 64; `FMLConstructModEvent` 65; `RegisterKeyMappingsEvent` 66 | medium: replace whole file by a `ModInitializer` + `ClientModInitializer` (section 3.5) |
| `DragonMountsLegacy.java` | 158 | MOD_ID/LOG/id(); event logic methods | `ClientHooks.registerLayerDefinition` 71; `IGeometryLoader` 74; `NeoForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS`/`.ENTITY_DATA_SERIALIZERS` 121,123; `EntityRenderers.register` is private in vanilla, public only via NeoForge AT 70; `CreativeModeTabs.SPAWN_EGGS/FUNCTIONAL_BLOCKS` private in vanilla (NF AT) 110-111; `TriConsumer` (log4j, fine) 103; uses `Minecraft.getInstance()` 87-89 | medium: mostly re-targeting registrars; egg-model loader hook (74-77) is hard (see DragonEggModel) |
| `DMLRegistry.java` | 107 | All registrations via `DeferredRegister` map | `DeferredRegister` 27-28 imports, 43, 99-105; `NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS` 59; `BlockEntityType.BlockEntitySupplier` private in vanilla (NF AT) 57 (Fabric API ships a transitive AW for this); `IEventBus` 26/39 | medium: rewrite as direct `Registry.register` in dependency order (block -> item -> entity type -> spawn egg), drop GLM serializer |
| `DMLConfig.java` | 218 | COMMON + CLIENT `ModConfigSpec` | `ModConfigSpec` + `.Builder/.BooleanValue/.DoubleValue/.IntValue` 9, 17-206 (21 usages); references `DragonEggLootMod.BUILT_IN_CHANCES` 170 (couples config to the GLM class) | medium: use Forge Config API Port (`net.neoforged.neoforge.common.ModConfigSpec` is provided as-is) and decouple `BUILT_IN_CHANCES` into a plain record list |

### accessors / mixins / util

| Path | Lines | Purpose | NF APIs | Difficulty |
|---|---|---|---|---|
| `accessors/ModelPartAccess.java` | 22 | Duck interface for per-part render scale | none | easy: delete; vanilla 1.21.1 `ModelPart` already has public `xScale/yScale/zScale` (verified by javap; `translateAndRotate` applies them) |
| `mixins/EnsureSafeFlyingVehicleMixin.java` | 26 | `@Redirect` PUTFIELD `clientVehicleIsFloating` in `ServerGamePacketListenerImpl.handleMoveVehicle` so riders of flying `FlyingAnimal`s aren't kicked for flying | none (target verified in vanilla) | none: copy |
| `mixins/ReplenishDragonEggMixin.java` | 25 | `@Redirect` GETFIELD `previouslyKilled` in `EndDragonFight.setDragonKilled(EnderDragon)` | none (target verified in vanilla) | none: copy |
| `mixins/client/ModelPartMixin.java` | 60 | `@Inject TAIL translateAndRotate` -> `PoseStack.scale(dm_x/y/zScale)`; implements `ModelPartAccess` | none | easy: delete (redundant, see above) and use `part.xScale/yScale/zScale` in `ModelPartProxy`, `DragonModel`, `DragonAnimator` |
| `util/CircularBuffer.java` | 48 | ring buffer for animation | none | none |
| `util/DMLUtil.java` | 29 | `HEX_CODEC`; `castRegistry(key)` -> `BuiltInRegistries.REGISTRY.getOrThrow` | none (relies on custom registries living in `BuiltInRegistries.REGISTRY`; Fabric registries built with `FabricRegistryBuilder` are registered there too) | none |
| `util/LerpedFloat.java` | 100 | lerped float helper | none | none |

### client

| Path | Lines | Purpose | NF APIs | Difficulty |
|---|---|---|---|---|
| `client/DragonAnimator.java` | 709 | per-dragon procedural animation state/tick, poses model parts | none (uses `ModelPartAccess` casts 306, 315, 476, 482) | none (swap 4 casts to direct fields) |
| `client/DragonEggModel.java` | 223 | NeoForge geometry loader + `IUnbakedGeometry` + `IDynamicBakedModel` for the egg block/item | `IDynamicBakedModel` 31/75, `ModelData`/`ModelProperty` 32-33/72/89/128/150, `IGeometryBakingContext`/`IGeometryLoader`/`IUnbakedGeometry` 34-36/48/58/190; `applyTransform` 144; `ItemOverrides` no-arg ctor is private in vanilla (NF AT) 166-170 | hard: full rewrite on Fabric Model Loading API / Renderer API (section 3.9) |
| `client/DragonModel.java` | 421 | `EntityModel<TameableDragon>`: part tree, `createBodyLayer(Properties)`, `Properties` record+codec | none (`ModelPartAccess` cast 365) | none/easy |
| `client/DragonRenderer.java` | 198 | `MobRenderer`, textures per breed, glow/saddle/death layers, per-breed model cache, custom `RenderType`s | `RenderType.create(7-arg)` 179 (private in vanilla, NF AT; Fabric API transitive AW covers it); consumes `DragonModelPropertiesListener.pollDefinitions()` 126 which depends on `ClientHooks` | medium: lazy-bake per-breed models (section 3.8) |
| `client/KeyMappings.java` | 45 | 2 keybinds + F6 press handler | none NF-specific (uses LWJGL `GLFW`, vanilla `KeyMapping`) | easy: register with `KeyBindingHelper`, handle F6 with `consumeClick()` in client tick |
| `client/ModelPartProxy.java` | 121 | proxies one model part rendered several times (neck/tail segments) | none (`ModelPartAccess` casts 86, 110) | easy |
| `client/MountCameraManager.java` | 38 | third-person on mount/dismount; camera offsets | calls `Camera.move`/`Camera.getMaxZoom` (34-35), only legal via own AT | easy-medium: AW + mixin hook (section 3.12) |
| `client/MountControlsMessenger.java` | 46 | delayed actionbar hint after mounting | none | none |

### data

| Path | Lines | Purpose | NF APIs | Difficulty |
|---|---|---|---|---|
| `data/CrossBreedingManager.java` | 83 | server `SimpleJsonResourceReloadListener` for `data/<ns>/dragonmounts/cross_breeding/*.json` | none (registered through `AddReloadListenerEvent`) | easy: wrap in `IdentifiableResourceReloadListener` |
| `data/loot/DragonEggLootMod.java` | 67 | `LootModifier` that appends (or replaces slot 0 with) a breed egg; also holds `BUILT_IN_CHANCES` | `LootModifier` 17/22/24/52, `IGlobalLootModifier` 16/62-64 | hard: no GLM on Fabric; replace with `LootTableEvents.MODIFY` (section 3.4) |
| `data/loot/conditions/RandomChanceByConfig.java` | 37 | `LootItemCondition` `dragonmounts:random_chance_by_config` | none (vanilla loot condition type) | none |
| `data/model/DragonModelPropertiesListener.java` | 56 | client reload listener for `assets/<ns>/models/entity/dragon/breed/properties/*.json`; registers one model layer per breed | `ClientHooks.registerLayerDefinition` 14/42 | medium |
| `data/providers/BlockTagProvider.java` | 76 | datagen: 9 block tags | `BlockTagsProvider`, `ExistingFileHelper` 12-13/17/25; `BlockTags.create(ResourceLocation)` 19-23 (NF patch) | n/a: datagen only, not shipped; outputs already in `src/generated` |
| `data/providers/DataProvider.java` | 28 | `@EventBusSubscriber` GatherDataEvent | `@EventBusSubscriber` 5/8, `@SubscribeEvent` 4/11, `GatherDataEvent` 6/12 | n/a (drop) |
| `data/providers/DragonBreedProvider.java` | 200 | datagen: the 8 built-in breeds as Java; source of truth for breed numbers | `DatapackBuiltinEntriesProvider` 33/40; `BlockTags.create(ResourceLocation)` 85 | n/a (drop; read it as a spec, the JSON is already generated) |
| `data/providers/LootModifierProvider.java` | 43 | datagen: 8 GLM JSONs | `GlobalLootModifierProvider` 13/18, `LootTableIdCondition` 14/35 | n/a (drop) |
| `data/providers/LootTableProvider.java` | 66 | datagen: egg block loot table (copies breed+name components, hatch_stage state) | none (vanilla) | n/a (output already generated) |
| `data/providers/ModelPropertiesProvider.java` | 44 | datagen: fire/ghost/water model property JSON | none (vanilla) | n/a (output already generated) |

### dragon

| Path | Lines | Purpose | NF APIs | Difficulty |
|---|---|---|---|---|
| `dragon/DragonBreed.java` | 161 | breed record, `DIRECT_CODEC`, `NETWORK_CODEC`, `CODEC` (RegistryFixedCodec), `STREAM_CODEC`, helpers | none | none |
| `dragon/DragonSpawnEgg.java` | 112 | spawn egg item with breed in components, tinting, tab population | `extends DeferredSpawnEggItem` 22/28, ctor `super(DMLRegistry.DRAGON, 0, 0, props)` ~32; `ServerLifecycleHooks.getCurrentServer()` 23/49 (null on client = upstream bug #259, fixed only on master) | medium |
| `dragon/TameableDragon.java` | 1186 | the entity (AI, riding, flight, age, breeding, abilities, sounds) | `Tags.Items.TOOLS_SHEAR` 66/524; `FluidType` + `canDrownInFluidType` 67/1139-1142; `getPickedResult(HitResult)` 692-697; `Blocks.SNOW.getSoundType(state, level, pos, entity)` 661; `driver.jumping` protected, via own AT 441, 472 | medium |
| `dragon/abilities/Ability.java` | 112 | ability interface, `Factory`, registry key + dispatch codec | none | none |
| `dragon/abilities/FootprintAbility.java` | 39 | base for footprint abilities | `EventHooks.canEntityGrief` 5/13 | easy |
| `dragon/abilities/FrostWalkerAbility.java` | 92 | frost walker | `EventHooks.onBlockPlace` + `BlockSnapshot` 13-14/63 | easy |
| `dragon/abilities/GreenToesAbility.java` | 107 | bone-meal/flowers | none | none |
| `dragon/abilities/HotFeetAbility.java` | 45 | burn plants; tag `hot_feet_burnables` | `BlockTags.create(ResourceLocation)` 19 (NF patch) | easy |
| `dragon/abilities/HydroStepAbility.java` | 83 | water footprints | none | none |
| `dragon/abilities/ReaperStepAbility.java` | 89 | plant death, soul soil/sand | `BlockTags.create(ResourceLocation)` 26-28 (NF patch) | easy |
| `dragon/abilities/SnowStepperAbility.java` | 48 | snow layers | none | none |
| `dragon/ai/DragonBodyController.java` | 25 | `BodyRotationControl` | none | none |
| `dragon/ai/DragonBreedGoal.java` | 67 | `BreedGoal` that spawns an egg | `NeoForge.EVENT_BUS.post(new BabyEntitySpawnEvent(...))` 8-9/53 | easy (drop the event; no Fabric equivalent) |
| `dragon/ai/DragonFollowOwnerGoal.java` | 214 | follow owner incl. flying teleport | none | none |
| `dragon/ai/DragonMoveController.java` | 56 | `MoveControl` with flight branch | none | none |
| `dragon/egg/HatchableEggBlock.java` | 391 | egg block + nested `Item` | `ServerLifecycleHooks` 52/370; `getCloneItemStack(state, HitResult, level, pos, player)` @Override 111-112 (NF); `onDestroyedByPlayer` @Override 159-169 (NF); `Minecraft.getInstance()` in common code 84-86 | medium |
| `dragon/egg/HatchableEggBlockEntity.java` | 269 | stores breed/name/transition; habitat update | none (all vanilla 1.21 BlockEntity component API) | easy (add `RenderDataBlockEntity`) |
| `dragon/egg/habitats/Habitat.java` | 36 | habitat interface, registry key, dispatch codec | none | none |
| `dragon/egg/habitats/BiomeHabitat.java` | 30 | biome tag | none | none |
| `dragon/egg/habitats/DragonBreathHabitat.java` | 31 | area-effect-cloud w/ dragon breath | none | none |
| `dragon/egg/habitats/FluidHabitat.java` | 32 | fluid tag nearby | none | none |
| `dragon/egg/habitats/HeightHabitat.java` | 30 | world height | none | none |
| `dragon/egg/habitats/LightHabitat.java` | 30 | light level | none | none |
| `dragon/egg/habitats/NearbyBlocksHabitat.java` | 32 | block tag nearby | none | none |
| `dragon/egg/habitats/PickyHabitat.java` | 36 | AND of habitats | none | none |

---------------------------------------------------------------------------------------------------

## 3. NeoForge-specific systems, in detail

### 3.1 Datapack registry for DragonBreed

- `DragonBreed.REGISTRY_KEY = ResourceKey.createRegistryKey(dragonmounts:dragon_breeds)` (DragonBreed.java:49). Entries are JSON at `data/<ns>/dragonmounts/dragon_breeds/<name>.json` (non-vanilla namespace registries are looked up under `data/<entry ns>/<registry ns>/<registry path>/`).
- Registration: `NeoModImpl:47` `DataPackRegistryEvent.NewRegistry` -> `DragonMountsLegacy.registerDatapacks` (103-106) -> `registrar.accept(REGISTRY_KEY, DragonBreed.DIRECT_CODEC, DragonBreed.NETWORK_CODEC)`. NeoForge syncs the registry to clients using the **network codec**.
- `DIRECT_CODEC` fields (JSON key: type, default): `primary_color` (hex string, required; `DMLUtil.HEX_CODEC`), `secondary_color` (hex string, required), `hatch_particles` (ParticleOptions, optional), `attributes` (map attribute holder -> double, `{}`), `abilities` (list of `{type: dragonmounts:<ability>, ...}`, `[]`), `habitats` (list, `[]`), `immunities` (HolderSet<DamageType>, empty), `ambient_sound` (SoundEvent holder, optional), `death_loot` (loot table ResourceKey, `minecraft:empty`), `growth_time` (int, 72000), `hatch_chance` (float, 0.1), `size_modifier` (float, 1.0), `taming_items` (HolderSet<Item>, `#minecraft:fishes`), `breeding_items` (HolderSet<Item>, `#minecraft:fishes`), `reproduction_limit` (int OR string, -1; string form `config:<breedname>` resolves through `DMLConfig.getReproLimitFor`).
- `NETWORK_CODEC` (client only needs these): `primary_color, secondary_color, hatch_particles, ambient_sound, growth_time, size_modifier`; the client-side breed is built by `DragonBreed.fromNetwork(...)` with empty abilities/habitats/etc.
- Fabric equivalent (confirmed present in Fabric API 0.116.17): `DynamicRegistries.registerSynced(REGISTRY_KEY, DIRECT_CODEC, NETWORK_CODEC)` (`net.fabricmc.fabric.api.event.registry.DynamicRegistries`, overload with two codecs + `SyncOption...`). Call from `ModInitializer.onInitialize`. Same JSON path convention, so the JSON files are reused untouched.
- Holder usage elsewhere: `DragonBreed.CODEC = RegistryFixedCodec.create(REGISTRY_KEY)` (persistent form of `Holder<DragonBreed>` in NBT / components) and `STREAM_CODEC = ByteBufCodecs.holderRegistry(REGISTRY_KEY)` (network). All vanilla; works with Fabric's synced dynamic registry.
- Entity data serializer: `TameableDragon.DRAGON_BREED_SERIALIZER = EntityDataSerializer.forValueType(DragonBreed.STREAM_CODEC.apply(ByteBufCodecs::optional))` (TameableDragon.java:104). Registered on NeoForge in `RegisterEvent` for `NeoForgeRegistries.ENTITY_DATA_SERIALIZERS` as `dragonmounts:dragon_breed_serializer` (DragonMountsLegacy.java:119-124). **Fabric**: Fabric API 0.116.17 has no tracked-data registry class (searched all modules). Call the vanilla public static `EntityDataSerializers.registerSerializer(serializer)` once in `onInitialize` (verified public). `SynchedEntityData.defineId` does not validate registration (bytecode checked), so class-init order is not a problem, but it must be registered before any entity is sent over the network. Note: ids are assigned by order of registration, so the mod set must match client/server (normal for Fabric).
- Static JSON to ship: `src/generated/resources/data/dragonmounts/dragonmounts/dragon_breeds/{aether,end,fire,forest,ghost,ice,nether,water}.json` (verified identical in meaning to `DragonBreedProvider.java`; note `ice` secondary colour is written as `"e1ff"` because `Integer.toHexString(0x00E1FF)`, parses back fine; `water` primary `"62ff"`).
- Example breed JSON for reference: `example_addon/data/example_addon/dragonmounts/dragon_breeds/example_breed.json` (uses `#forge:eggs` tags and `death_loot`).

### 3.2 NewRegistryEvent / RegistryBuilder (ability + habitat types)

Two **static custom registries of MapCodecs** used as dispatch codecs:
- `Ability.REGISTRY_KEY = dragonmounts:ability_type`, element type `MapCodec<? extends Ability.Factory<? extends Ability>>` (Ability.java:43). `Ability.CODEC = Codec.lazyInitialized(() -> DMLUtil.castRegistry(KEY).byNameCodec().dispatch(Factory::codec, identity))` (line 44). Created with `new RegistryBuilder<>(KEY).sync(true)` (NeoModImpl:50) -> **synced**.
- `Habitat.REGISTRY_KEY = dragonmounts:habitat_type`, element `MapCodec<? extends Habitat>` (Habitat.java:20-21). `new RegistryBuilder<>(KEY)` (NeoModImpl:51) -> **not synced**.
- Entries are registered by `DMLRegistry` static blocks with DeferredRegister (DMLRegistry.java:68-88): abilities `frost_walker, green_toes, snow_stepper, hot_feet, reaper_step, hydro_step`; habitats `picky, biome, in_fluid, world_height, light, nearby_blocks, dragon_breath`.
- Fabric: `FabricRegistryBuilder.createSimple(KEY).attribute(RegistryAttribute.SYNCED).buildAndRegister()` for abilities (no attribute for habitats). Verified: `FabricRegistryBuilder.createSimple(ResourceKey)`, `RegistryAttribute.SYNCED`. The result is registered in `BuiltInRegistries.REGISTRY`, so `DMLUtil.castRegistry` keeps working. Must be created **before** the `Registry.register(...)` calls for their entries and before any breed JSON is decoded (it is: data loads at world load). Registry-key typing: the generic `Registry<MapCodec<? extends X>>` needs unchecked casts, as upstream already does.
- Habitat JSON shape (all `{"type": "dragonmounts:<name>", ...}`): `biome` {points=2, biome_tag}; `in_fluid` {point_multiplier=0.5, fluid_tag}; `world_height` {points=3, below=false, height}; `light` {points=3, below=false, light}; `nearby_blocks` {point_multiplier=0.5, block_tag}; `picky` {required_habitats:[...]}; `dragon_breath` {} (10 points if an area effect cloud with `DRAGON_BREATH` particle intersects the egg pos).

### 3.3 DeferredRegister usage (DMLRegistry.java)

`DMLRegistry.REGISTRIES` maps `ResourceKey<Registry<?>>` -> `DeferredRegister`, created lazily by `register(name, registryKey, supplier)` (lines 97-106): if the registry already exists in `BuiltInRegistries.REGISTRY` it uses `DeferredRegister.create(Registry, modid)`, else `create(ResourceKey, modid)` (for the custom registries not yet created at construction time). `init(bus)` registers all to the mod bus then clears. Registered content (all `modid = dragonmounts`):

| Registry | Id | Object |
|---|---|---|
| BLOCK | `dragon_egg` | `HatchableEggBlock` (props: black map colour, strength 0/9, light level 1, noOcclusion) |
| ITEM | `dragon_egg` | `HatchableEggBlock.Item` (BlockItem, EPIC rarity) |
| ITEM | `spawn_egg` | `DragonSpawnEgg` |
| SOUND_EVENT | `entity.dragon.ambient`, `entity.dragon.step`, `entity.dragon.death`, `entity.dragon.ambient.ghost` | `SoundEvent.createVariableRangeEvent(id)` |
| ENTITY_TYPE | `dragon` | `EntityType.Builder.of(TameableDragon::new, CREATURE).sized(2.75f, 2.75f).eyeHeight(3.375f).clientTrackingRange(10).updateInterval(3).build("dragonmounts:dragon")` |
| BLOCK_ENTITY_TYPE | `dragon_egg` | `BlockEntityType.Builder.of(HatchableEggBlockEntity::new, EGG_BLOCK.get()).build(null)` |
| neoforge:loot_modifier_serializers | `dragon_egg_loot` | `DragonEggLootMod.CODEC` (NeoForge only) |
| LOOT_CONDITION_TYPE | `random_chance_by_config` | `new LootItemConditionType(RandomChanceByConfig.CODEC)` |
| DATA_COMPONENT_TYPE | `dragon_breed` | `DataComponentType<Holder<DragonBreed>>` persistent=`DragonBreed.CODEC`, network=`DragonBreed.STREAM_CODEC` |
| dragonmounts:ability_type | 6 ids | see 3.2 |
| dragonmounts:habitat_type | 7 ids | see 3.2 |
| (event) ENTITY_DATA_SERIALIZERS | `dragon_breed_serializer` | see 3.1 |

Fabric: replace with plain `Registry.register(BuiltInRegistries.X, DragonMountsLegacy.id(name), obj)` in this order: sounds, block, block item, entity type, spawn egg item (vanilla `SpawnEggItem` ctor needs the `EntityType` instance, it registers itself in `SpawnEggItem.BY_ID`), block entity type, data component type, loot condition type, then custom registries' entries. Keep static `Supplier`/field holders (`DMLRegistry.DRAGON`...) so call sites need minimal edits (`.get()` can stay if holders are `Supplier`s, or switch to plain fields).

### 3.4 Global loot modifiers

- Serializer: `dragonmounts:dragon_egg_loot` (`DragonEggLootMod.CODEC`): `conditions` (inherited via `LootModifier.codecStart`), `egg_breed` (`Holder<DragonBreed>` via `DragonBreed.CODEC.fieldOf`), `replace_first` (bool, default false). `doApply` creates `HatchableEggBlock.Item.create(breed)` and either `generatedLoot.set(0, egg)` or `add(egg)`.
- Built-in targets (DragonEggLootMod.java:386-395, config key = `<breed>_in_<last path segment of table>_chance`): aether/`chests/simple_dungeon` 0.15; fire/`desert_pyramid` 0.075; forest/`jungle_temple` 0.3; ghost/`woodland_mansion` 0.2; ghost/`abandoned_mineshaft` 0.095; ice/`igloo_chest` 0.2; nether/`bastion_treasure` 0.35; water/`buried_treasure` 0.175.
- Generated files (src/generated/resources):
  - `data/neoforge/loot_modifiers/global_loot_modifiers.json`: `{"entries":[ "dragonmounts:dragonmounts/aether/chests/simple_dungeon", ... 8 ids ], "replace": false}`
  - `data/dragonmounts/loot_modifiers/dragonmounts/<breed>/chests/<table>.json`, e.g.
    ```json
    { "type": "dragonmounts:dragon_egg_loot",
      "conditions": [ {"condition":"neoforge:loot_table_id","loot_table_id":"minecraft:chests/woodland_mansion"},
                      {"condition":"dragonmounts:random_chance_by_config","config_chance_target":"ghost_in_woodland_mansion_chance"} ],
      "egg_breed": "dragonmounts:ghost" }
    ```
- `LootTableIdCondition` (`neoforge:loot_table_id`): used only inside these JSONs and in `LootModifierProvider.java:35`; it matches the id of the table being rolled.
- `RandomChanceByConfig` (`dragonmounts:random_chance_by_config`, field `config_chance_target`): returns false unless `use_loot_tables`; else `random.nextFloat() < DMLConfig.getEggChanceFor(target)` (-1 for unknown targets => never).
- **Fabric plan**: none of the 9 JSONs works on Fabric (drop them). Implement in code with Fabric Loot API v3 (`net.fabricmc.fabric.api.loot.v3.LootTableEvents.MODIFY`, signature `(ResourceKey<LootTable>, LootTable.Builder, LootTableSource, HolderLookup.Provider)` - verified): for each of the 8 built-in `Target`s, when `key.location().equals(target.table().location())`, `tableBuilder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(new RandomChanceByConfig(path)).add(LootItem.lootTableItem(EGG_BLOCK_ITEM).apply(SetComponentsFunction.setComponent(DRAGON_BREED_COMPONENT, breedHolder))))` where `breedHolder = registries.lookupOrThrow(DragonBreed.REGISTRY_KEY).getOrThrow(breedKey)` (the 4th param is the registry lookup provider, available because `MODIFY` runs after registries loaded). A separate pool is the closest equivalent to "append one stack"; `replace_first` and datapack-defined modifiers cannot be reproduced without a custom reload listener (not needed for parity with built-ins; optionally write a tiny `dragonmounts/loot_modifiers/*.json` listener later). `LootItemCondition` types are in a vanilla registry (`LOOT_CONDITION_TYPE`), so `RandomChanceByConfig` keeps working.
- `HatchableEggBlock.Item.create(Holder<DragonBreed>)` is plain vanilla (sets the `dragon_breed` data component).

### 3.5 Events subscribed (every one) and what it does

NeoForge bus (`NeoForge.EVENT_BUS`):

| Event | Handler | What it does | Fabric replacement |
|---|---|---|---|
| `PlayerInteractEvent.RightClickBlock` (NeoModImpl:43) | `DragonMountsLegacy.overrideVanillaDragonEgg` (131-148); result `setCanceled(true)` when it handled it | If `allow_egg_override` and the clicked block is `Blocks.DRAGON_EGG` and breed `dragonmounts:end` exists: client -> swing main hand; server -> place a hatching `dragon_egg` block entity with breed `end` via `HatchableEggBlock.place`; cancels the interaction | `UseBlockCallback.EVENT` (verified): return `InteractionResult.SUCCESS` when handled, `PASS` otherwise |
| `AddReloadListenerEvent` (44) | `registerReloadListeners` -> adds `CrossBreedingManager.INSTANCE` | server data reload | `ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(IdentifiableResourceReloadListener)` (verified) |
| `ClientTickEvent.Post` (57) | `clientTick` -> `MountControlsMessenger.tick()` | delayed hint message | `ClientTickEvents.END_CLIENT_TICK` |
| `ViewportEvent.ComputeCameraAngles` (58) | `MountCameraManager.setMountCameraAngles(e.getCamera())` | adjusts camera when riding a dragon in third person | Mixin `Camera#setup` TAIL (section 3.12) |
| `InputEvent.Key` (59) | `onKeyPress(key, action, mods)` -> `KeyMappings.handleKeyPress` | when F6 (the `camera_flight` mapping's key) is pressed while riding a dragon: toggles `camera_driven_flight` config and shows an actionbar message | `ClientTickEvents.END_CLIENT_TICK` with `while (KeyMappings.CAMERA_CONTROLS.consumeClick())` |

Mod bus:

| Event | Handler | What it does | Fabric replacement |
|---|---|---|---|
| `EntityAttributeCreationEvent` (46) | `registerEntityAttributes` -> `put(DRAGON, TameableDragon.createAttributes().build())` | default attributes | `FabricDefaultAttributeRegistry.register(type, builder)` (verified) |
| `DataPackRegistryEvent.NewRegistry` (47) | `registerDatapacks` | see 3.1 | `DynamicRegistries.registerSynced` |
| `NewRegistryEvent` (48-52) | creates Ability (synced) + Habitat registries | see 3.2 | `FabricRegistryBuilder` |
| `RegisterEvent` (53) | `registerOtherObjects(registry)` only acts for `ENTITY_DATA_SERIALIZERS` | registers `DRAGON_BREED_SERIALIZER` | `EntityDataSerializers.registerSerializer` |
| `ModelEvent.RegisterGeometryLoaders` (61, client) | `registerEggModelLoader` -> `registrar.accept(dragonmounts:dragon_egg, DragonEggModel.Loader.INSTANCE)` | registers the model loader id used in `"loader": "dragonmounts:dragon_egg"` | see 3.9 |
| `BuildCreativeModeTabContentsEvent` (62, client) | `registerCreativeTabItems(tabKey, acceptor)` | tab `SPAWN_EGGS` -> `DragonSpawnEgg.populateTab`, tab `FUNCTIONAL_BLOCKS` -> `HatchableEggBlock.populateTab`; both read `Minecraft.getInstance().level.registryAccess()` and add one stack per breed (nothing if no client level) | `ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS)` / `FUNCTIONAL_BLOCKS` (verified; the key fields are private in vanilla but Fabric API's item-group AW makes them accessible) |
| `EntityRenderersEvent.RegisterRenderers` (63, client) | `registerRenderers` (68-72) | `EntityRenderers.register(DRAGON, DragonRenderer::new)` and `ClientHooks.registerLayerDefinition(DragonRenderer.MODEL_LOCATION, () -> DragonModel.createBodyLayer(STANDARD))` | `EntityRendererRegistry.register(...)`; `EntityModelLayerRegistry.registerModelLayer(MODEL_LOCATION, () -> createBodyLayer(STANDARD))` (both verified) |
| `RegisterColorHandlersEvent.Item` (64, client) | `registerItemColors` -> `colors.register(DragonSpawnEgg::getColor, SPAWN_EGG)` | spawn egg tint: index 0 = breed primaryColor, other = secondaryColor, alpha forced | `ColorProviderRegistry.ITEM.register(DragonSpawnEgg::getColor, item)` |
| `FMLConstructModEvent` (65, client) | `enqueueWork(registerReloadListenersEarly)` (85-91) | adds `DragonModelPropertiesListener` to the client `ReloadableResourceManager` early so it runs **before** entity models are baked | see 3.8 |
| `RegisterKeyMappingsEvent` (66, client) | `KeyMappings.registerKeybinds` | registers 2 key mappings | `KeyBindingHelper.registerKeyBinding` |

### 3.6 ModConfigSpec config

Files: NeoForge default names `dragonmounts-common.toml` (COMMON) and `dragonmounts-client.toml` (CLIENT). TOML tables as built by `DMLConfig`.

COMMON (`DMLConfig.COMMON_SPEC`; both sides read it; server-authoritative for gameplay):

| TOML path | Type | Default | Range | Used by |
|---|---|---|---|---|
| `config.allow_egg_override` | bool | true | - | `overrideVanillaDragonEgg` |
| `config.replenish_eggs` | bool | true | - | `ReplenishDragonEggMixin` |
| `config.use_loot_tables` | bool | false | - | `RandomChanceByConfig` |
| `config.update_habitats` | bool | true | - | `HatchableEggBlock.randomTick` |
| `egg_loot_chances.aether_in_simple_dungeon_chance` | double | 0.15 | 0..1 | loot |
| `egg_loot_chances.fire_in_desert_pyramid_chance` | double | 0.075 | 0..1 | loot |
| `egg_loot_chances.forest_in_jungle_temple_chance` | double | 0.3 | 0..1 | loot |
| `egg_loot_chances.ghost_in_woodland_mansion_chance` | double | 0.2 | 0..1 | loot |
| `egg_loot_chances.ghost_in_abandoned_mineshaft_chance` | double | 0.095 | 0..1 | loot |
| `egg_loot_chances.ice_in_igloo_chest_chance` | double | 0.2 | 0..1 | loot |
| `egg_loot_chances.nether_in_bastion_treasure_chance` | double | 0.35 | 0..1 | loot |
| `egg_loot_chances.water_in_buried_treasure_chance` | double | 0.175 | 0..1 | loot |
| `reproduction_limits.aether/end/fire/forest/ghost/ice/nether/water` (8 keys) | int | 2 (`TameableDragon.BASE_REPRO_LIMIT`) | -1..Integer.MAX | `DragonBreed.getReproductionLimit()` when JSON says `"config:<name>"` (-1 = no limit, 0 = cannot breed) |

CLIENT (`DMLConfig.CLIENT_SPEC`):

| TOML path | Type | Default | Range |
|---|---|---|---|
| `client.camera_driven_flight` | bool | true | - (also toggled at runtime via F6: `DMLConfig.CAMERA_DRIVEN_FLIGHT.set(...)`, KeyMappings.java:41) |
| `client.third_person_on_mount` | bool | true | - |
| `camera_offsets.third_person_back.distance` | double | 6.0 | -3..1000 |
| `camera_offsets.third_person_back.vertical` | double | 4.0 | -3..1000 |
| `camera_offsets.third_person_back.horizontal` | double | 0.0 | -1000..1000 |
| `camera_offsets.third_person_front.distance/vertical/horizontal` | double | 6.0 / 4.0 / 0.0 | same |

Accessors: `allowEggOverride()`, `replenishEggs()`, `useLootTables()`, `updateHabitats()`, `getEggChanceFor(String)` (-1 if unknown), `getReproLimitFor(String)` (strips `"config:"`, -1 if unknown), `cameraDrivenFlight()`, `thirdPersonOnMount()`, `getCameraPerspectiveOffset(boolean back)` -> `DoubleValue[3]`. Config values are read in hot paths (every random tick, every `MountCameraManager` call), so keep `.get()` cheap.

**Fabric**: verified `fuzs.forgeconfigapiport:forgeconfigapiport-fabric:21.1.x` (21.1.3 and 21.1.6 are in the local Gradle cache) ships `net.neoforged.neoforge.common.ModConfigSpec` (via `forgeconfigapiport-common-neoforgeapi`) and `fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry.INSTANCE.register(modId, ModConfig.Type.COMMON, spec)` (`net.neoforged.fml.config.ModConfig`). With it `DMLConfig` stays byte-for-byte identical except the `BUILT_IN_CHANCES` dependency (extract `Target` record + array into a neutral class) - the cheapest path. It adds a runtime mod dependency (declare `forgeconfigapiport` in `fabric.mod.json` depends, and bundle with jar-in-jar or require). Alternative: hand-rolled JSON/TOML config (then re-implement the keys above, plus runtime `set`).

### 3.7 ClientHooks / EventHooks / other NeoForge hook calls

| Call | Where | What it does on NeoForge | Fabric replacement |
|---|---|---|---|
| `ClientHooks.registerLayerDefinition(layer, supplier)` | DragonMountsLegacy:71, DragonModelPropertiesListener:42 | puts a `Supplier<LayerDefinition>` into a static map consumed when `EntityModelSet` reloads (`ClientHooks.loadLayerDefinitions`) | static: `EntityModelLayerRegistry.registerModelLayer`; dynamic per-breed: avoid, bake lazily (3.8) |
| `EventHooks.canEntityGrief(level, entity)` | FootprintAbility:13 | fires `EntityMobGriefingEvent` (default = `RULE_MOBGRIEFING` gamerule) | `level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)` |
| `EventHooks.onBlockPlace(entity, BlockSnapshot, Direction.UP)` | FrostWalkerAbility:63 | fires `BlockEvent.EntityPlaceEvent`; returns true if cancelled (protection mods) | drop (no Fabric equivalent); optionally call vanilla/protection hooks of your choice |
| `NeoForge.EVENT_BUS.post(new BabyEntitySpawnEvent(animal, partner, null))` | DragonBreedGoal:53 | mod-compat hook; cancel => reset love and set both ages to 6000 | drop the `if` block (keep the rest) |
| `ServerLifecycleHooks.getCurrentServer()` | DragonSpawnEgg:49, HatchableEggBlock:370 | static access to the running server inside `Item#verifyComponentsAfterLoad` | keep a static `MinecraftServer` set in `ServerLifecycleEvents.SERVER_STARTING` / cleared in `SERVER_STOPPED`; **must null-check and fall back to the client level's `registryAccess()`** (upstream master fixes crash #259 on dedicated server/client by choosing `Minecraft.getInstance().level` vs server by dist; this branch has the bug: `DragonSpawnEgg:49-51` dereferences a null server on the client, `HatchableEggBlock:370-374` guards `server != null`) |
| `DeferredSpawnEggItem` | DragonSpawnEgg:28 | spawn egg whose `EntityType` is a `Supplier` (registered later) | vanilla `SpawnEggItem(EntityType<? extends Mob>, int, int, Item.Properties)` after the entity type is registered (colours unused here; tinting via ItemColors) |

### 3.8 Per-breed model layers + DragonModelPropertiesListener (client)

- `DragonModelPropertiesListener` (`SimpleJsonResourceReloadListener`, folder `models/entity/dragon/breed/properties`) reads each `assets/<ns>/models/entity/dragon/breed/properties/<breed>.json`, decodes `DragonModel.Properties` (`middle_tail_scales` default true, `tail_horns` false, `thin_legs` false), creates a `ModelLayerLocation(dragonmounts:dragon, "<ns>:<breed>")` and calls `ClientHooks.registerLayerDefinition(loc, () -> DragonModel.createBodyLayer(properties))`. `pollDefinitions()` returns a copy and clears the internal map.
- `DragonRenderer.bakeModels` (line 123-129) calls `pollDefinitions()` in its constructor and bakes `bakery.bakeLayer(loc)` for each. Breeds without a properties file use the standard layer (`MODEL_LOCATION` = `dragonmounts:dragon#main`, Properties.STANDARD).
- Ordering constraint on NeoForge: the listener must run before `EntityModelSet` reloads, hence `registerReloadListenersEarly` (FMLConstructModEvent, line 85-91).
- Existing properties files: `assets/dragonmounts/models/entity/dragon/breed/properties/{fire (middle_tail_scales=false), ghost (thin_legs), water (tail_horns)}.json` - **generated** (ModelPropertiesProvider). Exact contents: fire `{"middle_tail_scales": false}`; ghost `{"thin_legs": true}`; water `{"tail_horns": true}`.
- **Fabric design (recommended)**: don't use layer registration for these. Keep `DragonModelPropertiesListener` as a plain client reload listener (wrap as `IdentifiableResourceReloadListener`, `ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)`) that only stores `Map<ResourceLocation, DragonModel.Properties>`; in `DragonRenderer` create models lazily on first render per breed: `new DragonModel(DragonModel.createBodyLayer(props).bakeRoot())` (`LayerDefinition.bakeRoot()` is public in vanilla - verified). This works regardless of listener ordering (Fabric listeners run after vanilla's). Register the STANDARD layer statically with `EntityModelLayerRegistry`.

### 3.9 Egg block/item model: IGeometryLoader / IDynamicBakedModel

How it works today:
1. `assets/dragonmounts/blockstates/dragon_egg.json`: `{"variants":{"":{"model":"dragonmounts:block/dragon_egg_models"}}}` (single variant for every state).
2. `assets/dragonmounts/models/block/dragon_egg_models.json`: `{"loader": "dragonmounts:dragon_egg"}` <- **the NeoForge-only key** (`loader`).
3. `assets/dragonmounts/models/item/dragon_egg.json`: `{"parent": "dragonmounts:block/dragon_egg_models"}` (inherits the loader, so the item renders through the same baked model).
4. `NeoModImpl:61` registers `DragonEggModel.Loader.INSTANCE` under id `dragonmounts:dragon_egg`. When the model JSON is parsed, `Loader.read` (DragonEggModel.java:190-221) lists `Minecraft.getInstance().getResourceManager().listResources("models/block/dragon_eggs", *.json)` across **all namespaces** (so addons contribute) and parses each as a vanilla `BlockModel` via `BlockModel.fromStream`. Map key = `<namespace>:<name minus "_dragon_egg" suffix>`, e.g. `dragonmounts:fire` from `models/block/dragon_eggs/fire_dragon_egg.json`. The returned `DragonEggModel` (an `IUnbakedGeometry`) bakes every sub-model (`resolveParents` + `bake(..., true)`).
5. `DragonEggModel.Baked implements IDynamicBakedModel`: `getQuads(state, side, rand, ModelData, renderType)` picks `models.get(breedId)` from `ModelData` property `Data.PROPERTY` (breed registered name), else falls back to the vanilla dragon egg model (`Minecraft.getBlockRenderer().getBlockModel(Blocks.DRAGON_EGG.defaultBlockState())`, memoized); `getModelData(level, pos, state, modelData)` reads `level.getBlockEntity(pos)` -> `HatchableEggBlockEntity.getBreedHolder().getRegisteredName()`; `getParticleIcon(ModelData)` per breed; `applyTransform` delegates to the fallback; `isGui3d/usesBlockLight/useAmbientOcclusion` true; `getOverrides()` returns `ItemModelResolver`.
6. `ItemModelResolver extends ItemOverrides` (no-arg ctor is private in vanilla, accessible via NeoForge AT): `resolve(original, stack, level, entity, seed)` first applies the nested overrides, then picks the sub-model by the stack's `dragonmounts:dragon_breed` component.
7. Per-breed models: `assets/dragonmounts/models/block/dragon_eggs/{aether,end,fire,forest,ghost,ice,nether,water}_dragon_egg.json`; all `parent: minecraft:block/dragon_egg` (end has no textures = vanilla look; others set `particle` + `all` texture `dragonmounts:block/<breed>_dragon_egg`; textures are animated 3-frame `.png.mcmeta` with `frametime 8, interpolate`). Addon example: `example_addon/assets/example_addon/models/block/dragon_eggs/example_breed_dragon_egg.json`.

Files that reference the loader: only `models/block/dragon_egg_models.json` (`loader`), transitively `models/item/dragon_egg.json` (parent), and the blockstate (model path).

Fabric implementation options (verified APIs exist in 0.116.17: `ModelLoadingPlugin`, `PreparableModelLoadingPlugin`, `ModelModifier`, `FabricBakedModel`, `QuadEmitter`, `RenderDataBlockEntity`, `BuiltinItemRendererRegistry`):
- **Option A (closest to upstream)**: `PreparableModelLoadingPlugin` (async-load the sub-model JSONs listing `models/block/dragon_eggs`) + `pluginContext.addModels(ids)` for the per-breed models so they are baked, and `pluginContext.resolveModel().register(ctx -> id equals dragonmounts:block/dragon_egg_models OR the item model id ? customUnbakedModel : null)` returning a custom `UnbakedModel` whose `bake` yields a `BakedModel` implementing `FabricBakedModel` (`isVanillaAdapter() == false`, `emitBlockQuads(...)` reads `blockView.getBlockEntityRenderData(pos)` and forwards `getQuads` of the matching per-breed baked model via `QuadEmitter`/`mesh`, `emitItemQuads` for the item). Make `HatchableEggBlockEntity implements RenderDataBlockEntity` returning the breed id string. Keep `ItemModelResolver` (vanilla `ItemOverrides.resolve`) for per-stack breed; for the ctor use `new ItemOverrides(baker, blockModel, List.of())` (public ctor) or an AW `accessible method net/minecraft/client/renderer/block/model/ItemOverrides <init> ()V`. Remember item id `dragonmounts:item/dragon_egg` must also resolve to the custom model (parent-based inheritance of a custom UnbakedModel from a BlockModel JSON will not work).
  Also available (verified on `ModelLoadingPlugin$Context`): `registerBlockStateResolver(Block, BlockStateResolver)` - can be used instead of `resolveModel()` to hand the egg block's blockstates directly to the custom model, with `addModels(...)`, `modifyModelOnLoad/BeforeBake/AfterBake`. `FabricBlockView.getBlockEntityRenderData(BlockPos)` is the accessor for the `RenderDataBlockEntity` value.
- **Option B (simplest and robust)**: no custom block model at all; blockstate points to a plain vanilla-format fallback model (e.g. `parent: minecraft:block/dragon_egg`), add a `BlockEntityRenderer<HatchableEggBlockEntity>` that renders the per-breed model with `ModelBlockRenderer`/`BlockRenderDispatcher.getModelRenderer().renderModel(...)` using models loaded via `ModelLoadingPlugin.addModels`, and `BuiltinItemRendererRegistry` (or an item overrides trick) for the item. Costs: no chunk-mesh batching/AO caching, but egg count is tiny.
- Do not forget `useAmbientOcclusion/isGui3d/usesBlockLight/getTransforms()` (vanilla `BakedModel` has abstract `getTransforms()`, which NeoForge's `IDynamicBakedModel` default-implemented through `applyTransform`; delegate to the fallback's `getTransforms()`).

### 3.10 Access transformers (src/main/resources/META-INF/accesstransformer.cfg)

```
public net.minecraft.world.entity.LivingEntity jumping
public net.minecraft.client.Camera move(FFF)V
public net.minecraft.client.Camera getMaxZoom(F)F
```
- `LivingEntity.jumping` (protected boolean): used at `TameableDragon:441` (`driver.jumping` = ascend while flying) and `:472` (take-off) - accessed on a `Player` instance, so protected access from the subclass does not suffice.
- `Camera.move(FFF)V` (protected) and `Camera.getMaxZoom(F)F` (private): used at `MountCameraManager:34-35`.
- Fabric `.accesswidener` (namespace `named` when using Loom + official Mojang mappings):
  ```
  accessWidener v2 named
  accessible field net/minecraft/world/entity/LivingEntity jumping Z
  accessible method net/minecraft/client/Camera move (FFF)V
  accessible method net/minecraft/client/Camera getMaxZoom (F)F
  ```
  (add `accessible method net/minecraft/client/renderer/block/model/ItemOverrides <init> ()V` if you keep `ItemModelResolver extends ItemOverrides` with a no-arg super call).
- **Implicit NeoForge ATs the code silently depends on** (found by compiling against vanilla): `RenderType.create(String, VertexFormat, Mode, int, boolean, boolean, CompositeState)` (DragonRenderer:179), `EntityRenderers.register` (DragonMountsLegacy:70), `BlockEntityType.BlockEntitySupplier` (DMLRegistry:57), `ItemOverrides.<init>()` (DragonEggModel:166-170), `CreativeModeTabs.SPAWN_EGGS/FUNCTIONAL_BLOCKS` (DragonMountsLegacy:110-111). On Fabric: `RenderType.create`, `RenderType$CompositeState`, `BlockEntitySupplier` and the `CreativeModeTabs` key fields are made accessible by **Fabric API's own transitive access wideners** (fabric-transitive-access-wideners-v1, fabric-object-builder-api-v1, fabric-item-group-api-v1; verified by decoding their .accesswidener files), as long as fabric-api is a dependency. `EntityRenderers.register` is not widened - use `EntityRendererRegistry.register`. `ItemOverrides.<init>()` is not widened (see above).

### 3.11 The 3 mixins (resources/dragonmounts.mixins.json)

```json
{ "required": true, "package": "com.github.kay9.dragonmounts.mixins", "minVersion": "0.8.5",
  "compatibilityLevel": "JAVA_21", "refmap": "dragonmounts.mixins.refmap.json",
  "mixins": ["EnsureSafeFlyingVehicleMixin", "ReplenishDragonEggMixin"], "client": ["client.ModelPartMixin"] }
```
(`neoforge.mods.toml` has `[[mixins]] config = "dragonmounts.mixins.json"`.) Fabric: reference it from `fabric.mod.json` `"mixins"`, remove the explicit `refmap` entry (Loom generates `dragonmounts-refmap.json` and injects it), keep `compatibilityLevel` JAVA_21. Use Loom + `officialMojangMappings()` (+ Parchment) so every string target below stays valid.

1. `EnsureSafeFlyingVehicleMixin` -> `ServerGamePacketListenerImpl`. `@Redirect` on the `PUTFIELD clientVehicleIsFloating` inside `handleMoveVehicle(ServerboundMoveVehiclePacket)`; handler `dragonmounts_ensureSafeFlyingVehicle(impl, flag)` sets `clientVehicleIsFloating = (!(impl.getPlayer().getRootVehicle() instanceof FlyingAnimal a) || !a.isFlying()) && flag`. `@Shadow private boolean clientVehicleIsFloating`. Purpose: riders of flying dragons are not kicked for "flying" without disabling the server check. Verified present in vanilla 1.21.1.
2. `ReplenishDragonEggMixin` -> `EndDragonFight`. `@Redirect` on `GETFIELD previouslyKilled` inside `setDragonKilled(EnderDragon)`; returns `!(!instance.hasPreviouslyKilledDragon() || DMLConfig.replenishEggs())`, i.e. the `if (!previouslyKilled)` egg-spawn branch also runs when `replenish_eggs` is on. Verified present (single GETFIELD in that method).
3. `client.ModelPartMixin` -> `ModelPart` (implements `ModelPartAccess`). `@Inject(method = "translateAndRotate(PoseStack)", at = TAIL)` -> `poseStack.scale(dm_xScale, dm_yScale, dm_zScale)`; three `@Unique` floats default 1 with getters/setters. **Redundant on 1.21.1**: vanilla `ModelPart` already declares `public float xScale, yScale, zScale` and applies them in `translateAndRotate` after rotation (javap of the Mojmap 1.21.1 jar). Delete the mixin and the interface; in `ModelPartProxy`/`DragonModel`/`DragonAnimator` use `part.xScale` etc. (`getXScale()` -> `part.xScale`; `setRenderScale(x,y,z)` -> assign the three fields).

### 3.12 Camera manipulation

`NeoModImpl:58` listens to `ViewportEvent.ComputeCameraAngles` (fired every frame after the camera is positioned) and calls `MountCameraManager.setMountCameraAngles(camera)`: if `Minecraft.player.getVehicle() instanceof TameableDragon` and the camera is not first-person, read offsets for the current perspective (`THIRD_PERSON_BACK` -> back, else front), then `camera.move(0, vertical, horizontal)` and `camera.move(-camera.getMaxZoom(distance), 0, 0)` (distance computed after the shift so block collisions are respected). On mount (`TameableDragon.addPassenger:948-951` when the controlling passenger is the local player) -> `MountCameraManager.onDragonMount()` (remember previous `CameraType`, force `THIRD_PERSON_BACK` if `third_person_on_mount`) and `MountControlsMessenger.sendControlsMessage()` (60 tick delay, then actionbar `mount.dragon.vertical_controls` with the jump and descent key names). Dismount (`removePassenger:958`) restores the previous perspective.
Fabric: no viewport event. Add a client mixin `@Mixin(Camera.class)`, `@Inject(method = "setup", at = @At("TAIL"))` calling `MountCameraManager.setMountCameraAngles((Camera)(Object) this)` (or `@Shadow` `move`/`getMaxZoom` in that mixin and drop the AW lines). Vanilla `Camera.setup(BlockGetter, Entity, boolean detached, boolean mirrored, float partialTick)` verified; `move(float,float,float)` is protected, `getMaxZoom(float)` is private.

### 3.13 Attachments / capabilities / networking / payloads

None. Grepped `PacketDistributor|CustomPacketPayload|Payload|Attachment|Capabilit|IItemHandler|getCapability` -> zero hits. All sync is vanilla: entity data (`DATA_BREED` optional holder via custom serializer, `DATA_SADDLED` bool, `DATA_AGE` int), data components (`dragon_breed` with stream codec), the block entity update packet (`ClientboundBlockEntityDataPacket` + `getUpdateTag`), and registry sync (dragon_breeds NETWORK_CODEC, ability_type registry). Client->server controls rely on vanilla rider movement packets (`ServerboundMoveVehiclePacket`, player input). **Nothing to port for networking.**

### 3.14 Key mappings

`KeyMappings.java`: category `key.category.dragonmounts` ("Dragon Mounts"); `flight_descent` -> name `key.dragonmounts.flight_descent` ("Flight Descent"), default `GLFW_KEY_Z`; `camera_flight` -> `key.dragonmounts.camera_flight` ("Flight Commands"), default `GLFW_KEY_F6`. Used: `FLIGHT_DESCENT_KEY.isDown()` in `TameableDragon.getRiddenInput:442` (descend while flying, local driver); `CAMERA_CONTROLS` pressed while on a dragon toggles `camera_driven_flight` and shows actionbar `mount.dragon.camera_controls.enabled|disabled` (with `%s` = dragon display name); `MountControlsMessenger` uses both keys' translated names. Vanilla 1.21.1 `KeyMapping(String, int, String category)` ctor is used, so it compiles unchanged on Fabric (`KeyBindingHelper.registerKeyBinding`).

### 3.15 Data generators and whether `src/generated/resources` can ship statically

Datagen run (`build.gradle` `runs.data`): `--mod dragonmounts --all --output src/generated/resources --existing src/main/resources`. Providers (`data/providers/*`, excluded from the production jar by `exclude('com/github/kay9/dragonmounts/data/providers/**')`):

| Provider | Output (all present in src/generated/resources) |
|---|---|
| `DragonBreedProvider` (`DatapackBuiltinEntriesProvider`) | `data/dragonmounts/dragonmounts/dragon_breeds/*.json` (8) |
| `BlockTagProvider` | `data/dragonmounts/tags/block/` `{fire,forest,ice,nether,water}_dragon_habitat_blocks, hot_feet_burnables, reaper_plant_death, reaper_plant_destruction, reaper_transform` (9 files; all vanilla format, only `minecraft:` ids and `#minecraft:` tags, no `c:`/`forge:` tags) |
| `LootModifierProvider` | `data/neoforge/loot_modifiers/global_loot_modifiers.json` + 8 files in `data/dragonmounts/loot_modifiers/dragonmounts/...` (NeoForge-only) |
| `LootTableProvider` | `data/dragonmounts/loot_table/blocks/dragon_egg.json` (block loot table: `copy_components` from block_entity {`dragonmounts:dragon_breed`, `minecraft:custom_name`} + `copy_state` {`hatch_stage`}, `random_sequence dragonmounts:blocks/dragon_egg`) |
| `ModelPropertiesProvider` (client) | `assets/dragonmounts/models/entity/dragon/breed/properties/{fire,ghost,water}.json` |

**Verdict: the generated outputs are complete.** Ship statically: copy `data/dragonmounts/dragonmounts/`, `data/dragonmounts/tags/`, `data/dragonmounts/loot_table/` and `assets/dragonmounts/models/entity/` into the Fabric project's `src/main/resources`. Do **not** copy `.cache/`, `data/neoforge/`, `data/dragonmounts/loot_modifiers/`. No need to run datagen on Fabric. Caveats: the egg block loot table JSON uses `minecraft:copy_components` with `source: block_entity` - vanilla 1.21 format, fine. The loot table relies on the block entity's `collectImplicitComponents` (already in `HatchableEggBlockEntity:88-94`).
(The cache file timestamps say generated 2025-05-20 with 1.21.1; they correspond to this tree. The later `master` commit that drops `death_loot` does not change any generated breed JSON since the default is omitted.)

### 3.16 JEI

No integration code (no `@JeiPlugin`, no JEI imports in src/main). `build.gradle` only adds `localRuntime "mezz.jei:jei-1.21.1-neoforge:19.21.1.304"` for dev. Nothing to port.

### 3.17 example_addon folder

`example_addon/` is a standalone sample data+resource pack mod (not compiled; `sourceSets` entry commented out in build.gradle). Files: `META-INF/neoforge.mods.toml` (modLoader `lowcodefml`, depends neoforge >=21.1.1, minecraft [1.21], dragonmounts [1.2,)), `pack.mcmeta`, `assets/example_addon/{lang/en_us.json, models/block/dragon_eggs/example_breed_dragon_egg.json, models/entity/dragon/breed/properties/example_breed.json (tail_horns true, middle_tail_scales false), sounds.json, textures/...}`, `data/example_addon/dragonmounts/{dragon_breeds/example_breed.json, cross_breeding/example_crossbreed.json}` and three loot modifier JSONs + `data/forge/loot_modifiers/global_loot_modifiers.json` (still the old Forge path; it should be `data/neoforge/...`; entries even end in `.json`, so the sample is stale/broken on NeoForge). Cross-breed format: `{"parent1": "<breed id>", "parent2": "<breed id>", "child": "<breed id>"}` (order-insensitive; parents compared as `ResourceKey`s). Useful as a test pack for Fabric (breed JSON + egg model + properties) but uses `#forge:eggs`/`#forge:ender_pearls` tags (use `#c:eggs`/`#c:ender_pearls` on Fabric) and `death_loot`.

---------------------------------------------------------------------------------------------------

## 4. Resources

Resource roots: `src/main/resources` (hand-written) and `src/generated/resources` (datagen; added as a resource srcDir). There is **no `data/` folder in `src/main/resources`**; all data comes from `src/generated`.

### 4.1 src/main/resources

| Path | Notes | NeoForge-specific? |
|---|---|---|
| `META-INF/neoforge.mods.toml` | modLoader `javafml`, loaderVersion `[4,)`, license `${license}` (GNU GPLv3), issue tracker `https://github.com/Kay9Unit/Dragon-Mounts-Legacy/issues`, mod id/name/version via gradle `expand` (`mod_id, mod_name, mod_description, mod_version, minecraft_version, neo_version...`), credits "ata4/barracuda - original project and idea. Mojang - Base game and sound assets.", authors `Kay9`, `[[mixins]] config=dragonmounts.mixins.json`, dependencies neoforge `[21.1.1,)` and minecraft `[1.21,1.21.1)` | YES -> write `fabric.mod.json` (entrypoints main+client, mixins, accessWidener `dragonmounts.accesswidener`, depends: fabricloader, fabric-api, forgeconfigapiport (if used), minecraft `~1.21.1`, java >=21; keep credits/license GPL-3.0-only) |
| `META-INF/accesstransformer.cfg` | 3 lines, section 3.10 | YES -> `.accesswidener` |
| `dragonmounts.mixins.json` | section 3.11 | adjust (refmap) |
| `pack.mcmeta` | `{"pack":{"description":"${mod_id} resources","pack_format":48}}` (48 is the 1.21/1.21.1 *data* pack format; resource format is 34; NeoForge doesn't check mod packs, Fabric builds its own mod pack metadata, so it is optional) | no |
| `assets/dragonmounts/blockstates/dragon_egg.json` | single variant `"model": "dragonmounts:block/dragon_egg_models"` | indirectly (target model uses loader) |
| `assets/dragonmounts/models/block/dragon_egg_models.json` | `{"loader":"dragonmounts:dragon_egg"}` | **YES (`loader`)** |
| `assets/dragonmounts/models/block/dragon_eggs/*_dragon_egg.json` (8) | vanilla-format models, parent `minecraft:block/dragon_egg` | no (reusable) |
| `assets/dragonmounts/models/item/dragon_egg.json` | parent = loader model | **YES (inherits loader)** |
| `assets/dragonmounts/models/item/spawn_egg.json` | parent `minecraft:item/template_spawn_egg` (tinted by ItemColors) | no |
| `assets/dragonmounts/sounds.json` + `sounds/entity/dragon/{breathe1,breathe2,death,step1..4}.ogg` | 4 sound events, vanilla format (uses `"type":"event"` redirects to `minecraft:entity.ender_dragon.growl`, `entity.skeleton.ambient`, and `dragonmounts:entity.dragon.ambient`) | no |
| `assets/dragonmounts/lang/{en_us,ja_jp,uk_ua}.json` | keys: `entity.dragonmounts.dragon`, `dragon_breed.dragonmounts.<breed>`, `item.dragonmounts.spawn_egg.dragonmounts.<breed>`, `block.dragonmounts.dragon_egg.dragonmounts.<breed>`, `block.dragonmounts.dragon_egg.hatch_stage.0-3`, `...remaining_time`, `key.category.dragonmounts`, `key.dragonmounts.flight_descent|camera_flight`, `mount.dragon.vertical_controls`, `mount.dragon.camera_controls.enabled|disabled`, `subtitles.entity.dragon.ambient|step|death|ambient.ghost` | no |
| `assets/dragonmounts/textures/block/<breed>_dragon_egg.png(.mcmeta)` | 7 breeds (no `end`; end uses vanilla texture), animated | no |
| `assets/dragonmounts/textures/entity/dragon/<breed>/{body,glow,saddle}.png` (8 breeds) + `dissolve.png` | no | no |

### 4.2 src/generated/resources

| Path | Notes | NeoForge-specific? |
|---|---|---|
| `.cache/*` (5 hash files) | datagen cache | drop |
| `assets/dragonmounts/models/entity/dragon/breed/properties/{fire,ghost,water}.json` | custom client data | no |
| `data/dragonmounts/dragonmounts/dragon_breeds/*.json` (8) | custom datapack registry | no (works with Fabric `DynamicRegistries`) |
| `data/dragonmounts/loot_table/blocks/dragon_egg.json` | vanilla loot table | no |
| `data/dragonmounts/tags/block/*.json` (9) | vanilla tag format with `minecraft:` ids only | no |
| `data/dragonmounts/loot_modifiers/dragonmounts/<breed>/chests/<table>.json` (8) | type `dragonmounts:dragon_egg_loot`, condition `neoforge:loot_table_id` | **YES -> delete, replace with code** |
| `data/neoforge/loot_modifiers/global_loot_modifiers.json` | GLM index | **YES -> delete** |

Namespaces check: no `forge:`/`c:`/`neoforge:` item/block/fluid tags in any of the mod's own data besides the loot-modifier condition and the example addon. Code-side tag reference: `Tags.Items.TOOLS_SHEAR` = `c:tools/shear` (NeoForge `Tags.java:844` `tag("tools/shear")`). Fabric API (convention-tags v2, verified by bytecode) has `net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.SHEAR_TOOLS` == `c:tools/shear` (exact same id; `SHEARS_TOOLS` is `c:tools/shears`, not the same). Use `SHEAR_TOOLS`.

### 4.3 Conversion checklist

1. Delete `data/neoforge`, `data/dragonmounts/loot_modifiers`, `.cache`.
2. Replace the `loader` model mechanism (3.9).
3. Write `fabric.mod.json`, `.accesswidener`, adjust mixin config.
4. Keep everything else verbatim (blockstate/models/textures/sounds/lang/tags/breeds/properties/loot table).

---------------------------------------------------------------------------------------------------

## 5. Vanilla classes extended/overridden, and NeoForge-only methods

### 5.1 Heavily extended/overridden vanilla classes

- `TameableDragon extends TamableAnimal implements Saddleable, FlyingAnimal, PlayerRideable, VariantHolder<Holder<DragonBreed>>`. Overrides (all vanilla-valid unless listed in 5.2): `createBodyControl` (returns `DragonBodyController`), `registerGoals` (FloatGoal, SitWhenOrdered, MeleeAttack, DragonFollowOwnerGoal, DragonBreedGoal, WaterAvoidingRandomStroll, LookAtPlayer, RandomLookAround; targets OwnerHurtBy/OwnerHurtTarget/HurtBy/NonTameRandomTarget), `defineSynchedData(Builder)`, `onSyncedDataUpdated`, `addAdditionalSaveData`/`readAdditionalSaveData`, `tick`, `travel(Vec3)` (custom flying movement), `getRiddenInput`, `tickRidden`, `getRiddenSpeed`, `mobInteract`, `getJumpPower`, `causeFallDamage`, `tickDeath` (custom 120 tick dissolve), `getAmbientSound`, `getHurtSound`, `getDeathSound`, `getEatingSound`, `playStepSound`, `playAmbientSound`, `getAmbientSoundInterval`, `getSoundVolume`, `getVoicePitch`, `getTypeName`, `isFood`, `removeWhenFarAway`, `onClimbable`, `dropEquipment`, `getDefaultLootTable` (returns breed `deathLoot()`), `swing`, `hurt`, `canMate`, `spawnChildFromBreeding` (places an egg block instead of a baby), `getBreedOffspring`, `wantsToAttack`, `canAttack`, `getControllingPassenger`, `addPassenger`, `removePassenger`, `positionRider(Entity, MoveFunction)`, `getPassengerAttachmentPoint`, `isInvulnerableTo`, `refreshDimensions`, `getDefaultDimensions(Pose)`, `getAgeScale`, `getAge`, `isBaby`, `setBaby`, `ageUp(int, boolean)`, `fireImmune`, `onChangedBlock(ServerLevel, BlockPos)`, `isInWall`, `getLightProbePosition(float)`, plus interface methods `isSaddleable`, `equipSaddle(ItemStack, SoundSource)`, `isSaddled`, `setVariant/getVariant`. I verified each against the vanilla Mojmap 1.21.1 jar with javap/javac: all of the above are vanilla (including `Entity.getLightProbePosition(float)`, `Entity.isInWall()`, `LivingEntity.onChangedBlock`, `LivingEntity.getRiddenInput/tickRidden/getRiddenSpeed`, `Mob.getDefaultLootTable() : ResourceKey<LootTable>`).
- `HatchableEggBlock extends DragonEggBlock implements EntityBlock, SimpleWaterloggedBlock` (overrides `useWithoutItem`, `attack`, `appendHoverText`, `updateShape`, `getFluidState`, `tick`, `isRandomlyTicking`, `randomTick`, `animateTick`, `newBlockEntity`, `getTicker`, `createBlockStateDefinition`). `HatchableEggBlock.Item extends BlockItem` (`verifyComponentsAfterLoad`, `getName`). `HatchableEggBlockEntity extends BlockEntity implements Nameable` (1.21 `saveAdditional/loadAdditional(tag, provider)`, `collectImplicitComponents`, `applyImplicitComponents`, `getUpdateTag`, `getUpdatePacket`). All vanilla.
- `DragonSpawnEgg extends DeferredSpawnEggItem` (NeoForge; vanilla parent is `SpawnEggItem`): overrides `verifyComponentsAfterLoad`, `getName(ItemStack)`, `spawnOffspringFromSpawnEgg(Player, Mob, EntityType, ServerLevel, Vec3, ItemStack)` - all exist on vanilla `SpawnEggItem`/`Item` in 1.21.1 (verified).
- Goals/controls: `DragonBreedGoal extends BreedGoal` (uses protected `partner`, `level`, `breed()`), `DragonFollowOwnerGoal extends Goal`, `DragonMoveController extends MoveControl` (protected `wantedX/Y/Z`, `operation`, `speedModifier`, `rotlerp`), `DragonBodyController extends BodyRotationControl`.
- Client: `DragonRenderer extends MobRenderer<TameableDragon, DragonModel>` (overrides `shouldRender`, `render`, `getRenderType`, `getTextureLocation`, `setupRotations(entity, ps, age, yaw, partials, scale)`, `getFlipDegrees`; inner `RenderLayer`s; inner `CustomRenderTypes extends RenderType` to reach protected `RENDERTYPE_EYES_SHADER` etc.), `DragonModel extends EntityModel<TameableDragon>`.
- Listeners: `CrossBreedingManager`, `DragonModelPropertiesListener` extend `SimpleJsonResourceReloadListener`.

### 5.2 Every NeoForge-only method/symbol (compile-verified against vanilla 1.21.1)

Each item is a place where vanilla 1.21.1 would not compile (or would silently never call the override):

| # | File:line | Symbol | Why NeoForge-only | Fabric/vanilla replacement |
|---|---|---|---|---|
| 1 | `TameableDragon:692-697` | `@Override public ItemStack getPickedResult(HitResult target)` (`IEntityExtension`) | vanilla only has `Entity.getPickResult()` (public, `@Nullable ItemStack`) - verified | override `getPickResult()`; same body (`getBreed()==null ? ItemStack.EMPTY : DragonSpawnEgg.create(getBreedHolder())`; return `null`/EMPTY per vanilla semantics) |
| 2 | `TameableDragon:1139-1144` | `@Override public boolean canDrownInFluidType(FluidType)` + `super.canDrownInFluidType` (`IEntityExtension`, `net.neoforged.neoforge.fluids.FluidType` import line 67) | not in vanilla; vanilla `LivingEntity.canBreatheUnderwater()` is `public final` (verified) so it can't be overridden | drop the override. Drown damage is already blocked by `isInvulnerableTo` (immunities contain the `drown` damage type). Optional: `@Mixin(LivingEntity)` `@Inject(HEAD, cancellable) canBreatheUnderwater` returning true for a `TameableDragon` whose breed is drown-immune (avoids the air-bubble particles) |
| 3 | `TameableDragon:66,524` | `Tags.Items.TOOLS_SHEAR` (`c:tools/shear`) | `net.neoforged.neoforge.common.Tags` | `ConventionalItemTags.SHEAR_TOOLS` / explicit `TagKey` / `Items.SHEARS` |
| 4 | `TameableDragon:661` | `Blocks.SNOW.getSoundType(state, level(), entityPos, this)` (`IBlockExtension`) | vanilla `BlockBehaviour.getSoundType(BlockState)` is protected | `Blocks.SNOW.defaultBlockState().getSoundType()` |
| 5 | `TameableDragon:441, 472` | `driver.jumping` | protected field; needs AT/AW (own AT) | AW (3.10) |
| 6 | `HatchableEggBlock:111-118` | `@Override getCloneItemStack(BlockState, HitResult, LevelReader, BlockPos, Player)` (`IBlockExtension`) | vanilla signature is `getCloneItemStack(LevelReader, BlockPos, BlockState)` (verified) | override the vanilla one; the missing `Player` was only used for `player.getRandom()` in the no-block-entity fallback -> use `RandomSource.create()` or `DragonBreed.getRandom(level.registryAccess(), ...)` |
| 7 | `HatchableEggBlock:159-170` | `@Override onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid)` + `super.onDestroyedByPlayer` (`IBlockExtension`) | not in vanilla (vanilla has `playerWillDestroy`, which cannot veto the removal) | purpose: non-creative player "breaking" a non-hatching `end` egg must NOT destroy it (it only teleports via `attack`). Use `PlayerBlockBreakEvents.BEFORE` (verified: `boolean beforeBlockBreak(Level, Player, BlockPos, BlockState, BlockEntity)`; return false to cancel) with the same condition, or a mixin on `ServerPlayerGameMode.destroyBlock`. Drop the `super` call (the default path just proceeds) |
| 8 | `HatchableEggBlock:52,370`, `DragonSpawnEgg:23,49` | `ServerLifecycleHooks.getCurrentServer()` | NeoForge static server accessor | see 3.7 |
| 9 | `DragonSpawnEgg:22,28,~32` | `DeferredSpawnEggItem` | NeoForge class | vanilla `SpawnEggItem` (ctor takes the `EntityType` directly; verified) |
| 10 | `HotFeetAbility:19`, `ReaperStepAbility:26,27,28`, `BlockTagProvider:19-23`, `DragonBreedProvider:85` | `BlockTags.create(ResourceLocation)` | vanilla `BlockTags.create(String)` is private/String-only (javac: "ResourceLocation cannot be converted to String"); NeoForge patches in the `ResourceLocation` overload | `TagKey.create(Registries.BLOCK, DragonMountsLegacy.id("..."))` (a nicer way: add `DMLUtil.blockTag(String)`) |
| 11 | `FootprintAbility:13` | `EventHooks.canEntityGrief(level, entity)` | NeoForge event hook | gamerule `RULE_MOBGRIEFING` |
| 12 | `FrostWalkerAbility:63` | `EventHooks.onBlockPlace(...)`, `BlockSnapshot.create(...)` | NeoForge | remove |
| 13 | `DragonBreedGoal:53` | `NeoForge.EVENT_BUS.post(new BabyEntitySpawnEvent(...))` | NeoForge | remove |
| 14 | `DragonRenderer:179` | `RenderType.create(7 args)` | private in vanilla; NeoForge AT | Fabric API transitive AW (present), or `RenderType.eyes(texture)` / `entityTranslucentEmissive(texture)` as visual stand-ins |
| 15 | `DMLRegistry:57` | `BlockEntityType.Builder.of(HatchableEggBlockEntity::new, ...)` | `BlockEntitySupplier` private; NeoForge AT | Fabric API object-builder AW makes it extendable; or `FabricBlockEntityTypeBuilder.create(...)` (verified class exists) |
| 16 | `DragonMountsLegacy:70` | `EntityRenderers.register(...)` | private in vanilla; NF AT | `EntityRendererRegistry.register` |
| 17 | `DragonMountsLegacy:110-111` | `CreativeModeTabs.SPAWN_EGGS`, `FUNCTIONAL_BLOCKS` | private in vanilla; NF AT | Fabric item-group AW / `ItemGroupEvents` |
| 18 | `DragonEggModel:166-170` | `extends ItemOverrides` + implicit `super()` | private no-arg ctor; NF AT | see 3.9 |
| 19 | `DragonEggModel` (whole file) | `IUnbakedGeometry`, `IGeometryBakingContext`, `IGeometryLoader`, `IDynamicBakedModel`, `ModelData`, `ModelProperty`, `applyTransform`, `getParticleIcon(ModelData)`, `getQuads(..., ModelData, RenderType)` | NeoForge client model API | 3.9 |
| 20 | `DragonEggLootMod` | `LootModifier`, `IGlobalLootModifier`, `codecStart` | NeoForge GLM | 3.4 |
| 21 | `DMLConfig` | `ModConfigSpec` | NeoForge config | 3.6 |
| 22 | `DataProvider`, `*Provider` | datagen classes/events | NeoForge datagen | drop (3.15) |

NeoForge also **patches vanilla at runtime** in ways that matter even where the code compiles on vanilla (behaviour differences to watch on Fabric):
- Pick-block calling `getPickedResult` (use `getPickResult`, #1) and `Block.getCloneItemStack` with the HitResult overload (#6).
- `onDestroyedByPlayer` hook (#7).
- `canDrownInFluidType` hook (#2).
- Fall-damage, `travel`, fluid-type swimming logic in `LivingEntity.travel` is patched by NeoForge; the dragon overrides `travel` fully for the flying branch and calls `super.travel(vec3)` only when not flying, so the vanilla (unpatched) behaviour is acceptable.
- The right-click-block event ordering (NeoForge fires `RightClickBlock` before item/block use; Fabric `UseBlockCallback` fires at the same point).
- `Block.getSoundType(state, level, pos, entity)` (#4), `DragonSpawnEgg` registered by NeoForge in `SpawnEggItem` maps and dispenser behaviour (vanilla ctor does it on Fabric).
- Registration timing (DeferredRegister runs at `RegisterEvent`, Fabric registers immediately at init: keep the dependency order of 3.3 and make sure client-only classes are not touched in common init).

### 5.3 Client/dedicated-server class references in common classes (same as upstream)

`HatchableEggBlock` (7, 84-86) and `DragonSpawnEgg` (4, 98-100) import `net.minecraft.client.Minecraft` and call it inside the `populateTab` static methods; `TameableDragon` references `client.KeyMappings` (442), `client.MountCameraManager`, `client.MountControlsMessenger`, `client.DragonAnimator`. They only execute client-side (guarded by `level.isClientSide`/`hasLocalDriver()`/event registration under `Dist.CLIENT`). Same pattern works on Fabric dedicated servers as long as the lambdas/methods are not invoked there; if a crash appears, move the creative-tab population to client-only code (`ClientModInitializer`).

---------------------------------------------------------------------------------------------------

## 6. `master` (Forge 1.21) vs `neoforge` (NeoForge 1.21.1)

- Merge base: `31abc86` ("update workflows to support multiplatform", 2025-05-20). `git rev-list --left-right --count origin/master...origin/neoforge` = **2 | 4**.
- **master-only (2 commits, both 2025-05-20, newer features/fixes missing on neoforge):**
  1. `9eb18b9` "separate loottable fields from dragon breeds and expand customization to the base dragon entity, fixes #266": removes the `death_loot` field from `DragonBreed` (record shrinks from 15 to 14 components, codec and `fromNetwork` updated); adds `DragonBreed.getLootTableKey(Holder<DragonBreed>, ReloadableServerRegistries.Holder)` which resolves `<ns>:entities/dragon_breeds/<breed>` from the server's reloadable registries; `TameableDragon.getDefaultLootTable()` now uses it on the server and otherwise `super.getDefaultLootTable()` (the base `dragonmounts:entities/dragon` loot table). Datapack authors can thus supply a per-breed or base dragon loot table; `DragonBreedProvider.builtIn` drops the `BuiltInLootTables.EMPTY` argument. Generated JSON unchanged (default was omitted anyway).
  2. `3c4c74e` "Fix a crash when opening the creative inventory on a ded. server; fixes #259": replaces `ServerLifecycleHooks.getCurrentServer()` in `DragonSpawnEgg.verifyComponentsAfterLoad` and `HatchableEggBlock.Item.verifyComponentsAfterLoad` with `DistExecutor.safeRunForDist(() -> Minecraft.getInstance().level::registryAccess, () -> ServerLifecycleHooks.getCurrentServer()::registryAccess)` and `RandomSource.create()` instead of `server.overworld().getRandom()`; also a javadoc tweak. (The neoforge branch still contains the NPE-prone code, see 3.7.)
- **neoforge-only (4 commits):** `404f06f` "port to neoforge 1.21.1", `59d6559`, `e8fb514` (gitignore/"unversioned"), merge `d055255` "Merge branch 'master' into neoforge" (this merged master up to `31abc86` only). Differences are the loader port: `ForgeModImpl` -> `NeoModImpl`, `IForgeRegistry`/`RegistryManager` -> vanilla `Registry` + `DMLUtil.castRegistry`, `mods.toml` -> `neoforge.mods.toml`, `DistExecutor`, `Tags.Items.SHEARS` -> `TOOLS_SHEAR`, `Holder.get()` -> `value()`, `forge:loot_table_id` -> `neoforge:loot_table_id`, `data/forge` -> `data/neoforge`, SRG names in the AT -> Mojmap, game version `1.21` (Forge 51.0.33) -> `1.21.1` (NeoForge 21.1.1).
- `git diff --stat origin/neoforge origin/master`: 47 files, +411/-320, essentially all loader-API renames plus the two commits above.
- **Recommendation: port from the `neoforge` branch.** It targets the exact MC version (1.21.1), uses Mojmap names and vanilla `Registry`-based helpers (no `IForgeRegistry`), so most files copy to Fabric unchanged. Then hand-apply the semantic content of the two master-only commits (they touch only `DragonBreed`, `DragonBreedProvider` (not shipped), `TameableDragon.getDefaultLootTable`, and the two `verifyComponentsAfterLoad` methods, i.e. about 4 small edits) - recommended, because it removes the `death_loot` coupling and the creative-inventory crash that the Fabric port will otherwise inherit. If you apply 9eb18b9, the example addon's `death_loot` field must go too (and note the change when documenting the breed JSON format). If you prefer to keep breed JSON compatibility with released 1.21.1 NeoForge builds, keep `death_loot`.

---------------------------------------------------------------------------------------------------

## 7. Suggested Fabric project skeleton (derived from the above)

- Build: Fabric Loom, `mappings loom.officialMojangMappings()` + Parchment `2024.11.17` for 1.21.1; Java 21; dependencies: `net.fabricmc.fabric-api:fabric-api:0.116.x+1.21.1` (0.116.17 inspected), `fuzs.forgeconfigapiport:forgeconfigapiport-fabric:21.1.x` (config).
- Entrypoints: `DragonMountsLegacyFabric implements ModInitializer` (registries, registry sync, attributes, UseBlockCallback, reload listener, loot event, break-event veto, server tracking for `ServerLifecycleEvents`, config registration, entity data serializer) and `DragonMountsLegacyClient implements ClientModInitializer` (renderer, layer, item color, key binding, client tick, creative tab events, model loading plugin, properties reload listener).
- Reusable verbatim: DragonBreed, Ability/Habitat families, all habitats and 4 of 6 abilities, AI goals, util, CrossBreedingManager logic, HatchableEggBlockEntity, RandomChanceByConfig, DragonAnimator/DragonModel/ModelPartProxy (after scale-field swap), MountControlsMessenger, 2 mixins, all assets and the generated data (minus loot modifiers).
- Needs real work: egg model (3.9), per-breed model baking (3.8), loot modifiers (3.4), DragonSpawnEgg/Item registry access (3.7), config library (3.6), camera mixin (3.12).
- Licensing: repo is GPLv3 (LICENSE.md). A Fabric port is a derivative work: keep GPLv3, the original copyright headers (@author Nico Bergemann / Kay9) and credits ("ata4/barracuda - original project and idea. Mojang - Base game and sound assets.").
