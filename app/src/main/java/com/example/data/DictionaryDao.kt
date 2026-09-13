package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for dictionary storage and retrieval in Room.
 * Supports reactive Flows and optimized queries for prefix search and usage tracking.
 */
@Dao
interface DictionaryDao {

    @Query("SELECT * FROM dictionary_words ORDER BY (user_frequency * 15 + frequency) DESC")
    fun getAllWordsFlow(): Flow<List<DictionaryWordEntity>>

    @Query("SELECT * FROM dictionary_words ORDER BY (user_frequency * 15 + frequency) DESC")
    suspend fun getAllWords(): List<DictionaryWordEntity>

    @Query("SELECT * FROM dictionary_words WHERE language = :language ORDER BY (user_frequency * 15 + frequency) DESC")
    fun getWordsByLanguage(language: String): Flow<List<DictionaryWordEntity>>

    @Query("SELECT * FROM dictionary_words WHERE word LIKE :prefix || '%' ORDER BY (user_frequency * 15 + frequency) DESC LIMIT :limit")
    fun findWordsByPrefixFlow(prefix: String, limit: Int = 20): Flow<List<DictionaryWordEntity>>

    @Query("SELECT * FROM dictionary_words WHERE word LIKE :prefix || '%' ORDER BY (user_frequency * 15 + frequency) DESC LIMIT :limit")
    suspend fun findWordsByPrefix(prefix: String, limit: Int = 20): List<DictionaryWordEntity>

    @Query("SELECT * FROM dictionary_words WHERE word = :word LIMIT 1")
    suspend fun getWord(word: String): DictionaryWordEntity?

    @Query("SELECT COUNT(*) FROM dictionary_words")
    fun getWordCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM dictionary_words")
    suspend fun getWordCountStatic(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: DictionaryWordEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWords(words: List<DictionaryWordEntity>)

    @Update
    suspend fun updateWord(word: DictionaryWordEntity)

    @Query("UPDATE dictionary_words SET user_frequency = user_frequency + 1, last_used_timestamp = :timestamp WHERE word = :word")
    suspend fun incrementWordUsage(word: String, timestamp: Long = System.currentTimeMillis()): Int

    @Query("DELETE FROM dictionary_words WHERE word = :word")
    suspend fun deleteWord(word: String): Int

    @Query("DELETE FROM dictionary_words WHERE is_custom = 1")
    suspend fun clearCustomWords(): Int

    @Query("SELECT * FROM dictionary_words WHERE word LIKE '%' || :query || '%' ORDER BY (user_frequency * 15 + frequency) DESC LIMIT 50")
    fun searchWords(query: String): Flow<List<DictionaryWordEntity>>
}
