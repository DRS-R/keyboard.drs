package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.keyboard.KeyboardController
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen(val title: String, val icon: ImageVector) {
    SETUP("التفعيل", Icons.Default.CheckCircle),
    SANDBOX("التجربة", Icons.Default.Keyboard),
    SNIPPETS("المقتطفات", Icons.Default.Code),
    CLIPBOARD("الحافظة", Icons.Default.ContentPaste),
    ALGORITHMS("الخوارزميات", Icons.Default.Info),
    SETTINGS("الإعدادات", Icons.Default.Tune)
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
                    containerColor = Color(0xFF0A0F1D)
                ) { innerPadding ->
                    OmniBoardMainContainer(
                        controller = controller,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun OmniBoardMainContainer(
    controller: KeyboardController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(AppScreen.SETUP) }
    var isEnabled by remember { mutableStateOf(ImeHelper.isImeEnabled(context)) }
    var isSelected by remember { mutableStateOf(ImeHelper.isImeSelected(context)) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Refresh IME state automatically on resume or when screen changes
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                isEnabled = ImeHelper.isImeEnabled(context)
                isSelected = ImeHelper.isImeSelected(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(currentScreen) {
        isEnabled = ImeHelper.isImeEnabled(context)
        isSelected = ImeHelper.isImeSelected(context)
    }

    val isReady = isEnabled && isSelected

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
    ) {
        // Futuristic Top Bar
        Surface(
            color = Color(0xFF0F172A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Brand logo & title
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
                            text = "OmniBoard 2027",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Pure Algorithms • Offline Core • Zero AI",
                            color = Color(0xFF00F0FF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Quick System IME Status Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isReady) Color(0xFF064E3B) else Color(0xFF1E293B))
                        .border(1.dp, if (isReady) Color(0xFF10B981) else Color(0xFF00F0FF), RoundedCornerShape(8.dp))
                        .clickable { currentScreen = AppScreen.SETUP }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isReady) Icons.Default.CheckCircle else Icons.Default.SettingsSuggest,
                            contentDescription = null,
                            tint = if (isReady) Color(0xFF34D399) else Color(0xFF00F0FF),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isReady) "جاهزة كلوحة افتراضية" else "تفعيل بالنظام",
                            color = if (isReady) Color(0xFFA7F3D0) else Color(0xFF00F0FF),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

        // Screen Body with smooth Crossfade
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Crossfade(
                targetState = currentScreen,
                animationSpec = tween(durationMillis = 200),
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    AppScreen.SETUP -> SetupScreen(controller = controller, modifier = Modifier.fillMaxSize())
                    AppScreen.SANDBOX -> SandboxScreen(controller = controller, modifier = Modifier.fillMaxSize())
                    AppScreen.SNIPPETS -> SnippetsScreen(controller = controller, modifier = Modifier.fillMaxSize())
                    AppScreen.CLIPBOARD -> ClipboardScreen(controller = controller, modifier = Modifier.fillMaxSize())
                    AppScreen.ALGORITHMS -> AlgorithmsLabScreen(controller = controller, modifier = Modifier.fillMaxSize())
                    AppScreen.SETTINGS -> SettingsScreen(controller = controller, modifier = Modifier.fillMaxSize())
                }
            }
        }

        HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

        // Modern 2027 Material 3 Bottom Navigation Bar
        NavigationBar(
            containerColor = Color(0xFF0B0F19),
            contentColor = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier.height(64.dp)
        ) {
            AppScreen.values().forEach { screen ->
                val isSelected = currentScreen == screen
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { currentScreen = screen },
                    icon = {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.title,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            text = screen.title,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0A0F1D),
                        selectedTextColor = Color(0xFF00F0FF),
                        indicatorColor = Color(0xFF00F0FF),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B)
                    )
                )
            }
        }
    }
}
