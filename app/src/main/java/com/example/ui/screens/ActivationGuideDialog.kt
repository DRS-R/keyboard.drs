package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object ImeHelper {
    fun isImeEnabled(context: Context): Boolean {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager ?: return false
        val list = imm.enabledInputMethodList
        val pkg = context.packageName
        return list.any { it.packageName == pkg }
    }

    fun isImeSelected(context: Context): Boolean {
        val defaultIme = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        return defaultIme != null && defaultIme.contains(context.packageName)
    }

    fun openImeSettings(context: Context) {
        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun showImePicker(context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showInputMethodPicker()
    }
}

@Composable
fun ActivationGuideDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isEnabled by remember { mutableStateOf(ImeHelper.isImeEnabled(context)) }
    var isSelected by remember { mutableStateOf(ImeHelper.isImeSelected(context)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF00F0FF))
                Spacer(Modifier.width(8.dp))
                Text("تفعيل OmniBoard في نظام أندرويد", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "لجعل OmniBoard لوحة المفاتيح الافتراضية لجميع تطبيقات هاتفك، اتبع الخطوتين التاليتين:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Step 1: Enable
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isEnabled) Color(0xFF064E3B) else Color(0xFF1E293B)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "1. تمكين اللوحة في الإعدادات",
                                fontWeight = FontWeight.SemiBold,
                                color = if (isEnabled) Color(0xFF34D399) else Color.White,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (isEnabled) "تم التمكين بنجاح ✓" else "مطلوب تفعيل OmniBoard",
                                fontSize = 11.sp,
                                color = if (isEnabled) Color(0xFFA7F3D0) else Color(0xFF94A3B8)
                            )
                        }
                        if (!isEnabled) {
                            Button(
                                onClick = {
                                    ImeHelper.openImeSettings(context)
                                    isEnabled = ImeHelper.isImeEnabled(context)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF))
                            ) {
                                Text("تمكين", color = Color.Black, fontSize = 12.sp)
                            }
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = Color(0xFF34D399))
                        }
                    }
                }

                // Step 2: Select
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF064E3B) else Color(0xFF1E293B)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "2. اختيارها كلوحة افتراضية",
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color(0xFF34D399) else Color.White,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (isSelected) "اللوحة نشطة حالياً كلوحة افتراضية ✓" else "اختر OmniBoard من قائمة اللوحات",
                                fontSize = 11.sp,
                                color = if (isSelected) Color(0xFFA7F3D0) else Color(0xFF94A3B8)
                            )
                        }
                        if (!isSelected) {
                            Button(
                                onClick = {
                                    ImeHelper.showImePicker(context)
                                    isSelected = ImeHelper.isImeSelected(context)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7))
                            ) {
                                Text("اختيار", color = Color.White, fontSize = 12.sp)
                            }
                        } else {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = Color(0xFF34D399))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}
