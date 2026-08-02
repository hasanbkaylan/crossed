#!/bin/bash

# Define paths
RES_DIR="app/src/main/res"

# 1. Create Vector Drawables
mkdir -p "$RES_DIR/drawable"

cat << 'XML' > "$RES_DIR/drawable/ic_launcher_foreground.xml"
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:strokeColor="#FFFFFF"
        android:strokeWidth="12"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:pathData="M 44 32 L 24 54 L 44 76 M 64 32 L 84 54 L 64 76" />
</vector>
XML

cat << 'XML' > "$RES_DIR/drawable/ic_launcher_background.xml"
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#151210"
        android:pathData="M0,0h108v108h-108z" />
</vector>
XML

cat << 'XML' > "$RES_DIR/drawable/ic_launcher_monochrome.xml"
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:strokeColor="#000000"
        android:strokeWidth="12"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:pathData="M 44 32 L 24 54 L 44 76 M 64 32 L 84 54 L 64 76" />
</vector>
XML

# 2. Update Adaptive Icons
cat << 'XML' > "$RES_DIR/mipmap-anydpi-v26/ic_launcher.xml"
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />
</adaptive-icon>
XML

cat << 'XML' > "$RES_DIR/mipmap-anydpi-v26/ic_launcher_round.xml"
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />
</adaptive-icon>
XML

# 3. Generate PNGs using ImageMagick
cat << 'SVG' > square.svg
<svg xmlns="http://www.w3.org/2000/svg" width="108" height="108" viewBox="0 0 108 108">
  <rect width="108" height="108" fill="#151210" />
  <path d="M 44 32 L 24 54 L 44 76" fill="none" stroke="#ffffff" stroke-width="12" stroke-linecap="round" stroke-linejoin="round" />
  <path d="M 64 32 L 84 54 L 64 76" fill="none" stroke="#ffffff" stroke-width="12" stroke-linecap="round" stroke-linejoin="round" />
</svg>
SVG

cat << 'SVG' > round.svg
<svg xmlns="http://www.w3.org/2000/svg" width="108" height="108" viewBox="0 0 108 108">
  <circle cx="54" cy="54" r="54" fill="#151210" />
  <path d="M 44 32 L 24 54 L 44 76" fill="none" stroke="#ffffff" stroke-width="12" stroke-linecap="round" stroke-linejoin="round" />
  <path d="M 64 32 L 84 54 L 64 76" fill="none" stroke="#ffffff" stroke-width="12" stroke-linecap="round" stroke-linejoin="round" />
</svg>
SVG

# Arrays for sizes
declare -A sizes=( ["mdpi"]=48 ["hdpi"]=72 ["xhdpi"]=96 ["xxhdpi"]=144 ["xxxhdpi"]=192 )

for dpi in "${!sizes[@]}"; do
    sz="${sizes[$dpi]}"
    mkdir -p "$RES_DIR/mipmap-$dpi"
    convert -background none square.svg -resize ${sz}x${sz} "$RES_DIR/mipmap-$dpi/ic_launcher.png"
    convert -background none round.svg -resize ${sz}x${sz} "$RES_DIR/mipmap-$dpi/ic_launcher_round.png"
done

rm square.svg round.svg
echo "Done"
