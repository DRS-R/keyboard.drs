package com.example.keyboard

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.InputType
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import com.example.OmniApplication
import com.example.data.ClipboardEntity
import com.example.data.DictionaryRepository
import com.example.data.SnippetEntity
import com.example.engine.*
import com.example.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class KeyboardUiState(
    val currentMode: KeyboardMode = KeyboardMode.ENGLISH_LOWER,
    val isShiftActive: Boolean = false,
    val isCapsLock: Boolean = false,
    val isCtrlActive: Boolean = false,
    val isAltActive: Boolean = false,
    val showDevQuickBar: Boolean = true,
    val suggestions: List<String> = emptyList(),
    val mathResult: MathResult? = null,
    val activeTheme: KeyboardTheme = KeyboardTheme.CYBERPUNK_NEON,
    val hapticEnabled: Boolean = true,
    val autoClosePairs: Boolean = true,
    val currentWord: String = "",
    val showClipboardSheet: Boolean = false,
    val showSnippetsSheet: Boolean = false,
    val showCaseConvertSheet: Boolean = false,
    val showSettingsSheet: Boolean = false,
    val keyHeightDp: Int = 48,
    val isPasswordField: Boolean = false,
    val imeAction: Int = EditorInfo.IME_ACTION_UNSPECIFIED,
    val imeActionLabel: String = "↵"
)

class KeyboardController(
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {
    private val _uiState = MutableStateFlow(KeyboardUiState())
    val uiState: StateFlow<KeyboardUiState> = _uiState.asStateFlow()

    val trieEngine = TrieEngine()
    private val dao = OmniApplication.instance.database.omniDao()
    private val dictDao = OmniApplication.instance.database.dictionaryDao()

    val dictionaryRepository = DictionaryRepository(dictDao, trieEngine, coroutineScope)

    val snippets: StateFlow<List<SnippetEntity>> = dao.getAllSnippets()
        .stateIn(coroutineScope, SharingStarted.Lazily, emptyList())

    val clipboardHistory: StateFlow<List<ClipboardEntity>> = dao.getClipboardHistory()
        .stateIn(coroutineScope, SharingStarted.Lazily, emptyList())

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun triggerHaptic(strong: Boolean = false) {
        if (!_uiState.value.hapticEnabled) return
        try {
            val duration = if (strong) 30L else 15L
            val amplitude = if (strong) 200 else 90
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(duration, amplitude))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(duration)
            }
        } catch (_: Exception) {
            // Ignore if vibration fails or not allowed
        }
    }

    fun handleKeyInput(target: OmniInputTarget, key: KeyDef, swipeDirection: KeySwipeDirection = KeySwipeDirection.NONE) {
        triggerHaptic()

        // 1. Check corner swipe input first if gesture detected
        if (swipeDirection != KeySwipeDirection.NONE) {
            val cornerChar = when (swipeDirection) {
                KeySwipeDirection.TOP_LEFT -> key.topLeft
                KeySwipeDirection.TOP_RIGHT -> key.topRight
                KeySwipeDirection.BOTTOM_LEFT -> key.bottomLeft
                KeySwipeDirection.BOTTOM_RIGHT -> key.bottomRight
                else -> null
            }
            if (!cornerChar.isNullOrEmpty()) {
                handleDirectCharacterCommit(target, cornerChar)
                return
            }
        }

        // 2. Standard key types
        when (key.type) {
            KeyType.CHARACTER -> {
                handleDirectCharacterCommit(target, key.primary)
            }
            KeyType.SPACE -> {
                target.commitText(" ")
                checkSnippetTrigger(target)
                updateWordAnalysis(target)
            }
            KeyType.DELETE -> {
                handleBackspace(target)
            }
            KeyType.ENTER -> {
                target.performAction()
                updateWordAnalysis(target)
            }
            KeyType.SHIFT -> {
                toggleShift()
            }
            KeyType.MODE_SWITCH -> {
                toggleModeSwitch()
            }
            KeyType.LANGUAGE_SWITCH -> {
                toggleLanguage()
            }
            KeyType.PROGRAMMER_BAR -> {
                toggleDevQuickBar()
            }
            KeyType.TAB -> {
                target.commitText("    ")
                updateWordAnalysis(target)
            }
            KeyType.ESC -> {
                if (!closeAllSheets()) {
                    target.sendKey(KeyEvent.KEYCODE_ESCAPE)
                }
            }
            KeyType.CTRL -> {
                _uiState.update { it.copy(isCtrlActive = !it.isCtrlActive) }
            }
            KeyType.ALT -> {
                _uiState.update { it.copy(isAltActive = !it.isAltActive) }
            }
            KeyType.ARROW_LEFT -> {
                target.moveCursor(-1)
                updateWordAnalysis(target)
            }
            KeyType.ARROW_RIGHT -> {
                target.moveCursor(1)
                updateWordAnalysis(target)
            }
            KeyType.ARROW_UP -> {
                target.moveCursor(-10)
                updateWordAnalysis(target)
            }
            KeyType.ARROW_DOWN -> {
                target.moveCursor(10)
                updateWordAnalysis(target)
            }
            KeyType.CLIPBOARD -> {
                _uiState.update { it.copy(showClipboardSheet = !it.showClipboardSheet) }
            }
            KeyType.SNIPPETS -> {
                _uiState.update { it.copy(showSnippetsSheet = !it.showSnippetsSheet) }
            }
            KeyType.CASE_CONVERT -> {
                _uiState.update { it.copy(showCaseConvertSheet = !it.showCaseConvertSheet) }
            }
            KeyType.VOICE -> {
                // Voice / settings placeholder
            }
            KeyType.HIDE_KEYBOARD -> {
                target.hideKeyboard()
            }
        }
    }

    private fun handleDirectCharacterCommit(target: OmniInputTarget, text: String) {
        // Handle Programmer CTRL shortcuts (Ctrl+C, Ctrl+V, Ctrl+Z, Ctrl+A, Ctrl+X, Ctrl+Y)
        if (_uiState.value.isCtrlActive) {
            _uiState.update { it.copy(isCtrlActive = false) }
            when (text.lowercase()) {
                "c" -> { target.copy(); return }
                "v" -> { target.paste(); return }
                "x" -> { target.cut(); return }
                "a" -> { target.selectAll(); return }
                "z" -> { target.undo(); return }
                "y" -> { target.redo(); return }
                else -> {
                    val char = text.firstOrNull()
                    if (char != null && char.isLetter()) {
                        val keyCode = KeyEvent.keyCodeFromString("KEYCODE_${char.uppercaseChar()}")
                        if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
                            target.sendKey(keyCode, KeyEvent.META_CTRL_ON)
                            return
                        }
                    }
                }
            }
        }

        // Handle ALT modifier
        if (_uiState.value.isAltActive) {
            _uiState.update { it.copy(isAltActive = false) }
            val char = text.firstOrNull()
            if (char != null && char.isLetter()) {
                val keyCode = KeyEvent.keyCodeFromString("KEYCODE_${char.uppercaseChar()}")
                if (keyCode != KeyEvent.KEYCODE_UNKNOWN) {
                    target.sendKey(keyCode, KeyEvent.META_ALT_ON)
                    return
                }
            }
        }

        // Auto-close pair if enabled and single opening char
        if (_uiState.value.autoClosePairs && text.length == 1) {
            val char = text[0]
            val closing = TextTransformEngine.getMatchingClosingPair(char)
            if (closing != null) {
                target.commitText("$char$closing")
                target.moveCursor(-1)
                updateWordAnalysis(target)
                return
            }
        }

        target.commitText(text)

        // Reset single-tap shift if not caps lock
        if (_uiState.value.isShiftActive && !_uiState.value.isCapsLock) {
            _uiState.update {
                it.copy(
                    isShiftActive = false,
                    currentMode = if (it.currentMode == KeyboardMode.ENGLISH_UPPER) KeyboardMode.ENGLISH_LOWER else it.currentMode
                )
            }
        }

        updateWordAnalysis(target)
    }

    private fun handleBackspace(target: OmniInputTarget) {
        val before = target.getTextBeforeCursor(1)
        val after = target.getTextAfterCursor(1)

        // Auto-close pair smart deletion: If cursor is between () or {}, delete both!
        if (before.isNotEmpty() && after.isNotEmpty()) {
            if (TextTransformEngine.isMatchingPair(before[0], after[0])) {
                target.deleteBackward(1)
                target.moveCursor(1)
                target.deleteBackward(1)
                updateWordAnalysis(target)
                return
            }
        }

        target.deleteBackward(1)
        updateWordAnalysis(target)
    }

    fun applySuggestion(target: OmniInputTarget, word: String) {
        val currentWord = _uiState.value.currentWord
        if (currentWord.isNotEmpty()) {
            target.deleteBackward(currentWord.length)
        }
        target.commitText("$word ")
        // Learn word in Room dictionary & Trie in background if not in password mode
        if (!_uiState.value.isPasswordField) {
            coroutineScope.launch(Dispatchers.IO) {
                dictionaryRepository.recordWordUsage(word)
                dao.insertOrUpdateWord(com.example.data.WordEntity(word, 5, "en"))
            }
        }
        updateWordAnalysis(target)
    }

    fun applyMathResult(target: OmniInputTarget, result: MathResult) {
        val currentWord = _uiState.value.currentWord
        if (currentWord.isNotEmpty()) {
            target.deleteBackward(currentWord.length)
        }
        target.commitText(result.formattedDecimal)
        updateWordAnalysis(target)
    }

    fun insertSnippet(target: OmniInputTarget, snippet: SnippetEntity) {
        target.commitText(snippet.content)
        _uiState.update { it.copy(showSnippetsSheet = false) }
        coroutineScope.launch(Dispatchers.IO) {
            dao.incrementSnippetUsage(snippet.id)
        }
        updateWordAnalysis(target)
    }

    fun insertClipboardText(target: OmniInputTarget, content: String) {
        target.commitText(content)
        _uiState.update { it.copy(showClipboardSheet = false) }
        updateWordAnalysis(target)
    }

    fun applyCaseConversion(target: OmniInputTarget, style: CaseStyle) {
        val selected = target.getSelectedText()
        if (selected.isNotBlank()) {
            val converted = TextTransformEngine.convertCase(selected, style)
            target.replaceSelection(converted)
        } else {
            val current = _uiState.value.currentWord
            if (current.isNotBlank()) {
                val converted = TextTransformEngine.convertCase(current, style)
                target.deleteBackward(current.length)
                target.commitText(converted)
            }
        }
        _uiState.update { it.copy(showCaseConvertSheet = false) }
        updateWordAnalysis(target)
    }

    private fun checkSnippetTrigger(target: OmniInputTarget) {
        val before = target.getTextBeforeCursor(20).trim()
        val lastToken = before.split(Regex("\\s+")).lastOrNull() ?: return

        if (lastToken.startsWith("!")) {
            coroutineScope.launch(Dispatchers.IO) {
                val snippet = dao.getSnippetByTrigger(lastToken)
                if (snippet != null) {
                    withContext(Dispatchers.Main) {
                        target.deleteBackward(lastToken.length + 1) // delete token + space
                        target.commitText(snippet.content)
                        dao.incrementSnippetUsage(snippet.id)
                    }
                }
            }
        }
    }

    fun updateWordAnalysis(target: OmniInputTarget) {
        if (_uiState.value.isPasswordField) {
            _uiState.update { it.copy(currentWord = "", mathResult = null, suggestions = emptyList()) }
            return
        }
        val textBefore = target.getTextBeforeCursor(40)
        val lastWord = textBefore.takeLastWhile { !it.isWhitespace() && it !in "()[]{}<>=;:,." }

        _uiState.update { it.copy(currentWord = lastWord) }

        // Math evaluator check
        val math = MathExpressionEngine.evaluate(lastWord.ifBlank { textBefore.trim().takeLastWhile { it != '\n' } })
        _uiState.update { it.copy(mathResult = math) }

        // Real-time Trie autocomplete & Damerau-Levenshtein autocorrection without AI
        if (lastWord.length >= 2) {
            coroutineScope.launch(Dispatchers.Default) {
                val suggestionsList = trieEngine.getSuggestions(lastWord, maxResults = 4).map { it.word }
                _uiState.update { it.copy(suggestions = suggestionsList) }
            }
        } else {
            _uiState.update { it.copy(suggestions = emptyList()) }
        }
    }

    fun toggleShift() {
        val newShift = !_uiState.value.isShiftActive
        _uiState.update {
            it.copy(
                isShiftActive = newShift,
                currentMode = if (newShift) KeyboardMode.ENGLISH_UPPER else KeyboardMode.ENGLISH_LOWER
            )
        }
    }

    fun toggleModeSwitch() {
        _uiState.update {
            val next = when (it.currentMode) {
                KeyboardMode.ENGLISH_LOWER, KeyboardMode.ENGLISH_UPPER, KeyboardMode.ARABIC -> KeyboardMode.PROGRAMMER_SYMBOLS
                KeyboardMode.PROGRAMMER_SYMBOLS -> KeyboardMode.NUMBERS_HEX
                KeyboardMode.NUMBERS_HEX, KeyboardMode.FUNCTION_KEYS -> KeyboardMode.ENGLISH_LOWER
            }
            it.copy(currentMode = next)
        }
    }

    fun toggleLanguage() {
        _uiState.update {
            val next = when (it.currentMode) {
                KeyboardMode.ARABIC -> KeyboardMode.ENGLISH_LOWER
                else -> KeyboardMode.ARABIC
            }
            it.copy(currentMode = next)
        }
    }

    fun toggleDevQuickBar() {
        _uiState.update { it.copy(showDevQuickBar = !it.showDevQuickBar) }
    }

    fun setTheme(theme: KeyboardTheme) {
        _uiState.update { it.copy(activeTheme = theme) }
    }

    fun toggleHaptic() {
        _uiState.update { it.copy(hapticEnabled = !it.hapticEnabled) }
    }

    fun toggleAutoClosePairs() {
        _uiState.update { it.copy(autoClosePairs = !it.autoClosePairs) }
    }

    fun toggleAutoClose() = toggleAutoClosePairs()

    fun setKeyHeight(heightDp: Int) {
        _uiState.update { it.copy(keyHeightDp = heightDp.coerceIn(40, 68)) }
    }

    fun toggleSettingsSheet() {
        _uiState.update { it.copy(showSettingsSheet = !it.showSettingsSheet) }
    }

    fun addCustomSnippet(trigger: String, title: String, content: String, category: String) {
        coroutineScope.launch(Dispatchers.IO) {
            dao.insertSnippet(
                SnippetEntity(
                    trigger = trigger,
                    title = title,
                    content = content,
                    category = category,
                    isPinned = true
                )
            )
        }
    }

    fun addSnippet(trigger: String, title: String, content: String, category: String) =
        addCustomSnippet(trigger, title, content, category)

    fun addClipboard(content: String) {
        coroutineScope.launch(Dispatchers.IO) {
            dao.insertClipboard(
                ClipboardEntity(
                    content = content,
                    isPinned = false,
                    isCode = content.contains("{") || content.contains("fun ") || content.contains("def ")
                )
            )
        }
    }

    fun deleteSnippet(snippet: SnippetEntity) {
        coroutineScope.launch(Dispatchers.IO) {
            dao.deleteSnippet(snippet)
        }
    }

    fun deleteClipboard(item: ClipboardEntity) {
        coroutineScope.launch(Dispatchers.IO) {
            dao.deleteClipboard(item)
        }
    }

    fun togglePinClipboard(id: Long) {
        coroutineScope.launch(Dispatchers.IO) {
            dao.togglePinClipboard(id)
        }
    }

    fun clearClipboardHistory() {
        coroutineScope.launch(Dispatchers.IO) {
            dao.clearUnpinnedClipboard()
        }
    }

    fun onEditorInfoChanged(attribute: EditorInfo?) {
        if (attribute == null) return
        val inputType = attribute.inputType
        val isPassword = when (inputType and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_TEXT -> {
                val variation = inputType and InputType.TYPE_MASK_VARIATION
                variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
            }
            InputType.TYPE_CLASS_NUMBER -> {
                val variation = inputType and InputType.TYPE_MASK_VARIATION
                variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
            }
            else -> false
        }

        val isNumber = when (inputType and InputType.TYPE_MASK_CLASS) {
            InputType.TYPE_CLASS_NUMBER,
            InputType.TYPE_CLASS_PHONE,
            InputType.TYPE_CLASS_DATETIME -> true
            else -> false
        }

        val action = attribute.imeOptions and EditorInfo.IME_MASK_ACTION
        val actionLabel = when (action) {
            EditorInfo.IME_ACTION_GO -> "Go"
            EditorInfo.IME_ACTION_SEARCH -> "Search"
            EditorInfo.IME_ACTION_SEND -> "Send"
            EditorInfo.IME_ACTION_NEXT -> "Next"
            EditorInfo.IME_ACTION_DONE -> "Done"
            else -> attribute.actionLabel?.toString() ?: "↵"
        }

        _uiState.update {
            it.copy(
                isPasswordField = isPassword,
                imeAction = action,
                imeActionLabel = actionLabel,
                currentMode = if (isNumber) KeyboardMode.NUMBERS_HEX else if (it.currentMode == KeyboardMode.NUMBERS_HEX) KeyboardMode.ENGLISH_LOWER else it.currentMode
            )
        }
    }

    fun onInputViewStarted(target: OmniInputTarget, info: EditorInfo?) {
        onEditorInfoChanged(info)
        updateWordAnalysis(target)
    }

    fun onSelectionChanged(target: OmniInputTarget, selStart: Int, selEnd: Int) {
        updateWordAnalysis(target)
    }

    fun closeAllSheets(): Boolean {
        val state = _uiState.value
        val wasOpen = state.showClipboardSheet || state.showSnippetsSheet || state.showCaseConvertSheet || state.showSettingsSheet
        if (wasOpen) {
            _uiState.update {
                it.copy(
                    showClipboardSheet = false,
                    showSnippetsSheet = false,
                    showCaseConvertSheet = false,
                    showSettingsSheet = false
                )
            }
        }
        return wasOpen
    }

    fun resetTransientModifiers() {
        _uiState.update {
            it.copy(
                isShiftActive = if (it.isCapsLock) it.isShiftActive else false,
                isCtrlActive = false,
                isAltActive = false
            )
        }
    }
}
