package com.example.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeyDef
import com.example.model.KeyType
import com.example.model.KeyboardLayouts
import com.example.ui.theme.KeyboardPalette

@Composable
fun DevQuickBarView(
    palette: KeyboardPalette,
    isCtrlActive: Boolean,
    isAltActive: Boolean,
    onKeyTap: (KeyDef) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(palette.smartbarBg)
            .padding(horizontal = 4.dp, vertical = 3.dp)
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KeyboardLayouts.devQuickBarKeys.forEach { key ->
            val isActiveModifier = (key.type == KeyType.CTRL && isCtrlActive) || (key.type == KeyType.ALT && isAltActive)
            val btnBg = if (isActiveModifier) palette.accent else palette.keyBackground
            val txtColor = if (isActiveModifier) palette.background else if (key.isSpecial) palette.secondaryText else palette.primaryText

            Box(
                modifier = Modifier
                    .height(34.dp)
                    .defaultMinSize(minWidth = 34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(btnBg)
                    .clickable { onKeyTap(key) }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = key.displayLabel,
                    color = txtColor,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
