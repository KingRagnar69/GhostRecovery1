package com.RagDev69.ghostrecovery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.RagDev69.ghostrecovery.ui.dark.GhostRecoveryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            var isDarkMode by remember { mutableStateOf(false) }

            GhostRecoveryTheme(darkTheme = isDarkMode) {

                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "categories"
                ) {

                    composable("categories") {
                        CategoryScreen(
                            navController = navController,
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = {
                                isDarkMode = !isDarkMode
                            }
                        )
                    }

                    composable("scan/{category}") { backStackEntry ->
                        val categoryName =
                            backStackEntry.arguments?.getString("category") ?: "OTHER"

                        ScanScreen(selectedCategory = categoryName)
                    }
                }
            }
        }
    }
}