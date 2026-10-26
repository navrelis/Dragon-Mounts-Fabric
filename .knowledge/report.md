# Final report: Dragon Mounts Fabric (Unofficial Port) 1.21.1-1.0.0

Branch `port/fabric-1.21.1`. Native Fabric 1.21.1 port of Dragon Mounts: Legacy (upstream `neoforge` branch plus 2 master fixes). Not pushed: creating the private GitHub repo was blocked by the permission check and needs the owner's OK.

## Deliverables
- **Jar:** `build/libs/dragonmounts-fabric-1.21.1-1.0.0.jar`. The same jar is installed in the pack's `mods\`.
- **Description:** `branding/description.md`. Replace `BANNER_URL` with the uploaded banner's URL.
- **Summary:** `branding/summary.txt` (93 characters).
- **Logo, banner and icon (true pixel art):**
  - `branding/logo.png`: 400×400, an 80×80 grid at 5×.
  - `branding/banner.png`: 1200×400, a 240×80 grid at 5×.
  - `src/main/resources/assets/dragonmounts/icon.png`: 128×128, a 64×64 grid at 2×.
  - Every pixel was placed by hand in code; no AI.
- **CurseForge categories:** Mobs (primary), Adventure and RPG.

## Requirements and how they are met
| Requirement | Result |
|---|---|
| Real Fabric port (no Sporran) | Fabric `ModInitializer` and `ClientModInitializer`, Fabric API events, FRAPI egg model, Forge Config API Port config |
| Our mod owns the vanilla egg | `UseBlockCallback` converts a right-clicked vanilla egg into a hatchable End egg. The pack's AdventureZ has `allow_dragon_hatching=false` |
| An egg after every dragon kill | Wraps `setDragonKilled` in `EnderDragon.tickDeath` and `kill`, and places an egg only if none is in the portal column. Tested with YUNG's Better End Island in the pack |
| Correct egg loot, also with Lootr | `LootTableEvents.MODIFY` on 8 vanilla targets plus the pack's replacement tables (YUNG's, Repurposed Structures, Beach Party, Moog's Nether Structures). On by default. Lootr rolls the live table, so eggs appear per player. The tag `lootr:integrated_loot/excluded` stops eggs leaking into unrelated chests |
| Description, summary, logo | Delivered as listed above |

## Main changes (commits on the branch)
1. **Scaffold, branding, common and client port** (earlier session).
2. **Egg model** (`client/DragonEggModel`, `HatchableEggBlockEntity`):
   - a `PreparableModelLoadingPlugin` plus an after-bake wrap;
   - `FabricBakedModel` reads the breed from `RenderDataBlockEntity`;
   - the client re-meshes when the breed changes.
3. **Server mixins:**
   - the flying-kick fix uses `@WrapOperation` on the `PUTFIELD`;
   - egg replenish is wrapped in `tickDeath` and `kill`;
   - AW entries for `portalLocation` and `origin`.
4. **Loot:** `DragonEggLootTargets` holds the extra tables per config key, and `registerLoot()` is implemented. `use_loot_tables` defaults to true.
5. **Integrations:**
   - Jade: egg breed and hatch stage; dragon breed, stage and saddle.
   - EMI: per-breed stacks and info pages.
   - Mod Menu links.
   - Carry On blacklist tags.
   - Lootr integrated-loot exclusion tag.
6. **Tests:**
   - 17 Fabric game tests;
   - the `remapGametestJar` task, which builds a production test jar.
7. **Texts and art:** description, README and summary are refreshed; logo, banner and icon are redrawn as true pixel art.
8. **Graphify graph:** `graphify-out/`, code only.

## Key decisions
See `decisions.md`. In short:
- Loom 1.17 on JDK 21.
- Forge Config API Port for config.
- The FRAPI egg model rather than a BlockEntityRenderer.
- Replenish wraps the caller of `setDragonKilled`, so it works when YUNG's cancels that method.
- Loot uses MODIFY plus pack replacement tables.
- The Lootr exclusion is a tag, not `item_bans`.
- In-pack checks are headless, because the owner does not want the desktop controlled.

## Checks run
- `./gradlew build`: green. The access widener validates, and the mixin targets are remapped to intermediary.
- `./gradlew runGametest` (dev): **17/17 pass**.
- Dev `runServer`: reaches `Done`, with no mixin errors.
- **Full pack, headless server** (Sporran modpack harness, scratch copy, Sporran pinned to the pack's jar): **PASS**. All dimensions ticked, with no dragonmounts or mixin errors.
- **Our game tests inside the full pack** (scratch copy):
  - **17/17 pass**, with YUNG's Better End Island, Lootr, AdventureZ, Sporran and the other ~340 mods.
  - The scratch run needed:
    - the `fabric-gametest-api-v1` module, which the pack's Fabric API jar leaves out;
    - Xaero, FTB Ranks/Essentials and OPAC disabled, because they crash under `GameTestServer`.
  - The only failing test is Porting Lib's bundled `Tests.test`, which is not ours.
- Branding build: all images use integer grids, binary alpha and 16–29 colours.

## Manual test checklist (client-side, not automatable headless)
- [ ] Pack client starts with no `dragonmounts` errors in `latest.log`, with Sodium and Iris (and shaders) on.
- [ ] Every breed's egg renders correctly as a block and as an item (GUI, hand, ground, item frame).
- [ ] A breed change during a habitat transition shows without a relog.
- [ ] Ride and fly: Space ascends, Z descends, F6 toggles camera flight. The camera offset works with CameraOverhaul.
- [ ] Riding a flying dragon on a server for more than 5 s does not get you kicked.
- [ ] Jade tooltips, EMI egg entries and info pages, and the Mod Menu link work.
- [ ] Carry On cannot pick up dragons or eggs.
- [ ] Breaking an egg shows vanilla-egg particles (known limitation).

## Pack changes (uncommitted in the pack repo)
- `config/adventurez.json5`: `allow_dragon_hatching` is set to `false`.
- `config/entityculling.json`: `dragonmounts:dragon` is added to `entityWhitelist` and `tickCullingWhitelist`.
- `mods/dragonmounts-fabric-1.21.1-1.0.0.jar`: added.

## Model usage
- Work units (short = 1, long = 2): Opus 4 (T4 egg model, T9c pixel-art logo) and Sonnet about 25. That is roughly **14 % Opus**.
- No precision overrides were needed.

## Known limitations and recommendations
- **Breaking particles** always use the vanilla egg texture, because Fabric API 0.116 has no per-position particle hook.
- **Upstream TODOs remain:**
  - `DragonAnimator:252` (attack animation);
  - `TameableDragon:165` (AI).
- **GitHub links:** the description and README link to `github.com/navrelis/Dragon-Mounts-Fabric`. While that repo is private (or not created), those links 404 for the public. Make it public at release, or remove the links. Note that GPLv3 requires offering the source anyway.
- **In-pack test server:** to rerun the full-pack game tests, use the scratch setup described above. The pack's own Fabric API lacks the gametest module, which is expected.
- **Leftover file:** `handoff-2026-10-26-678928bc.md` in the project root is the old session handoff. It is not committed and can be deleted.
