package com.example.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity representing a word in the offline algorithmic dictionary.
 * Indexed by language and frequency for ultra-fast query and pre-loading.
 */
@Entity(
    tableName = "dictionary_words",
    indices = [
        Index(value = ["language"]),
        Index(value = ["frequency"]),
        Index(value = ["user_frequency"]),
        Index(value = ["category"])
    ]
)
data class DictionaryWordEntity(
    @PrimaryKey
    @ColumnInfo(name = "word")
    val word: String,

    @ColumnInfo(name = "frequency")
    val frequency: Int = 1,

    @ColumnInfo(name = "language")
    val language: String = "en",

    @ColumnInfo(name = "category")
    val category: String = "general",

    @ColumnInfo(name = "user_frequency")
    val userFrequency: Int = 0,

    @ColumnInfo(name = "last_used_timestamp")
    val lastUsedTimestamp: Long = 0L,

    @ColumnInfo(name = "is_custom")
    val isCustom: Boolean = false
)
