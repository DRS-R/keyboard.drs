package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.keyboard.KeyboardController
import com.example.keyboard.OmniInputTarget
import com.example.ui.keyboard.OmniKeyboardView
import com.example.ui.screens.ActivationGuideDialog
import com.example.ui.screens.AlgorithmsLabScreen
import com.example.ui.screens.ImeHelper
import com.example.ui.theme.MyApplicationTheme

enum class SandboxTab {
    DEV_CODE,
    EVERYDAY_TEXT,
    ALGORITHMS_LAB
}

class MainActivity : ComponentActivity() {

    private lateinit var controller: KeyboardController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        controller = KeyboardController(this, lifecycleScope)

        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    containerColor = Color(0xFF0B0F19)
                ) { innerPadding ->
                    OmniBoardAppContent(
                        controller = controller,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun OmniBoardAppContent(
    controller: KeyboardController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(SandboxTab.DEV_CODE) }
    var showActivationGuide by remember { mutableStateOf(false) }

    // Text editor state
    var devCodeState by remember {
        mutableStateOf(
            TextFieldValue(
                text = """// مرحباً بك في OmniBoard - لوحة المفاتيح الأسطورية للمبرمجين والمستخدمين
fun main(args: Array<String>) {
    val message = "OmniBoard: 100% Offline • Zero AI • Pure Algorithms"
    println(message)
}
""",
                selection = TextRange(230)
            )
        )
    }

    var everydayTextState by remember {
        mutableStateOf(
            TextFieldValue(
                text = "مرحباً! يمكنك تجربة الكتابة باللغة العربية والإنجليزية، وتجربة السحب الزاوي لكتابة الأرقام والرموز والحركات التشكيلية دون الحاجة لتبديل التخطيط.",
                selection = TextRange(140)
            )
        )
    }

    // Unified InputTarget bound to the currently active tab's TextFieldState
    val currentTextFieldState = if (selectedTab == SandboxTab.DEV_CODE) devCodeState else everydayTextState

    val inputTarget = remember(selectedTab, currentTextFieldState) {
        object : OmniInputTarget {
            override fun commitText(text: String) {
                if (selectedTab == SandboxTab.DEV_CODE) {
                    devCodeState = insertText(devCodeState, text)
                } else {
                    everydayTextState = insertText(everydayTextState, text)
                }
            }

            override fun deleteBackward(count: Int) {
                if (selectedTab == SandboxTab.DEV_CODE) {
                    devCodeState = deleteText(devCodeState, count)
                } else {
                    everydayTextState = deleteText(everydayTextState, count)
                }
            }

            override fun moveCursor(offset: Int) {
                if (selectedTab == SandboxTab.DEV_CODE) {
                    val newPos = (devCodeState.selection.start + offset).coerceIn(0, devCodeState.text.length)
                    devCodeState = devCodeState.copy(selection = TextRange(newPos))
                } else {
                    val newPos = (everydayTextState.selection.start + offset).coerceIn(0, everydayTextState.text.length)
                    everydayTextState = everydayTextState.copy(selection = TextRange(newPos))
                }
            }

            override fun performAction() {
                commitText("\n")
            }

            override fun getTextBeforeCursor(length: Int): String {
                val state = if (selectedTab == SandboxTab.DEV_CODE) devCodeState else everydayTextState
                val start = maxOf(0, state.selection.min - length)
                return state.text.substring(start, state.selection.min)
            }

            override fun getTextAfterCursor(length: Int): String {
                val state = if (selectedTab == SandboxTab.DEV_CODE) devCodeState else everydayTextState
                val end = minOf(state.text.length, state.selection.max + length)
                return state.text.substring(state.selection.max, end)
            }

            override fun getSelectedText(): String {
                val state = if (selectedTab == SandboxTab.DEV_CODE) devCodeState else everydayTextState
                return if (state.selection.collapsed) ""
                else state.text.substring(state.selection.min, state.selection.max)
            }

            override fun replaceSelection(text: String) {
                commitText(text)
            }

            private fun insertText(current: TextFieldValue, text: String): TextFieldValue {
                val sel = current.selection
                val newTxt = current.text.replaceRange(sel.min, sel.max, text)
                val newCursor = sel.min + text.length
                return TextFieldValue(newTxt, TextRange(newCursor))
            }

            private fun deleteText(current: TextFieldValue, count: Int): TextFieldValue {
                val sel = current.selection
                return if (sel.collapsed) {
                    if (sel.start > 0) {
                        val delCount = minOf(count, sel.start)
                        val newTxt = current.text.removeRange(sel.start - delCount, sel.start)
                        val newCursor = sel.start - delCount
                        TextFieldValue(newTxt, TextRange(newCursor))
                    } else current
                } else {
                    val newTxt = current.text.removeRange(sel.min, sel.max)
                    TextFieldValue(newTxt, TextRange(sel.min))
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // App Header Bar
        Surface(
            color = Color(0xFF0F172A),
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF00F0FF).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFF00F0FF), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "{ }",
                            color = Color(0xFF00F0FF),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "OmniBoard",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Offline • No AI • Algorithmic Core",
                            color = Color(0xFF00F0FF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Activation Button
                Button(
                    onClick = { showActivationGuide = true },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(8.dp))
                ) {
                    val isEnabled = remember { ImeHelper.isImeEnabled(context) }
                    Icon(
                        imageVector = if (isEnabled) Icons.Default.CheckCircle else Icons.Default.SettingsSuggest,
                        contentDescription = null,
                        tint = if (isEnabled) Color(0xFF34D399) else Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isEnabled) "اللوحة مفعلة ✓" else "تفعيل بالنظام",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            }
        }

        // Tab Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TabPill(
                title = "💻 محرر الأكواد (Terminal)",
                isSelected = selectedTab == SandboxTab.DEV_CODE,
                onClick = { selectedTab = SandboxTab.DEV_CODE }
            )
            TabPill(
                title = "📝 محرر النصوص (Everyday)",
                isSelected = selectedTab == SandboxTab.EVERYDAY_TEXT,
                onClick = { selectedTab = SandboxTab.EVERYDAY_TEXT }
            )
            TabPill(
                title = "🔬 فحص الخوارزميات",
                isSelected = selectedTab == SandboxTab.ALGORITHMS_LAB,
                onClick = { selectedTab = SandboxTab.ALGORITHMS_LAB }
            )
        }

        // Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                SandboxTab.DEV_CODE -> {
                    CodePlaygroundView(
                        value = devCodeState,
                        onValueChange = { devCodeState = it },
                        onInsertSnippet = { inputTarget.commitText(it) }
                    )
                }
                SandboxTab.EVERYDAY_TEXT -> {
                    EverydayNoteView(
                        value = everydayTextState,
                        onValueChange = { everydayTextState = it }
                    )
                }
                SandboxTab.ALGORITHMS_LAB -> {
                    AlgorithmsLabScreen(controller = controller, modifier = Modifier.fillMaxSize())
                }
            }
        }

        // Live Embedded Keyboard at bottom
        if (selectedTab != SandboxTab.ALGORITHMS_LAB) {
            HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)
            OmniKeyboardView(
                controller = controller,
                inputTarget = inputTarget,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showActivationGuide) {
        ActivationGuideDialog(onDismiss = { showActivationGuide = false })
    }
}

@Composable
fun TabPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Color(0xFF00F0FF).copy(alpha = 0.2f) else Color(0xFF1E293B))
            .border(
                1.dp,
                if (isSelected) Color(0xFF00F0FF) else Color(0xFF334155),
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            color = if (isSelected) Color(0xFF00F0FF) else Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun CodePlaygroundView(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onInsertSnippet: (String) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B14))
            .padding(10.dp)
    ) {
        // Snippet quick injection pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("قوالب سريعة:", color = Color(0xFF64748B), fontSize = 11.sp)
            QuickCodeChip("Kotlin fun", "!fun") { onInsertSnippet("\nfun processData(input: String): Boolean {\n    return input.isNotEmpty()\n}") }
            QuickCodeChip("Python def", "!py") { onInsertSnippet("\ndef compute(values: list) -> int:\n    return sum(values)") }
            QuickCodeChip("SQL Query", "!sql") { onInsertSnippet("SELECT id, name FROM users WHERE active = 1;") }
            QuickCodeChip("Git push", "!git") { onInsertSnippet("git add . && git commit -m \"feat: update\" && git push") }
            QuickCodeChip("Arrow fn", "!arrow") { onInsertSnippet("const calculate = (x, y) => x * y;") }
        }

        // Code Editor Box with line numbers
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0D1322))
                .border(1.dp, Color(0xFF1E2A44), RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = Color(0xFFE2E8F0),
                    fontSize = 13.5.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 20.sp
                ),
                cursorBrush = SolidColor(Color(0xFF00F0FF)),
                modifier = Modifier.fillMaxSize()
            )
        }

        // Bottom status & actions bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "الأسطر: ${value.text.lines().size} | الأحرف: ${value.text.length} | المؤشر: ${value.selection.start}",
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Code", value.text))
                        Toast.makeText(context, "تم نسخ الكود للحافظة!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("نسخ", fontSize = 11.sp, color = Color.White)
                }

                Button(
                    onClick = { onValueChange(TextFieldValue("", TextRange.Zero)) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D1515)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("مسح", fontSize = 11.sp, color = Color(0xFFF87171))
                }
            }
        }
    }
}

@Composable
fun EverydayNoteView(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1E293B))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = Color(0xFFF8FAFC),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                ),
                cursorBrush = SolidColor(Color(0xFF38BDF8)),
                modifier = Modifier.fillMaxSize()
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "عدد الكلمات: ${value.text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size} | الحروف: ${value.text.length}",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Text", value.text))
                    Toast.makeText(context, "تم النسخ!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("نسخ النص", fontSize = 11.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun QuickCodeChip(
    label: String,
    trigger: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF162032))
            .border(1.dp, Color(0xFF233658), RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(4.dp))
            Text("($trigger)", color = Color(0xFF64748B), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        }
    }
}
