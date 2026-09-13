package com.example.data

import com.example.engine.TrieEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Repository pattern implementation for dictionary data persistence and
 * real-time algorithmic Trie synchronization.
 */
class DictionaryRepository(
    private val dictionaryDao: DictionaryDao,
    private val trieEngine: TrieEngine,
    private val externalScope: CoroutineScope
) {
    val allWords: Flow<List<DictionaryWordEntity>> = dictionaryDao.getAllWordsFlow()
    val wordCount: Flow<Int> = dictionaryDao.getWordCount()

    init {
        // Hydrate Trie from Room Database in background
        externalScope.launch(Dispatchers.IO) {
            val words = dictionaryDao.getAllWords()
            for (w in words) {
                trieEngine.insert(
                    word = w.word,
                    frequency = w.frequency,
                    userFrequency = w.userFrequency,
                    language = w.language
                )
            }
        }
    }

    suspend fun getInitialWords(): List<DictionaryWordEntity> = withContext(Dispatchers.IO) {
        dictionaryDao.getAllWords()
    }

    suspend fun insertWord(
        word: String,
        frequency: Int = 50,
        language: String = "en",
        category: String = "custom",
        isCustom: Boolean = true
    ) = withContext(Dispatchers.IO) {
        val normalized = word.trim().lowercase()
        if (normalized.isNotBlank()) {
            val entity = DictionaryWordEntity(
                word = normalized,
                frequency = frequency,
                language = language,
                category = category,
                userFrequency = 1,
                lastUsedTimestamp = System.currentTimeMillis(),
                isCustom = isCustom
            )
            dictionaryDao.insertWord(entity)
            // Immediately sync into in-memory Trie
            trieEngine.insert(
                word = normalized,
                frequency = frequency,
                userFrequency = 1,
                language = language
            )
        }
    }

    suspend fun recordWordUsage(word: String) = withContext(Dispatchers.IO) {
        val normalized = word.trim().lowercase()
        if (normalized.isNotBlank()) {
            val existing = dictionaryDao.getWord(normalized)
            if (existing != null) {
                dictionaryDao.incrementWordUsage(normalized)
                trieEngine.recordWordUsage(normalized)
            } else {
                // Auto-learn new typed word into Room dictionary
                val entity = DictionaryWordEntity(
                    word = normalized,
                    frequency = 10,
                    language = detectLanguage(normalized),
                    category = "user_learned",
                    userFrequency = 1,
                    lastUsedTimestamp = System.currentTimeMillis(),
                    isCustom = true
                )
                dictionaryDao.insertWord(entity)
                trieEngine.insert(
                    word = normalized,
                    frequency = 10,
                    userFrequency = 1,
                    language = entity.language
                )
            }
        }
    }

    suspend fun deleteWord(word: String) = withContext(Dispatchers.IO) {
        dictionaryDao.deleteWord(word)
        trieEngine.remove(word)
    }

    fun searchDictionary(query: String): Flow<List<DictionaryWordEntity>> {
        return dictionaryDao.searchWords(query)
    }

    private fun detectLanguage(text: String): String {
        val hasArabic = text.any { it in '\u0600'..'\u06FF' }
        return if (hasArabic) "ar" else "en"
    }
}
