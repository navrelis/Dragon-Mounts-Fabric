// Pixel-grid helpers for the branding build. Everything is drawn on an integer cell grid that
// stores palette keys (or null = transparent); PNGs are written with an integer
// nearest-neighbour upscale, so every output pixel is fully opaque or fully transparent.
//
// pngjs is NOT stored in the project: point BRANDING_DEPS at a folder whose node_modules
// contains it (see branding/README-branding.md).
import { createRequire } from 'node:module';
import path from 'node:path';

const depsDir = process.env.BRANDING_DEPS;
if (!depsDir) {
  console.error('Set BRANDING_DEPS to a folder whose node_modules contains pngjs (see branding/README-branding.md).');
  process.exit(1);
}
const req = createRequire(path.join(path.resolve(depsDir), 'noop.js'));
export const { PNG } = req('pngjs');

export class Grid {
  constructor(w, h) {
    this.w = w;
    this.h = h;
    this.p = new Array(w * h).fill(null);
  }
  in(x, y) { return x >= 0 && y >= 0 && x < this.w && y < this.h; }
  get(x, y) { return this.in(x, y) ? this.p[y * this.w + x] : null; }
  set(x, y, c) { if (this.in(x, y)) this.p[y * this.w + x] = c; }

  rect(x, y, w, h, c) {
    for (let j = y; j < y + h; j++) for (let i = x; i < x + w; i++) this.set(i, j, c);
  }

  /** Filled disc, tested at cell centres: a stepped circle. */
  disc(cx, cy, r, c) {
    for (let y = Math.floor(cy - r); y <= Math.ceil(cy + r); y++)
      for (let x = Math.floor(cx - r); x <= Math.ceil(cx + r); x++)
        if ((x - cx) ** 2 + (y - cy) ** 2 <= r * r) this.set(x, y, c);
  }

  /** Bresenham line, one cell wide. */
  line(x0, y0, x1, y1, c) {
    const dx = Math.abs(x1 - x0), dy = -Math.abs(y1 - y0);
    const sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
    let err = dx + dy;
    for (;;) {
      this.set(x0, y0, c);
      if (x0 === x1 && y0 === y1) break;
      const e2 = 2 * err;
      if (e2 >= dy) { err += dy; x0 += sx; }
      if (e2 <= dx) { err += dx; y0 += sy; }
    }
  }

  /** Filled polygon with integer vertices, sampled at cell centres (even-odd rule). */
  poly(pts, c) {
    const ys = pts.map((p) => p[1]);
    for (let y = Math.min(...ys); y <= Math.max(...ys); y++) {
      const sy = y + 0.5;
      const xs = [];
      for (let i = 0, j = pts.length - 1; i < pts.length; j = i++) {
        const [xi, yi] = pts[i], [xj, yj] = pts[j];
        if ((yi > sy) !== (yj > sy)) xs.push(xi + ((sy - yi) * (xj - xi)) / (yj - yi));
      }
      xs.sort((a, b) => a - b);
      for (let k = 0; k + 1 < xs.length; k += 2)
        for (let x = Math.ceil(xs[k] - 0.5); x + 0.5 <= xs[k + 1]; x++) this.set(x, y, c);
    }
  }

  /** Add a one-cell outline of colour c around everything drawn (4-neighbourhood). */
  outline(c) {
    const add = [];
    for (let y = 0; y < this.h; y++) for (let x = 0; x < this.w; x++) {
      if (this.get(x, y) !== null) continue;
      if (this.get(x - 1, y) !== null || this.get(x + 1, y) !== null || this.get(x, y - 1) !== null || this.get(x, y + 1) !== null)
        add.push([x, y]);
    }
    for (const [x, y] of add) this.set(x, y, c);
  }

  /**
   * Stamp a hand-drawn char map. `rows` is a string (one line per grid row; leading/trailing
   * blank lines are ignored, indentation is NOT stripped so columns stay exact - use '|' as an
   * optional left margin marker). `legend` maps chars to palette keys; '.' and ' ' are skipped.
   */
  stamp(rows, legend, ox = 0, oy = 0, { flip = false } = {}) {
    const lines = charRows(rows);
    const w = Math.max(...lines.map((l) => l.length));
    lines.forEach((line, y) => {
      for (let x = 0; x < line.length; x++) {
        const ch = line[x];
        if (ch === '.' || ch === ' ') continue;
        const c = legend[ch];
        if (c === undefined) throw new Error(`no legend entry for '${ch}' (row ${y}: ${line})`);
        this.set(ox + (flip ? w - 1 - x : x), oy + y, c);
      }
    });
  }

  /** Copy every non-null cell of `g` onto this grid at (ox, oy). */
  draw(g, ox = 0, oy = 0) {
    for (let y = 0; y < g.h; y++) for (let x = 0; x < g.w; x++) {
      const c = g.p[y * g.w + x];
      if (c !== null) this.set(ox + x, oy + y, c);
    }
  }

}

/** Split a char-map template into rows ('|' marks an optional left edge). */
export function charRows(rows) {
  const lines = rows.split('\n').map((l) => l.replace(/\s+$/, ''));
  while (lines.length && lines[0].trim() === '') lines.shift();
  while (lines.length && lines[lines.length - 1].trim() === '') lines.pop();
  return lines.map((l) => (l.includes('|') ? l.slice(l.indexOf('|') + 1) : l));
}

export const hexToRgb = (h) => [1, 3, 5].map((i) => parseInt(h.slice(i, i + 2), 16));

/** Grid -> PNG buffer, every cell scaled to `scale` x `scale` output pixels. */
export function toPng(grid, palette, scale) {
  const png = new PNG({ width: grid.w * scale, height: grid.h * scale });
  for (let y = 0; y < grid.h; y++) for (let x = 0; x < grid.w; x++) {
    const key = grid.get(x, y);
    let rgba = [0, 0, 0, 0];
    if (key !== null) {
      const hex = palette[key];
      if (!hex) throw new Error('unknown palette key ' + key);
      rgba = [...hexToRgb(hex), 255];
    }
    for (let j = 0; j < scale; j++) for (let i = 0; i < scale; i++) {
      const k = ((y * scale + j) * png.width + (x * scale + i)) * 4;
      png.data[k] = rgba[0]; png.data[k + 1] = rgba[1]; png.data[k + 2] = rgba[2]; png.data[k + 3] = rgba[3];
    }
  }
  return PNG.sync.write(png, { colorType: 6 });
}
