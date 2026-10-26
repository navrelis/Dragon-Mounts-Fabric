// Shared helpers for the branding build. Dependencies (pngjs, @resvg/resvg-js) are NOT stored
// in the project; point BRANDING_DEPS at a directory that contains node_modules with them.
import { createRequire } from 'node:module';
import path from 'node:path';

const depsDir = process.env.BRANDING_DEPS;
if (!depsDir) {
  console.error('Set BRANDING_DEPS to a folder whose node_modules contains pngjs and @resvg/resvg-js (see branding/README-branding.md).');
  process.exit(1);
}
const req = createRequire(path.join(path.resolve(depsDir), 'noop.js'));
export const { PNG } = req('pngjs');
export const { Resvg } = req('@resvg/resvg-js');

export const hex = (c) => '#' + c.slice(0, 3).map((v) => Math.round(v).toString(16).padStart(2, '0')).join('');

/** Emit a pixel grid (array of rows of null | [r,g,b,a?]) as merged <rect> runs. */
export function gridToRects(grid) {
  let out = '';
  for (let y = 0; y < grid.length; y++) {
    const row = grid[y];
    let x = 0;
    while (x < row.length) {
      const c = row[x];
      if (!c) { x++; continue; }
      const key = hex(c) + '/' + (c[3] ?? 255);
      let x2 = x + 1;
      while (x2 < row.length && row[x2] && hex(row[x2]) + '/' + (row[x2][3] ?? 255) === key) x2++;
      const a = (c[3] ?? 255) / 255;
      out += `<rect x="${x}" y="${y}" width="${x2 - x}" height="1" fill="${hex(c)}"${a < 1 ? ` fill-opacity="${a.toFixed(2)}"` : ''}/>`;
      x = x2;
    }
    out += '\n';
  }
  return out;
}

/** Pixel font glyph rows -> grid placement list. */
export function textRects(text, glyphs, cell, x0, y0, fill, extra = '') {
  let out = '';
  let cx = 0;
  for (const ch of text) {
    const g = glyphs[ch];
    if (!g) throw new Error('missing glyph for ' + JSON.stringify(ch));
    const w = g[0].length;
    for (let r = 0; r < g.length; r++) {
      let c = 0;
      while (c < w) {
        if (g[r][c] !== '#') { c++; continue; }
        let c2 = c + 1;
        while (c2 < w && g[r][c2] === '#') c2++;
        out += `<rect x="${x0 + (cx + c) * cell}" y="${y0 + r * cell}" width="${(c2 - c) * cell}" height="${cell}" fill="${fill}"${extra}/>`;
        c = c2;
      }
    }
    cx += w + 1;
  }
  return out;
}

export function textWidth(text, glyphs, cell) {
  let n = 0;
  for (const ch of text) n += glyphs[ch][0].length + 1;
  return (n - 1) * cell;
}

export function render(svg, width) {
  return new Resvg(svg, { fitTo: { mode: 'width', value: width }, font: { loadSystemFonts: false } }).render().asPng();
}
