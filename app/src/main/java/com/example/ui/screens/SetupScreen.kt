package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.keyboard.KeyboardController
import com.example.keyboard.OmniInputTarget
import com.example.ui.keyboard.OmniKeyboardView

@Composable
fun SetupScreen(
    controller: KeyboardController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isEnabled by remember { mutableStateOf(ImeHelper.isImeEnabled(context)) }
    var isSelected by remember { mutableStateOf(ImeHelper.isImeSelected(context)) }
    var testInputText by remember { mutableStateOf("") }
    var cursorPosition by remember { mutableIntStateOf(0) }
    var showPopupKeyboard by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Auto-refresh state when returning from system settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isEnabled = ImeHelper.isImeEnabled(context)
                isSelected = ImeHelper.isImeSelected(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val testInputTarget = remember(testInputText, cursorPosition) {
        object : OmniInputTarget {
            override fun commitText(text: String) {
                val current = testInputText
                val pos = cursorPosition.coerceIn(0, current.length)
                val newText = current.substring(0, pos) + text + current.substring(pos)
                testInputText = newText
                cursorPosition = pos + text.length
            }

            override fun deleteBackward(count: Int) {
                val current = testInputText
                val pos = cursorPosition.coerceIn(0, current.length)
                if (pos > 0) {
                    val deleteCount = minOf(count, pos)
                    val newText = current.substring(0, pos - deleteCount) + current.substring(pos)
                    testInputText = newText
                    cursorPosition = pos - deleteCount
                }
            }

            override fun deleteForward(count: Int) {
                val current = testInputText
                val pos = cursorPosition.coerceIn(0, current.length)
                if (pos < current.length) {
                    val deleteCount = minOf(count, current.length - pos)
                    val newText = current.substring(0, pos) + current.substring(pos + deleteCount)
                    testInputText = newText
                }
            }

            override fun moveCursor(offset: Int) {
                cursorPosition = (cursorPosition + offset).coerceIn(0, testInputText.length)
            }

            override fun performAction() {
                commitText("\n")
            }

            override fun getTextBeforeCursor(length: Int): String {
                val pos = cursorPosition.coerceIn(0, testInputText.length)
                val start = maxOf(0, pos - length)
                return testInputText.substring(start, pos)
            }

            override fun getTextAfterCursor(length: Int): String {
                val pos = cursorPosition.coerceIn(0, testInputText.length)
                val end = minOf(testInputText.length, pos + length)
                return testInputText.substring(pos, end)
            }

            override fun getSelectedText(): String = ""
            override fun replaceSelection(text: String) = commitText(text)
            override fun hideKeyboard() {
                showPopupKeyboard = false
            }
            override fun switchToNextIme() {
                ImeHelper.showImePicker(context)
            }
        }
    }

    val isFullyReady = isEnabled && isSelected

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Status Card (2027 Futuristic Dashboard Style)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isFullyReady) Color(0xFF042F2E) else Color(0xFF111827)
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (isFullyReady) Color(0xFF14B8A6) else Color(0xFF00F0FF).copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFullyReady) Color(0xFF10B981).copy(alpha = 0.2f)
                                    else Color(0xFF00F0FF).copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isFullyReady) Icons.Default.CheckCircle else Icons.Default.SettingsSuggest,
                                contentDescription = null,
                                tint = if (isFullyReady) Color(0xFF34D399) else Color(0xFF00F0FF),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isFullyReady) "OmniBoard جاهزة وتعمل كلوحة افتراضية" else "إعداد وتفعيل لوحة المفاتيح للنظام",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (isFullyReady) "النظام معتمد • 100% بدون إنترنت • صفر ذكاء اصطناعي" else "اتبع الخطوات لتفعيلها كلوحة مفاتيح رئيسية لهاتفك",
                                color = if (isFullyReady) Color(0xFFA7F3D0) else Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Status Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusBadge(
                        label = "التمكين بالنظام",
                        isSuccess = isEnabled,
                        modifier = Modifier.weight(1f)
                    )
                    StatusBadge(
                        label = "اللوحة الافتراضية",
                        isSuccess = isSelected,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Step 1: Enable in Android System Settings
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isEnabled) Color(0xFF059669) else Color(0xFF1E293B), RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = Color(0xFF00F0FF),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "الخطوة 1: تمكين OmniBoard في الإعدادات",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isEnabled) "تم التمكين بنجاح في نظام أندرويد" else "اضغط لفتح شاشة إعدادات لوحات المفاتيح وتفعيل OmniBoard",
                            color = if (isEnabled) Color(0xFF34D399) else Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                if (!isEnabled) {
                    Button(
                        onClick = {
                            ImeHelper.openImeSettings(context)
                            isEnabled = ImeHelper.isImeEnabled(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("تمكين", color = Color(0xFF0A0F1D), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Done",
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Step 2: Set as Default System Keyboard
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (isSelected) Color(0xFF059669) else Color(0xFF1E293B), RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = null,
                            tint = Color(0xFFA855F7),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "الخطوة 2: اختيارها كلوحة مفاتيح افتراضية",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isSelected) "OmniBoard هي لوحة المفاتيح الافتراضية النشطة حالياً" else "اضغط لفتح نافذة اختيار لوحة المفاتيح الافتراضية",
                            color = if (isSelected) Color(0xFF34D399) else Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                if (!isSelected) {
                    Button(
                        onClick = {
                            ImeHelper.showImePicker(context)
                            isSelected = ImeHelper.isImeSelected(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("اختيار", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Done",
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Step 3: Immediate Live System & In-App Popup Test Field
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
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
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "الخطوة 3: حقل الاختبار واللوحة المنبثقة",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // Indicator
                    Surface(
                        color = if (showPopupKeyboard) Color(0xFF00F0FF).copy(alpha = 0.2f) else Color(0xFF334155),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (showPopupKeyboard) "المنبثقة نشطة" else "جاهز",
                            color = if (showPopupKeyboard) Color(0xFF00F0FF) else Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = "يمكنك تجربة لوحة OmniBoard فوراً عبر اللوحة المنبثقة التفاعلية أدناه، أو طلب إظهار لوحة النظام:",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )

                // Interactive Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showPopupKeyboard = !showPopupKeyboard },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showPopupKeyboard) Color(0xFF0284C7) else Color(0xFF00F0FF)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (showPopupKeyboard) Icons.Default.KeyboardHide else Icons.Default.Keyboard,
                            contentDescription = null,
                            tint = if (showPopupKeyboard) Color.White else Color(0xFF0A0F1D),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (showPopupKeyboard) "إخفاء المنبثقة" else "إظهار اللوحة المنبثقة",
                            color = if (showPopupKeyboard) Color.White else Color(0xFF0A0F1D),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            ImeHelper.forceShowSoftKeyboard(context)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("لوحة النظام", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Text Display / Input Box
                OutlinedTextField(
                    value = testInputText,
                    onValueChange = {
                        testInputText = it
                        cursorPosition = it.length
                    },
                    placeholder = { Text("المس هنا أو اضغط 'إظهار اللوحة المنبثقة' لتجربة الكتابة الفورية...", color = Color(0xFF64748B), fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF00F0FF),
                        unfocusedBorderColor = Color(0xFF334155)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (testInputText.isNotEmpty()) "تمت كتابة: ${testInputText.length} حرف" else "جاهز للكتابة",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp
                    )
                    if (testInputText.isNotEmpty()) {
                        TextButton(onClick = {
                            testInputText = ""
                            cursorPosition = 0
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("مسح النص", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                }

                // In-App Interactive Popup Keyboard Preview
                AnimatedVisibility(visible = showPopupKeyboard) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .background(Color(0xFF0B0F19))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF111827))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Keyboard,
                                    contentDescription = null,
                                    tint = Color(0xFF00F0FF),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "اللوحة المنبثقة التفاعلية المباشرة (Live Interactive Mode)",
                                    color = Color(0xFF00F0FF),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = { showPopupKeyboard = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        OmniKeyboardView(
                            controller = controller,
                            inputTarget = testInputTarget,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Diagnostics & Architecture Panel
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF00F0FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "مواصفات ومعمارية النظام (System Architecture)",
                        color = Color(0xFF00F0FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

                DiagnosticRow(label = "خدمة نظام الإدخال (IME Service):", value = "OmniKeyboardService (Active)")
                DiagnosticRow(label = "تصريح النظام (Permission):", value = "BIND_INPUT_METHOD (Protected)")
                DiagnosticRow(label = "خوارزمية التنبؤ (Prediction Engine):", value = "Prefix Trie + Damerau-Levenshtein")
                DiagnosticRow(label = "قاعدة البيانات المحلية (Local Storage):", value = "Room Database SQLite v2 (Offline)")
                DiagnosticRow(label = "تخطيطات اللغات المدعومة:", value = "English (US) Programmer + Arabic Pro")
                DiagnosticRow(label = "معدل الاستجابة والخصوصية:", value = "<0.2ms Latency • 100% Local • Zero AI")
            }
        }
    }
}

@Composable
private fun StatusBadge(
    label: String,
    isSuccess: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSuccess) Color(0xFF064E3B) else Color(0xFF1E293B))
            .border(
                1.dp,
                if (isSuccess) Color(0xFF10B981) else Color(0xFF334155),
                RoundedCornerShape(8.dp)
            )
            .padding(vertical = 8.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSuccess) Color(0xFF34D399) else Color(0xFF94A3B8),
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                color = if (isSuccess) Color(0xFFA7F3D0) else Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DiagnosticRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 11.sp)
        Text(
            text = value,
            color = Color.White,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}
