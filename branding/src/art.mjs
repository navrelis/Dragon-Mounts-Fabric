// Composition of the branding images on integer grids.
import { Grid } from './lib.mjs';
import { DRAGON, HEAD, EGG_SMALL, EGG_BIG, EGG_PATTERN, SPADE, CLAW } from './sprites.mjs';

/** Directional shading of a flat layer: light from the upper left. ramp = [dark, mid, light]. */
function shade(g, ramp) {
  const out = new Grid(g.w, g.h);
  const on = (x, y) => g.get(x, y) !== null;
  for (let y = 0; y < g.h; y++) for (let x = 0; x < g.w; x++) {
    if (!on(x, y)) continue;
    const lit = !on(x - 1, y) || !on(x, y - 1);
    const dark = !on(x + 1, y) || !on(x, y + 1);
    out.set(x, y, lit && !dark ? ramp[2] : dark && !lit ? ramp[0] : ramp[1]);
  }
  return out;
}

function wing() {
  const S = [24, 36], E = [13, 26], W = [16, 6];
  const F = [[0, 0], [0, 14], [1, 27], [8, 37]];
  const m = new Grid(60, 60);
  m.poly([W, F[0], [7, 8], F[1], [7, 20], F[2], [9, 30], F[3], [15, 37], [20, 43], S, E], 'x');
  const g = shade(m, ['w1', 'w2', 'r1']);
  for (const f of F) {
    g.line(W[0], W[1], f[0], f[1], 'r2');
    // thicken the inner half of each finger
    g.line(W[0], W[1] + 1, Math.round((W[0] + f[0]) / 2), Math.round((W[1] + f[1]) / 2) + 1, 'r1');
  }
  // upper arm + forearm, three cells thick: lit top edge
  for (const [a, b] of [[S, E], [E, W]]) {
    g.line(a[0] + 1, a[1], b[0] + 1, b[1], 'r1');
    g.line(a[0], a[1], b[0], b[1], 'r2');
    g.line(a[0] - 1, a[1], b[0] - 1, b[1], 'r3');
  }
  // wrist claw
  g.set(W[0] - 1, W[1] - 1, 'moon'); g.set(W[0] - 1, W[1] - 2, 'moon'); g.set(W[0], W[1] - 1, 'moon2');
  g.outline('r0');
  return g;
}

// Neck: hand-chosen row spans [y, left, right] (outline cells included).
const NECK = [
  [22, 28, 42], [23, 28, 42], [24, 28, 41], [25, 28, 41], [26, 28, 41], [27, 27, 40], [28, 27, 40], [29, 26, 40],
  [30, 26, 39], [31, 25, 39], [32, 25, 39], [33, 24, 39], [34, 24, 39], [35, 23, 39], [36, 23, 40],
  [37, 22, 40], [38, 22, 40], [39, 21, 41], [40, 21, 41], [41, 21, 41], [42, 20, 41], [43, 20, 41],
  [44, 20, 41], [45, 19, 41], [46, 19, 41], [47, 19, 41], [48, 19, 41],
];
const SPIKES = [25, 30, 35]; // rows of the dorsal spikes

function neck(c) {
  const sp = new Grid(60, 60);
  for (const y of SPIKES) {
    const L = NECK.find((r) => r[0] === y)[1];
    sp.set(L, y, 'moon2'); sp.set(L, y + 1, 'moon2'); sp.set(L - 1, y - 1, 'moon2'); sp.set(L - 1, y, 'moon2');
    sp.set(L - 2, y - 2, 'moon'); sp.set(L - 2, y - 1, 'moon');
  }
  sp.outline('r0');
  c.draw(sp);
  for (const [y, L, R] of NECK) {
    const plate = (y - 22) % 3; // belly plates: light, mid, shadow line
    for (let x = L; x <= R; x++) {
      let k = 'r2';
      if (x === L || x === R) k = 'r0';
      else if (x === L + 1) k = 'r3';
      else if (x >= R - 5) k = plate === 2 ? 'o3' : x <= R - 4 && plate === 0 ? 'o2' : 'o1';
      else if (x === R - 6) k = 'r1';
      // cast shadow of the jaw
      if ((y === 22 && x > L + 1 && x < R - 6) || (y === 23 && x > L + 3 && x < R - 6)) k = 'r1';
      c.set(x, y, k);
    }
  }
}

function tail(c) {
  const t = new Grid(60, 60);
  const P = [[40, 56], [45, 55], [49, 53], [52, 50], [54, 46], [55, 42], [55, 38], [54, 34], [53, 31]];
  for (let i = 0; i + 1 < P.length; i++) {
    const r = i < 4 ? 1.5 : 1;
    const [x0, y0] = P[i], [x1, y1] = P[i + 1];
    const n = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
    for (let s = 0; s <= n; s++) t.disc(Math.round(x0 + ((x1 - x0) * s) / n), Math.round(y0 + ((y1 - y0) * s) / n), r, 'x');
  }
  const sh = shade(t, ['r1', 'r2', 'r3']);
  sh.outline('r0');
  c.draw(sh);
  c.stamp(SPADE, DRAGON, 50, 26);
}

// Hero egg: row spans of the full egg [y, left, right]; crack line = top row per column.
const EGG_ROWS = [
  [43, 18, 41], [44, 17, 42], [45, 16, 43], [46, 16, 43], [47, 15, 44], [48, 15, 44], [49, 15, 44], [50, 15, 44],
  [51, 15, 44], [52, 15, 44], [53, 16, 43], [54, 16, 43], [55, 17, 42], [56, 18, 41], [57, 20, 39], [58, 22, 37], [59, 25, 34],
];
const CRACK = [48, 47, 46, 45, 44, 45, 46, 47, 46, 45, 44, 43, 44, 45, 46, 47, 46, 45, 44, 45, 46, 47, 48, 47, 46, 45, 44, 45, 46, 47];

function egg(c, shards) {
  const e = new Grid(60, 60);
  for (const [y, L, R] of EGG_ROWS) for (let x = L; x <= R; x++) if (y >= CRACK[x - 15]) e.set(x, y, 'x');
  const sh = shade(e, ['r0', 'r1', 'r1']);
  // shell thickness along the crack: shell cells touching open air above / beside the crack
  for (let x = 15; x <= 44; x++) for (let y = 43; y < 50; y++)
    if (e.get(x, y) && y <= CRACK[x - 15] + 1 && (!e.get(x, y - 1) || (y < 49 && (!e.get(x - 1, y) || !e.get(x + 1, y))))) sh.set(x, y, 'moon');
  // glossy highlight, lava veins (hand placed)
  for (const [x, y] of [[17, 49], [17, 50], [18, 48], [17, 51], [18, 52]]) sh.set(x, y, 'r2');
  sh.set(18, 50, 'r3'); sh.set(18, 49, 'r3');
  for (const [x, y] of [[21, 50], [22, 51], [23, 51], [24, 52], [24, 53], [25, 54], [23, 54], [33, 49], [34, 50], [34, 51], [35, 52], [36, 52], [37, 53], [35, 54], [29, 55], [30, 56], [31, 56]])
    sh.set(x, y, 'o1');
  sh.outline('ink');
  c.draw(sh);
  c.stamp(CLAW, DRAGON, 15, 41);
  // flying shell shards
  if (shards) c.stamp('|KK.\n|KBbK\n|.KK.', DRAGON, 7, 44);
  c.stamp('|.KK\n|KBbK\n|KKK', DRAGON, 45, 38);
}

export function buildDragon({ shards = true } = {}) {
  const c = new Grid(60, 60);
  c.draw(wing());
  tail(c);
  neck(c);
  c.stamp(HEAD, DRAGON, 24, 2);
  egg(c, shards);
  return c;
}

// ---- shared scenery -------------------------------------------------------------------------

export const BREED_ORDER = ['aether', 'end', 'fire', 'forest', 'ghost', 'ice', 'nether', 'water'];
const BREEDS = {
  aether: { K: 'ink', h: 'i2', m: 'a1', d: 'e1', a: 'c1' },
  end: { K: 'ink', h: 'dim', m: 'e1', d: 'e1', a: 'p1' },
  fire: { K: 'ink', h: 'r2', m: 'r1', d: 'r0', a: 'o1' },
  forest: { K: 'ink', h: 'f3', m: 'f2', d: 'f1', a: 'f3' },
  ghost: { K: 'ink', h: 'i1', m: 'g1', d: 'a1', a: 'c1' },
  ice: { K: 'ink', h: 'star', m: 'i1', d: 'i2', a: 'star' },
  nether: { K: 'ink', h: 'o3', m: 'n1', d: 'r0', a: 'o1' },
  water: { K: 'ink', h: 'i2', m: 'u2', d: 'u1', a: 'i1' },
};
const smallEgg = (g, breed, x, y) => g.stamp(EGG_SMALL, BREEDS[breed], x, y);
const bigEgg = (g, breed, x, y) => { g.stamp(EGG_BIG, BREEDS[breed], x, y); g.stamp(EGG_PATTERN[breed], BREEDS[breed], x, y); };

/**
 * Stepped rounded-square badge: clears everything outside the shape and paints frame rings
 * (outermost first). 'bevel' = lit (dim) on the top/left half, sky2 on the bottom/right half.
 */
function badge(g, inset, rings) {
  const W = g.w, H = g.h;
  const inMask = (x, y) => {
    if (x < 0 || y < 0 || x >= W || y >= H) return false;
    const r = Math.min(y, H - 1 - y), cIn = r < inset.length ? inset[r] : 0;
    return x >= cIn && x < W - cIn;
  };
  const dist = (x, y) => {
    for (let d = 0; d < rings.length; d++)
      for (let j = -d - 1; j <= d + 1; j++) for (let i = -d - 1; i <= d + 1; i++) if (!inMask(x + i, y + j)) return d;
    return rings.length;
  };
  for (let y = 0; y < H; y++) for (let x = 0; x < W; x++) {
    if (!inMask(x, y)) { g.set(x, y, null); continue; }
    const d = dist(x, y);
    if (d >= rings.length) continue;
    const ring = rings[d];
    g.set(x, y, ring === 'bevel' ? (x + y < W ? 'dim' : 'sky2') : ring);
  }
}

function stars(g, list) {
  for (const [x, y, big] of list) {
    g.set(x, y, 'star');
    if (big) for (const [i, j] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) g.set(x + i, y + j, 'dim');
  }
}

function moon(g, cx, cy, r) {
  g.disc(cx, cy, r, 'i1');
  for (const [x, y] of [[-3, -2], [-2, -2], [-3, -1], [2, 1], [3, 1], [2, 2], [-1, 3], [0, 4]]) g.set(cx + x, cy + y, 'i2');
}

/** Hills: polyline of [x, topY] points, lit top edge. */
function hills(g, pts) {
  for (let x = 0; x < g.w; x++) {
    let k = 0;
    while (k + 2 < pts.length && pts[k + 1][0] <= x) k++;
    const [x0, y0] = pts[k], [x1, y1] = pts[k + 1];
    const top = Math.round(y0 + ((y1 - y0) * (x - x0)) / (x1 - x0));
    g.set(x, top, 'dim');
    for (let y = top + 1; y < g.h; y++) g.set(x, y, 'sky2');
  }
}

/** Glyph mask of a string (1 cell gap between glyphs). */
function textMask(text, font, gap = 1) {
  const h = font[text[0]].length;
  const w = [...text].reduce((n, ch) => n + font[ch][0].length + gap, -gap);
  const m = new Grid(w, h);
  let cx = 0;
  for (const ch of text) {
    const gl = font[ch];
    if (!gl) throw new Error('missing glyph ' + ch);
    gl.forEach((row, y) => [...row].forEach((v, x) => { if (v === '#') m.set(cx + x, y, 'x'); }));
    cx += gl[0].length + gap;
  }
  return m;
}

// ---- logo (80 x 80 grid) --------------------------------------------------------------------

export function buildLogo() {
  const g = new Grid(80, 80);
  g.rect(0, 0, 80, 80, 'sky');
  moon(g, 67, 10, 5.5);
  stars(g, [[8, 34, 0], [40, 8, 1], [70, 40, 0], [6, 7, 0], [74, 52, 1], [30, 6, 0], [7, 52, 0], [56, 46, 0], [52, 5, 0]]);
  hills(g, [[0, 69], [8, 67], [16, 68], [24, 66], [56, 66], [64, 67], [72, 66], [80, 68]]);
  g.draw(buildDragon(), 10, 8);
  // the hero egg is Fire; the other seven breeds sit around it
  for (const [b, x, y] of [['ice', 4, 62], ['water', 10, 64], ['aether', 16, 61], ['forest', 21, 65], ['ghost', 55, 64], ['nether', 61, 61], ['end', 67, 64]])
    smallEgg(g, b, x, y);
  badge(g, [4, 2, 1, 1], ['ink', 'bevel', 'sky2', 'ink']);
  return g;
}

// ---- icon (64 x 64 grid) --------------------------------------------------------------------

export function buildIcon() {
  const g = new Grid(64, 64);
  g.rect(0, 0, 64, 64, 'sky');
  stars(g, [[33, 5, 0], [58, 34, 0], [5, 40, 0], [47, 5, 0]]);
  hills(g, [[0, 58], [20, 57], [44, 57], [64, 58]]);
  g.draw(buildDragon({ shards: false }), 3, 1);
  badge(g, [3, 1, 1], ['ink', 'bevel', 'ink']);
  return g;
}

// ---- banner (240 x 80 grid) -----------------------------------------------------------------

export function buildBanner(font, title) {
  const g = new Grid(240, 80);
  g.rect(0, 0, 240, 80, 'sky');
  moon(g, 81, 11, 6.5);
  stars(g, [[40, 4, 1], [6, 44, 0], [100, 4, 0], [140, 5, 1], [178, 4, 0], [212, 6, 0], [231, 14, 1], [88, 42, 0], [233, 40, 0], [62, 50, 0], [6, 20, 0], [230, 50, 0]]);
  hills(g, [[0, 70], [12, 68], [40, 69], [72, 66], [86, 62], [98, 60], [130, 59], [160, 60], [196, 59], [226, 60], [240, 62]]);
  g.draw(buildDragon(), 8, 9);

  // title: gold, lit top / shaded bottom, red outline, extruded shadow
  const t = textMask('DRAGON MOUNTS', title);
  const tx = Math.round(78 + (158 - t.w) / 2), ty = 12;
  const ext = new Grid(240, 80);
  for (let d = 1; d <= 2; d++) ext.draw(t, tx + 0, ty + d);
  const extS = new Grid(240, 80);
  for (let y = 0; y < 80; y++) for (let x = 0; x < 240; x++) if (ext.get(x, y)) extS.set(x, y, 'r1');
  const face = new Grid(240, 80);
  for (let y = 0; y < t.h; y++) for (let x = 0; x < t.w; x++) if (t.get(x, y)) face.set(tx + x, ty + y, y < 6 ? 'o2' : 'o1');
  const both = new Grid(240, 80);
  both.draw(extS); both.draw(face);
  both.outline('ink');
  g.draw(both);

  // subtitle
  const sub = textMask('FABRIC • UNOFFICIAL PORT', font);
  const sx = Math.round(78 + (158 - sub.w) / 2), sy = 33;
  for (let y = 0; y < sub.h; y++) for (let x = 0; x < sub.w; x++) if (sub.get(x, y)) {
    g.set(sx + x + 1, sy + y + 1, 'ink');
  }
  for (let y = 0; y < sub.h; y++) for (let x = 0; x < sub.w; x++) if (sub.get(x, y)) g.set(sx + x, sy + y, 'i1');

  // the eight breed eggs
  const ex = Math.round(78 + (158 - (8 * 9 + 7 * 5)) / 2);
  BREED_ORDER.forEach((b, i) => bigEgg(g, b, ex + i * 14, 48));
  return g;
}
