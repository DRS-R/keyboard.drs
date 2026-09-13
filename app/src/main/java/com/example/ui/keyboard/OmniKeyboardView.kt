package com.example.ui.keyboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.keyboard.KeyboardController
import com.example.keyboard.KeyboardUiState
import com.example.keyboard.OmniInputTarget
import com.example.model.KeyboardLayouts
import com.example.model.KeyboardMode
import com.example.ui.theme.KeyboardThemeConfig

@Composable
fun OmniKeyboardView(
    controller: KeyboardController,
    inputTarget: OmniInputTarget,
    modifier: Modifier = Modifier
) {
    val uiState by controller.uiState.collectAsState()
    val snippets by controller.snippets.collectAsState()
    val clipboardItems by controller.clipboardHistory.collectAsState()

    val palette = remember(uiState.activeTheme) {
        KeyboardThemeConfig.getPalette(uiState.activeTheme)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.background)
            .navigationBarsPadding()
    ) {
        // 1. Popup Sheet Overlays (Clipboard, Snippets, Case, Settings)
        AnimatedVisibility(visible = uiState.showClipboardSheet) {
            ClipboardSheet(
                palette = palette,
                clipboardItems = clipboardItems,
                onSelectText = { controller.insertClipboardText(inputTarget, it) },
                onClearAll = { controller.clearClipboardHistory() },
                onClose = { controller.handleKeyInput(inputTarget, com.example.model.KeyDef("", type = com.example.model.KeyType.ESC)) }
            )
        }

        AnimatedVisibility(visible = uiState.showSnippetsSheet) {
            SnippetsSheet(
                palette = palette,
                snippets = snippets,
                onSelectSnippet = { controller.insertSnippet(inputTarget, it) },
                onAddSnippet = { trigger, title, content, category ->
                    controller.addCustomSnippet(trigger, title, content, category)
                },
                onClose = { controller.handleKeyInput(inputTarget, com.example.model.KeyDef("", type = com.example.model.KeyType.ESC)) }
            )
        }

        AnimatedVisibility(visible = uiState.showCaseConvertSheet) {
            CaseConvertSheet(
                palette = palette,
                onSelectStyle = { controller.applyCaseConversion(inputTarget, it) },
                onClose = { controller.handleKeyInput(inputTarget, com.example.model.KeyDef("", type = com.example.model.KeyType.ESC)) }
            )
        }

        AnimatedVisibility(visible = uiState.showSettingsSheet) {
            SettingsSheet(
                palette = palette,
                activeTheme = uiState.activeTheme,
                hapticEnabled = uiState.hapticEnabled,
                autoClosePairs = uiState.autoClosePairs,
                keyHeightDp = uiState.keyHeightDp,
                onThemeChange = { controller.setTheme(it) },
                onToggleHaptic = { controller.toggleHaptic() },
                onToggleAutoClose = { controller.toggleAutoClosePairs() },
                onKeyHeightChange = { controller.setKeyHeight(it) },
                onClose = { controller.toggleSettingsSheet() }
            )
        }

        // 2. Smartbar with suggestions & tools
        SmartbarView(
            palette = palette,
            suggestions = uiState.suggestions,
            mathResult = uiState.mathResult,
            onApplySuggestion = { controller.applySuggestion(inputTarget, it) },
            onApplyMath = { controller.applyMathResult(inputTarget, it) },
            onOpenClipboard = { controller.handleKeyInput(inputTarget, com.example.model.KeyDef("", type = com.example.model.KeyType.CLIPBOARD)) },
            onOpenSnippets = { controller.handleKeyInput(inputTarget, com.example.model.KeyDef("", type = com.example.model.KeyType.SNIPPETS)) },
            onOpenCaseConvert = { controller.handleKeyInput(inputTarget, com.example.model.KeyDef("", type = com.example.model.KeyType.CASE_CONVERT)) },
            onOpenSettings = { controller.toggleSettingsSheet() },
            onMoveCursor = { inputTarget.moveCursor(it) }
        )

        // 3. Developer Quick Bar (Esc, Tab, Ctrl, Alt, Brackets, Arrows)
        if (uiState.showDevQuickBar) {
            DevQuickBarView(
                palette = palette,
                isCtrlActive = uiState.isCtrlActive,
                isAltActive = uiState.isAltActive,
                onKeyTap = { controller.handleKeyInput(inputTarget, it) }
            )
        }

        // 4. Keyboard Key Matrix
        val layout = remember(uiState.currentMode, uiState.isShiftActive) {
            when (uiState.currentMode) {
                KeyboardMode.ENGLISH_LOWER -> KeyboardLayouts.getEnglishLayout(caps = false)
                KeyboardMode.ENGLISH_UPPER -> KeyboardLayouts.getEnglishLayout(caps = true)
                KeyboardMode.ARABIC -> KeyboardLayouts.getArabicLayout()
                KeyboardMode.PROGRAMMER_SYMBOLS -> KeyboardLayouts.getProgrammerSymbolsLayout()
                KeyboardMode.NUMBERS_HEX, KeyboardMode.FUNCTION_KEYS -> KeyboardLayouts.getNumberHexLayout()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp, vertical = 2.dp)
        ) {
            layout.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    row.forEach { key ->
                        KeyView(
                            key = key,
                            palette = palette,
                            keyHeightDp = uiState.keyHeightDp,
                            modifier = Modifier.weight(key.weight),
                            onKeyAction = { keyDef, swipeDir ->
                                controller.handleKeyInput(inputTarget, keyDef, swipeDir)
                            }
                        )
                    }
                }
            }
        }
    }
}
