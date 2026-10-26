// The one shared palette for all branding images. Keys are used by the char maps in sprites.mjs
// and by art.mjs. Hue-shifted ramps: shadows lean violet/cool, highlights lean orange/warm.
// Breed colours are taken from the mod's own egg/dragon textures.
export const PALETTE = {
  // night sky + frame
  ink: '#0e0a1a', // darkest outline, badge border, pupils
  sky: '#1c1840', // background
  sky2: '#2a2559', // frame band, distant hills
  dim: '#4a4389', // dim stars, frame bevel, End egg light
  star: '#fff3d6', // bright stars, glints, Ice egg highlight
  moon: '#f3e2ae', // moon, bone/horn light, shell inside
  moon2: '#c9ad78', // moon craters, bone/horn shade

  // dragon (fire red) + wings
  r0: '#3d0b22', // dragon outline
  r1: '#761530', // shadow, Fire egg base
  r2: '#b3242f', // base
  r3: '#e04a33', // light
  o1: '#f2842a', // hot highlight, belly, lava
  o2: '#ffcf55', // gold: eye, belly light
  o3: '#b0501f', // belly shadow
  w1: '#4a0f38', // wing membrane shadow
  w2: '#7a1c44', // wing membrane

  // breed accents (eggs)
  a1: '#4c5d74', // Aether base
  i2: '#8fb2d0', // Aether light / Ice shade
  c1: '#3fd0f5', // Aether + Ghost cyan
  e1: '#241d36', // End base
  p1: '#b04ae0', // End purple
  f1: '#1f4a1b', // Forest dark
  f2: '#3d7a28', // Forest base
  f3: '#7fb53c', // Forest light
  g1: '#a5a9b4', // Ghost base
  i1: '#d3e3f0', // Ice base / Ghost light
  n1: '#4d2620', // Nether base
  u1: '#2c6cc0', // Water shade
  u2: '#4fa3e6', // Water base
};
