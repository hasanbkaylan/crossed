#!/bin/bash
RES_DIR="app/src/main/res"
declare -A sizes=( ["mdpi"]=48 ["hdpi"]=72 ["xhdpi"]=96 ["xxhdpi"]=144 ["xxxhdpi"]=192 )

npm install -g sharp-cli

for dpi in "${!sizes[@]}"; do
    sz="${sizes[$dpi]}"
    mkdir -p "$RES_DIR/mipmap-$dpi"
    npx sharp-cli -i square.svg -o "$RES_DIR/mipmap-$dpi/ic_launcher.png" resize $sz $sz
    npx sharp-cli -i round.svg -o "$RES_DIR/mipmap-$dpi/ic_launcher_round.png" resize $sz $sz
done
echo "PNG generation done"
