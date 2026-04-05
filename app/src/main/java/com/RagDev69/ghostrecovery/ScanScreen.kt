package com.RagDev69.ghostrecovery

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// 🔹 Data model
data class RecoverableFile(
    val name: String,
    val uri: Uri,
    val size: Long,
    val lastModified: Long,
    val recoveryChance: String,
    val category: String
)

@Composable
fun ScanScreen(selectedCategory: String) {
    val context = LocalContext.current
    var files by remember { mutableStateOf<List<RecoverableFile>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    val recoveredFiles = remember { mutableStateListOf<Uri>() }

    // 🔹 Preview image functionality
    var previewUri by remember { mutableStateOf<Uri?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxSize().padding(bottom = 100.dp)) {

            if (loading) {
                CircularProgressIndicator(modifier = Modifier.padding(10.dp))
            }

            LazyColumn {
                items(files) { file ->
                    val isRecovered = recoveredFiles.contains(file.uri)
                    FileItemPreview(file, isRecovered) {
                        previewUri = file.uri // show preview when clicked
                    }
                }
            }
        }

        // 🔹 Start Scan button
        Button(
            onClick = {
                loading = true
                CoroutineScope(Dispatchers.IO).launch {
                    val result = scanDeletedTraces(context)
                        .filter { it.category.equals(selectedCategory, true) }
                        .sortedBy { it.lastModified }

                    withContext(Dispatchers.Main) {
                        files = result
                        loading = false
                        previewUri = result.firstOrNull()?.uri
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter)
        ) {
            Text("Start Scan")
        }
    }

    // 🔹 Preview dialog
    previewUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { previewUri = null },
            text = {
                val bitmap = remember(uri) {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val file = files.find { it.uri == uri }
                    if (file != null) {
                        recoverFile(context, file)
                        recoveredFiles.add(uri)
                    }
                    previewUri = null
                }) {
                    Text("Recover / Close")
                }
            }
        )
    }
}

// 🔍 Scan MediaStore + hidden folders
fun scanDeletedTraces(context: Context): List<RecoverableFile> {
    val list = mutableListOf<RecoverableFile>()

    // 1️⃣ MediaStore images
    val mediaCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.SIZE,
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATA
    )

    context.contentResolver.query(mediaCollection, projection, null, null, null)?.use { cursor ->
        val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
        val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

        while (cursor.moveToNext()) {
            val name = cursor.getString(nameCol)
            val size = cursor.getLong(sizeCol)
            val id = cursor.getLong(idCol)
            val path = cursor.getString(pathCol)
            val uri = ContentUris.withAppendedId(mediaCollection, id)

            val category = when {
                path.contains("/WhatsApp/Media/") -> "WhatsApp"
                path.contains("/Download/") -> "Download"
                path.contains("/DCIM/Camera/") -> "Camera"
                else -> "Other"
            }

            val lastModified = File(path).lastModified()

            list.add(
                RecoverableFile(name, uri, size, lastModified, "Accessible", category)
            )
        }
    }

    // 2️⃣ Hidden folders + deleted traces
    val folders = listOf(
        "${Environment.getExternalStorageDirectory().path}/DCIM/.thumbnails",
        "${Environment.getExternalStorageDirectory().path}/Pictures",
        "${Environment.getExternalStorageDirectory().path}/Download",
        "${Environment.getExternalStorageDirectory().path}/WhatsApp/Media/.Statuses"
    )

    folders.forEach { path ->
        val folder = File(path)
        folder.listFiles()?.forEach { f ->
            if (f.isFile && list.none { it.name == f.name }) {

                val category = when {
                    path.contains("WhatsApp") -> "WhatsApp"
                    path.contains("Download") -> "Download"
                    path.contains("DCIM") -> "Camera"
                    else -> "Other"
                }

                list.add(
                    RecoverableFile(
                        name = f.name,
                        uri = Uri.fromFile(f),
                        size = f.length(),
                        lastModified = f.lastModified(),
                        recoveryChance = if (f.name.startsWith(".")) "High" else "Medium",
                        category = category
                    )
                )
            }
        }
    }

    return list
}

// 💾 Recover file
fun recoverFile(context: Context, file: RecoverableFile) {
    try {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/GhostRecovery/Recovered"
            )
        }

        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )

        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { out ->
                context.contentResolver.openInputStream(file.uri)?.use { input ->
                    input.copyTo(out)
                }
            }
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// 🖼 File item UI with preview
@Composable
fun FileItemPreview(file: RecoverableFile, isRecovered: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(enabled = !isRecovered) { onClick() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            val context = LocalContext.current
            val bitmap = remember(file.uri) {
                context.contentResolver.openInputStream(file.uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }

            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            Text(text = file.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = "Size: ${file.size / 1024} KB", style = MaterialTheme.typography.bodySmall)
            Text(
                text = "Date: ${SimpleDateFormat("dd/MM/yyyy HH:mm").format(Date(file.lastModified))}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(text = "Recovery: ${file.recoveryChance}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Category: ${file.category}", style = MaterialTheme.typography.bodySmall)
            if (isRecovered) Text(text = "✅ Recovered", style = MaterialTheme.typography.bodySmall)
        }
    }
}