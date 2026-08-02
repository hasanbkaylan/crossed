const fs = require('fs');
const sharp = require('sharp');

const squareSvg = `
<svg xmlns="http://www.w3.org/2000/svg" width="108" height="108" viewBox="0 0 108 108">
  <rect width="108" height="108" fill="#151210" />
  <path d="M 40 40 L 26 54 L 40 68 M 68 40 L 82 54 L 68 68" fill="none" stroke="#ffffff" stroke-width="10" stroke-linecap="round" stroke-linejoin="round" />
</svg>
`;

const roundSvg = `
<svg xmlns="http://www.w3.org/2000/svg" width="108" height="108" viewBox="0 0 108 108">
  <circle cx="54" cy="54" r="54" fill="#151210" />
  <path d="M 40 40 L 26 54 L 40 68 M 68 40 L 82 54 L 68 68" fill="none" stroke="#ffffff" stroke-width="10" stroke-linecap="round" stroke-linejoin="round" />
</svg>
`;

async function main() {
    fs.writeFileSync('square.svg', squareSvg);
    fs.writeFileSync('round.svg', roundSvg);
    const sizes = { mdpi: 48, hdpi: 72, xhdpi: 96, xxhdpi: 144, xxxhdpi: 192 };
    
    for (const [dpi, sz] of Object.entries(sizes)) {
        await sharp('square.svg').resize(sz, sz).toFile(`app/src/main/res/mipmap-${dpi}/ic_launcher.png`);
        await sharp('round.svg').resize(sz, sz).toFile(`app/src/main/res/mipmap-${dpi}/ic_launcher_round.png`);
        console.log(`Generated for ${dpi}`);
    }
}
main().catch(console.error);
