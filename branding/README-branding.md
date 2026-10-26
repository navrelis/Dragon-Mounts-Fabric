# Branding assets

**Hand-placed pixel art. No AI image generation was used:** every cell is set by hand in
char maps, or by drawing primitives that work on whole grid cells (filled polygons, lines,
stepped discs). There are no gradients, no anti-aliasing and no semi-transparent pixels.

| Output | Art grid | Scale | PNG size |
| --- | --- | --- | --- |
| `logo.png` | 80 x 80 | x5 | 400 x 400 |
| `banner.png` | 240 x 80 | x5 | 1200 x 400 |
| `../src/main/resources/assets/dragonmounts/icon.png` | 64 x 64 | x2 | 128 x 128 |

All images are upscaled with integer nearest-neighbour only. Every pixel is fully opaque or
fully transparent (alpha 0 or 255).

## Design

A red (Fire-breed) dragon hatches from a cracked Fire egg against a night sky. It has a
raised wing, curved horns, dorsal spikes, gold belly plates, a spade tail and a claw on the
shell rim. Light comes from the upper left.

- **Logo:** the dragon inside a stepped, rounded-square badge with a bevelled frame, a moon
  and a few single-pixel stars. The other seven breeds (ice, water, aether, forest, ghost,
  nether, end) sit around the hero egg as small eggs.
- **Icon:** a simplified version of the logo for small sizes. It drops the small eggs, the
  moon and the flying shell shards, and has a thinner frame.
- **Banner:** the same dragon on the left. On the right are the title "DRAGON MOUNTS" in a
  bold pixel font and "FABRIC • UNOFFICIAL PORT" in the 5x7 pixel font. Below them stands a
  row of the eight breed eggs, each with its own pattern.

## Palette

There are 29 colours in total, defined in `src/palette.mjs`. The logo and banner use all 29;
the icon uses 16. Shadows shift towards violet and highlights towards orange or gold. The
breed colours come from the mod's own egg and dragon textures.

| Group | Colours |
| --- | --- |
| Sky and frame | `#0e0a1a` `#1c1840` `#2a2559` `#4a4389` `#fff3d6` |
| Bone, horn and shell | `#f3e2ae` `#c9ad78` |
| Dragon | `#3d0b22` `#761530` `#b3242f` `#e04a33` |
| Gold and lava | `#f2842a` `#ffcf55` `#b0501f` |
| Wing membrane | `#4a0f38` `#7a1c44` |
| Breed accents | `#4c5d74` `#8fb2d0` `#3fd0f5` `#241d36` `#b04ae0` `#1f4a1b` `#3d7a28` `#7fb53c` `#a5a9b4` `#d3e3f0` `#4d2620` `#2c6cc0` `#4fa3e6` |

## Sources (`src/`)

- `palette.mjs`: the shared palette.
- `sprites.mjs`: hand-drawn char maps for the head, claw, tail spade, the small (7x9) and
  large (9x12) breed eggs, and the eight per-breed egg patterns.
- `font.mjs`: hand-made pixel fonts, 5x7 for the subtitle and a bold 12-row title font.
- `art.mjs`: the composition. It contains the neck and egg row spans, the wing polygon,
  the tail path, the badge, the hills, the stars and the layouts of the logo, icon and banner.
- `lib.mjs`: the integer grid (stamp, polygon, line, disc and outline helpers) and the PNG
  writer.
- `build.mjs`: writes the three PNGs and verifies them.

Text deliverables: `description.md` (the CurseForge description; replace `BANNER_URL`),
`summary.txt` and `summary-alt.txt`.

## Rebuild

You need Node.js 18+ and `pngjs`. Install `pngjs` outside the project:

```sh
mkdir -p /tmp/branding-deps && cd /tmp/branding-deps && npm init -y && npm i pngjs
cd <project root>
BRANDING_DEPS=/tmp/branding-deps node branding/src/build.mjs [previewDir]
```

The build checks every output and exits non-zero if any check fails. It checks the size, the
colour count, that alpha is only 0 or 255, and that every scale x scale block is uniform.
The optional `previewDir` gets 1x grid renders and a 4x icon preview.
