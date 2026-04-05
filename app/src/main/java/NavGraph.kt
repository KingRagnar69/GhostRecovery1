package com.RagDev69.ghostrecovery

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun NavScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Text("Select Image Category:", style = MaterialTheme.typography.titleLarge)

        Button(onClick = { navController.navigate("scan/${ImageCategory.CAMERA.name}") }) {
            Text("Camera")
        }

        Button(onClick = { navController.navigate("scan/${ImageCategory.WHATSAPP.name}") }) {
            Text("WhatsApp")
        }

        Button(onClick = { navController.navigate("scan/${ImageCategory.OTHER.name}") }) {
            Text("Other")
        }
    }
}