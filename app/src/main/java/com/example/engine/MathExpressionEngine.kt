package com.example.engine

import kotlin.math.*

data class MathResult(
    val expression: String,
    val decimal: Double,
    val formattedDecimal: String,
    val hex: String?,
    val binary: String?
)

object MathExpressionEngine {

    private val MATH_PATTERN = Regex("""^[\s\d\+\-\*\/\%\^\(\)\.\&\|\<\>xXbBabcdefABCDEFoO]+$""")

    fun evaluate(input: String): MathResult? {
        val trimmed = input.trim()
        if (trimmed.length < 2) return null

        // Check if string contains at least one operator or base prefix
        val hasOperator = trimmed.any { it in "+-*/%^&|<>" }
        val hasBase = trimmed.startsWith("0x", true) || trimmed.startsWith("0b", true) || trimmed.startsWith("0o", true)
        if (!hasOperator && !hasBase) return null

        // Quick syntax filter
        if (!trimmed.matches(MATH_PATTERN) && !trimmed.startsWith("sqrt", true)) {
            return null
        }

        return try {
            val eval = ExpressionParser(trimmed).parse()
            val formatted = if (eval % 1.0 == 0.0) {
                eval.toLong().toString()
            } else {
                String.format("%.4f", eval).trimEnd('0').trimEnd('.')
            }

            val longVal = eval.toLong()
            val hexStr = if (eval == longVal.toDouble() && longVal >= 0) "0x" + java.lang.Long.toHexString(longVal).uppercase() else null
            val binStr = if (eval == longVal.toDouble() && longVal in 0..65535) "0b" + java.lang.Long.toBinaryString(longVal) else null

            MathResult(
                expression = trimmed,
                decimal = eval,
                formattedDecimal = formatted,
                hex = hexStr,
                binary = binStr
            )
        } catch (_: Exception) {
            null
        }
    }

    private class ExpressionParser(val str: String) {
        var pos = -1
        var ch = ' '

        fun nextChar() {
            pos++
            ch = if (pos < str.length) str[pos] else '\u0000'
        }

        fun eat(charToEat: Char): Boolean {
            while (ch == ' ') nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseBitwiseOr()
            if (pos < str.length) throw RuntimeException("Unexpected: $ch")
            return x
        }

        fun parseBitwiseOr(): Double {
            var x = parseBitwiseAnd()
            while (true) {
                x = when {
                    eat('|') -> (x.toLong() or parseBitwiseAnd().toLong()).toDouble()
                    else -> return x
                }
            }
        }

        fun parseBitwiseAnd(): Double {
            var x = parseShift()
            while (true) {
                x = when {
                    eat('&') -> (x.toLong() and parseShift().toLong()).toDouble()
                    else -> return x
                }
            }
        }

        fun parseShift(): Double {
            var x = parseExpression()
            while (true) {
                x = when {
                    eat('<') && eat('<') -> (x.toLong() shl parseExpression().toInt()).toDouble()
                    eat('>') && eat('>') -> (x.toLong() shr parseExpression().toInt()).toDouble()
                    else -> return x
                }
            }
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                x = when {
                    eat('+') -> x + parseTerm()
                    eat('-') -> x - parseTerm()
                    else -> return x
                }
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                x = when {
                    eat('*') || eat('x') || eat('X') -> x * parseFactor()
                    eat('/') -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        x / divisor
                    }
                    eat('%') -> x % parseFactor()
                    else -> return x
                }
            }
        }

        fun parseFactor(): Double {
            if (eat('+')) return parseFactor()
            if (eat('-')) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('(')) {
                x = parseBitwiseOr()
                eat(')')
            } else if ((ch in '0'..'9') || ch == '.') {
                // Check if hex, bin, or standard number
                if (ch == '0' && pos + 1 < str.length && (str[pos + 1] == 'x' || str[pos + 1] == 'X')) {
                    nextChar() // 0
                    nextChar() // x
                    val hexStart = pos
                    while ((ch in '0'..'9') || (ch in 'a'..'f') || (ch in 'A'..'F')) nextChar()
                    val hexStr = str.substring(hexStart, pos)
                    x = hexStr.toLong(16).toDouble()
                } else if (ch == '0' && pos + 1 < str.length && (str[pos + 1] == 'b' || str[pos + 1] == 'B')) {
                    nextChar() // 0
                    nextChar() // b
                    val binStart = pos
                    while (ch == '0' || ch == '1') nextChar()
                    val binStr = str.substring(binStart, pos)
                    x = binStr.toLong(2).toDouble()
                } else {
                    while ((ch in '0'..'9') || ch == '.') nextChar()
                    x = str.substring(startPos, pos).toDouble()
                }
            } else if (ch in 'a'..'z' || ch in 'A'..'Z') {
                while (ch in 'a'..'z' || ch in 'A'..'Z') nextChar()
                val func = str.substring(startPos, pos).lowercase()
                x = parseFactor()
                x = when (func) {
                    "sqrt" -> sqrt(x)
                    "abs" -> abs(x)
                    "sin" -> sin(Math.toRadians(x))
                    "cos" -> cos(Math.toRadians(x))
                    else -> throw RuntimeException("Unknown function: $func")
                }
            } else {
                throw RuntimeException("Unexpected: $ch")
            }

            if (eat('^')) x = x.pow(parseFactor())

            return x
        }
    }
}
