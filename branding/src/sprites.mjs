// Hand-drawn char-map sprites. One char = one grid cell; '.' = transparent.
// Light comes from the upper left: highlights on top/left faces, shadows bottom/right.

// Dragon legend (fire-red hero dragon).
export const DRAGON = {
  K: 'r0', d: 'r1', m: 'r2', l: 'r3', h: 'o1', Y: 'o2', g: 'o1', G: 'o3',
  B: 'moon', b: 'moon2', e: 'ink', W: 'star', w: 'w2', v: 'w1',
};

// Head in profile, facing right (36 x 21). Two swept-back horns, brow ridge, slit eye,
// nostril, fang, gold under-jaw.
export const HEAD = `
|KK
|KBKK
|.KBBKK
|.KbBBBKK
|..KbbBBBKK.......KKKKK
|...KKbbBBBK....KKlllllKK
|.....KKbbBBKKKKllllllllhKKK
|.......KKKbbKlllllllllllllhhKKK
|....KKKKKbKlllllllllllllllllllhKKK
|...KBBbbKKllllKKKKKKKlllllllllllllhK
|..KBBbbKKlllKKYYYYYeKmmmmmmmmmmmmlllK
|.KBbKKllllmmKdKKKKKKmmmmmmmmmmmmmmKmK
|.KKKKlmmmmmmmdddddmmmmmmmmmmmmmmmmmmK
|...KlmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmdK
|...KlmmmmmmmmmmmmmmmmmmmKKKKKKKKKKKK
|..KlmmmmmmmmmmmmmmmmmmKdddddddddWddK
|.KBKmmmmmmmmmmmmmmmmmKmmmmmmmmmmWmK
|KBbKKmmmmmmmmmmmmmmmdKggggggggggKK
|KKK.KdmmmmmmmmmmmmmdKGGGGGGGGKK
|....KdmmmmmmmmmmmmdKKKKKKKKK
`;

// Small breed egg, 7 x 9. h = light, m = base, d = shade, a = accent, K = outline.
export const EGG_SMALL = `
|..KKK..
|.KhmmK.
|.KhamK.
|KhmmmdK
|KhmmadK
|KmamddK
|KmmmddK
|.KmddK.
|..KKK..
`;

// Front claw hooked over the shell rim.
export const CLAW = `
|.KKKKK.
|KllmmmK
|KmmmmdK
|KdKdKdK
|KBKBKBK
|.b.b.b.
`;

// Tail spade, pointing up.
export const SPADE = `
|...K
|..KhK
|.KlhmK
|KllmmdK
|KKlmdKK
|..KdK
`;

// Large breed egg for the banner, 9 x 12, plus one accent overlay per breed
// (patterns echo the mod's egg textures).
export const EGG_BIG = `
|...KKK...
|..KhmmK..
|.KhmmmmK.
|.KhmmmmK.
|KhmmmmmdK
|KhmmmmmdK
|KmmmmmmdK
|KmmmmmddK
|KmmmmmddK
|.KmmmddK.
|..KdddK..
|...KKK...
`;
export const EGG_PATTERN = {
  aether: `
|.........
|.........
|.....a...
|....a....
|...a...a.
|..a...a..
|.....a...
|....a....
|...a.....`,
  end: `
|.........
|.........
|....a....
|.........
|..a...a..
|.........
|....a....
|.a.......
|......a..
|...a.....`,
  fire: `
|.........
|.........
|....a....
|...a.....
|...aa....
|.....a...
|....a..a.
|...a..a..
|......a..
|....a....`,
  forest: `
|.........
|.........
|...aa....
|.........
|.....aa..
|..a......
|..aa.....
|......a..
|.....aa..
|...a.....`,
  ghost: `
|.........
|.........
|.........
|...aaa...
|..a...a..
|.........
|...aaa...
|..a...a..`,
  ice: `
|.........
|.........
|...a.....
|..aaa....
|...a.....
|......a..
|.....aaa.
|......a..`,
  nether: `
|.........
|.........
|....a....
|.........
|..aa.....
|......a..
|.....aa..
|...a.....
|...aa....`,
  water: `
|.........
|.........
|.........
|..aa.aa..
|.a..a..a.
|.........
|..aa.aa..
|.a..a..a.`,
};
