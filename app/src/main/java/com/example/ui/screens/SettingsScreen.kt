package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.keyboard.KeyboardController
import com.example.model.KeyboardTheme

@Composable
fun SettingsScreen(
    controller: KeyboardController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by controller.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFA855F7).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFFA855F7), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = Color(0xFFA855F7),
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = "إعدادات وتخصيص OmniBoard",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "تحكم في المظهر والارتفاع وسلوك المفاتيح والاهتزاز",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }

        // 1. Theme Selection Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, contentDescription = null, tint = Color(0xFF00F0FF), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("سمة المظهر (Keyboard Theme)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(KeyboardTheme.values()) { theme ->
                        val isSelected = theme == uiState.activeTheme
                        val displayName = when (theme) {
                            KeyboardTheme.CYBERPUNK_NEON -> "Cyberpunk Neon"
                            KeyboardTheme.DEV_MONOKAI -> "Monokai Pro"
                            KeyboardTheme.AMOLED_OBSIDIAN -> "Midnight OLED"
                            KeyboardTheme.MATERIAL_YOU -> "Material You"
                            KeyboardTheme.ARCTIC_CLEAN -> "Minimal Light"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF00F0FF) else Color(0xFF1E293B))
                                .border(1.dp, if (isSelected) Color(0xFF00F0FF) else Color(0xFF334155), RoundedCornerShape(10.dp))
                                .clickable {
                                    controller.setTheme(theme)
                                    Toast.makeText(context, "تم تطبيق سمة $displayName", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = displayName,
                                color = if (isSelected) Color(0xFF0A0F1D) else Color.White,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // 2. Keyboard Height & Ergonomics
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Height, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("ارتفاع المفاتيح (Key Height)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Text("${uiState.keyHeightDp} dp", color = Color(0xFF00F0FF), fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Slider(
                    value = uiState.keyHeightDp.toFloat(),
                    onValueChange = { controller.setKeyHeight(it.toInt()) },
                    valueRange = 40f..65f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF00F0FF),
                        activeTrackColor = Color(0xFF00F0FF),
                        inactiveTrackColor = Color(0xFF1E293B)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("مدمج (40dp)", color = Color(0xFF64748B), fontSize = 10.sp)
                    Text("قياسي (48dp)", color = Color(0xFF64748B), fontSize = 10.sp)
                    Text("كبير (65dp)", color = Color(0xFF64748B), fontSize = 10.sp)
                }
            }
        }

        // 3. Toggles (Haptic, Auto-close, QuickBar)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Haptic feedback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("الاهتزاز اللمسي (Haptic Feedback)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("اهتزاز خفيف فوري عند لمس المفاتيح", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = uiState.hapticEnabled,
                        onCheckedChange = { controller.toggleHaptic() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00F0FF))
                    )
                }

                HorizontalDivider(color = Color(0xFF1E293B))

                // Auto-close pairs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("إغلاق الأقواس والاقتباسات تلقائياً", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("إكمال () {} [] \"\" تلقائياً ووضع المؤشر بالوسط", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = uiState.autoClosePairs,
                        onCheckedChange = { controller.toggleAutoClose() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00F0FF))
                    )
                }

                HorizontalDivider(color = Color(0xFF1E293B))

                // Programmer QuickBar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ViewAgenda, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("شريط أدوات المبرمج (Dev QuickBar)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("أزرار Esc, Tab, Ctrl, Alt، والأسهم ومفاتيح البرمجة", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                    Switch(
                        checked = uiState.showDevQuickBar,
                        onCheckedChange = { controller.toggleDevQuickBar() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00F0FF))
                    )
                }
            }
        }

        // 4. Privacy & Zero AI Guarantee Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B).copy(alpha = 0.4f)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFF34D399),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "ضمان الخصوصية التامة (100% Offline & Pure Algorithmic)",
                        color = Color(0xFFA7F3D0),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "لا توجد أي تصاريح إنترنت، لا يتم إرسال أي ضربة مفتاح خارج هاتفك، وجميع الخوارزميات (Trie + Levenshtein) تعمل محلياً بذاكرة RAM وقاعدة بيانات Room فقط.",
                        color = Color(0xFFD1FAE5),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
