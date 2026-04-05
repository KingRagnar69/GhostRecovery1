package com.RagDev69.ghostrecovery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
fun CategoryScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Text(
            text = "Select a Category to Scan",
            style = MaterialTheme.typography.headlineSmall
        )

        ImageCategory.values().forEach { category ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Navigate to ScanScreen with selected category
                        navController.navigate("scan/${category.name}")
                    }
                    .padding(vertical = 5.dp),
                elevation = CardDefaults.cardElevation(5.dp)
            ) {
                Box(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}