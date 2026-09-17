package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ClipboardEntity
import com.example.keyboard.KeyboardController

@Composable
fun ClipboardScreen(
    controller: KeyboardController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardItems by controller.clipboardHistory.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var showAddNoteDialog by remember { mutableStateOf(false) }

    val filteredItems = remember(clipboardItems, searchQuery) {
        if (searchQuery.isBlank()) clipboardItems
        else clipboardItems.filter { it.content.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF38BDF8).copy(alpha = 0.15f))
                        .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "سجل الحافظة الذكي",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "تخزين محلي 100% مشفر داخل قاعدة بيانات الهاتف",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showAddNoteDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Note", tint = Color(0xFF00F0FF))
                }
                if (clipboardItems.any { !it.isPinned }) {
                    TextButton(onClick = {
                        controller.clearClipboardHistory()
                        Toast.makeText(context, "تم مسح العناصر غير المثبتة", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("مسح", color = Color(0xFFEF4444), fontSize = 12.sp)
                    }
                }
            }
        }

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث في النصوص والأكواد المنسوخة...", fontSize = 12.sp, color = Color(0xFF64748B)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF38BDF8)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF94A3B8))
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFF38BDF8),
                unfocusedBorderColor = Color(0xFF1E293B),
                focusedContainerColor = Color(0xFF111827),
                unfocusedContainerColor = Color(0xFF111827)
            )
        )

        // Clipboard List
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPasteOff,
                        contentDescription = null,
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "لا توجد عناصر محفوظة في الحافظة",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    ClipboardItemCard(
                        item = item,
                        onCopy = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("clipboard", item.content))
                            Toast.makeText(context, "تم نسخ النص إلى الحافظة", Toast.LENGTH_SHORT).show()
                        },
                        onTogglePin = {
                            controller.togglePinClipboard(item.id)
                        },
                        onDelete = {
                            controller.deleteClipboard(item)
                            Toast.makeText(context, "تم الحذف من الحافظة", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    if (showAddNoteDialog) {
        var noteContent by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            containerColor = Color(0xFF111827),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NoteAdd, contentDescription = null, tint = Color(0xFF38BDF8))
                    Spacer(Modifier.width(8.dp))
                    Text("إضافة نص جديد للحافظة", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                OutlinedTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    placeholder = { Text("اكتب أو الصق النص أو الكود هنا...", fontSize = 12.sp) },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteContent.isNotBlank()) {
                            controller.addClipboard(noteContent.trim())
                            showAddNoteDialog = false
                            Toast.makeText(context, "تمت الإضافة إلى الحافظة بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                ) {
                    Text("إضافة", color = Color(0xFF0A0F1D), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("إلغاء", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
private fun ClipboardItemCard(
    item: ClipboardEntity,
    onCopy: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (item.isPinned) Color(0xFF38BDF8) else Color(0xFF1E293B),
                RoundedCornerShape(10.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (item.isCode) Icons.Default.Code else Icons.Default.Description,
                        contentDescription = null,
                        tint = if (item.isCode) Color(0xFF00F0FF) else Color(0xFF34D399),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (item.isCode) "كود برمجي" else "نص محفوظ",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onTogglePin, modifier = Modifier.size(26.dp)) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pin",
                            tint = if (item.isPinned) Color(0xFF38BDF8) else Color(0xFF64748B),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    IconButton(onClick = onCopy, modifier = Modifier.size(26.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0B0F19))
                    .clickable { onCopy() }
                    .padding(8.dp)
            ) {
                Text(
                    text = item.content,
                    color = Color(0xFFE2E8F0),
                    fontFamily = if (item.isCode) FontFamily.Monospace else FontFamily.Default,
                    fontSize = 12.sp,
                    maxLines = 3
                )
            }
        }
    }
}
