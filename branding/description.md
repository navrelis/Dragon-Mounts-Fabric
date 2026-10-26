![Dragon Mounts Fabric](https://raw.githubusercontent.com/navrelis/Dragon-Mounts-Fabric/mc/1.21.1/branding/banner.png)

**Hatch dragon eggs, raise the hatchlings, and fly on their backs - now natively on Fabric 1.21.1.**

## 🐉 What is this?

**Dragon Mounts Fabric** is an **unofficial, native Fabric port** of [Dragon Mounts: Legacy](https://github.com/MWall541/Dragon-Mounts-Legacy) (NeoForge) by **Kay9** (Dragon Mounts: Legacy), which in turn continues the original *Dragon Mounts* idea by **ata4 / BarracudaATA** ([original repository](https://github.com/ata4/dragon-mounts)). The port itself is made by **navrelis**.

- It runs directly on Fabric. There is no NeoForge compatibility layer.
- It is licensed under the **GNU GPLv3**, like the upstream mod.
- It is **not affiliated with or endorsed by** Kay9, ata4 or the upstream project.
- **Please report bugs of this port to this project's issue tracker, not to the upstream authors.** If something only happens here, it is most likely the port's fault.

## ✨ Features

### Eggs & hatching
- Dragon eggs are placeable blocks. Right-click an egg to start the hatching process; it then progresses through several stages until a baby dragon hatches.
- The vanilla **Dragon Egg** is supported: right-click it to turn it into a hatchable **End dragon egg**. It still teleports when you punch it until you set it to hatch.
- An egg **replenishes after every Ender Dragon kill**, not only the first one. It is placed on the exit portal, and only if there is no dragon egg there yet, so mods that place their own egg (for example YUNG's Better End Island) don't end up with duplicates. Can be switched off in the config.
- Eggs remember their breed and custom name when picked up and placed again.

### Breeds & habitats
While an egg is hatching it looks at its surroundings. If the place fits another breed better, the egg **changes its breed** (with a short transition) - so *where* you hatch an egg decides what you get. This can be turned off in the config.

| Breed | Hatch it here (habitat) | Special traits | Egg also found in (chest loot, default chance) |
|---|---|---|---|
| **Aether** | High in the sky, above Y 200 | Faster flying speed | Dungeons (15%) |
| **End** | Inside a lingering dragon's breath cloud; also what the vanilla Dragon Egg becomes | More health (75) | Vanilla Dragon Egg |
| **Fire** | Next to fire, lava, magma blocks or campfires; lava counts extra | Immune to fire and lava | Desert pyramids (7.5%) |
| **Forest** | Among leaves, saplings, flowers, moss or vines; in jungles | - | Jungle temples (30%) |
| **Ghost** | Deep underground (below Y 0, no sky access) | Immune to drowning | Woodland mansions (20%), abandoned mineshafts (9.5%) |
| **Ice** | Surrounded by ice or snow | Immune to drowning and freezing | Igloos (20%) |
| **Nether** | In Nether biomes or near Nether blocks (netherrack, soul fire, shroomlight, warped/crimson stems, ancient debris, ...) | Extra armor; immune to fire and lava | Bastion treasure rooms (35%) |
| **Water** | In or next to water, corals, seagrass, kelp, sea pickles, prismarine or sea lanterns | Immune to drowning | Buried treasure (17.5%) |

All breeds are immune to cactus, sweet berry bushes and dragon's breath. Breeds are data-driven, so datapacks can add more (see below).

### Taming & riding
- **Tame** a baby dragon by feeding it **fish** (each fish has a chance to win its trust).
- Use a **saddle** to ride an adult dragon (only its owner can ride). **Shears** remove the saddle again.
- **Bone** or **sneak + right-click** makes your dragon sit or stand. **Meat** heals it.
- Dragons grow from hatchlings to juveniles to adults over time; size, health and attack grow with them. Hatchlings can't be saddled or fly.
- **Controls:** the dragon takes off when you jump while riding.
  - **Jump** = ascend
  - **Z** (rebindable, "Flight Descent") = descend
  - **F6** (rebindable) = toggle **camera-driven flight**, where the camera pitch steers your climb and descent
- The camera can switch to third person automatically when you mount, and the third-person camera offsets are configurable.

### Breeding & cross-breeding
- Feed fish to two tamed, adult dragons and they lay an egg. The egg's breed is chosen from one of the parents.
- **Cross-breeding** lets datapacks define special results for two parent breeds (for example "A + B = C"). The base mod does not define any by default, so this is a tool for modpacks and addons.
- Each dragon has a reproduction limit per breed (configurable).
- Parent custom names are mixed into the egg's name.

### Abilities per breed
Some breeds leave a mark on the world as they walk (these respect the `mobGriefing` game rule):

| Breed | Ability |
|---|---|
| Fire | **Hot Feet** - scorches flammable plants |
| Forest | **Green Toes** - regrows grass, spawns flowers and mushrooms |
| Ghost | **Reaper Step** - withers plants, turns some ground into soul soil or soul sand |
| Ice | **Frost Walker** (wide radius) and **Snow Stepper** - frosted ice and snow layers in cold places |
| Water | **Hydro Step** - moistens farmland and sponges, cools magma, extinguishes fire |

Aether, End and Nether dragons rely on their attribute bonuses instead.

### Loot
Dragon eggs can be found in structure chests, **enabled by default** (`use_loot_tables`). The table above lists the vanilla structure for each breed and its default chance; every chance is configurable (`egg_loot_chances`, `0` = never, `1` = guaranteed). Structure mods that replace a vanilla chest table share the chance of the table they replace:

- **YUNG's Better Dungeons** - the skeleton, zombie and small dungeon chests (Aether eggs)
- **YUNG's Better Desert Temples** - the tomb and pharaoh chests (Fire eggs)
- **YUNG's Better Jungle Temples** - the treasure chest (Forest eggs)
- **YUNG's Better Ocean Monuments** - the upper side chamber chest (Water eggs)
- **Repurposed Structures** - the dungeon (Aether), jungle pyramid (Forest), mansion (Ghost), igloo (Ice) and underground bastion treasure (Nether) chests
- **Beach Party** - buried treasure (Water eggs)
- **Moog's Nether Structures** - the dragon arena epic chest (Nether eggs)

Structures from mods that are not installed are simply ignored.

## 📦 Requirements
- **Minecraft 1.21.1**
- **Fabric Loader** 0.19.5 or newer
- **[Fabric API](https://modrinth.com/mod/fabric-api)**
- **Forge Config API Port**

No Sinytra Connector or Sporran needed - this is a native Fabric port.

## 🔌 Optional integrations
None of these are required.
- **Jade** - shows the breed and hatch stage of dragon eggs, and the breed, life stage (hatchling / juvenile / adult) and saddle status of dragons.
- **EMI** - lists an egg and a spawn egg for every breed, plus info pages explaining hatching, habitats and the vanilla Dragon Egg.
- **Mod Menu** - shows the mod entry with its icon and a link to the upstream project.
- **Carry On** - dragons and dragon eggs can't be picked up (they are on Carry On's blacklists).
- **Lootr** - dragon eggs appear in Lootr chests and, as with all Lootr loot, are generated per player. Dragon eggs are excluded from Lootr's "integrated loot" (which samples items from other loot tables into unrelated modded chests), so they don't leak into random chests; they only show up in the structures listed above.

## 🧩 Compatibility notes
- **Sodium / Iris:** supported - the mod is made to work alongside them.
- **Lootr:** dragon eggs in chest loot work with Lootr containers and are excluded from Lootr's integrated loot (see above).
- **YUNG's structures, Repurposed Structures, Beach Party, Moog's Nether Structures:** their chests can contain eggs (see Loot above).
- **YUNG's Better End Island and similar mods:** no duplicate eggs - the egg is only replenished when there isn't one on the exit portal already.
- **AdventureZ:** if AdventureZ is installed, Dragon Mounts takes over the vanilla Dragon Egg's right-click. To avoid both systems reacting, set `allow_dragon_hatching` to `false` in `config/adventurez.json5`.
- **Other mods touching the Dragon Egg:** if another mod needs the untouched vanilla behaviour, set `allow_egg_override = false` in the common config.

## ⚙️ Configuration
Two files are created in your `config` folder:

- `dragonmounts-common.toml` (server / world side)
  - `allow_egg_override` - allow the vanilla Dragon Egg to be hatched (default: on)
  - `replenish_eggs` - place a dragon egg on the exit portal after every Ender Dragon kill, if there isn't one already (default: on)
  - `update_habitats` - let hatching eggs change breed to match their surroundings (default: on)
  - `use_loot_tables` - generate eggs in structure chest loot (default: on), with per-structure chance values (`egg_loot_chances`)
  - `reproduction_limits` - how often each built-in breed may breed (`-1` = unlimited, `0` = cannot breed)
- `dragonmounts-client.toml` (client only)
  - `camera_driven_flight` - vertical flight follows the camera pitch
  - `third_person_on_mount` - switch to third person when mounting
  - `camera_offsets` - distance / vertical / horizontal offsets of the riding camera

## 🧪 Datapack addons
Dragon Mounts Fabric is fully data-driven. Without writing any code you can add:

- **Breeds:** `data/<namespace>/dragonmounts/dragon_breeds/<name>.json`. A breed defines colors, hatch particles, attributes, abilities, egg habitats, immunities, ambient sound, growth time, hatch chance, size, taming and breeding items and reproduction limit. The built-in breeds are good examples.
- **Cross-breeding:** `data/<namespace>/dragonmounts/cross_breeding/<name>.json`:
  ```json
  {
    "parent1": "mynamespace:my_breed",
    "parent2": "dragonmounts:fire",
    "child": "dragonmounts:ghost"
  }
  ```
  The order of the parents does not matter.

Custom breeds additionally need their own textures and lang entries in a resource pack. Have a look at the upstream project's `example_addon` for a full template (its loot-modifier files are NeoForge specific and are not used on Fabric).

## ❓ FAQ
**Is this the same mod as Dragon Mounts: Legacy?**
It is a port of it: same idea and content, rebuilt to run natively on Fabric. It is unofficial and not made by the original authors.

**Can I use my NeoForge world / addons?**
Worlds can't be moved between loaders without conversion. Datapack-based breeds and cross-breeding files use the same format; NeoForge-specific parts of an addon (like global loot modifiers) will not work.

**How do I get my first egg?**
Defeat the Ender Dragon and hatch the vanilla Dragon Egg (interact with it), or find one in the chest loot of the structures listed above (loot generation is on by default).

**My egg changed color or breed!**
That's the habitat system: eggs adapt to where they hatch. Hatch eggs in the right environment, or disable `update_habitats` in the config.

**Does it need Sinytra Connector or Sporran?**
No. It is a native Fabric mod and runs directly on Fabric Loader.

**Where do I report problems?**
In this project's [issue tracker](https://github.com/navrelis/Dragon-Mounts-Fabric/issues), please include your Fabric Loader and mod versions and the log.

## 💜 Credits & License
- **Dragon Mounts: Legacy** by **Kay9** - the mod this port is based on: [github.com/MWall541/Dragon-Mounts-Legacy](https://github.com/MWall541/Dragon-Mounts-Legacy)
- **Dragon Mounts** by **ata4 / BarracudaATA** - the original mod and the procedural dragon model/animation: [github.com/ata4/dragon-mounts](https://github.com/ata4/dragon-mounts)
- **Fabric port** by **navrelis** - [github.com/navrelis/Dragon-Mounts-Fabric](https://github.com/navrelis/Dragon-Mounts-Fabric)
- The logo and banner are pixel art made for this port from the mod's own egg textures.

Licensed under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.html). Source code is available in the [project's repository](https://github.com/navrelis/Dragon-Mounts-Fabric).

*Dragon Mounts Fabric is an unofficial port. It is not affiliated with Kay9, ata4, Mojang, Microsoft or the Fabric project.*
