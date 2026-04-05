package com.RagDev69.ghostrecovery

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.RagDev69.ghostrecovery.ui.theme.GhostRecoveryTheme

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "categories") {
                composable("categories") {
                    CategoryScreen(navController)
                }
                composable("scan/{category}") { backStackEntry ->
                    val categoryName = backStackEntry.arguments?.getString("category") ?: "OTHER"
                    ScanScreen(selectedCategory = categoryName) // pass as String
                }
            }
        }
    }
}