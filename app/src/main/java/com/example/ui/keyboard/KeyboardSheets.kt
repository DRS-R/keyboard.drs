package com.example.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClipboardEntity
import com.example.data.SnippetEntity
import com.example.engine.CaseStyle
import com.example.model.KeyboardTheme
import com.example.ui.theme.KeyboardPalette

@Composable
fun ClipboardSheet(
    palette: KeyboardPalette,
    clipboardItems: List<ClipboardEntity>,
    onSelectText: (String) -> Unit,
    onClearAll: () -> Unit,
    onClose: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(clipboardItems, searchQuery) {
        if (searchQuery.isBlank()) clipboardItems
        else clipboardItems.filter { it.content.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 300.dp)
            .background(palette.background)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📋 الحافظة الذكية (Clipboard)",
                color = palette.accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Row {
                TextButton(onClick = onClearAll) {
                    Text("مسح الكل", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = palette.primaryText)
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث في الحافظة...", fontSize = 12.sp, color = palette.secondaryText) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = palette.primaryText,
                unfocusedTextColor = palette.primaryText,
                focusedBorderColor = palette.accent,
                unfocusedBorderColor = palette.keyBorder
            )
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (filtered.isEmpty()) {
                item {
                    Text(
                        text = "لا توجد عناصر محفوظة",
                        color = palette.secondaryText,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(filtered) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.keyBackground)
                            .border(1.dp, if (item.isPinned) palette.accent else palette.keyBorder, RoundedCornerShape(8.dp))
                            .clickable { onSelectText(item.content) }
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (item.isCode) "💻 كود برمجـي" else "📝 نص",
                                    color = palette.secondaryText,
                                    fontSize = 10.sp
                                )
                                if (item.isPinned) {
                                    Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = palette.accent, modifier = Modifier.size(12.dp))
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = item.content,
                                color = palette.primaryText,
                                fontSize = 12.sp,
                                fontFamily = if (item.isCode) FontFamily.Monospace else FontFamily.Default,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SnippetsSheet(
    palette: KeyboardPalette,
    snippets: List<SnippetEntity>,
    onSelectSnippet: (SnippetEntity) -> Unit,
    onAddSnippet: (trigger: String, title: String, content: String, category: String) -> Unit,
    onClose: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("الكل") }
    var showAddDialog by remember { mutableStateOf(false) }

    val categories = listOf("الكل", "Kotlin", "Android", "Git", "SQL", "JavaScript", "Compose", "Python", "General")

    val filtered = remember(snippets, selectedCategory) {
        if (selectedCategory == "الكل") snippets
        else snippets.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 320.dp)
            .background(palette.background)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡ المقتطفات والماكرو (Snippets)",
                color = palette.accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Row {
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Snippet", tint = palette.accent)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = palette.primaryText)
                }
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) palette.accent else palette.keyBackground)
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) palette.background else palette.primaryText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filtered) { snippet ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.keyBackground)
                        .border(1.dp, palette.keyBorder, RoundedCornerShape(8.dp))
                        .clickable { onSelectSnippet(snippet) }
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = snippet.title,
                                color = palette.accent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = snippet.trigger,
                                color = palette.secondaryText,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = snippet.content,
                            color = palette.primaryText,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var trigger by remember { mutableStateOf("!") }
        var title by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Custom") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("إضافة مقتطف جديد") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = trigger,
                        onValueChange = { trigger = it },
                        label = { Text("بادئة التفعيل (Trigger e.g. !fun)") }
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("عنوان المقتطف") }
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("محتوى الكود أو النص") },
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (trigger.isNotBlank() && content.isNotBlank()) {
                            onAddSnippet(trigger, title.ifBlank { trigger }, content, category)
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("إلغاء") }
            }
        )
    }
}

@Composable
fun CaseConvertSheet(
    palette: KeyboardPalette,
    onSelectStyle: (CaseStyle) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.background)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔤 محول التسميات البرمجية (Case Styles)",
                color = palette.accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = palette.primaryText)
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.heightIn(max = 240.dp)
        ) {
            items(CaseStyle.values()) { style ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.keyBackground)
                        .border(1.dp, palette.keyBorder, RoundedCornerShape(8.dp))
                        .clickable { onSelectStyle(style) }
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = style.label,
                            color = palette.primaryText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = style.example,
                            color = palette.secondaryText,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSheet(
    palette: KeyboardPalette,
    activeTheme: KeyboardTheme,
    hapticEnabled: Boolean,
    autoClosePairs: Boolean,
    keyHeightDp: Int,
    onThemeChange: (KeyboardTheme) -> Unit,
    onToggleHaptic: () -> Unit,
    onToggleAutoClose: () -> Unit,
    onKeyHeightChange: (Int) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.background)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚙️ إعدادات وتخصيص OmniBoard",
                color = palette.accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = palette.primaryText)
            }
        }

        Spacer(Modifier.height(6.dp))

        Text("🎨 سمة المظهر (Theme):", color = palette.primaryText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            items(KeyboardTheme.values()) { theme ->
                val isSelected = theme == activeTheme
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) palette.accent else palette.keyBackground)
                        .border(1.dp, if (isSelected) palette.accent else palette.keyBorder, RoundedCornerShape(8.dp))
                        .clickable { onThemeChange(theme) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = theme.name.replace("_", " "),
                        color = if (isSelected) palette.background else palette.primaryText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("الاهتزاز عند اللمس (Haptic Feedback)", color = palette.primaryText, fontSize = 12.sp)
            Switch(
                checked = hapticEnabled,
                onCheckedChange = { onToggleHaptic() },
                colors = SwitchDefaults.colors(checkedThumbColor = palette.accent)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("إغلاق الأقواس تلقائياً () {} [] \"\"", color = palette.primaryText, fontSize = 12.sp)
            Switch(
                checked = autoClosePairs,
                onCheckedChange = { onToggleAutoClose() },
                colors = SwitchDefaults.colors(checkedThumbColor = palette.accent)
            )
        }

        Spacer(Modifier.height(4.dp))

        Text("ارتفاع المفاتيح: ${keyHeightDp}dp", color = palette.primaryText, fontSize = 12.sp)
        Slider(
            value = keyHeightDp.toFloat(),
            onValueChange = { onKeyHeightChange(it.toInt()) },
            valueRange = 40f..65f,
            colors = SliderDefaults.colors(thumbColor = palette.accent, activeTrackColor = palette.accent)
        )
    }
}
