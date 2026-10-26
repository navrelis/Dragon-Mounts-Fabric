# Plan
Reference material: `.knowledge/ref/upstream-map.md` (file map and NeoForge→Fabric mapping), `ref/research-versions-compat.md` (versions and pack compatibility), `ref/loot_all.txt` (pack chest loot ids).
Upstream clone: the session scratchpad `upstream/` (branch `neoforge`). If it is gone, re-clone it.

Usage tally (work units; short = 1, long = 2): **Sonnet 23 (… + T5c, T6b, T6c in-pack gametest debug ×2, T10a)** · Opus 4 (T4, T9c logo) ≈ 15 % Opus. Target: Opus ≤ 50 %.

| ID | Task | Model | Size/Diff | Depends | Parallel with | Status |
|---|---|---|---|---|---|---|
| T1 | Scaffold: git init, Gradle/Loom setup, fabric.mod.json, AW, mixin config, copy upstream sources + resources (drop GLM/datagen/NeoModImpl/ModelPart mixin), LICENSE, .gitignore. DoD: Gradle resolves deps, `compileJava` runs and reports only expected NeoForge-symbol errors | Sonnet | short/easy | – | – | done |
| T2 | Common port: DMLRegistry, Fabric `ModInitializer`, DMLConfig via FCAP, TameableDragon/HatchableEggBlock/DragonSpawnEgg/abilities/goals fixes, master fixes, reload listener, vanilla-egg UseBlockCallback, server holder, block-break veto, culling box. DoD: zero compile errors in common files | Sonnet | long/easy | T1 | T3, T4 | done |
| T3 | Client port: `ClientModInitializer`, renderer/layers, lazy per-breed models, scale fields, keybinds, camera TAIL mixin, creative tabs, item colors. DoD: zero compile errors in client files | Sonnet | long/easy | T1 | T2, T4 | done |
| T4 | Egg block/item model on Fabric Model Loading API + FabricBakedModel, `RenderDataBlockEntity`, egg model JSONs. DoD: compiles, egg renders per breed (block + item) in dev client | Opus | long/hard | T1 | T2, T3 | done (code; runtime checks in T10 checklist) |
| T5 | Server mixins + loot: flying-kick fix (WrapOperation), egg replenish every kill (wrap in tickDeath, portal-top check), LootTableEvents.MODIFY with pack table mapping, config keys. DoD: full build green | Sonnet | long/medium | T2 | T3/T4 tail | done (code; runtime via T6) |
| T6 | Gametests (headless `runGametest`): breed registry, egg item/loot injection (chance forced), hatching, dragon tame/saddle/ride basics, replenish logic. Dev `runServer` smoke. DoD: all pass | Sonnet | long/easy | T5 | T7 | done (16/16 pass, runServer Done) |
| T7 | Optional integrations: Jade plugin (egg breed/hatch stage, dragon breed/age), EMI egg stacks/info, Mod Menu metadata; Carry On/culling compat via data/tag where possible | Sonnet | short/easy | T5 | T6 | done (code; runtime via T6) |
| T8 | Lootr Remastered check: confirm via its source and a dev-client run with the Lootr jar that eggs appear in Lootr chests; integrated-loot leakage behaviour | Sonnet | short/medium | T5 | T9 | done (tag shipped; runtime check in T10) |
| T9 | Logo (SVG/pixel art from mod textures, no AI → PNG 400×400 + banner), CurseForge description.md, summary, README | Sonnet | short/easy | T1 | anything | done (T9b text refresh done) |
| T10 | Pack integration: build jar, launch the pack client with the jar (lead), log/mixin check, in-game smoke checklist, pack config tweaks (adventurez), copy jar to mods\ | Lead | – | T6–T8 | – | done (headless; jar in pack mods/) |
| T11 | Final: review, Graphify, report, push | Lead | – | all | – | done (pushed, public) |
| T9c | New logo/banner/icon in true pixel art (integer grid, no gradients/AA, hand-placed, no AI) — owner request | Opus | long/hard (visual quality) | – | T6c | done |
| T6c | Run our gametests inside the full pack (headless scratch server) | Sonnet | long/medium | T6b | T9c | done |
