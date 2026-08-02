#!/bin/bash
sed -i '/import androidx.compose.ui.text.style.TextAlign/a import androidx.compose.ui.draw.clip\nimport coil.compose.AsyncImage\nimport androidx.compose.ui.layout.ContentScale' app/src/main/java/com/example/ui/screens/NearbyScreen.kt
sed -i 's/\.androidx\.compose\.ui\.draw\.clip/.clip/g' app/src/main/java/com/example/ui/screens/NearbyScreen.kt
sed -i 's/coil\.compose\.AsyncImage/AsyncImage/g' app/src/main/java/com/example/ui/screens/NearbyScreen.kt
