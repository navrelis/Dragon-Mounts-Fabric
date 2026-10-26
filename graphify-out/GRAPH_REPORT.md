# Graph Report - src  (2026-10-26)

## Corpus Check
- Corpus is ~37,750 words - fits in a single context window. You may not need a graph.

## Summary
- 953 nodes · 2705 edges · 50 communities (43 shown, 7 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 73 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Jade Plugin & Block Imports
- Egg Model (FRAPI)
- Dragon Abilities Core
- Entity Attributes & Registry
- Game Tests
- Habitat Codecs
- Dragon Breed Data
- Dragon Model Geometry
- Breed JSON Loading
- TameableDragon Core
- Dragon Animator
- Egg Block Entity
- Dragon Sounds & Body
- Spawn Egg & Components
- Habitat Types
- Breeding & Reproduction
- Jade Providers
- Green Toes Ability
- Loot Condition & Registries
- Animation Buffers
- Config (DMLConfig)
- Follow Owner Goal
- Game Test Helpers
- EMI Plugin
- Dragon Render Types
- Fabric Init & Loot Events
- Mount Camera
- Model Part Rendering
- Flight & Riding Input
- Dragon Renderer
- Client Init
- Egg Replenish Mixin
- Test Random Source
- Spawn Egg Item
- Saddle & Taming
- Mount Controls Messenger
- Mod Logging & Registries
- Habitat Transitions
- Egg Item
- Passengers & Riding
- Growth & Dimensions
- Keybinds
- Cross-Breeding
- Egg Loot Targets
- Clamped Values
- Flying Kick Mixin
- Common Entrypoint
- Built-in Breeds
- Hot Feet Ability

## God Nodes (most connected - your core abstractions)
1. `TameableDragon` - 179 edges
2. `DragonBreed` - 75 edges
3. `HatchableEggBlock` - 41 edges
4. `DragonModel` - 37 edges
5. `DragonMountsGameTests` - 36 edges
6. `HatchableEggBlockEntity` - 31 edges
7. `DragonAnimator` - 28 edges
8. `DMLRegistry` - 27 edges
9. `DMLConfig` - 26 edges
10. `Habitat` - 23 edges

## Surprising Connections (you probably didn't know these)
- `DragonMountsGameTests` --references--> `DragonBreed`  [EXTRACTED]
  gametest/java/com/github/kay9/dragonmounts/gametest/DragonMountsGameTests.java → main/java/com/github/kay9/dragonmounts/dragon/DragonBreed.java
- `DMLRegistry` --references--> `DragonBreed`  [EXTRACTED]
  main/java/com/github/kay9/dragonmounts/DMLRegistry.java → main/java/com/github/kay9/dragonmounts/dragon/DragonBreed.java
- `DMLRegistry` --references--> `HatchableEggBlockEntity`  [EXTRACTED]
  main/java/com/github/kay9/dragonmounts/DMLRegistry.java → main/java/com/github/kay9/dragonmounts/dragon/egg/HatchableEggBlockEntity.java
- `DMLRegistry` --references--> `TameableDragon`  [EXTRACTED]
  main/java/com/github/kay9/dragonmounts/DMLRegistry.java → main/java/com/github/kay9/dragonmounts/dragon/TameableDragon.java
- `DragonAnimator` --references--> `TameableDragon`  [EXTRACTED]
  main/java/com/github/kay9/dragonmounts/client/DragonAnimator.java → main/java/com/github/kay9/dragonmounts/dragon/TameableDragon.java

## Import Cycles
- None detected.

## Communities (50 total, 7 thin omitted)

### Community 0 - "Jade Plugin & Block Imports"
Cohesion: 0.06
Nodes (36): blockbehaviour, blockitemstateproperties, fallingblockentity, fluids, DragonMountsJadePlugin, Override, HatchableEggBlock, Builder (+28 more)

### Community 1 - "Egg Model (FRAPI)"
Cohesion: 0.07
Nodes (30): completablefuture, envtype, executor, fabricloader, Baked, DragonEggModel, ItemModelResolver, Context (+22 more)

### Community 2 - "Dragon Abilities Core"
Cohesion: 0.06
Nodes (21): blockpos, collisioncontext, fabricregistrybuilder, frostediceblock, Ability, Factory, FootprintAbility, Override (+13 more)

### Community 3 - "Entity Attributes & Registry"
Cohesion: 0.05
Nodes (34): attributemodifier, attributes, attributesupplier, booleanop, bytebufcodecs, conventionalitemtags, experienceorb, gameevent (+26 more)

### Community 4 - "Game Tests"
Cohesion: 0.16
Nodes (7): DragonMountsGameTests, BooleanValue, TestableEnderDragon, net.fabricmc.fabric.api.gametest.v1.FabricGameTest, net.minecraft.gametest.framework.GameTest, net.minecraft.gametest.framework.GameTestHelper, net.minecraft.resources.ResourceKey

### Community 5 - "Habitat Codecs"
Cohesion: 0.11
Nodes (18): codec, com.mojang.serialization.MapCodec, Override, DragonBreathHabitat, INSTANCE, Override, FluidHabitat, Override (+10 more)

### Community 6 - "Dragon Breed Data"
Cohesion: 0.12
Nodes (19): attributeinstance, com.mojang.datafixers.util.Either, com.mojang.serialization.Codec, core, HolderSet, CrossBreedResult, DragonBreed, Holder (+11 more)

### Community 7 - "Dragon Model Geometry"
Cohesion: 0.17
Nodes (10): DragonModel, SuppressWarnings, meshdefinition, net.minecraft.client.model.EntityModel, net.minecraft.client.model.geom.builders.CubeListBuilder, net.minecraft.client.model.geom.builders.LayerDefinition, net.minecraft.client.model.geom.builders.PartDefinition, net.minecraft.client.model.geom.ModelPart (+2 more)

### Community 8 - "Breed JSON Loading"
Cohesion: 0.15
Nodes (14): com.google.gson.JsonElement, gsonbuilder, hashmap, jsonops, jsonparseexception, Target, DragonModelPropertiesListener, Override (+6 more)

### Community 9 - "TameableDragon Core"
Cohesion: 0.13
Nodes (8): Builder, EntityType, TameableDragon, net.minecraft.network.syncher.EntityDataAccessor, PlayerRideable, Saddleable, TamableAnimal, VariantHolder

### Community 10 - "Dragon Animator"
Cohesion: 0.14
Nodes (3): DragonAnimator, SuppressWarnings, Override

### Community 11 - "Egg Block Entity"
Cohesion: 0.23
Nodes (8): DataComponentInput, HatchableEggBlockEntity, Builder, Override, Provider, SuppressWarnings, net.minecraft.core.Holder, net.minecraft.network.chat.Component

### Community 12 - "Dragon Sounds & Body"
Cohesion: 0.14
Nodes (3): Override, net.minecraft.sounds.SoundEvent, net.minecraft.world.damagesource.DamageSource

### Community 13 - "Spawn Egg & Components"
Cohesion: 0.11
Nodes (18): block, clientboundblockentitydatapacket, compoundtag, customdata, datacomponentmap, datacomponents, holderlookup, nbtops (+10 more)

### Community 14 - "Habitat Types"
Cohesion: 0.18
Nodes (12): com.mojang.serialization.codecs.RecordCodecBuilder, dataresult, BiomeHabitat, NearbyBlocksHabitat, DMLUtil, SuppressWarnings, net.minecraft.core.Registry, net.minecraft.tags.TagKey (+4 more)

### Community 15 - "Breeding & Reproduction"
Cohesion: 0.15
Nodes (3): AgeableMob, SuppressWarnings, net.minecraft.world.entity.animal.Animal

### Community 16 - "Jade Providers"
Cohesion: 0.18
Nodes (13): chatformatting, DragonEggProvider, INSTANCE, Override, DragonProvider, INSTANCE, Override, snownee.jade.api.BlockAccessor (+5 more)

### Community 17 - "Green Toes Ability"
Cohesion: 0.20
Nodes (12): blocks, blocktags, bonemealableblock, farmblock, itementity, itemstack, levelevent, net.minecraft.core.BlockPos (+4 more)

### Community 18 - "Loot Condition & Registries"
Cohesion: 0.18
Nodes (13): abilities, builtinregistries, habitats, Builder, RandomChanceByConfig, DMLRegistry, mapcodec, mobcategory (+5 more)

### Community 19 - "Animation Buffers"
Cohesion: 0.18
Nodes (4): arrays, CircularBuffer, LerpedFloat, mth

### Community 20 - "Config (DMLConfig)"
Cohesion: 0.17
Nodes (8): com.google.common.collect.ImmutableMap, DoubleValue, IntValue, Override, DMLConfig, BooleanValue, Builder, net.minecraft.world.level.storage.loot.LootContext

### Community 21 - "Follow Owner Goal"
Cohesion: 0.16
Nodes (6): enumset, leavesblock, DragonFollowOwnerGoal, net.minecraft.world.entity.ai.goal.Goal, net.minecraft.world.level.LevelReader, walknodeevaluator

### Community 22 - "Game Test Helpers"
Cohesion: 0.13
Nodes (14): aabb, blockstate, entitytype, field, gametestassertexception, gametype, items, lootcontextparams (+6 more)

### Community 23 - "EMI Plugin"
Cohesion: 0.15
Nodes (14): arraylist, comparison, dev.emi.emi.api.EmiEntrypoint, dev.emi.emi.api.EmiPlugin, dev.emi.emi.api.EmiRegistry, emiinforecipe, emiingredient, emistack (+6 more)

### Community 24 - "Dragon Render Types"
Cohesion: 0.13
Nodes (13): axis, defaultvertexformat, entityrendererprovider, CustomRenderTypes, net.minecraft.client.model.geom.ModelLayerLocation, net.minecraft.client.renderer.culling.Frustum, net.minecraft.client.renderer.entity.layers.RenderLayer, net.minecraft.client.renderer.entity.MobRenderer (+5 more)

### Community 25 - "Fabric Init & Loot Events"
Cohesion: 0.13
Nodes (14): constantvalue, dynamicregistries, entitydataserializers, fabricdefaultattributeregistry, interactionresult, lootitem, lootpool, loottableevents (+6 more)

### Community 26 - "Mount Camera"
Cohesion: 0.23
Nodes (8): SuppressWarnings, MountCameraManager, CameraMixin, net.minecraft.client.Camera, net.minecraft.client.CameraType, org.spongepowered.asm.mixin.injection.callback.CallbackInfo, org.spongepowered.asm.mixin.injection.Inject, org.spongepowered.asm.mixin.Mixin

### Community 27 - "Model Part Rendering"
Cohesion: 0.26
Nodes (4): com.mojang.blaze3d.vertex.PoseStack, com.mojang.blaze3d.vertex.VertexConsumer, SuppressWarnings, ModelPartProxy

### Community 29 - "Dragon Renderer"
Cohesion: 0.26
Nodes (4): DragonRenderer, Context, Override, SuppressWarnings

### Community 30 - "Client Init"
Cohesion: 0.18
Nodes (11): clienttickevents, colorproviderregistry, creativemodetabs, entitymodellayerregistry, entityrendererregistry, itemgroupevents, keybindinghelper, DragonMountsFabricClient (+3 more)

### Community 31 - "Egg Replenish Mixin"
Cohesion: 0.39
Nodes (7): com.llamalad7.mixinextras.injector.wrapoperation.Operation, com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation, endpodiumfeature, heightmap, ReplenishDragonEggMixin, net.minecraft.world.entity.boss.enderdragon.EnderDragon, net.minecraft.world.level.dimension.end.EndDragonFight

### Community 32 - "Test Random Source"
Cohesion: 0.29
Nodes (3): Override, LuckyRandom, net.minecraft.world.level.levelgen.PositionalRandomFactory

### Community 33 - "Spawn Egg Item"
Cohesion: 0.23
Nodes (5): Properties, DragonSpawnEgg, Override, Provider, net.minecraft.world.item.SpawnEggItem

### Community 35 - "Mount Controls Messenger"
Cohesion: 0.20
Nodes (7): component, consumer, glfw, localplayer, SuppressWarnings, MountControlsMessenger, minecraft

### Community 36 - "Mod Logging & Registries"
Cohesion: 0.33
Nodes (8): function, logmanager, DragonMountsLegacy, org.apache.logging.log4j.Logger, registry, resourcekey, supplier, suppliers

### Community 37 - "Habitat Transitions"
Cohesion: 0.29
Nodes (3): Reference, TransitionHandler, net.minecraft.nbt.CompoundTag

### Community 38 - "Egg Item"
Cohesion: 0.29
Nodes (5): SuppressWarnings, Item, Provider, net.minecraft.world.item.BlockItem, net.minecraft.world.item.ItemStack

### Community 39 - "Passengers & Riding"
Cohesion: 0.22
Nodes (3): Entity, LivingEntity, MoveFunction

### Community 41 - "Keybinds"
Cohesion: 0.27
Nodes (4): SuppressWarnings, KeyMappings, Override, net.minecraft.client.KeyMapping

### Community 42 - "Cross-Breeding"
Cohesion: 0.31
Nodes (4): Couple, CrossBreedingManager, Override, Provider

### Community 44 - "Egg Loot Targets"
Cohesion: 0.28
Nodes (4): builtin, immutablelist, DragonEggLootTargets, net.neoforged.neoforge.common.ModConfigSpec

### Community 46 - "Flying Kick Mixin"
Cohesion: 0.38
Nodes (5): at, EnsureSafeFlyingVehicleMixin, net.minecraft.server.network.ServerGamePacketListenerImpl, net.minecraft.world.entity.animal.FlyingAnimal, opcodes

### Community 47 - "Common Entrypoint"
Cohesion: 0.38
Nodes (3): DragonMountsFabric, Override, net.fabricmc.api.ModInitializer

## Knowledge Gaps
- **3 isolated node(s):** `INSTANCE`, `INSTANCE`, `INSTANCE`
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 175 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **7 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `TameableDragon` connect `TameableDragon Core` to `Jade Plugin & Block Imports`, `Dragon Abilities Core`, `Entity Attributes & Registry`, `Game Tests`, `Dragon Breed Data`, `Dragon Model Geometry`, `Breed JSON Loading`, `Dragon Animator`, `Egg Block Entity`, `Dragon Sounds & Body`, `Spawn Egg & Components`, `Breeding & Reproduction`, `Jade Providers`, `Green Toes Ability`, `Loot Condition & Registries`, `Animation Buffers`, `Follow Owner Goal`, `Game Test Helpers`, `Dragon Render Types`, `Fabric Init & Loot Events`, `Mount Camera`, `Flight & Riding Input`, `Dragon Renderer`, `Saddle & Taming`, `Mount Controls Messenger`, `Mod Logging & Registries`, `Habitat Transitions`, `Passengers & Riding`, `Growth & Dimensions`, `Dragon Save State`, `Egg Loot Targets`, `Flying Kick Mixin`, `Hot Feet Ability`?**
  _High betweenness centrality (0.353) - this node is a cross-community bridge._
- **Why does `DragonBreed` connect `Dragon Breed Data` to `Jade Plugin & Block Imports`, `Egg Model (FRAPI)`, `Dragon Abilities Core`, `Game Tests`, `Habitat Codecs`, `Breed JSON Loading`, `TameableDragon Core`, `Egg Block Entity`, `Dragon Sounds & Body`, `Spawn Egg & Components`, `Breeding & Reproduction`, `Jade Providers`, `Loot Condition & Registries`, `Game Test Helpers`, `EMI Plugin`, `Dragon Render Types`, `Fabric Init & Loot Events`, `Dragon Renderer`, `Spawn Egg Item`, `Mod Logging & Registries`, `Habitat Transitions`, `Egg Item`, `Cross-Breeding`, `Dragon Save State`, `Egg Loot Targets`, `Built-in Breeds`?**
  _High betweenness centrality (0.115) - this node is a cross-community bridge._
- **Why does `DragonAnimator` connect `Dragon Animator` to `Entity Attributes & Registry`, `TameableDragon Core`, `Growth & Dimensions`, `Animation Buffers`?**
  _High betweenness centrality (0.059) - this node is a cross-community bridge._
- **What connects `INSTANCE`, `INSTANCE`, `INSTANCE` to the rest of the system?**
  _3 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Jade Plugin & Block Imports` be split into smaller, more focused modules?**
  _Cohesion score 0.06451612903225806 - nodes in this community are weakly interconnected._
- **Should `Egg Model (FRAPI)` be split into smaller, more focused modules?**
  _Cohesion score 0.0677555958862674 - nodes in this community are weakly interconnected._
- **Should `Dragon Abilities Core` be split into smaller, more focused modules?**
  _Cohesion score 0.06009783368273934 - nodes in this community are weakly interconnected._