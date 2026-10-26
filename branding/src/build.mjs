// Builds branding/logo.svg|png, src/main/resources/assets/dragonmounts/icon.png and branding/banner.svg|png.
// Usage: BRANDING_DEPS=<dir with node_modules> node branding/src/build.mjs [previewDir]
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { gridToRects, textRects, textWidth, render } from './lib.mjs';
import { buildArt, N } from './art.mjs';
import { glyphs } from './font.mjs';

const here = path.dirname(fileURLToPath(import.meta.url));
const brandingDir = path.resolve(here, '..');
const projectDir = path.resolve(brandingDir, '..');
const previewDir = process.argv[2];

const art = buildArt();
const rects = gridToRects(art);

const BG_TOP = '#2a0f55', BG_BOT = '#0b4b57';
const defs = (id) => `<defs>
<linearGradient id="${id}" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="${BG_TOP}"/><stop offset="1" stop-color="${BG_BOT}"/></linearGradient>
<radialGradient id="${id}glow" cx="0.5" cy="0.58" r="0.5"><stop offset="0" stop-color="#9b6bff" stop-opacity="0.45"/><stop offset="1" stop-color="#9b6bff" stop-opacity="0"/></radialGradient>
</defs>`;

/** Square logo. size = canvas px, px = px per art cell, off = canvas offset of the art grid. */
function logoSvg(size, px, off) {
  const rx = Math.round(size * 0.18);
  const inset = Math.round(size * 0.035);
  return `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}" viewBox="0 0 ${size} ${size}">
<title>Dragon Mounts Fabric</title>
${defs('bg')}
<rect width="${size}" height="${size}" rx="${rx}" fill="url(#bg)"/>
<rect width="${size}" height="${size}" rx="${rx}" fill="url(#bgglow)"/>
<rect x="${inset}" y="${inset}" width="${size - 2 * inset}" height="${size - 2 * inset}" rx="${rx - inset}" fill="none" stroke="#ffffff" stroke-opacity="0.16" stroke-width="${Math.max(1, size / 130)}" stroke-dasharray="${size / 40} ${size / 50}" stroke-linecap="butt"/>
<g transform="translate(${off} ${off}) scale(${px})" shape-rendering="crispEdges">
${rects}</g>
</svg>
`;
}

// 400px logo: 6 px per art cell (64 * 6 = 384, offset 8). 128px icon: 2 px per cell, exact.
const logo = logoSvg(400, 6, 8);
const icon = logoSvg(128, 2, 0);
fs.writeFileSync(path.join(brandingDir, 'logo.svg'), logo);
fs.writeFileSync(path.join(brandingDir, 'logo.png'), render(logo, 400));
const iconPath = path.join(projectDir, 'src/main/resources/assets/dragonmounts/icon.png');
fs.mkdirSync(path.dirname(iconPath), { recursive: true });
fs.writeFileSync(iconPath, render(icon, 128));

// ---- banner ---------------------------------------------------------------------------------
const BW = 1200, BH = 400;
const CELL = 9, SUB = 4;
const line1 = 'DRAGON MOUNTS', line2 = 'FABRIC', sub = 'UNOFFICIAL PORT • FABRIC 1.21.1';
const tx = 440;
const capH = 7 * CELL;
const subH = 7 * SUB;
const blockH = capH * 2 + CELL * 2 + 26 + subH;
const ty = Math.round((BH - blockH) / 2);
const t = (txt, cell, x, y, fill) => textRects(txt, glyphs, cell, x, y, fill);
const w1 = textWidth(line1, glyphs, CELL);
const subY = ty + capH * 2 + CELL * 2 + 26;
const banner = `<svg xmlns="http://www.w3.org/2000/svg" width="${BW}" height="${BH}" viewBox="0 0 ${BW} ${BH}">
<title>Dragon Mounts Fabric - Unofficial Port - Fabric 1.21.1</title>
<defs>
<linearGradient id="bbg" x1="0" y1="0" x2="1" y2="0.6"><stop offset="0" stop-color="${BG_TOP}"/><stop offset="1" stop-color="${BG_BOT}"/></linearGradient>
<radialGradient id="bglow" cx="0.18" cy="0.55" r="0.42"><stop offset="0" stop-color="#9b6bff" stop-opacity="0.5"/><stop offset="1" stop-color="#9b6bff" stop-opacity="0"/></radialGradient>
</defs>
<rect width="${BW}" height="${BH}" fill="url(#bbg)"/>
<rect width="${BW}" height="${BH}" fill="url(#bglow)"/>
<rect x="10" y="10" width="${BW - 20}" height="${BH - 20}" fill="none" stroke="#ffffff" stroke-opacity="0.14" stroke-width="3" stroke-dasharray="14 10"/>
<g transform="translate(24 8) scale(6)" shape-rendering="crispEdges">
${rects}</g>
<g shape-rendering="crispEdges">
${t(line1, CELL, tx + CELL, ty + CELL, '#14082b')}
${t(line2, CELL, tx + CELL, ty + capH + CELL * 2 + CELL, '#14082b')}
${t(line1, CELL, tx, ty, '#ffffff')}
${t(line2, CELL, tx, ty + capH + CELL * 2, '#9fe8ff')}
${t(sub, SUB, tx + 3, subY + 3, '#14082b')}
${t(sub, SUB, tx, subY, '#d9c8ff')}
</g>
</svg>
`;
fs.writeFileSync(path.join(brandingDir, 'banner.svg'), banner);
fs.writeFileSync(path.join(brandingDir, 'banner.png'), render(banner, BW));

if (previewDir) {
  fs.mkdirSync(previewDir, { recursive: true });
  fs.writeFileSync(path.join(previewDir, 'logo-64.png'), render(logo, 64));
  fs.writeFileSync(path.join(previewDir, 'logo-32.png'), render(logo, 32));
  fs.writeFileSync(path.join(previewDir, 'icon-64.png'), render(icon, 64));
  // contact sheet: 400, 128, 64, 32 on light and dark backgrounds
  const sheet = `<svg xmlns="http://www.w3.org/2000/svg" width="760" height="460">
<rect width="380" height="460" fill="#ffffff"/><rect x="380" width="380" height="460" fill="#202225"/>
${[0, 380].map((ox) => `<g transform="translate(${ox + 10} 10)"><image href="data:image/png;base64,${render(logo, 360).toString('base64')}" width="360" height="360"/></g>
<image x="${ox + 10}" y="380" width="64" height="64" href="data:image/png;base64,${render(logo, 64).toString('base64')}"/>
<image x="${ox + 90}" y="380" width="32" height="32" href="data:image/png;base64,${render(logo, 32).toString('base64')}"/>
<image x="${ox + 140}" y="380" width="128" height="64" preserveAspectRatio="xMinYMin" href="data:image/png;base64,${render(icon, 128).toString('base64')}"/>`).join('')}
</svg>`;
  fs.writeFileSync(path.join(previewDir, 'sheet.png'), render(sheet, 760));
}
console.log('done');
