package com.RagDev69.ghostrecovery

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/* ── Entry point ───────────────────────────── */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GhostApp() }
    }
}

/* ── Root: reads theme, wraps MaterialTheme ── */
@Composable
fun GhostApp() {
    val context = LocalContext.current
    val theme   by themeFlow(context).collectAsState(initial = AppTheme.GHOST)

    MaterialTheme(colorScheme = ThemeColors.colorSchemeFor(theme)) {
        Surface { GhostNavHost(currentTheme = theme) }
    }
}

/* ── Bottom nav items ──────────────────────── */
sealed class BottomNav(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Home      : BottomNav("home",      "Home",      Icons.Default.Home)
    object Scan      : BottomNav("categories","Scan",      Icons.Default.Search)
    object Recovered : BottomNav("recovered", "Recovered", Icons.Default.Favorite)
    object Profile   : BottomNav("profile",   "Profile",   Icons.Default.Person)
}

private val navItems = listOf(
    BottomNav.Home,
    BottomNav.Scan,
    BottomNav.Recovered,
    BottomNav.Profile
)

/* ── NavHost + Scaffold ────────────────────── */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GhostNavHost(currentTheme: AppTheme) {
    val navController = rememberNavController()
    val accent        = ThemeColors.accentFor(currentTheme)

    // Runtime permissions
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    val permState = rememberMultiplePermissionsState(permissions)
    LaunchedEffect(Unit) {
        if (!permState.allPermissionsGranted) permState.launchMultiplePermissionRequest()
    }

    val backStack    by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    // Hide bottom bar inside the scan detail screen
    val showBar = currentRoute?.startsWith("scan/") == false

    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    navItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick  = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon  = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label,
                                    tint = if (selected) accent
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                )
                            },
                            label = {
                                Text(
                                    item.label,
                                    color = if (selected) accent
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController    = navController,
            startDestination = "home",
            modifier         = Modifier.padding(padding)
        ) {
            composable("home")       { LandScreen(navController) }
            composable("categories") { CategoryScreen(navController, currentTheme) }
            composable("scan/{category}") { back ->
                val cat = back.arguments?.getString("category") ?: "CAMERA"
                ScanScreen(selectedCategory = cat, theme = currentTheme)
            }
            composable("recovered")  { RecoveredScreen(currentTheme) }
            composable("profile")    { ProfileScreen(currentTheme) }
        }
    }
}