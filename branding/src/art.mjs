// Procedural pixel art for the Dragon Mounts Fabric logo: an egg built from the eight breed
// palettes, in front of a pair of dragon wings. Everything is drawn on a 64x64 grid.
// The only image data used are the mod's own (GPLv3) egg block textures in ./egg-textures
// plus a synthesised "end" palette (the vanilla End egg texture is not part of the mod).
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { PNG } from './lib.mjs';

export const N = 64;
const here = path.dirname(fileURLToPath(import.meta.url));

// ---- textures -----------------------------------------------------------------------------
function loadTexture(breed) {
  const p = PNG.sync.read(fs.readFileSync(path.join(here, 'egg-textures', `${breed}_dragon_egg.png`)));
  // frame 0 only (the strips are 16 px wide, animated downwards)
  return (x, y) => {
    const k = ((y % 16) * p.width + (x % 16)) * 4;
    return [p.data[k], p.data[k + 1], p.data[k + 2]];
  };
}

function endTexture() {
  // deterministic noise: near-black body with violet grain and magenta speckles
  let s = 1234567;
  const rnd = () => ((s = (s * 1103515245 + 12345) & 0x7fffffff) / 0x7fffffff);
  const px = [];
  for (let i = 0; i < 256; i++) {
    const r = rnd();
    px.push(r > 0.93 ? [255, 99, 232] : r > 0.86 ? [176, 62, 160] : r > 0.55 ? [44, 30, 66] : [20, 14, 30]);
  }
  return (x, y) => px[(y % 16) * 16 + (x % 16)];
}

// left -> right (lit from the upper left, so the dark End band sits in the shadow)
const ORDER = ['ice', 'water', 'aether', 'forest', 'ghost', 'fire', 'nether', 'end'];
const textures = Object.fromEntries(ORDER.map((b) => [b, b === 'end' ? endTexture() : loadTexture(b)]));

// ---- grid helpers ---------------------------------------------------------------------------
const mk = () => Array.from({ length: N }, () => Array(N).fill(null));
const inside = (x, y) => x >= 0 && y >= 0 && x < N && y < N;
const clamp = (v, a, b) => Math.max(a, Math.min(b, v));
const mul = (c, f) => [clamp(c[0] * f, 0, 255), clamp(c[1] * f, 0, 255), clamp(c[2] * f, 0, 255)];
const mix = (a, b, t) => a.map((v, i) => v + (b[i] - v) * t);

function pip(poly, px, py) {
  let c = false;
  for (let i = 0, j = poly.length - 1; i < poly.length; j = i++) {
    const [xi, yi] = poly[i], [xj, yj] = poly[j];
    if ((yi > py) !== (yj > py) && px < ((xj - xi) * (py - yi)) / (yj - yi) + xi) c = !c;
  }
  return c;
}

function line(mask, x0, y0, x1, y1) {
  x0 = Math.round(x0); y0 = Math.round(y0); x1 = Math.round(x1); y1 = Math.round(y1);
  const dx = Math.abs(x1 - x0), dy = -Math.abs(y1 - y0), sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
  let err = dx + dy;
  for (;;) {
    if (inside(x0, y0)) mask[y0][x0] = true;
    if (x0 === x1 && y0 === y1) break;
    const e2 = 2 * err;
    if (e2 >= dy) { err += dy; x0 += sx; }
    if (e2 <= dx) { err += dx; y0 += sy; }
  }
}

// ---- wings ----------------------------------------------------------------------------------
// Left wing outline in continuous cell coordinates (mirrored around x = 32 for the right one).
const WING = [
  [25, 31], [20, 22], [14, 15], [8, 10], [3, 7],   // arm: shoulder -> elbow -> wrist
  [1, 11], [4, 18], [2, 26], [6, 32], [5, 40], [11, 37], [14, 42], [20, 39], [24, 40],
];
// finger bones (from wrist (3,7)) and arm bone
const BONES = [[[25, 31], [3, 7]], [[3, 7], [2, 26]], [[3, 7], [5, 40]], [[3, 7], [14, 42]], [[3, 7], [4, 18]]];

function drawWings(g) {
  const sq = (x) => 32 - (32 - x) * 0.9;
  const polyL = WING.map(([x, y]) => [sq(x), y]);
  const polyR = polyL.map(([x, y]) => [64 - x, y]);
  const mask = Array.from({ length: N }, () => Array(N).fill(false));
  for (let y = 0; y < N; y++)
    for (let x = 0; x < N; x++) if (pip(polyL, x + 0.5, y + 0.5) || pip(polyR, x + 0.5, y + 0.5)) mask[y][x] = true;
  const bone = Array.from({ length: N }, () => Array(N).fill(false));
  for (const [[ax, ay], [bx, by]] of BONES) {
    line(bone, sq(ax), ay, sq(bx), by);
    line(bone, 64 - sq(ax) - 1, ay, 64 - sq(bx) - 1, by);
  }
  const MEM_A = [86, 52, 160], MEM_B = [58, 32, 116], BONE = [142, 104, 226], EDGE = [22, 10, 48];
  for (let y = 0; y < N; y++)
    for (let x = 0; x < N; x++) {
      if (!mask[y][x] && !bone[y][x]) continue;
      if (bone[y][x] && mask[y][x]) g[y][x] = BONE;
      else if (mask[y][x]) g[y][x] = y < 30 ? MEM_A : MEM_B;
    }
  // outline
  const out = [];
  for (let y = 0; y < N; y++)
    for (let x = 0; x < N; x++) {
      if (g[y][x]) continue;
      for (const [dx, dy] of [[1, 0], [-1, 0], [0, 1], [0, -1]])
        if (inside(x + dx, y + dy) && g[y + dy][x + dx] && g[y + dy][x + dx] !== EDGE) { out.push([x, y]); break; }
    }
  for (const [x, y] of out) g[y][x] = EDGE;
  return mask;
}

// ---- egg ------------------------------------------------------------------------------------
const CX = 32, TOP = 12, BOT = 54, A = 14.2;
function eggMask() {
  const m = Array.from({ length: N }, () => Array(N).fill(false));
  const cy = (TOP + BOT) / 2, b = (BOT - TOP) / 2;
  for (let y = TOP; y < BOT; y++) {
    const t = (y + 0.5 - cy) / b;
    const hw = A * Math.sqrt(Math.max(0, 1 - t * t)) * (1 + 0.17 * t);
    for (let x = 0; x < N; x++) if (Math.abs(x + 0.5 - CX) <= hw) m[y][x] = true;
  }
  return m;
}

function drawEgg(g) {
  const m = eggMask();
  const cy = (TOP + BOT) / 2;
  // slanted band coordinate
  const u = (x, y) => x + 0.5 - CX + 0.3 * (y + 0.5 - cy);
  let lo = Infinity, hi = -Infinity;
  for (let y = 0; y < N; y++) for (let x = 0; x < N; x++) if (m[y][x]) { lo = Math.min(lo, u(x, y)); hi = Math.max(hi, u(x, y)); }
  const OUTLINE = [20, 10, 40];
  for (let y = 0; y < N; y++)
    for (let x = 0; x < N; x++) {
      if (!m[y][x]) continue;
      const band = clamp(Math.floor(((u(x, y) - lo) / (hi - lo + 1e-6)) * 8), 0, 7);
      const breed = ORDER[band];
      let c = textures[breed]((x - 8 + band * 3) & 15, (y - TOP) & 15);
      const edgeBR = !(inside(x + 2, y + 2) && m[y + 2][x + 2]) || !(inside(x + 1, y + 1) && m[y + 1][x + 1]);
      const edgeTL = !(inside(x - 2, y - 2) && m[y - 2][x - 2]) || !(inside(x - 1, y - 1) && m[y - 1][x - 1]);
      if (edgeBR) c = mul(c, 0.62);
      else if (edgeTL) c = mix(c, [255, 255, 255], 0.28);
      g[y][x] = c.map(Math.round);
    }
  // dark outline
  const outl = [];
  for (let y = 0; y < N; y++)
    for (let x = 0; x < N; x++) {
      if (m[y][x]) continue;
      for (const [dx, dy] of [[1, 0], [-1, 0], [0, 1], [0, -1]])
        if (inside(x + dx, y + dy) && m[y + dy][x + dx]) { outl.push([x, y]); break; }
    }
  for (const [x, y] of outl) g[y][x] = OUTLINE;
  // specular glints
  const W = [255, 255, 255];
  for (const [x, y] of [[24, 20], [25, 19], [26, 18], [24, 22], [23, 24]]) g[y][x] = W;
  return m;
}

// ---- background decoration (stars) ------------------------------------------------------------
function drawStars(g) {
  const S = [200, 226, 255, 230];
  const plus = (x, y) => { for (const [dx, dy] of [[0, 0], [1, 0], [-1, 0], [0, 1], [0, -1]]) if (!g[y + dy][x + dx]) g[y + dy][x + dx] = S; };
  const dot = (x, y) => { if (!g[y][x]) g[y][x] = [200, 226, 255, 170]; };
  plus(52, 12); plus(11, 55);
  [[58, 24], [6, 47], [47, 6], [14, 6], [57, 56], [30, 59], [60, 38]].forEach(([x, y]) => dot(x, y));
}

export function buildArt() {
  const g = mk();
  drawWings(g);
  // soft ground shadow under the egg
  for (let y = 54; y < 58; y++)
    for (let x = 0; x < N; x++) {
      const dx = (x + 0.5 - CX) / 14, dy = (y + 0.5 - 55.5) / 2.6;
      if (dx * dx + dy * dy <= 1 && !g[y][x]) g[y][x] = [8, 4, 20, 150];
    }
  drawEgg(g);
  drawStars(g);
  return g;
}
