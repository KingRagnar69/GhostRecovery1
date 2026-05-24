package com.RagDev69.ghostrecovery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import androidx.navigation.NavController

/* ── Categories ────────────────────────────── */
enum class ImageCategory(
    val displayName: String,
    val emoji: String,
    val description: String
) {
    CAMERA("Camera",     "📷", "Photos & screenshots"),
    WHATSAPP("WhatsApp", "💬", "Images, videos & statuses"),
    DOWNLOAD("Download", "⬇️", "Downloaded files"),
    VIDEO("Videos",      "🎬", "MP4 & recorded clips"),
    DOCUMENT("Documents","📄", "PDFs, DOCX & more"),
    OTHER("Other",       "📁", "Everything else")
}

/* ── Screen ────────────────────────────────── */
@Composable
fun CategoryScreen(navController: NavController, theme: AppTheme) {
    val context = LocalContext.current
    val accent  = ThemeColors.accentFor(theme)
    val card    = ThemeColors.cardFor(theme)
    val colors  = MaterialTheme.colorScheme

    // Permission — matches your existing logic, extended for video
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* result handled per-tap below */ }

    val isGranted = permissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    LaunchedEffect(Unit) {
        if (!isGranted) launcher.launch(permissions.toTypedArray())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(20.dp)
    ) {

        Text(
            "What are you\nlooking for?",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color      = colors.onBackground
            )
        )
        Text(
            "Select a category to begin scanning",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = colors.onBackground.copy(alpha = 0.5f)
            ),
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // Permission warning banner
        if (!isGranted) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚠️  ", fontSize = 18.sp)
                    Text(
                        "Storage permission needed to scan files. Tap a category to grant it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        LazyVerticalGrid(
            columns              = GridCells.Fixed(2),
            verticalArrangement  = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(ImageCategory.values()) { category ->
                CategoryCard(
                    category  = category,
                    accent    = accent,
                    cardColor = card,
                    onClick   = {
                        if (isGranted) {
                            navController.navigate("scan/${category.name}")
                        } else {
                            launcher.launch(permissions.toTypedArray())
                        }
                    }
                )
            }
        }
    }
}

/* ── Category tile ─────────────────────────── */
@Composable
fun CategoryCard(
    category: ImageCategory,
    accent: androidx.compose.ui.graphics.Color,
    cardColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(
                Brush.linearGradient(listOf(cardColor, cardColor.copy(alpha = 0.65f))),
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(category.emoji, fontSize = 36.sp)
            Column {
                Text(
                    category.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color      = accent
                    )
                )
                Text(
                    category.description,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                )
            }
        }
    }
}