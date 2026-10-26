# Graph Report - src  (2026-10-26)

## Corpus Check
- 100 files · ~37,413 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 10 file(s) not represented in the graph (top: .mcmeta 8, (none) 1, .accesswidener 1)

## Summary
- 980 nodes · 2718 edges · 55 communities (44 shown, 11 thin omitted)
- Extraction: 97% EXTRACTED · 3% INFERRED · 0% AMBIGUOUS · INFERRED: 73 edges (avg confidence: 0.81)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `93a9c110`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- DragonBreed
- DMLConfig
- Ability
- DragonMountsGameTests
- com.mojang.serialization.MapCodec
- DragonEggModel.java
- net.minecraft.core.BlockPos
- CrossBreedingManager.java
- TameableDragon.java
- Override
- DragonModel
- DragonFollowOwnerGoal
- TameableDragon
- DragonAnimator
- HatchableEggBlock.java
- DragonEggProvider.java
- .getBreed
- DragonMountsGameTests.java
- GreenToesAbility.java
- .tick
- DragonMountsLegacy
- DragonMountsEmiPlugin.java
- net.minecraft.resources.ResourceLocation
- DragonMountsFabricClient.java
- DragonMountsFabric.java
- ReaperStepAbility.java
- SidedAccess.java
- com.mojang.blaze3d.vertex.PoseStack
- DragonRenderer.java
- dmlregistry
- .isFlying
- .mobInteract
- DMLRegistry.java
- KeyMappings.java
- list
- LuckyRandom
- DragonRenderer
- net.minecraft.world.item.ItemStack
- DragonSpawnEgg.java
- DragonSpawnEgg
- .getAgeScale
- .onInitializeClient
- hatchableeggblock
- Clamped
- DragonMoveController
- .onInitialize
- .updateAgeProperties
- DragonBodyController
- BiomeHabitat.java
- ServerHolder
- DMLConfig.java
- .id
- Properties
- .TameableDragon

## God Nodes (most connected - your core abstractions)
1. `TameableDragon` - 179 edges
2. `DragonBreed` - 75 edges
3. `HatchableEggBlock` - 41 edges
4. `DragonMountsGameTests` - 37 edges
5. `DragonModel` - 37 edges
6. `HatchableEggBlockEntity` - 31 edges
7. `DragonAnimator` - 28 edges
8. `DMLRegistry` - 27 edges
9. `DMLConfig` - 26 edges
10. `Habitat` - 23 edges

## Surprising Connections (you probably didn't know these)
- `DragonMountsGameTests` --references--> `DragonBreed`  [EXTRACTED]
  src/gametest/java/com/github/kay9/dragonmounts/gametest/DragonMountsGameTests.java → src/main/java/com/github/kay9/dragonmounts/dragon/DragonBreed.java
- `DMLRegistry` --references--> `DragonBreed`  [EXTRACTED]
  src/main/java/com/github/kay9/dragonmounts/DMLRegistry.java → src/main/java/com/github/kay9/dragonmounts/dragon/DragonBreed.java
- `DMLRegistry` --references--> `HatchableEggBlockEntity`  [EXTRACTED]
  src/main/java/com/github/kay9/dragonmounts/DMLRegistry.java → src/main/java/com/github/kay9/dragonmounts/dragon/egg/HatchableEggBlockEntity.java
- `DMLRegistry` --references--> `TameableDragon`  [EXTRACTED]
  src/main/java/com/github/kay9/dragonmounts/DMLRegistry.java → src/main/java/com/github/kay9/dragonmounts/dragon/TameableDragon.java
- `DragonAnimator` --references--> `TameableDragon`  [EXTRACTED]
  src/main/java/com/github/kay9/dragonmounts/client/DragonAnimator.java → src/main/java/com/github/kay9/dragonmounts/dragon/TameableDragon.java

## Import Cycles
- None detected.

## Communities (55 total, 11 thin omitted)

### Community 0 - "DragonBreed"
Cohesion: 0.05
Nodes (45): attributeinstance, block, clientboundblockentitydatapacket, com.mojang.datafixers.util.Either, com.mojang.serialization.Codec, core, DataComponentInput, datacomponentmap (+37 more)

### Community 1 - "DMLConfig"
Cohesion: 0.08
Nodes (25): at, com.google.common.collect.ImmutableMap, com.llamalad7.mixinextras.injector.wrapoperation.Operation, com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation, DoubleValue, IntValue, SuppressWarnings, MountCameraManager (+17 more)

### Community 2 - "Ability"
Cohesion: 0.07
Nodes (17): fabricregistrybuilder, Ability, Factory, FootprintAbility, Override, FrostWalkerAbility, Override, GreenToesAbility (+9 more)

### Community 3 - "DragonMountsGameTests"
Cohesion: 0.17
Nodes (7): DragonMountsGameTests, BooleanValue, TestableEnderDragon, BuiltIn, net.minecraft.gametest.framework.GameTest, net.minecraft.gametest.framework.GameTestHelper, net.minecraft.resources.ResourceKey

### Community 4 - "com.mojang.serialization.MapCodec"
Cohesion: 0.10
Nodes (18): com.mojang.serialization.codecs.RecordCodecBuilder, com.mojang.serialization.MapCodec, DragonBreathHabitat, INSTANCE, Override, FluidHabitat, Override, Habitat (+10 more)

### Community 5 - "DragonEggModel.java"
Cohesion: 0.11
Nodes (19): completablefuture, executor, Baked, DragonEggModel, ItemModelResolver, Context, Override, modelloadingplugin (+11 more)

### Community 6 - "net.minecraft.core.BlockPos"
Cohesion: 0.19
Nodes (8): HatchableEggBlock, Builder, Override, SuppressWarnings, net.minecraft.core.BlockPos, net.minecraft.server.level.ServerLevel, net.minecraft.world.level.block.state.BlockState, net.minecraft.world.level.Level

### Community 7 - "CrossBreedingManager.java"
Cohesion: 0.13
Nodes (16): com.google.gson.JsonElement, dragonmodel, gsonbuilder, hashmap, holderlookup, jsonops, jsonparseexception, Couple (+8 more)

### Community 8 - "TameableDragon.java"
Cohesion: 0.08
Nodes (25): attributemodifier, attributesupplier, booleanop, bytebufcodecs, conventionalitemtags, dragonbodycontroller, dragonbreedgoal, dragonfollowownergoal (+17 more)

### Community 9 - "Override"
Cohesion: 0.11
Nodes (7): Entity, EntityDimensions, LivingEntity, Override, MoveFunction, net.minecraft.world.damagesource.DamageSource, Pose

### Community 10 - "DragonModel"
Cohesion: 0.17
Nodes (10): DragonModel, SuppressWarnings, meshdefinition, net.minecraft.client.model.EntityModel, net.minecraft.client.model.geom.builders.CubeListBuilder, net.minecraft.client.model.geom.builders.LayerDefinition, net.minecraft.client.model.geom.builders.PartDefinition, net.minecraft.client.model.geom.ModelPart (+2 more)

### Community 11 - "DragonFollowOwnerGoal"
Cohesion: 0.11
Nodes (12): aabb, blockpos, blockstate, collisioncontext, enumset, frostediceblock, leavesblock, DragonFollowOwnerGoal (+4 more)

### Community 12 - "TameableDragon"
Cohesion: 0.13
Nodes (8): Builder, TameableDragon, net.minecraft.sounds.SoundEvent, net.minecraft.world.InteractionHand, PlayerRideable, Saddleable, TamableAnimal, VariantHolder

### Community 14 - "HatchableEggBlock.java"
Cohesion: 0.09
Nodes (20): blockbehaviour, blockitemstateproperties, fallingblockentity, fluids, mapcolor, net.minecraft.sounds.SoundSource, net.minecraft.world.InteractionResult, net.minecraft.world.item.TooltipFlag (+12 more)

### Community 15 - "DragonEggProvider.java"
Cohesion: 0.16
Nodes (15): chatformatting, dragonbreed, hatchableeggblockentity, DragonEggProvider, INSTANCE, Override, DragonProvider, INSTANCE (+7 more)

### Community 16 - ".getBreed"
Cohesion: 0.15
Nodes (3): AgeableMob, SuppressWarnings, net.minecraft.world.entity.animal.Animal

### Community 17 - "DragonMountsGameTests.java"
Cohesion: 0.11
Nodes (18): chunkpos, endpodiumfeature, entitytype, field, gametestassertexception, gametype, heightmap, lootcontextparams (+10 more)

### Community 18 - "GreenToesAbility.java"
Cohesion: 0.18
Nodes (11): blocks, blocktags, bonemealableblock, codec, farmblock, levelevent, logmanager, particletypes (+3 more)

### Community 19 - ".tick"
Cohesion: 0.18
Nodes (4): arrays, CircularBuffer, LerpedFloat, mth

### Community 20 - "DragonMountsLegacy"
Cohesion: 0.20
Nodes (13): builtinregistries, dataresult, dmlutil, dragonmountslegacy, function, DragonMountsLegacy, DMLUtil, SuppressWarnings (+5 more)

### Community 21 - "DragonMountsEmiPlugin.java"
Cohesion: 0.15
Nodes (14): arraylist, comparison, dev.emi.emi.api.EmiEntrypoint, dev.emi.emi.api.EmiPlugin, dev.emi.emi.api.EmiRegistry, emiinforecipe, emiingredient, emistack (+6 more)

### Community 22 - "net.minecraft.resources.ResourceLocation"
Cohesion: 0.17
Nodes (7): builtin, immutablelist, CustomRenderTypes, DragonEggLootTargets, Target, net.minecraft.client.renderer.RenderType, net.minecraft.resources.ResourceLocation

### Community 23 - "DragonMountsFabricClient.java"
Cohesion: 0.13
Nodes (15): clienttickevents, colorproviderregistry, creativemodetabs, dragoneggmodel, dragonrenderer, entitymodellayerregistry, entityrendererregistry, itemgroupevents (+7 more)

### Community 24 - "DragonMountsFabric.java"
Cohesion: 0.12
Nodes (15): constantvalue, crossbreedingmanager, dynamicregistries, entitydataserializers, fabricdefaultattributeregistry, interactionresult, lootitem, lootpool (+7 more)

### Community 25 - "ReaperStepAbility.java"
Cohesion: 0.22
Nodes (7): itementity, itemstack, Override, ReaperStepAbility, net.minecraft.tags.TagKey, net.minecraft.world.level.block.Block, soundevents

### Community 26 - "SidedAccess.java"
Cohesion: 0.21
Nodes (8): clientaccess, envtype, fabricloader, ClientAccess, SidedAccess, minecraft, net.fabricmc.api.Environment, net.minecraft.core.RegistryAccess

### Community 27 - "com.mojang.blaze3d.vertex.PoseStack"
Cohesion: 0.26
Nodes (4): com.mojang.blaze3d.vertex.PoseStack, com.mojang.blaze3d.vertex.VertexConsumer, SuppressWarnings, ModelPartProxy

### Community 28 - "DragonRenderer.java"
Cohesion: 0.15
Nodes (12): axis, defaultvertexformat, dragonmodelpropertieslistener, entityrendererprovider, net.minecraft.client.model.geom.ModelLayerLocation, net.minecraft.client.renderer.culling.Frustum, net.minecraft.client.renderer.entity.layers.RenderLayer, net.minecraft.client.renderer.entity.MobRenderer (+4 more)

### Community 29 - "dmlregistry"
Cohesion: 0.21
Nodes (7): dmlregistry, Builder, Override, RandomChanceByConfig, net.minecraft.world.level.storage.loot.LootContext, net.minecraft.world.level.storage.loot.predicates.LootItemCondition, net.minecraft.world.level.storage.loot.predicates.LootItemConditionType

### Community 32 - "DMLRegistry.java"
Cohesion: 0.23
Nodes (11): abilities, dragonspawnegg, habitats, DMLRegistry, mapcodec, mobcategory, net.minecraft.core.component.DataComponentType, net.minecraft.world.entity.EntityType (+3 more)

### Community 33 - "KeyMappings.java"
Cohesion: 0.18
Nodes (8): component, consumer, dmlconfig, glfw, localplayer, SuppressWarnings, MountControlsMessenger, tameabledragon

### Community 34 - "list"
Cohesion: 0.21
Nodes (6): experienceorb, gamerules, list, DragonBreedGoal, Override, net.minecraft.world.entity.ai.goal.BreedGoal

### Community 35 - "LuckyRandom"
Cohesion: 0.29
Nodes (3): Override, LuckyRandom, net.minecraft.world.level.levelgen.PositionalRandomFactory

### Community 36 - "DragonRenderer"
Cohesion: 0.27
Nodes (4): DragonRenderer, Context, Override, SuppressWarnings

### Community 37 - "net.minecraft.world.item.ItemStack"
Cohesion: 0.29
Nodes (5): SuppressWarnings, Item, Provider, net.minecraft.world.item.BlockItem, net.minecraft.world.item.ItemStack

### Community 39 - "DragonSpawnEgg.java"
Cohesion: 0.20
Nodes (9): compoundtag, customdata, item, nbtops, net.minecraft.world.item.SpawnEggItem, optional, registryops, sidedaccess (+1 more)

### Community 40 - "DragonSpawnEgg"
Cohesion: 0.31
Nodes (4): DragonSpawnEgg, Override, Provider, net.minecraft.world.entity.Mob

### Community 42 - ".onInitializeClient"
Cohesion: 0.31
Nodes (4): SuppressWarnings, KeyMappings, Override, net.minecraft.client.KeyMapping

### Community 43 - "hatchableeggblock"
Cohesion: 0.36
Nodes (6): hatchableeggblock, DragonMountsJadePlugin, Override, snownee.jade.api.IWailaClientRegistration, snownee.jade.api.IWailaPlugin, snownee.jade.api.WailaPlugin

### Community 45 - "DragonMoveController"
Cohesion: 0.33
Nodes (4): attributes, DragonMoveController, Override, net.minecraft.world.entity.ai.control.MoveControl

### Community 46 - ".onInitialize"
Cohesion: 0.38
Nodes (3): DragonMountsFabric, Override, net.fabricmc.api.ModInitializer

### Community 48 - "DragonBodyController"
Cohesion: 0.40
Nodes (3): DragonBodyController, Override, net.minecraft.world.entity.ai.control.BodyRotationControl

### Community 49 - "BiomeHabitat.java"
Cohesion: 0.47
Nodes (3): BiomeHabitat, Override, net.minecraft.world.level.biome.Biome

### Community 51 - "DMLConfig.java"
Cohesion: 0.40
Nodes (4): dragoneggloottargets, map, net.neoforged.neoforge.common.ModConfigSpec, resourcekey

## Knowledge Gaps
- **3 isolated node(s):** `INSTANCE`, `INSTANCE`, `INSTANCE`
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 183 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **11 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `TameableDragon` connect `TameableDragon` to `DragonBreed`, `DMLConfig`, `Ability`, `DragonMountsGameTests`, `TameableDragon.java`, `Override`, `DragonModel`, `DragonFollowOwnerGoal`, `DragonAnimator`, `DragonEggProvider.java`, `.getBreed`, `DragonMountsGameTests.java`, `GreenToesAbility.java`, `.tick`, `DragonMountsLegacy`, `net.minecraft.resources.ResourceLocation`, `DragonMountsFabric.java`, `ReaperStepAbility.java`, `DragonRenderer.java`, `.isFlying`, `.mobInteract`, `DMLRegistry.java`, `KeyMappings.java`, `list`, `DragonRenderer`, `.tick`, `.getAgeScale`, `hatchableeggblock`, `DragonMoveController`, `.updateAgeProperties`, `DragonBodyController`, `DMLConfig.java`, `.TameableDragon`?**
  _High betweenness centrality (0.371) - this node is a cross-community bridge._
- **Why does `DragonBreed` connect `DragonBreed` to `Ability`, `DragonMountsGameTests`, `com.mojang.serialization.MapCodec`, `net.minecraft.core.BlockPos`, `CrossBreedingManager.java`, `TameableDragon`, `HatchableEggBlock.java`, `DragonEggProvider.java`, `.getBreed`, `DragonMountsGameTests.java`, `GreenToesAbility.java`, `DragonMountsEmiPlugin.java`, `net.minecraft.resources.ResourceLocation`, `DragonMountsFabric.java`, `DragonRenderer.java`, `.mobInteract`, `DMLRegistry.java`, `DragonRenderer`, `net.minecraft.world.item.ItemStack`, `DragonSpawnEgg`, `.updateAgeProperties`?**
  _High betweenness centrality (0.128) - this node is a cross-community bridge._
- **Why does `DragonModel` connect `DragonModel` to `DragonRenderer`, `CrossBreedingManager.java`, `.getAgeScale`, `TameableDragon`, `DragonAnimator`, `Properties`, `DragonMountsFabricClient.java`, `com.mojang.blaze3d.vertex.PoseStack`?**
  _High betweenness centrality (0.059) - this node is a cross-community bridge._
- **What connects `INSTANCE`, `INSTANCE`, `INSTANCE` to the rest of the system?**
  _3 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `DragonBreed` be split into smaller, more focused modules?**
  _Cohesion score 0.05493827160493827 - nodes in this community are weakly interconnected._
- **Should `DMLConfig` be split into smaller, more focused modules?**
  _Cohesion score 0.08325624421831637 - nodes in this community are weakly interconnected._
- **Should `Ability` be split into smaller, more focused modules?**
  _Cohesion score 0.07123034227567067 - nodes in this community are weakly interconnected._