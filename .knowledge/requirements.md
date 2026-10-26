# Requirements

## Goal
A native Fabric 1.21.1 port of Dragon Mounts: Legacy (upstream https://github.com/MWall541/Dragon-Mounts-Legacy, branch `neoforge`). It must be fully compatible with the NytheriaDevelopment modpack (`%USERPROFILE%\curseforge\minecraft\Instances\NytheriaDevelopment`).
- Name: **Dragon Mounts Fabric (Unofficial Port)**.
- Also needed: a description, a short summary, and a new logo.
- The port must be a real Fabric port, not a run through Sporran (the reason is performance).

## User answers (2026-10-26)
- **Vanilla dragon egg:** it belongs to our mod. Right-clicking it turns it into our hatchable end egg. AdventureZ hatching must not take over.
- **Egg replenish:** an egg must come back every time the Ender Dragon is killed, not only the first time.
- **Egg loot:** must be correct for this pack, which means the YUNG's/modded replacement chest tables. It must be verified with Lootr Remastered, which is the owner's own mod (source at `F:\Coding\Lootr Remastered`).
- Everything else: "do whatever is needed", so all proposed defaults apply.

## Accepted defaults
- **Feature parity:** a 1:1 port of the `neoforge` branch. Also include two `master` fixes:
  - creative-tab crash #259
  - per-breed loot tables `entities/dragon_breeds/<breed>`
- **Success criteria:**
  - the jar builds
  - gametests pass
  - a dedicated server starts
  - the jar loads in the pack with no crash and no mixin errors
  - eggs hatch, and dragons can be tamed, ridden and flown
  - egg loot shows up, also in Lootr chests
- **Stack:** Java 21, MC 1.21.1, Fabric Loader 0.19.5, Fabric API 0.116.17+1.21.1, Loom 1.17.x (plugin `net.fabricmc.fabric-loom-remap`), Gradle 9.5.0, Mojang mappings + Parchment 2024.11.17.
- **Dependencies:**
  - required: Forge Config API Port 21.1.6 (already in the pack)
  - optional, compile-only: Mod Menu, Jade, EMI
  - JEI is dropped
- **Datagen:** dropped. The generated resources ship as static files, converted to Fabric format.
- **Identity:** mod id `dragonmounts`, package `com.github.kay9.dragonmounts`, version `1.21.1-1.0.0`.
- **License:** GPLv3, crediting the upstream authors (Kay9/MWall541, ata4/barracuda). The author is shown as "navrelis" only.
- **Privacy:** never the owner's real name.
- **Logo:** no AI image generation. Hand-built SVG/pixel art, rendered to PNG, built from the mod's own textures.
- **Texts:** CurseForge description (Markdown) and summary (<100 chars), in English.
- **Git:** new private repo `navrelis/Dragon-Mounts-Fabric`, branch `port/fabric-1.21.1` (not main). No CurseForge upload.
- **Install:** at the end, copy the built jar into the pack's `mods\`. Small pack config tweaks are allowed where needed for compatibility (owner: "whatever is needed"). They stay uncommitted in the pack repo and get reported.

## Owner rules carried over from the pack
- `py -I` for Python (plain `python` is broken).
- No AI images.
- No whole-drive searches.
- Cold pack client start takes about 6 minutes, so launch rarely. Only the lead launches the client.
