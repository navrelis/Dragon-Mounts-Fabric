![Dragon Mounts Fabric](BANNER_URL)

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
- The vanilla **Dragon Egg** is supported: interact with it to turn it into a hatchable **End dragon egg**. It still teleports when you punch it until you set it to hatch.
- An egg **replenishes after every Ender Dragon kill**, not only the first one (can be switched off in the config).
- Eggs remember their breed and custom name when picked up and placed again.

### Breeds & habitats
While an egg is hatching it looks at its surroundings. If the place fits another breed better, the egg **changes its breed** (with a short transition) - so *where* you hatch an egg decides what you get. This can be turned off in the config.

| Breed | Hatch it here (habitat) | Special traits | Egg also found in (chest loot) |
|---|---|---|---|
| **Aether** | High in the sky, above Y 200 | Faster flying speed | Dungeons |
| **End** | Inside a lingering dragon's breath cloud; also what the vanilla Dragon Egg becomes | More health (75) | Vanilla Dragon Egg |
| **Fire** | Next to fire, lava, magma blocks or campfires; lava counts extra | Immune to fire and lava | Desert pyramids |
| **Forest** | Among leaves, saplings, flowers, moss or vines; in jungles | - | Jungle temples |
| **Ghost** | Deep underground (below Y 0, no sky access) | Immune to drowning | Woodland mansions, abandoned mineshafts |
| **Ice** | Surrounded by ice or snow | Immune to drowning and freezing | Igloos |
| **Nether** | In Nether biomes or near Nether blocks (netherrack, soul fire, shroomlight, warped/crimson stems, ancient debris, ...) | Extra armor; immune to fire and lava | Bastion treasure rooms |
| **Water** | In or next to water, corals, seagrass, kelp, sea pickles, prismarine or sea lanterns | Immune to drowning | Buried treasure |

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
- Eggs can appear in the chests listed in the table above (and in popular structure mods such as YUNG's structures). Loot generation is controlled by the `use_loot_tables` option and the chance per structure is configurable. It works with **Lootr** as well.

## 📦 Requirements
- **Minecraft 1.21.1**
- **Fabric Loader** 0.19.5 or newer
- **[Fabric API](https://modrinth.com/mod/fabric-api)**
- **Forge Config API Port**

## 🔌 Optional support
- **Jade** - tooltip integration
- **EMI** - recipe viewer integration
- **Mod Menu** - shows the mod entry with its icon

## 🧩 Compatibility notes
- **Sodium / Iris:** supported - the mod is made to work alongside them.
- **Lootr:** dragon eggs in chest loot work with Lootr containers.
- **YUNG's structures** (and other structure mods using vanilla loot tables): their chests can contain eggs.
- **AdventureZ and other mods touching the Dragon Egg:** the vanilla Dragon Egg is handled by this mod. If another mod needs the untouched vanilla behaviour, set `allow_egg_override = false` in the common config.

## ⚙️ Configuration
Two files are created in your `config` folder:

- `dragonmounts-common.toml` (server / world side)
  - `allow_egg_override` - allow the vanilla Dragon Egg to be hatched
  - `replenish_eggs` - let the Ender Dragon drop an egg after every kill
  - `update_habitats` - let hatching eggs change breed to match their surroundings
  - `use_loot_tables` - generate eggs in chest loot, with per-structure chance values (`egg_loot_chances`)
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
Defeat the Ender Dragon and hatch the vanilla Dragon Egg (interact with it), or find one in the chest loot of the structures listed above if loot generation is enabled in the config.

**My egg changed color or breed!**
That's the habitat system: eggs adapt to where they hatch. Hatch eggs in the right environment, or disable `update_habitats` in the config.

**Where do I report problems?**
In this project's issue tracker, please include your Fabric Loader and mod versions and the log.

## 💜 Credits & License
- **Dragon Mounts: Legacy** by **Kay9** - the mod this port is based on: [github.com/MWall541/Dragon-Mounts-Legacy](https://github.com/MWall541/Dragon-Mounts-Legacy)
- **Dragon Mounts** by **ata4 / BarracudaATA** - the original mod and the procedural dragon model/animation: [github.com/ata4/dragon-mounts](https://github.com/ata4/dragon-mounts)
- **Fabric port** by **navrelis**
- The logo and banner are pixel art made for this port from the mod's own egg textures.

Licensed under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.html). Source code is available in the project's repository.

*Dragon Mounts Fabric is an unofficial port. It is not affiliated with Kay9, ata4, Mojang, Microsoft or the Fabric project.*
