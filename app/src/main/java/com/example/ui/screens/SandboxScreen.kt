package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import com.example.keyboard.KeyboardController
import com.example.keyboard.OmniInputTarget
import com.example.ui.keyboard.OmniKeyboardView

enum class SandboxSubTab {
    CODE_TERMINAL,
    EVERYDAY_TEXT,
    MATH_CALCULATOR
}

@Composable
fun SandboxScreen(
    controller: KeyboardController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(SandboxSubTab.CODE_TERMINAL) }
    var showEmbeddedKeyboard by remember { mutableStateOf(true) }

    var codeState by remember {
        mutableStateOf(
            TextFieldValue(
                text = """// OmniBoard 2027: Pure Algorithmic Keyboard
fun computeArchitecture(): String {
    val engine = "Prefix Trie + Top-K Branch Pruning"
    val autocorrect = "Trie-directed Damerau-Levenshtein"
    return "OmniBoard: 100% Offline • Zero AI"
}
""",
                selection = TextRange(180)
            )
        )
    }

    var textState by remember {
        mutableStateOf(
            TextFieldValue(
                text = "مرحباً بك في OmniBoard! يمكنك تجربة الكتابة السريعة باللغة العربية والإنجليزية، وتجربة السحب الزاوي لكتابة الأرقام والرموز والحركات التشكيلية دون الحاجة لتبديل التخطيط.",
                selection = TextRange(150)
            )
        )
    }

    var mathState by remember {
        mutableStateOf(
            TextFieldValue(
                text = "512 * 4 + 256\n0x1A + 0x05\n3.14159 * 10^2",
                selection = TextRange(40)
            )
        )
    }

    val activeTextFieldState = when (currentTab) {
        SandboxSubTab.CODE_TERMINAL -> codeState
        SandboxSubTab.EVERYDAY_TEXT -> textState
        SandboxSubTab.MATH_CALCULATOR -> mathState
    }

    val inputTarget = remember(currentTab, activeTextFieldState) {
        object : OmniInputTarget {
            override fun commitText(text: String) {
                when (currentTab) {
                    SandboxSubTab.CODE_TERMINAL -> codeState = insertText(codeState, text)
                    SandboxSubTab.EVERYDAY_TEXT -> textState = insertText(textState, text)
                    SandboxSubTab.MATH_CALCULATOR -> mathState = insertText(mathState, text)
                }
            }

            override fun deleteBackward(count: Int) {
                when (currentTab) {
                    SandboxSubTab.CODE_TERMINAL -> codeState = deleteText(codeState, count)
                    SandboxSubTab.EVERYDAY_TEXT -> textState = deleteText(textState, count)
                    SandboxSubTab.MATH_CALCULATOR -> mathState = deleteText(mathState, count)
                }
            }

            override fun moveCursor(offset: Int) {
                when (currentTab) {
                    SandboxSubTab.CODE_TERMINAL -> {
                        val pos = (codeState.selection.start + offset).coerceIn(0, codeState.text.length)
                        codeState = codeState.copy(selection = TextRange(pos))
                    }
                    SandboxSubTab.EVERYDAY_TEXT -> {
                        val pos = (textState.selection.start + offset).coerceIn(0, textState.text.length)
                        textState = textState.copy(selection = TextRange(pos))
                    }
                    SandboxSubTab.MATH_CALCULATOR -> {
                        val pos = (mathState.selection.start + offset).coerceIn(0, mathState.text.length)
                        mathState = mathState.copy(selection = TextRange(pos))
                    }
                }
            }

            override fun performAction() {
                commitText("\n")
            }

            override fun getTextBeforeCursor(length: Int): String {
                val state = when (currentTab) {
                    SandboxSubTab.CODE_TERMINAL -> codeState
                    SandboxSubTab.EVERYDAY_TEXT -> textState
                    SandboxSubTab.MATH_CALCULATOR -> mathState
                }
                val start = maxOf(0, state.selection.min - length)
                return state.text.substring(start, state.selection.min)
            }

            override fun getTextAfterCursor(length: Int): String {
                val state = when (currentTab) {
                    SandboxSubTab.CODE_TERMINAL -> codeState
                    SandboxSubTab.EVERYDAY_TEXT -> textState
                    SandboxSubTab.MATH_CALCULATOR -> mathState
                }
                val end = minOf(state.text.length, state.selection.max + length)
                return state.text.substring(state.selection.max, end)
            }

            override fun getSelectedText(): String {
                val state = when (currentTab) {
                    SandboxSubTab.CODE_TERMINAL -> codeState
                    SandboxSubTab.EVERYDAY_TEXT -> textState
                    SandboxSubTab.MATH_CALCULATOR -> mathState
                }
                return state.text.substring(state.selection.min, state.selection.max)
            }

            override fun replaceSelection(text: String) {
                commitText(text)
            }

            override fun hideKeyboard() {
                showEmbeddedKeyboard = false
            }

            override fun switchToNextIme() {
                ImeHelper.showImePicker(context)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
    ) {
        // Sub-tabs Row with Icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SandboxPill(
                    icon = Icons.Default.Terminal,
                    title = "محرر الأكواد",
                    isSelected = currentTab == SandboxSubTab.CODE_TERMINAL,
                    onClick = { currentTab = SandboxSubTab.CODE_TERMINAL }
                )
                SandboxPill(
                    icon = Icons.Default.EditNote,
                    title = "محرر النصوص",
                    isSelected = currentTab == SandboxSubTab.EVERYDAY_TEXT,
                    onClick = { currentTab = SandboxSubTab.EVERYDAY_TEXT }
                )
                SandboxPill(
                    icon = Icons.Default.Calculate,
                    title = "الحاسبة الرياضية",
                    isSelected = currentTab == SandboxSubTab.MATH_CALCULATOR,
                    onClick = { currentTab = SandboxSubTab.MATH_CALCULATOR }
                )
            }

            // Keyboard View Toggle
            IconButton(
                onClick = { showEmbeddedKeyboard = !showEmbeddedKeyboard },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (showEmbeddedKeyboard) Icons.Default.KeyboardHide else Icons.Default.Keyboard,
                    contentDescription = "Toggle Keyboard",
                    tint = if (showEmbeddedKeyboard) Color(0xFF00F0FF) else Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Active Tab Editor Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentTab) {
                SandboxSubTab.CODE_TERMINAL -> {
                    CodeEditorView(
                        value = codeState,
                        onValueChange = { codeState = it },
                        onInsert = { inputTarget.commitText(it) }
                    )
                }
                SandboxSubTab.EVERYDAY_TEXT -> {
                    TextEditorView(
                        value = textState,
                        onValueChange = { textState = it },
                        onCopy = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("text", textState.text))
                            Toast.makeText(context, "تم نسخ النص", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                SandboxSubTab.MATH_CALCULATOR -> {
                    MathEditorView(
                        value = mathState,
                        onValueChange = { mathState = it }
                    )
                }
            }
        }

        // Live Embedded Keyboard at bottom
        if (showEmbeddedKeyboard) {
            HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)
            OmniKeyboardView(
                controller = controller,
                inputTarget = inputTarget,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SandboxPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFF00F0FF) else Color(0xFF1E293B))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color(0xFF0A0F1D) else Color(0xFF94A3B8),
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = title,
                color = if (isSelected) Color(0xFF0A0F1D) else Color.White,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CodeEditorView(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onInsert: (String) -> Unit
) {
    val quickInserts = listOf("fun ", "val ", "if ()", "for ()", "->", "{ }", "( )", "[ ]", ";", " = ")

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0F1D))) {
        // Quick syntax insertion strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF111827))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            quickInserts.forEach { token ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1E293B))
                        .clickable { onInsert(token) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = token,
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Terminal Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF050811))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = Color(0xFF00F0FF),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 20.sp
                ),
                cursorBrush = SolidColor(Color(0xFF00F0FF)),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun TextEditorView(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onCopy: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "تجربة الكتابة العربية وتشكيل الحروف والإيماءات",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
            IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF00F0FF), modifier = Modifier.size(16.dp))
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF111827))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 15.sp,
                    lineHeight = 24.sp
                ),
                cursorBrush = SolidColor(Color(0xFF00F0FF)),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun MathEditorView(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "اكتب أي معادلة حسابية وسيتم تقييمها فورياً في شريط Smartbar (مثل 25 * 4 أو 0xFF)",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF111827))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = Color(0xFF34D399),
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 22.sp
                ),
                cursorBrush = SolidColor(Color(0xFF34D399)),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun insertText(state: TextFieldValue, insert: String): TextFieldValue {
    val text = state.text
    val selection = state.selection
    val newText = text.replaceRange(selection.min, selection.max, insert)
    val newCursor = selection.min + insert.length
    return state.copy(text = newText, selection = TextRange(newCursor))
}

private fun deleteText(state: TextFieldValue, count: Int): TextFieldValue {
    val text = state.text
    val selection = state.selection
    if (selection.min != selection.max) {
        val newText = text.removeRange(selection.min, selection.max)
        return state.copy(text = newText, selection = TextRange(selection.min))
    }
    if (selection.min == 0) return state
    val start = maxOf(0, selection.min - count)
    val newText = text.removeRange(start, selection.min)
    return state.copy(text = newText, selection = TextRange(start))
}
