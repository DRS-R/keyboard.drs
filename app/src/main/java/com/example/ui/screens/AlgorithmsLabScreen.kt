package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Spellcheck
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DictionaryWordEntity
import com.example.engine.*
import com.example.keyboard.KeyboardController
import kotlinx.coroutines.launch

@Composable
fun AlgorithmsLabScreen(
    controller: KeyboardController,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val trie = controller.trieEngine
    val repository = controller.dictionaryRepository

    // Room Database Observables
    val wordCount by repository.wordCount.collectAsStateWithLifecycle(initialValue = 0)

    // Interactive States
    var trieQuery by remember { mutableStateOf("fun") }
    val trieResults = remember(trieQuery) { trie.searchPrefix(trieQuery, 5) }

    var typoWord by remember { mutableStateOf("fucntion") }
    val autocorrectResults = remember(typoWord) {
        trie.findAutocorrections(typoWord, maxDistance = 2, limit = 4)
    }

    var liveInput by remember { mutableStateOf("ovver") }
    val hybridResults = remember(liveInput) {
        trie.getSuggestions(liveInput, maxResults = 5)
    }

    // New Word Injection to Room Database
    var newWordInput by remember { mutableStateOf("") }
    var newWordLang by remember { mutableStateOf("en") }

    // Dictionary Room Search
    var dbSearchQuery by remember { mutableStateOf("") }
    val dbSearchResults by repository.searchDictionary(dbSearchQuery)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "🔬 مختبر خوارزميات OmniBoard",
                    color = Color(0xFF00F0FF),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Room Database + Trie Engine • Zero AI • Pure Algorithmic Core",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0284C7).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "قاعدة البيانات: $wordCount كلمة",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 1. Room Database Dictionary Management Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "1. إدارة قاموس Room Database (تخزين محلي دائم)",
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = "يتم حفظ الكلمات في Room مع ترددها الزمني، ويتم مزامنتها آنياً مع شجرة الـ Trie.",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )

                // Add word input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newWordInput,
                        onValueChange = { newWordInput = it },
                        placeholder = { Text("أضف كلمة جديدة للقاموس...", fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (newWordInput.isNotBlank()) {
                                val word = newWordInput.trim()
                                scope.launch {
                                    repository.insertWord(word, frequency = 60, language = newWordLang, isCustom = true)
                                    Toast.makeText(context, "تمت إضافة '$word' إلى Room و Trie!", Toast.LENGTH_SHORT).show()
                                    newWordInput = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("حفظ", fontSize = 12.sp)
                    }
                }

                // Search DB Words
                OutlinedTextField(
                    value = dbSearchQuery,
                    onValueChange = { dbSearchQuery = it },
                    placeholder = { Text("بحث في كلمات قاعدة البيانات...", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (dbSearchResults.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(dbSearchResults.take(15)) { entity ->
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        text = "${entity.word} [f:${entity.frequency} u:${entity.userFrequency}]",
                                        fontSize = 11.sp
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // 2. Trie Prefix Auto-completion Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DataObject, contentDescription = null, tint = Color(0xFF00F0FF), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "2. خوارزمية إكمال الكلمات (Prefix Trie with Top-K Pruning)",
                        color = Color(0xFF00F0FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                OutlinedTextField(
                    value = trieQuery,
                    onValueChange = { trieQuery = it },
                    label = { Text("أدخل بداية الكلمة (Prefix)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "الاقتراحات المستخرجة من شجرة الـ Trie مرتبة حسب النتيجة الحسابية:",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (trieResults.isEmpty()) {
                        Text("لا توجد كلمات مطابقة للبادئة", color = Color(0xFF64748B), fontSize = 12.sp)
                    } else {
                        trieResults.forEach { res ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text("${res.word} (نقاط: ${res.score})", fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // 3. Trie-based Autocorrection (Damerau-Levenshtein Pruning)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Spellcheck, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "3. خوارزمية التصحيح الإملائي الذاتي (Trie Damerau-Levenshtein)",
                        color = Color(0xFFA855F7),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = "تبحث مباشرة في تفرعات الشجرة مع تقليم الفروع البعيدة مبكراً؛ تعالج التبديل والإضافة والحذف في أجزاء من الميلي ثانية.",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
                OutlinedTextField(
                    value = typoWord,
                    onValueChange = { typoWord = it },
                    label = { Text("أدخل كلمة بها خطأ مطبعي (مثل fucntion أو teh أو improt)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "التصحيحات المقترحة بدون استدعاء أي نموذج ذكاء اصطناعي:",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (autocorrectResults.isEmpty()) {
                        Text("لم يتم العثور على بديل قريب في القاموس", color = Color(0xFF64748B), fontSize = 12.sp)
                    } else {
                        autocorrectResults.forEach { sug ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text("${sug.word} [مسافة: ${sug.editDistance}]", fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // 4. Real-time Hybrid Autocomplete & Autocorrect Pipeline
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "4. مسار التنبؤ الحي المدمج (Hybrid Pipeline + LRU Cache)",
                        color = Color(0xFF34D399),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                OutlinedTextField(
                    value = liveInput,
                    onValueChange = { liveInput = it },
                    label = { Text("جرب كتابة أي كلمة كأنك تكتب بلوحة المفاتيح") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "شريط الاقتراحات الفوري المماثل للوحة المفاتيح:",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (hybridResults.isEmpty()) {
                        Text("اكتب حرفين أو أكثر لتفعيل التنبؤ الحي", color = Color(0xFF64748B), fontSize = 12.sp)
                    } else {
                        hybridResults.forEach { sug ->
                            FilterChip(
                                selected = sug.isAutocorrect,
                                onClick = {},
                                label = {
                                    Text(
                                        text = if (sug.isAutocorrect) "✏️ ${sug.word}" else sug.word,
                                        fontSize = 12.sp
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
