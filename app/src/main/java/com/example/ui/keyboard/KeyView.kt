package com.example.ui.keyboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.CornerSwipeEngine
import com.example.engine.KeySwipeDirection
import com.example.model.KeyDef
import com.example.model.KeyType
import com.example.ui.theme.KeyboardPalette

@Composable
fun KeyView(
    key: KeyDef,
    palette: KeyboardPalette,
    keyHeightDp: Int,
    modifier: Modifier = Modifier,
    onKeyAction: (KeyDef, KeySwipeDirection) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var currentSwipeDir by remember { mutableStateOf(KeySwipeDirection.NONE) }
    var startX by remember { mutableFloatStateOf(0f) }
    var startY by remember { mutableFloatStateOf(0f) }

    val scale by animateFloatAsState(targetValue = if (isPressed) 0.94f else 1f, label = "keyScale")

    val bg = when {
        isPressed -> palette.accent.copy(alpha = 0.25f)
        key.isAccent -> palette.accent.copy(alpha = 0.15f)
        key.isSpecial -> palette.specialKeyBackground
        else -> palette.keyBackground
    }

    val borderColor = when {
        isPressed -> palette.accent
        key.isSpecial -> palette.keyBorder.copy(alpha = 0.6f)
        else -> palette.keyBorder
    }

    Box(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 3.dp)
            .height(keyHeightDp.dp)
            .scale(scale)
            .shadow(elevation = if (isPressed) 0.dp else 1.5.dp, shape = RoundedCornerShape(7.dp))
            .clip(RoundedCornerShape(7.dp))
            .background(bg)
            .border(
                width = if (isPressed) 1.5.dp else 0.8.dp,
                color = borderColor,
                shape = RoundedCornerShape(7.dp)
            )
            .pointerInput(key) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    startX = down.position.x
                    startY = down.position.y
                    currentSwipeDir = KeySwipeDirection.NONE

                    val pointerId = down.id
                    var isCancelled = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == pointerId }
                        if (change == null || change.isConsumed) {
                            isCancelled = true
                            break
                        }
                        if (!change.pressed) {
                            change.consume()
                            break
                        }
                        val curX = change.position.x
                        val curY = change.position.y
                        currentSwipeDir = CornerSwipeEngine.detectDirection(
                            startX, startY, curX, curY
                        )
                    }

                    isPressed = false
                    if (!isCancelled) {
                        onKeyAction(key, currentSwipeDir)
                    }
                    currentSwipeDir = KeySwipeDirection.NONE
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Corner Indicators (Top-Left, Top-Right, Bottom-Left, Bottom-Right)
        if (key.topLeft != null) {
            Text(
                text = key.topLeft,
                color = if (currentSwipeDir == KeySwipeDirection.TOP_LEFT) palette.accent else palette.secondaryText.copy(alpha = 0.7f),
                fontSize = 9.sp,
                fontWeight = if (currentSwipeDir == KeySwipeDirection.TOP_LEFT) FontWeight.Bold else FontWeight.Normal,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 3.dp, top = 1.dp)
            )
        }

        if (key.topRight != null) {
            Text(
                text = key.topRight,
                color = if (currentSwipeDir == KeySwipeDirection.TOP_RIGHT) palette.accent else palette.secondaryText.copy(alpha = 0.7f),
                fontSize = 9.sp,
                fontWeight = if (currentSwipeDir == KeySwipeDirection.TOP_RIGHT) FontWeight.Bold else FontWeight.Normal,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 3.dp, top = 1.dp)
            )
        }

        if (key.bottomLeft != null) {
            Text(
                text = key.bottomLeft,
                color = if (currentSwipeDir == KeySwipeDirection.BOTTOM_LEFT) palette.accent else palette.secondaryText.copy(alpha = 0.6f),
                fontSize = 8.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 3.dp, bottom = 1.dp)
            )
        }

        if (key.bottomRight != null) {
            Text(
                text = key.bottomRight,
                color = if (currentSwipeDir == KeySwipeDirection.BOTTOM_RIGHT) palette.accent else palette.secondaryText.copy(alpha = 0.6f),
                fontSize = 8.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 3.dp, bottom = 1.dp)
            )
        }

        // Center Main Label
        val labelColor = when {
            key.isSpecial -> palette.secondaryText
            key.isAccent -> palette.accent
            else -> palette.primaryText
        }

        val fontSize = when {
            key.type == KeyType.SPACE -> 11.sp
            key.displayLabel.length > 2 -> 11.sp
            key.displayLabel.length == 2 -> 13.sp
            else -> 17.sp
        }

        Text(
            text = key.displayLabel,
            color = labelColor,
            fontSize = fontSize,
            fontWeight = if (key.isSpecial || key.isAccent) FontWeight.SemiBold else FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}
