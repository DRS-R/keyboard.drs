package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.model.KeyboardTheme

data class KeyboardPalette(
    val background: Color,
    val keyBackground: Color,
    val specialKeyBackground: Color,
    val keyBorder: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val accent: Color,
    val smartbarBg: Color,
    val candidateBg: Color
)

object KeyboardThemeConfig {
    fun getPalette(theme: KeyboardTheme): KeyboardPalette {
        return when (theme) {
            KeyboardTheme.CYBERPUNK_NEON -> KeyboardPalette(
                background = Color(0xFF090D16),
                keyBackground = Color(0xFF131C2D),
                specialKeyBackground = Color(0xFF1B273E),
                keyBorder = Color(0xFF233658),
                primaryText = Color(0xFFE2E8F0),
                secondaryText = Color(0xFF38BDF8),
                accent = Color(0xFF00F0FF),
                smartbarBg = Color(0xFF0D1424),
                candidateBg = Color(0xFF1E293B)
            )
            KeyboardTheme.DEV_MONOKAI -> KeyboardPalette(
                background = Color(0xFF1E1E22),
                keyBackground = Color(0xFF2A282D),
                specialKeyBackground = Color(0xFF3B383E),
                keyBorder = Color(0xFF4A464E),
                primaryText = Color(0xFFF8F8F2),
                secondaryText = Color(0xFFFFD866),
                accent = Color(0xFFA9DC76),
                smartbarBg = Color(0xFF222226),
                candidateBg = Color(0xFF3E3B42)
            )
            KeyboardTheme.AMOLED_OBSIDIAN -> KeyboardPalette(
                background = Color(0xFF000000),
                keyBackground = Color(0xFF121212),
                specialKeyBackground = Color(0xFF1E1E1E),
                keyBorder = Color(0xFF2E2E2E),
                primaryText = Color(0xFFFFFFFF),
                secondaryText = Color(0xFF9E9E9E),
                accent = Color(0xFF60A5FA),
                smartbarBg = Color(0xFF080808),
                candidateBg = Color(0xFF1A1A1A)
            )
            KeyboardTheme.MATERIAL_YOU -> KeyboardPalette(
                background = Color(0xFF1B1B1F),
                keyBackground = Color(0xFF2D2F34),
                specialKeyBackground = Color(0xFF3B3E45),
                keyBorder = Color(0xFF43474E),
                primaryText = Color(0xFFE3E2E6),
                secondaryText = Color(0xFFC4C6D0),
                accent = Color(0xFFADC6FF),
                smartbarBg = Color(0xFF202024),
                candidateBg = Color(0xFF32353B)
            )
            KeyboardTheme.ARCTIC_CLEAN -> KeyboardPalette(
                background = Color(0xFFF1F5F9),
                keyBackground = Color(0xFFFFFFFF),
                specialKeyBackground = Color(0xFFE2E8F0),
                keyBorder = Color(0xFFCBD5E1),
                primaryText = Color(0xFF0F172A),
                secondaryText = Color(0xFF64748B),
                accent = Color(0xFF0284C7),
                smartbarBg = Color(0xFFE2E8F0),
                candidateBg = Color(0xFFFFFFFF)
            )
        }
    }
}
