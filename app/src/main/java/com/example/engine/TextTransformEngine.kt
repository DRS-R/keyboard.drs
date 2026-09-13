package com.example.engine

enum class CaseStyle(val label: String, val example: String) {
    CAMEL("camelCase", "myVariableName"),
    PASCAL("PascalCase", "MyClassName"),
    SNAKE("snake_case", "my_variable_name"),
    KEBAB("kebab-case", "my-css-class"),
    CONSTANT("SCREAMING_SNAKE", "MY_CONSTANT_VAL"),
    TITLE("Title Case", "My Great Title"),
    LOWER("lowercase", "my variable"),
    UPPER("UPPERCASE", "MY VARIABLE")
}

object TextTransformEngine {

    /**
     * Splits text by words, considering camelCase, snake_case, kebab-case, spaces, etc.
     */
    fun splitWords(input: String): List<String> {
        if (input.isBlank()) return emptyList()
        // Replace punctuation and separators with space
        val cleaned = input
            .replace(Regex("(?<=[a-z])(?=[A-Z])"), " ")
            .replace(Regex("(?<=[A-Z])(?=[A-Z][a-z])"), " ")
            .replace(Regex("[_\\-\\.\\:\\;\\,\\/\\\\]"), " ")
        return cleaned.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    }

    fun convertCase(input: String, targetStyle: CaseStyle): String {
        val words = splitWords(input)
        if (words.isEmpty()) return input

        return when (targetStyle) {
            CaseStyle.CAMEL -> {
                words.mapIndexed { index, word ->
                    if (index == 0) word.lowercase()
                    else word.lowercase().replaceFirstChar { it.uppercase() }
                }.joinToString("")
            }
            CaseStyle.PASCAL -> {
                words.joinToString("") { word ->
                    word.lowercase().replaceFirstChar { it.uppercase() }
                }
            }
            CaseStyle.SNAKE -> {
                words.joinToString("_") { it.lowercase() }
            }
            CaseStyle.KEBAB -> {
                words.joinToString("-") { it.lowercase() }
            }
            CaseStyle.CONSTANT -> {
                words.joinToString("_") { it.uppercase() }
            }
            CaseStyle.TITLE -> {
                words.joinToString(" ") { word ->
                    word.lowercase().replaceFirstChar { it.uppercase() }
                }
            }
            CaseStyle.LOWER -> {
                words.joinToString(" ") { it.lowercase() }
            }
            CaseStyle.UPPER -> {
                words.joinToString(" ") { it.uppercase() }
            }
        }
    }

    /**
     * Returns matching closing character if the input is an opening bracket/quote.
     */
    fun getMatchingClosingPair(char: Char): Char? {
        return when (char) {
            '(' -> ')'
            '[' -> ']'
            '{' -> '}'
            '<' -> '>'
            '"' -> '"'
            '\'' -> '\''
            '`' -> '`'
            else -> null
        }
    }

    /**
     * Checks if two characters form an empty pair.
     */
    fun isMatchingPair(open: Char, close: Char): Boolean {
        return (open == '(' && close == ')') ||
                (open == '[' && close == ']') ||
                (open == '{' && close == '}') ||
                (open == '<' && close == '>') ||
                (open == '"' && close == '"') ||
                (open == '\'' && close == '\'') ||
                (open == '`' && close == '`')
    }
}
