// Builds the branding PNGs from the hand-placed pixel art in art.mjs / sprites.mjs / font.mjs:
//   branding/logo.png    80 x 80 grid  x5 = 400 x 400
//   branding/banner.png  240 x 80 grid x5 = 1200 x 400
//   src/main/resources/assets/dragonmounts/icon.png  64 x 64 grid x2 = 128 x 128
// and verifies every output (palette size, alpha in {0,255}, uniform scale x scale blocks).
// Usage: BRANDING_DEPS=<dir with node_modules/pngjs> node branding/src/build.mjs [previewDir]
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { PNG, toPng } from './lib.mjs';
import { PALETTE } from './palette.mjs';
import { buildLogo, buildIcon, buildBanner } from './art.mjs';
import { glyphs, title } from './font.mjs';

const here = path.dirname(fileURLToPath(import.meta.url));
const brandingDir = path.resolve(here, '..');
const projectDir = path.resolve(brandingDir, '..');
const previewDir = process.argv[2];

const outputs = [
  { name: 'logo', grid: buildLogo(), scale: 5, file: path.join(brandingDir, 'logo.png') },
  { name: 'banner', grid: buildBanner(glyphs, title), scale: 5, file: path.join(brandingDir, 'banner.png') },
  { name: 'icon', grid: buildIcon(), scale: 2, file: path.join(projectDir, 'src/main/resources/assets/dragonmounts/icon.png') },
];

let ok = true;
for (const o of outputs) {
  fs.mkdirSync(path.dirname(o.file), { recursive: true });
  fs.writeFileSync(o.file, toPng(o.grid, PALETTE, o.scale));
  if (previewDir) {
    fs.mkdirSync(previewDir, { recursive: true });
    fs.writeFileSync(path.join(previewDir, `${o.name}-1x.png`), toPng(o.grid, PALETTE, 1));
    if (o.name === 'icon') fs.writeFileSync(path.join(previewDir, 'icon-4x.png'), toPng(o.grid, PALETTE, 4));
  }

  // verify the written file
  const png = PNG.sync.read(fs.readFileSync(o.file));
  const { width: w, height: h, data } = png;
  const colours = new Set();
  const alphas = new Set();
  let blocksBad = 0;
  for (let i = 0; i < w * h; i++) {
    alphas.add(data[i * 4 + 3]);
    if (data[i * 4 + 3]) colours.add(data.readUInt32BE(i * 4));
  }
  const s = o.scale;
  for (let by = 0; by < h; by += s) for (let bx = 0; bx < w; bx += s) {
    const ref = data.readUInt32BE((by * w + bx) * 4);
    for (let j = 0; j < s; j++) for (let i = 0; i < s; i++)
      if (data.readUInt32BE(((by + j) * w + bx + i) * 4) !== ref) { blocksBad++; j = s; break; }
  }
  const alphaOk = [...alphas].every((a) => a === 0 || a === 255);
  const pass = alphaOk && blocksBad === 0 && w === o.grid.w * s && h === o.grid.h * s;
  ok &&= pass;
  console.log(
    `${pass ? 'OK  ' : 'FAIL'} ${path.relative(projectDir, o.file)}  ${w}x${h} (grid ${o.grid.w}x${o.grid.h} x${s})  ` +
      `colours=${colours.size}  alpha=${[...alphas].sort((a, b) => a - b).join('/')}  non-uniform ${s}x${s} blocks=${blocksBad}`,
  );
}
if (!ok) process.exit(1);
