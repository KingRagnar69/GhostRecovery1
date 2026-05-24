package com.RagDev69.ghostrecovery

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(currentTheme: AppTheme) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    val accent  = ThemeColors.accentFor(currentTheme)
    val colors  = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(20.dp)
    ) {

        Text(
            "Themes",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold, color = accent
            )
        )
        Text(
            "Pick your GhostRecovery vibe",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = colors.onBackground.copy(alpha = 0.5f)
            ),
            modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
        )

        LazyVerticalGrid(
            columns               = GridCells.Fixed(2),
            verticalArrangement   = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(AppTheme.values()) { theme ->
                ThemeCard(
                    theme      = theme,
                    isSelected = theme == currentTheme,
                    onClick    = { scope.launch { saveTheme(context, theme) } }
                )
            }
        }

        Spacer(Modifier.height(28.dp))
        HorizontalDivider(color = colors.onBackground.copy(alpha = 0.1f))
        Spacer(Modifier.height(20.dp))

        Text(
            "GhostRecovery  👻",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold, color = accent
            )
        )
        Text(
            "v1.0.0  •  Made by RagDev69",
            style = MaterialTheme.typography.bodySmall.copy(
                color = colors.onBackground.copy(alpha = 0.45f)
            )
        )
    }
}

@Composable
fun ThemeCard(theme: AppTheme, isSelected: Boolean, onClick: () -> Unit) {
    val scheme = ThemeColors.colorSchemeFor(theme)
    val accent = ThemeColors.accentFor(theme)

    val borderColor by animateColorAsState(
        targetValue   = if (isSelected) accent else Color.Transparent,
        animationSpec = tween(280),
        label         = "border"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(scheme.surface, scheme.background)))
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Color dot preview
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(accent, scheme.secondary, scheme.primaryContainer).forEach { c ->
                    Box(Modifier.size(11.dp).clip(CircleShape).background(c))
                }
            }

            Spacer(Modifier.height(12.dp))

            // Mini card mock
            Box(
                Modifier.fillMaxWidth().height(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ThemeColors.cardFor(theme))
            )

            Spacer(Modifier.height(6.dp))

            Box(
                Modifier.fillMaxWidth(0.55f).height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(accent.copy(alpha = 0.45f))
            )

            Spacer(Modifier.weight(1f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(theme.emoji, fontSize = 20.sp)
                Spacer(Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        theme.displayName,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color      = scheme.onBackground
                        )
                    )
                    Text(
                        theme.description,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = scheme.onBackground.copy(alpha = 0.5f)
                        ),
                        maxLines = 1
                    )
                }
                if (isSelected) {
                    Icon(
                        Icons.Default.Check, null,
                        tint     = accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}