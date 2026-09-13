package com.example.ui.keyboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.MathResult
import com.example.ui.theme.KeyboardPalette

@Composable
fun SmartbarView(
    palette: KeyboardPalette,
    suggestions: List<String>,
    mathResult: MathResult?,
    onApplySuggestion: (String) -> Unit,
    onApplyMath: (MathResult) -> Unit,
    onOpenClipboard: () -> Unit,
    onOpenSnippets: () -> Unit,
    onOpenCaseConvert: () -> Unit,
    onOpenSettings: () -> Unit,
    onMoveCursor: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Surface(
        color = palette.smartbarBg,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quick tool action buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onOpenSnippets,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Snippets",
                        tint = palette.accent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onOpenClipboard,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Clipboard",
                        tint = palette.secondaryText,
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = onOpenCaseConvert,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Transform,
                        contentDescription = "Case Convert",
                        tint = palette.secondaryText,
                        modifier = Modifier.size(17.dp)
                    )
                }

                IconButton(
                    onClick = { onMoveCursor(-1) },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Cursor Left",
                        tint = palette.secondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { onMoveCursor(1) },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Cursor Right",
                        tint = palette.secondaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            VerticalDivider(
                modifier = Modifier
                    .height(20.dp)
                    .padding(horizontal = 4.dp),
                color = palette.keyBorder
            )

            // Dynamic suggestions & Math pills
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Real-time Math evaluator result pill
                if (mathResult != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(palette.accent.copy(alpha = 0.2f))
                            .border(1.dp, palette.accent, RoundedCornerShape(6.dp))
                            .clickable { onApplyMath(mathResult) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        val extra = if (mathResult.hex != null) " (${mathResult.hex})" else ""
                        Text(
                            text = "= ${mathResult.formattedDecimal}$extra",
                            color = palette.accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Autocomplete & spell suggestions
                if (suggestions.isEmpty() && mathResult == null) {
                    Text(
                        text = "OmniBoard • No AI • 100% Offline Fast",
                        color = palette.secondaryText.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                } else {
                    suggestions.forEach { word ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(palette.candidateBg)
                                .clickable { onApplySuggestion(word) }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = word,
                                color = palette.primaryText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Settings button
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = palette.secondaryText,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}
