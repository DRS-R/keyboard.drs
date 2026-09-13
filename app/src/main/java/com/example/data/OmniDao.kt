package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OmniDao {
    // Snippets
    @Query("SELECT * FROM snippets ORDER BY isPinned DESC, usageCount DESC, id DESC")
    fun getAllSnippets(): Flow<List<SnippetEntity>>

    @Query("SELECT * FROM snippets WHERE trigger = :trigger LIMIT 1")
    suspend fun getSnippetByTrigger(trigger: String): SnippetEntity?

    @Query("SELECT * FROM snippets WHERE category = :category ORDER BY usageCount DESC")
    fun getSnippetsByCategory(category: String): Flow<List<SnippetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnippet(snippet: SnippetEntity): Long

    @Update
    suspend fun updateSnippet(snippet: SnippetEntity)

    @Delete
    suspend fun deleteSnippet(snippet: SnippetEntity)

    @Query("UPDATE snippets SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementSnippetUsage(id: Long)

    // Clipboard
    @Query("SELECT * FROM clipboard_history ORDER BY isPinned DESC, timestamp DESC LIMIT 50")
    fun getClipboardHistory(): Flow<List<ClipboardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClipboard(item: ClipboardEntity): Long

    @Delete
    suspend fun deleteClipboard(item: ClipboardEntity)

    @Query("DELETE FROM clipboard_history WHERE isPinned = 0")
    suspend fun clearUnpinnedClipboard()

    @Query("UPDATE clipboard_history SET isPinned = NOT isPinned WHERE id = :id")
    suspend fun togglePinClipboard(id: Long)

    // User Dictionary
    @Query("SELECT * FROM user_words WHERE word LIKE :prefix || '%' ORDER BY frequency DESC LIMIT 15")
    suspend fun findWordsByPrefix(prefix: String): List<WordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWord(word: WordEntity)

    @Query("UPDATE user_words SET frequency = frequency + 1 WHERE word = :word")
    suspend fun incrementWordFrequency(word: String): Int

    // Macros
    @Query("SELECT * FROM macros")
    fun getAllMacros(): Flow<List<MacroEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacro(macro: MacroEntity)

    @Delete
    suspend fun deleteMacro(macro: MacroEntity)
}
