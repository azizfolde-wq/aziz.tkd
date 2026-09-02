package com.example.ui.screens.calculator

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

class MathEvaluator(private val isDegreeMode: Boolean = true) {

    private val decimalFormat = DecimalFormat("#,##0.########", DecimalFormatSymbols(Locale.US)).apply {
        isGroupingUsed = false
    }

    sealed class EvaluationResult {
        data class Success(val value: Double, val formattedText: String) : EvaluationResult()
        data class Error(val messageKurdish: String) : EvaluationResult()
    }

    fun evaluate(expression: String): EvaluationResult {
        if (expression.isBlank()) return EvaluationResult.Error("تکایە هاوکێشەیەک بنووسە")

        try {
            // Clean and sanitize string
            var sanitized = expression
                .replace("×", "*")
                .replace("÷", "/")
                .replace("π", Math.PI.toString())
                .replace("e", Math.E.toString())
                .replace("√", "sqrt")
                .replace(" ", "")

            val result = Parser(sanitized, isDegreeMode).parse()
            if (result.isNaN() || result.isInfinite()) {
                return EvaluationResult.Error("ئەنجام دیاری نەکراوە (دابەشکردن بەسەر سفر)")
            }

            // Format nicely
            val formatted = if (result % 1.0 == 0.0 && abs(result) < 1e12) {
                result.toLong().toString()
            } else {
                decimalFormat.format(result)
            }

            return EvaluationResult.Success(result, formatted)
        } catch (e: Exception) {
            return EvaluationResult.Error("هاوکێشەکە هەڵەیە")
        }
    }

    private class Parser(private val src: String, private val isDegree: Boolean) {
        private var pos = -1
        private var ch = ' '

        private fun nextChar() {
            ch = if (++pos < src.length) src[pos] else '\u0000'
        }

        private fun eat(charToEat: Char): Boolean {
            while (ch == ' ') nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < src.length) throw RuntimeException("Unexpected: $ch")
            return x
        }

        // Expression = Term (+ | - Term)*
        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+') -> x += parseTerm()
                    eat('-') -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        // Term = Factor (* | / | % Factor)*
        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*') -> x *= parseFactor()
                    eat('/') -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        x /= divisor
                    }
                    eat('%') -> {
                        val mod = parseFactor()
                        x %= mod
                    }
                    else -> return x
                }
            }
        }

        // Factor = Unary (+ | -) Factor | Primary (^ Factor)?
        private fun parseFactor(): Double {
            if (eat('+')) return +parseFactor()
            if (eat('-')) return -parseFactor()

            var x: Double
            val startPos = pos

            if (eat('(')) {
                x = parseExpression()
                eat(')')
            } else if ((ch in '0'..'9') || ch == '.') {
                while ((ch in '0'..'9') || ch == '.') nextChar()
                val numStr = src.substring(startPos, pos)
                x = numStr.toDouble()
            } else if (ch in 'a'..'z') {
                while (ch in 'a'..'z') nextChar()
                val func = src.substring(startPos, pos)
                val arg = parseFactor()
                x = when (func) {
                    "sqrt" -> {
                        if (arg < 0) throw RuntimeException("Square root of negative")
                        sqrt(arg)
                    }
                    "sin" -> {
                        val angle = if (isDegree) Math.toRadians(arg) else arg
                        sin(angle)
                    }
                    "cos" -> {
                        val angle = if (isDegree) Math.toRadians(arg) else arg
                        cos(angle)
                    }
                    "tan" -> {
                        val angle = if (isDegree) Math.toRadians(arg) else arg
                        tan(angle)
                    }
                    "log" -> {
                        if (arg <= 0) throw RuntimeException("Log of non-positive")
                        log10(arg)
                    }
                    "ln" -> {
                        if (arg <= 0) throw RuntimeException("Ln of non-positive")
                        ln(arg)
                    }
                    "abs" -> abs(arg)
                    else -> throw RuntimeException("Unknown function: $func")
                }
            } else {
                throw RuntimeException("Unexpected: $ch")
            }

            if (eat('^')) {
                x = x.pow(parseFactor())
            }

            // Check for factorial !
            if (eat('!')) {
                x = factorial(x)
            }

            return x
        }

        private fun factorial(n: Double): Double {
            if (n < 0 || n != floor(n)) throw RuntimeException("Invalid factorial")
            var res = 1.0
            for (i in 2..n.toInt()) {
                res *= i
            }
            return res
        }
    }
}
