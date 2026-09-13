package com.example.model

enum class KeyType {
    CHARACTER,
    SHIFT,
    DELETE,
    ENTER,
    SPACE,
    MODE_SWITCH,
    LANGUAGE_SWITCH,
    PROGRAMMER_BAR,
    TAB,
    ESC,
    CTRL,
    ALT,
    ARROW_LEFT,
    ARROW_RIGHT,
    ARROW_UP,
    ARROW_DOWN,
    CLIPBOARD,
    SNIPPETS,
    CASE_CONVERT,
    VOICE,
    HIDE_KEYBOARD
}

data class KeyDef(
    val primary: String,
    val displayLabel: String = primary,
    val type: KeyType = KeyType.CHARACTER,
    val topLeft: String? = null,
    val topRight: String? = null,
    val bottomLeft: String? = null,
    val bottomRight: String? = null,
    val weight: Float = 1f,
    val isSpecial: Boolean = false,
    val isAccent: Boolean = false
)

enum class KeyboardMode {
    ENGLISH_LOWER,
    ENGLISH_UPPER,
    ARABIC,
    PROGRAMMER_SYMBOLS,
    NUMBERS_HEX,
    FUNCTION_KEYS
}

enum class KeyboardTheme {
    CYBERPUNK_NEON,
    DEV_MONOKAI,
    AMOLED_OBSIDIAN,
    MATERIAL_YOU,
    ARCTIC_CLEAN
}
