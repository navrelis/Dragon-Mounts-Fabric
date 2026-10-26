# Branding assets

Hand-made pixel art, no AI image generation. The art is drawn procedurally on a 64x64 grid
(`src/art.mjs`): eight breed-textured egg bands (the mod's own GPLv3 egg textures, copied to
`src/egg-textures/`, plus a synthesised End palette) in front of a pair of dragon wings.
The banner title uses a hand-made 5x7 pixel font (`src/font.mjs`) emitted as rectangles, so no
system font is needed.

Outputs: `logo.svg/png` (400x400), `banner.svg/png` (1200x400),
`../src/main/resources/assets/dragonmounts/icon.png` (128x128, art at exactly 2 px per cell).
Text deliverables: `description.md` (CurseForge description; replace `BANNER_URL`), `summary.txt`, `summary-alt.txt`.

## Rebuild

Needs Node.js 18+ and two packages, installed OUTSIDE the project:

```sh
mkdir -p /tmp/branding-deps && cd /tmp/branding-deps && npm init -y && npm i @resvg/resvg-js pngjs
cd <project root>
BRANDING_DEPS=/tmp/branding-deps node branding/src/build.mjs [previewDir]
```

(`previewDir` optionally writes 32/64 px previews and a contact sheet.)
