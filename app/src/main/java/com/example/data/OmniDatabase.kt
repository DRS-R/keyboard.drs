package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SnippetEntity::class,
        ClipboardEntity::class,
        WordEntity::class,
        MacroEntity::class,
        DictionaryWordEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class OmniDatabase : RoomDatabase() {
    abstract fun omniDao(): OmniDao
    abstract fun dictionaryDao(): DictionaryDao

    companion object {
        @Volatile
        private var INSTANCE: OmniDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): OmniDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OmniDatabase::class.java,
                    "omniboard_database"
                )
                    .addCallback(OmniDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class OmniDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.omniDao(), database.dictionaryDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: OmniDao, dictDao: DictionaryDao) {
                // Prepopulate programmer snippets
                val defaultSnippets = listOf(
                    SnippetEntity(
                        trigger = "!fun",
                        title = "Kotlin Function",
                        content = "fun myMethod(arg: String): Boolean {\n    return true\n}",
                        category = "Kotlin",
                        isPinned = true
                    ),
                    SnippetEntity(
                        trigger = "!log",
                        title = "Android Log",
                        content = "Log.d(\"OmniBoard\", \"Value: \$it\")",
                        category = "Android",
                        isPinned = true
                    ),
                    SnippetEntity(
                        trigger = "!for",
                        title = "Loop statement",
                        content = "for (item in items) {\n    println(item)\n}",
                        category = "General"
                    ),
                    SnippetEntity(
                        trigger = "!git",
                        title = "Git Commit & Push",
                        content = "git add . && git commit -m \"Update\" && git push origin main",
                        category = "Git",
                        isPinned = true
                    ),
                    SnippetEntity(
                        trigger = "!sql",
                        title = "SQL Select Query",
                        content = "SELECT * FROM users WHERE status = 'active' ORDER BY created_at DESC;",
                        category = "SQL"
                    ),
                    SnippetEntity(
                        trigger = "!arrow",
                        title = "Arrow Function (JS)",
                        content = "const handleAction = (event) => {\n  console.log(event);\n};",
                        category = "JavaScript"
                    ),
                    SnippetEntity(
                        trigger = "!box",
                        title = "Compose Box/Column",
                        content = "Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {\n    \n}",
                        category = "Compose",
                        isPinned = true
                    ),
                    SnippetEntity(
                        trigger = "!py",
                        title = "Python Def",
                        content = "def solve(data: list) -> dict:\n    return {k: v for k, v in data}",
                        category = "Python"
                    ),
                    SnippetEntity(
                        trigger = "!brk",
                        title = "JSON Block",
                        content = "{\n  \"status\": \"success\",\n  \"code\": 200\n}",
                        category = "JSON"
                    )
                )

                for (s in defaultSnippets) {
                    dao.insertSnippet(s)
                }

                // Initial clipboard history samples
                val defaultClips = listOf(
                    ClipboardEntity(
                        content = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC3... user@dev",
                        isPinned = true,
                        isCode = true
                    ),
                    ClipboardEntity(
                        content = "docker compose up -d --build",
                        isPinned = true,
                        isCode = true
                    ),
                    ClipboardEntity(
                        content = "https://github.com/torvalds/linux",
                        isPinned = false,
                        isCode = false
                    ),
                    ClipboardEntity(
                        content = "مرحباً بك في لوحة مفاتيح OmniBoard المتقدمة للمبرمجين!",
                        isPinned = true,
                        isCode = false
                    )
                )

                for (c in defaultClips) {
                    dao.insertClipboard(c)
                }

                // Initial user dictionary
                val defaultWords = listOf(
                    WordEntity("function", 50, "en"),
                    WordEntity("val", 45, "en"),
                    WordEntity("var", 40, "en"),
                    WordEntity("return", 42, "en"),
                    WordEntity("import", 38, "en"),
                    WordEntity("class", 35, "en"),
                    WordEntity("override", 30, "en"),
                    WordEntity("private", 28, "en"),
                    WordEntity("public", 28, "en"),
                    WordEntity("suspend", 26, "en"),
                    WordEntity("interface", 24, "en"),
                    WordEntity("السلام", 60, "ar"),
                    WordEntity("عليكم", 58, "ar"),
                    WordEntity("ورحمة", 55, "ar"),
                    WordEntity("الله", 65, "ar"),
                    WordEntity("وبركاته", 50, "ar"),
                    WordEntity("شكرا", 45, "ar"),
                    WordEntity("مبرمج", 40, "ar"),
                    WordEntity("تطبيق", 38, "ar"),
                    WordEntity("برمجة", 36, "ar"),
                    WordEntity("لوحة", 34, "ar"),
                    WordEntity("مفاتيح", 32, "ar")
                )

                for (w in defaultWords) {
                    dao.insertOrUpdateWord(w)
                }

                // Prepopulate comprehensive Room dictionary schema
                val initialDictionary = listOf(
                    // Dev / code words
                    DictionaryWordEntity("function", 100, "code", "keyword"),
                    DictionaryWordEntity("return", 98, "code", "keyword"),
                    DictionaryWordEntity("override", 92, "code", "keyword"),
                    DictionaryWordEntity("val", 96, "code", "keyword"),
                    DictionaryWordEntity("var", 94, "code", "keyword"),
                    DictionaryWordEntity("private", 90, "code", "keyword"),
                    DictionaryWordEntity("public", 90, "code", "keyword"),
                    DictionaryWordEntity("interface", 85, "code", "keyword"),
                    DictionaryWordEntity("class", 96, "code", "keyword"),
                    DictionaryWordEntity("suspend", 88, "code", "keyword"),
                    DictionaryWordEntity("coroutines", 82, "code", "keyword"),
                    DictionaryWordEntity("composable", 94, "code", "keyword"),
                    DictionaryWordEntity("modifier", 96, "code", "keyword"),
                    DictionaryWordEntity("string", 94, "code", "type"),
                    DictionaryWordEntity("boolean", 90, "code", "type"),
                    DictionaryWordEntity("integer", 85, "code", "type"),
                    DictionaryWordEntity("import", 98, "code", "keyword"),
                    DictionaryWordEntity("package", 94, "code", "keyword"),
                    DictionaryWordEntity("commit", 92, "code", "git"),
                    DictionaryWordEntity("branch", 88, "code", "git"),
                    DictionaryWordEntity("terminal", 85, "code", "tool"),
                    DictionaryWordEntity("database", 90, "code", "room"),
                    DictionaryWordEntity("schema", 88, "code", "room"),
                    DictionaryWordEntity("entity", 88, "code", "room"),

                    // English general words
                    DictionaryWordEntity("the", 100, "en", "common"),
                    DictionaryWordEntity("and", 95, "en", "common"),
                    DictionaryWordEntity("that", 90, "en", "common"),
                    DictionaryWordEntity("have", 88, "en", "common"),
                    DictionaryWordEntity("with", 88, "en", "common"),
                    DictionaryWordEntity("this", 90, "en", "common"),
                    DictionaryWordEntity("from", 85, "en", "common"),
                    DictionaryWordEntity("they", 85, "en", "common"),
                    DictionaryWordEntity("will", 90, "en", "common"),
                    DictionaryWordEntity("there", 85, "en", "common"),
                    DictionaryWordEntity("what", 90, "en", "common"),
                    DictionaryWordEntity("about", 85, "en", "common"),
                    DictionaryWordEntity("which", 85, "en", "common"),
                    DictionaryWordEntity("hello", 90, "en", "greeting"),
                    DictionaryWordEntity("welcome", 85, "en", "greeting"),
                    DictionaryWordEntity("thanks", 90, "en", "greeting"),
                    DictionaryWordEntity("please", 85, "en", "greeting"),

                    // Arabic words
                    DictionaryWordEntity("السلام", 100, "ar", "greeting"),
                    DictionaryWordEntity("عليكم", 98, "ar", "greeting"),
                    DictionaryWordEntity("ورحمة", 95, "ar", "greeting"),
                    DictionaryWordEntity("الله", 100, "ar", "general"),
                    DictionaryWordEntity("وبركاته", 95, "ar", "greeting"),
                    DictionaryWordEntity("شكرا", 98, "ar", "greeting"),
                    DictionaryWordEntity("جزيلا", 88, "ar", "general"),
                    DictionaryWordEntity("مرحبا", 92, "ar", "greeting"),
                    DictionaryWordEntity("أهلا", 88, "ar", "greeting"),
                    DictionaryWordEntity("وسهلا", 88, "ar", "greeting"),
                    DictionaryWordEntity("الحمد", 98, "ar", "general"),
                    DictionaryWordEntity("لله", 98, "ar", "general"),
                    DictionaryWordEntity("صباح", 88, "ar", "greeting"),
                    DictionaryWordEntity("الخير", 88, "ar", "greeting"),
                    DictionaryWordEntity("مساء", 88, "ar", "greeting"),
                    DictionaryWordEntity("النور", 88, "ar", "greeting"),
                    DictionaryWordEntity("تطبيق", 90, "ar", "dev"),
                    DictionaryWordEntity("برمجة", 90, "ar", "dev"),
                    DictionaryWordEntity("كود", 90, "ar", "dev"),
                    DictionaryWordEntity("مبرمج", 90, "ar", "dev"),
                    DictionaryWordEntity("مشروع", 88, "ar", "dev"),
                    DictionaryWordEntity("خوارزمية", 88, "ar", "dev"),
                    DictionaryWordEntity("لوحة", 90, "ar", "dev"),
                    DictionaryWordEntity("مفاتيح", 90, "ar", "dev"),
                    DictionaryWordEntity("بيانات", 85, "ar", "dev")
                )

                dictDao.insertWords(initialDictionary)
            }
        }
    }
}
