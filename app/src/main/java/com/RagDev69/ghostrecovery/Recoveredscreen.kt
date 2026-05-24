package com.RagDev69.ghostrecovery

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecoveredScreen(theme: AppTheme) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    val accent  = ThemeColors.accentFor(theme)
    val colors  = MaterialTheme.colorScheme

    var files   by remember { mutableStateOf<List<RecoverableFile>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        files   = withContext(Dispatchers.IO) { loadRecoveredFiles(context) }
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(20.dp)
    ) {
        Text(
            "Recovered Files",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold, color = accent
            )
        )
        Text(
            "${files.size} files",
            style = MaterialTheme.typography.bodySmall.copy(
                color = colors.onBackground.copy(alpha = 0.5f)
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = accent)
            }
            files.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📂", fontSize = 52.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("No recovered files yet",
                        color = colors.onBackground.copy(alpha = 0.5f))
                }
            }
            else -> LazyVerticalGrid(
                columns               = GridCells.Fixed(2),
                verticalArrangement   = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(files, key = { it.id }) { file ->
                    RecoveredTile(file, theme)
                }
            }
        }
    }
}

@Composable
fun RecoveredTile(file: RecoverableFile, theme: AppTheme) {
    val context = LocalContext.current
    val card    = ThemeColors.cardFor(theme)
    val accent  = ThemeColors.accentFor(theme)

    Card(
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = card),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            if (file.isImage) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(file.uri).crossfade(true).size(256).build(),
                    contentDescription = null,
                    modifier     = Modifier.fillMaxWidth().height(110.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    Modifier.fillMaxWidth().height(110.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (file.isVideo) "🎬" else "📄", fontSize = 44.sp)
                }
            }
            Column(Modifier.padding(10.dp)) {
                Text(
                    file.name, maxLines = 1,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold, color = accent
                    )
                )
                Text(
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(Date(file.lastModified)),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}

private fun loadRecoveredFiles(context: Context): List<RecoverableFile> {
    val result     = mutableListOf<RecoverableFile>()
    val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.SIZE,
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATA
    )

    context.contentResolver.query(
        collection, projection,
        "${MediaStore.Images.Media.DATA} LIKE ?",
        arrayOf("%GhostRecovery/Recovered%"),
        null
    )?.use { cursor ->
        val iName = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
        val iSize = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
        val iId   = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val iData = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

        while (cursor.moveToNext()) {
            result.add(
                RecoverableFile(
                    name           = cursor.getString(iName) ?: "file",
                    uri            = ContentUris.withAppendedId(collection, cursor.getLong(iId)),
                    size           = cursor.getLong(iSize),
                    lastModified   = File(cursor.getString(iData) ?: "").lastModified(),
                    recoveryChance = "Recovered",
                    category       = "Recovered"
                )
            )
        }
    }
    return result
}