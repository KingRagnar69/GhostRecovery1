package com.RagDev69.ghostrecovery

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

/* ── Data model ────────────────────────────── */
data class RecoverableFile(
    val id: Long = System.nanoTime(),
    val name: String,
    val uri: Uri,
    val size: Long,
    val lastModified: Long,
    val recoveryChance: String,
    val category: String,
    val mimeType: String = ""
) {
    val isImage get() = mimeType.startsWith("image") ||
            name.matches(Regex(".*\\.(jpg|jpeg|png|webp|gif|bmp)", RegexOption.IGNORE_CASE))
    val isVideo get() = mimeType.startsWith("video") ||
            name.matches(Regex(".*\\.(mp4|mkv|avi|mov|3gp)", RegexOption.IGNORE_CASE))
    val isDoc   get() = name.matches(Regex(".*\\.(pdf|docx|doc|xlsx|xls|txt|pptx|ppt)", RegexOption.IGNORE_CASE))
}

/* ── ScanScreen ────────────────────────────── */
@Composable
fun ScanScreen(selectedCategory: String, theme: AppTheme) {
    val context  = LocalContext.current
    val scope    = rememberCoroutineScope()          // ✅ no leak
    val accent   = ThemeColors.accentFor(theme)
    val colors   = MaterialTheme.colorScheme

    var files       by remember { mutableStateOf<List<RecoverableFile>>(emptyList()) }
    var loading     by remember { mutableStateOf(false) }
    var scanned     by remember { mutableStateOf(false) }
    var previewFile by remember { mutableStateOf<RecoverableFile?>(null) }
    val recovered   = remember { mutableStateListOf<Long>() }

    // Pulsing scan button animation
    val pulse = rememberInfiniteTransition(label = "btn")
    val btnScale by pulse.animateFloat(
        initialValue  = 1f, targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            tween(700, easing = EaseInOutSine), RepeatMode.Reverse
        ), label = "btnScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    selectedCategory.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color      = accent
                    ),
                    modifier = Modifier.weight(1f)
                )
                if (scanned) {
                    Text(
                        "${files.size} found",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = colors.onBackground.copy(alpha = 0.55f)
                        )
                    )
                }
            }

            // Progress bar
            if (loading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color    = accent
                )
            }

            // Empty state
            if (scanned && files.isEmpty() && !loading) {
                Box(
                    Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔍", fontSize = 52.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Nothing found in this category",
                            color = colors.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // File list
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(files, key = { it.id }) { file ->
                    AnimatedVisibility(
                        visible = true,
                        enter   = fadeIn() + slideInVertically()
                    ) {
                        FileCard(
                            file        = file,
                            theme       = theme,
                            isRecovered = recovered.contains(file.id),
                            onClick     = { previewFile = file }
                        )
                    }
                }
            }
        }

        // ── Styled scan button (no drawable needed) ──
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(28.dp)
                .scale(if (loading) 1f else btnScale)
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(accent, accent.copy(alpha = 0.6f))
                    )
                )
                .clickable(enabled = !loading) {
                    scope.launch {
                        loading = true
                        scanned = false
                        val result = withContext(Dispatchers.IO) {
                            scanAllFiles(context, selectedCategory)
                        }
                        files   = result
                        loading = false
                        scanned = true
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    color     = Color.White,
                    modifier  = Modifier.size(28.dp),
                    strokeWidth = 3.dp
                )
            } else {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Scan",
                    tint     = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }

    // ── Preview dialog ──
    previewFile?.let { file ->
        AlertDialog(
            onDismissRequest = { previewFile = null },
            containerColor   = colors.surface,
            title = {
                Text(
                    file.name, maxLines = 1,
                    style = MaterialTheme.typography.titleSmall.copy(color = accent)
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (file.isImage) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(file.uri).crossfade(true).build(),
                            contentDescription = null,
                            modifier     = Modifier.fillMaxWidth().height(260.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            if (file.isVideo) "🎬" else "📄",
                            fontSize = 64.sp,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Size: ${file.size / 1024} KB",
                        style = MaterialTheme.typography.bodySmall)
                    Text(
                        "Modified: ${SimpleDateFormat("dd MMM yyyy  HH:mm", Locale.getDefault())
                            .format(Date(file.lastModified))}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text("Chance: ${file.recoveryChance}",
                        style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val f = file
                        scope.launch(Dispatchers.IO) {
                            val saved = recoverFile(context, f)
                            withContext(Dispatchers.Main) {
                                if (saved) {
                                    recovered.add(f.id)
                                    Toast.makeText(
                                        context,
                                        "✅ Saved to GhostRecovery/Recovered",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(context, "❌ Recovery failed", Toast.LENGTH_SHORT).show()
                                }
                                previewFile = null
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accent)
                ) { Text("Recover") }
            },
            dismissButton = {
                TextButton(onClick = { previewFile = null }) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Close")
                }
            }
        )
    }
}

/* ── File card ─────────────────────────────── */
@Composable
fun FileCard(
    file: RecoverableFile,
    theme: AppTheme,
    isRecovered: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val accent  = ThemeColors.accentFor(theme)
    val card    = ThemeColors.cardFor(theme)
    val colors  = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isRecovered, onClick = onClick),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = card)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.background),
                contentAlignment = Alignment.Center
            ) {
                if (file.isImage) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(file.uri)
                            .crossfade(true)
                            .size(128)          // ✅ decode small → fast
                            .build(),
                        contentDescription = null,
                        modifier     = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        when { file.isVideo -> "🎬"; file.isDoc -> "📄"; else -> "📁" },
                        fontSize = 28.sp
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    file.name, maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color      = colors.onSurface
                    )
                )
                Text(
                    "${file.size / 1024} KB  •  ${file.category}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.onSurface.copy(alpha = 0.5f)
                    )
                )
                Text(
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(Date(file.lastModified)),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.onSurface.copy(alpha = 0.4f)
                    )
                )
            }

            // Badge
            if (isRecovered) {
                Icon(Icons.Default.CheckCircle, null, tint = accent, modifier = Modifier.size(22.dp))
            } else {
                val badgeColor = when (file.recoveryChance) {
                    "High"       -> Color(0xFF00C853)
                    "Medium"     -> Color(0xFFFFAB00)
                    "Accessible" -> accent
                    else         -> Color(0xFFDD2C00)
                }
                Surface(
                    color = badgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        file.recoveryChance,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(color = badgeColor)
                    )
                }
            }
        }
    }
}

/* ── Scanner — images + videos + docs + hidden folders ── */
fun scanAllFiles(context: Context, selectedCategory: String): List<RecoverableFile> {
    val list = mutableListOf<RecoverableFile>()
    val cat  = selectedCategory.uppercase()

    fun matches(c: String) = c == cat || cat == "OTHER"

    // 1. Images via MediaStore
    if (matches("CAMERA") || matches("WHATSAPP") || matches("DOWNLOAD") || matches("OTHER")) {
        list += queryMediaStore(
            context,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            cat, "image/jpeg"
        )
    }

    // 2. Videos via MediaStore
    if (matches("VIDEO") || matches("WHATSAPP")) {
        list += queryMediaStore(
            context,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            cat, "video/mp4"
        )
    }

    // 3. Documents (Android Q+)
    if (matches("DOCUMENT") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        list += queryMediaStore(
            context,
            MediaStore.Files.getContentUri("external"),
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            cat, ""
        )
    }

    // 4. Hidden / deleted-trace folders
    val extRoot = Environment.getExternalStorageDirectory().path
    val folders = listOf(
        "$extRoot/DCIM/.thumbnails",
        "$extRoot/Pictures",
        "$extRoot/Download",
        "$extRoot/WhatsApp/Media/.Statuses",
        "$extRoot/WhatsApp/Media/WhatsApp Images/Sent",
        "$extRoot/Android/media/com.whatsapp/WhatsApp/Media/.Statuses",
        "$extRoot/.trash",
        "$extRoot/.recently-deleted"         // Samsung / some OEMs
    )

    folders.forEach { path ->
        File(path).listFiles()?.forEach { f ->
            if (f.isFile && list.none { it.name == f.name }) {
                val fileCat = when {
                    path.contains("WhatsApp", true) -> "WHATSAPP"
                    path.contains("Download", true) -> "DOWNLOAD"
                    path.contains("DCIM",     true) -> "CAMERA"
                    else                            -> "OTHER"
                }
                if (matches(fileCat)) {
                    list.add(
                        RecoverableFile(
                            name           = f.name,
                            uri            = Uri.fromFile(f),
                            size           = f.length(),
                            lastModified   = f.lastModified(),
                            recoveryChance = if (f.name.startsWith(".")) "High" else "Medium",
                            category       = fileCat
                        )
                    )
                }
            }
        }
    }

    return list.sortedByDescending { it.lastModified }
}

private fun queryMediaStore(
    context: Context,
    collection: Uri,
    nameColName: String,
    sizeColName: String,
    idColName: String,
    dataColName: String,
    selectedCat: String,
    defaultMime: String
): List<RecoverableFile> {
    val result     = mutableListOf<RecoverableFile>()
    val projection = arrayOf(nameColName, sizeColName, idColName, dataColName)

    context.contentResolver.query(collection, projection, null, null, null)?.use { cursor ->
        val iName = cursor.getColumnIndex(nameColName).coerceAtLeast(0)
        val iSize = cursor.getColumnIndex(sizeColName).coerceAtLeast(0)
        val iId   = cursor.getColumnIndex(idColName).coerceAtLeast(0)
        val iData = cursor.getColumnIndex(dataColName)

        while (cursor.moveToNext()) {
            val name = cursor.getString(iName) ?: continue
            val size = cursor.getLong(iSize)
            val id   = cursor.getLong(iId)
            val path = if (iData >= 0) cursor.getString(iData) ?: "" else ""
            val uri  = ContentUris.withAppendedId(collection, id)

            val fileCat = when {
                path.contains("/WhatsApp/", true) -> "WHATSAPP"
                path.contains("/Download/", true) -> "DOWNLOAD"
                path.contains("/DCIM/",     true) -> "CAMERA"
                path.contains("/Movies/",   true) ||
                        path.contains("/Video/",    true)  -> "VIDEO"
                else                              -> "OTHER"
            }

            if (fileCat == selectedCat || selectedCat == "OTHER") {
                result.add(
                    RecoverableFile(
                        name           = name,
                        uri            = uri,
                        size           = size,
                        lastModified   = if (path.isNotEmpty()) File(path).lastModified() else 0L,
                        recoveryChance = "Accessible",
                        category       = fileCat,
                        mimeType       = defaultMime
                    )
                )
            }
        }
    }
    return result
}

/* ── Recovery ──────────────────────────────── */
fun recoverFile(context: Context, file: RecoverableFile): Boolean {
    return try {
        val mime = when {
            file.isImage -> "image/jpeg"
            file.isVideo -> "video/mp4"
            else         -> "application/octet-stream"
        }
        val relPath = when {
            file.isVideo -> "${Environment.DIRECTORY_MOVIES}/GhostRecovery/Recovered"
            file.isDoc   -> "${Environment.DIRECTORY_DOWNLOADS}/GhostRecovery/Recovered"
            else         -> "${Environment.DIRECTORY_PICTURES}/GhostRecovery/Recovered"
        }

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "recovered_${file.name}")
            put(MediaStore.MediaColumns.MIME_TYPE, mime)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relPath)
        }

        val destCol = if (file.isVideo)
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        else
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val destUri = context.contentResolver.insert(destCol, values) ?: return false

        context.contentResolver.openOutputStream(destUri)?.use { out ->
            context.contentResolver.openInputStream(file.uri)?.use { it.copyTo(out) }
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}