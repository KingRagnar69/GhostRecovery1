package com.RagDev69.ghostrecovery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController

// 🔹 Categories Enum
enum class ImageCategory(val displayName: String) {
    CAMERA("Camera"),
    WHATSAPP("WhatsApp"),
    DOWNLOAD("Download"),
    OTHER("Other")
}

// 🔹 Category selection screen
@Composable
fun CategoryScreen(
    navController: NavController,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit
) {
    val context = LocalContext.current

    // 🔹 Permission setup
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // optional: handle result if needed
    }

    // ✅ Check if permission is already granted
    val isGranted = ContextCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED

    // ✅ Ask for permission automatically if not granted
    LaunchedEffect(Unit) {
        if (!isGranted) {
            launcher.launch(permission)
        }
    }

    // 🔹 UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        // 🔥 TOP BAR WITH DARK MODE BUTTON
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "GhostRecovery 👻",
                style = MaterialTheme.typography.headlineMedium
            )

            Button(
                onClick = { onToggleDarkMode() }
            ) {
                Text(
                    text = if (isDarkMode) "🌙 ON" else "☀️ OFF"
                )
            }
        }

        Text(
            text = "Select a category to scan",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 🔹 CATEGORY LOOP
        ImageCategory.values().forEach { category ->

            ElevatedCard(
                onClick = {
                    // 🔹 Request permission before navigating
                    if (isGranted) {
                        navController.navigate("scan/${category.name}")
                    } else {
                        launcher.launch(permission)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(8.dp)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column {
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Text(
                            text = "Tap to scan",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Text(
                        text = when (category) {
                            ImageCategory.CAMERA -> "📷"
                            ImageCategory.WHATSAPP -> "💬"
                            ImageCategory.DOWNLOAD -> "⬇️"
                            ImageCategory.OTHER -> "📁"
                        },
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 🔻 INFO CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = "⚠️ Permissions & Info",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text =
                        "• This app requires media access permission to scan your files.\n" +
                                "• If no results appear, please enable permissions in settings.\n\n" +
                                "• Scan results may include existing images along with recoverable (deleted) traces.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}