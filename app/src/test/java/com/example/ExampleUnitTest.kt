package com.example

import com.example.engine.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testCornerSwipeEngine() {
        val swipeTL = CornerSwipeEngine.detectDirection(100f, 100f, 50f, 50f)
        assertEquals(KeySwipeDirection.TOP_LEFT, swipeTL)

        val swipeTR = CornerSwipeEngine.detectDirection(100f, 100f, 150f, 50f)
        assertEquals(KeySwipeDirection.TOP_RIGHT, swipeTR)

        val swipeNone = CornerSwipeEngine.detectDirection(100f, 100f, 105f, 105f)
        assertEquals(KeySwipeDirection.NONE, swipeNone)
    }

    @Test
    fun testMathExpressionEngine() {
        val math1 = MathExpressionEngine.evaluate("24*5+12")
        assertNotNull(math1)
        assertEquals("132", math1?.formattedDecimal)

        val mathHex = MathExpressionEngine.evaluate("0xFF")
        assertNotNull(mathHex)
        assertEquals("255", mathHex?.formattedDecimal)
    }

    @Test
    fun testTextTransformEngine() {
        val snake = TextTransformEngine.convertCase("helloWorld", CaseStyle.SNAKE)
        assertEquals("hello_world", snake)

        val camel = TextTransformEngine.convertCase("hello_world", CaseStyle.CAMEL)
        assertEquals("helloWorld", camel)

        val pascal = TextTransformEngine.convertCase("hello_world", CaseStyle.PASCAL)
        assertEquals("HelloWorld", pascal)

        val closing = TextTransformEngine.getMatchingClosingPair('{')
        assertEquals('}', closing)
    }

    @Test
    fun testTrieEngine() {
        val trie = TrieEngine()
        trie.insert("function", 10)
        trie.insert("fun", 5)
        trie.insert("future", 8)

        val results = trie.searchPrefix("fu")
        assertTrue(results.isNotEmpty())
        assertEquals("function", results[0].word)
    }

    @Test
    fun testTrieAutocorrectionDamerauLevenshtein() {
        val trie = TrieEngine()
        trie.insert("function", 100)
        trie.insert("override", 95)
        trie.insert("import", 90)
        trie.insert("return", 95)

        // Transposition test ("fucntion" -> "function")
        val typoTransposition = trie.findAutocorrections("fucntion", maxDistance = 2)
        assertTrue(typoTransposition.isNotEmpty())
        assertEquals("function", typoTransposition[0].word)
        assertEquals(1, typoTransposition[0].editDistance)

        // Insertion / typo test ("improt" -> "import")
        val typoImport = trie.findAutocorrections("improt", maxDistance = 2)
        assertTrue(typoImport.isNotEmpty())
        assertEquals("import", typoImport[0].word)

        // Hybrid pipeline test
        val hybrid = trie.getSuggestions("fucntion")
        assertTrue(hybrid.isNotEmpty())
        assertEquals("function", hybrid[0].word)
        assertTrue(hybrid[0].isAutocorrect)
    }

    @Test
    fun testTrieWordUsageLearning() {
        val trie = TrieEngine()
        trie.insert("apple", 10)
        trie.insert("application", 10)

        // Initially apple and application have same base frequency
        trie.recordWordUsage("application")
        trie.recordWordUsage("application")

        val results = trie.searchPrefix("app")
        assertEquals("application", results[0].word)
    }
}
