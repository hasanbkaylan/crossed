import re

with open('app/src/main/java/com/example/ui/screens/NearbyScreen.kt', 'r') as f:
    content = f.read()

# Add timer state
timer_state = """    val permissionState = rememberMultiplePermissionsState(permissions)

    var statusTimer by remember { mutableStateOf(0) }
    LaunchedEffect(matchStatus) {
        statusTimer = 0
        while (true) {
            kotlinx.coroutines.delay(1000)
            statusTimer++
        }
    }
"""
content = content.replace("    val permissionState = rememberMultiplePermissionsState(permissions)", timer_state)

content = content.replace(
    'Text("Hazırlanıyor...", style = MaterialTheme.typography.titleLarge)',
    'Text("Hazırlanıyor... ($statusTimer sn)", style = MaterialTheme.typography.titleLarge)'
)
content = content.replace(
    'Text(stringResource(R.string.nearby_looking), style = MaterialTheme.typography.titleLarge)',
    'Text(stringResource(R.string.nearby_looking) + " ($statusTimer sn)", style = MaterialTheme.typography.titleLarge)'
)
content = content.replace(
    'Text(stringResource(R.string.nearby_connecting, status.device.name), style = MaterialTheme.typography.titleLarge)',
    'Text(stringResource(R.string.nearby_connecting, status.device.name) + " ($statusTimer sn)", style = MaterialTheme.typography.titleLarge)'
)
content = content.replace(
    'Text(stringResource(R.string.nearby_exchanging), style = MaterialTheme.typography.titleLarge)',
    'Text(stringResource(R.string.nearby_exchanging) + " ($statusTimer sn)", style = MaterialTheme.typography.titleLarge)'
)

with open('app/src/main/java/com/example/ui/screens/NearbyScreen.kt', 'w') as f:
    f.write(content)
