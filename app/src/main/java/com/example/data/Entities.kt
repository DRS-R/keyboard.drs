package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "snippets")
data class SnippetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trigger: String,
    val title: String,
    val content: String,
    val category: String,
    val usageCount: Int = 0,
    val isPinned: Boolean = false
)

@Entity(tableName = "clipboard_history")
data class ClipboardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val isCode: Boolean = false
)

@Entity(tableName = "user_words")
data class WordEntity(
    @PrimaryKey val word: String,
    val frequency: Int = 1,
    val language: String = "en"
)

@Entity(tableName = "macros")
data class MacroEntity(
    @PrimaryKey val trigger: String,
    val expansion: String
)
