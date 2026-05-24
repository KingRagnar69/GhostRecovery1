package com.RagDev69.ghostrecovery

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.navigation.NavController

@Composable
fun LandScreen(navController: NavController) {
    val context = LocalContext.current
    val theme   by themeFlow(context).collectAsState(initial = AppTheme.GHOST)
    val accent  = ThemeColors.accentFor(theme)
    val colors  = MaterialTheme.colorScheme

    // Ghost pulse
    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue   = 1f,
        targetValue    = 1.15f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(950, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ghostScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.background, colors.surface))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(32.dp)
        ) {

            Text("👻", fontSize = 88.sp, modifier = Modifier.scale(scale))

            Text(
                "GhostRecovery",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color      = accent
                ),
                textAlign = TextAlign.Center
            )

            Text(
                "Recover deleted photos, videos,\nWhatsApp media & documents.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = colors.onBackground.copy(alpha = 0.65f)
                ),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { navController.navigate("categories") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) {
                Text(
                    "Start Recovery",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color      = colors.onPrimary
                    )
                )
            }

            // Type chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("📷 Photos", "🎬 Videos", "💬 WhatsApp", "📄 Docs").forEach { label ->
                    SuggestionChip(
                        onClick  = {},
                        label    = { Text(label, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}