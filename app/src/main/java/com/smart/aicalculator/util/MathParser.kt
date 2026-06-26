package com.smart.aicalculator.util

import kotlin.math.*

object MathParser {

    fun evaluate(expression: String): Double {
        // Clean expression for parsing
        val cleaned = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace(" ", "")

        if (cleaned.isEmpty()) return 0.0

        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < cleaned.length) cleaned[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < cleaned.length) throw IllegalArgumentException("Unexpected character: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm()
                    else if (eat('-'.code)) x -= parseTerm()
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x *= parseFactor()
                    else if (eat('/'.code)) {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        x /= divisor
                    } else if (eat('%'.code)) {
                        x *= 0.01
                    } else {
                        // Implicit multiplication: e.g. 2pi, 2(3+4), pi(3)
                        while (ch == ' '.code) nextChar()
                        if (ch == '('.code || ch == 'π'.code || ch == 'e'.code || 
                            (ch >= '0'.code && ch <= '9'.code) || 
                            (ch >= 'a'.code && ch <= 'z'.code) || ch == '√'.code) {
                            x *= parseFactor()
                        } else {
                            return x
                        }
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return +parseFactor()
                if (eat('-'.code)) return -parseFactor()

                var x: Double
                val startPos = this.pos
                if (eat('('.code)) {
                    x = parseExpression()
                    if (!eat(')'.code)) throw IllegalArgumentException("Missing closing parenthesis")
                } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) {
                    while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                    x = cleaned.substring(startPos, this.pos).toDouble()
                } else if ((ch >= 'a'.code && ch <= 'z'.code) || ch == 'π'.code || ch == '√'.code) {
                    while ((ch >= 'a'.code && ch <= 'z'.code) || ch == 'π'.code || ch == '√'.code) nextChar()
                    val func = cleaned.substring(startPos, this.pos)
                    
                    if (func == "pi" || func == "π") {
                        x = PI
                    } else if (func == "e") {
                        x = E
                    } else {
                        val arg = parseFactor()
                        x = when (func) {
                            "sqrt" -> sqrt(arg)
                            "√" -> sqrt(arg)
                            "sin" -> sin(Math.toRadians(arg))
                            "cos" -> cos(Math.toRadians(arg))
                            "tan" -> {
                                // Handle tan(90) undefined case
                                if (abs(arg % 180 - 90) < 1e-9) throw ArithmeticException("Tangent undefined")
                                tan(Math.toRadians(arg))
                            }
                            "log" -> log10(arg)
                            "ln" -> ln(arg)
                            else -> throw IllegalArgumentException("Unknown function: $func")
                        }
                    }
                } else {
                    throw IllegalArgumentException("Unexpected character: " + ch.toChar())
                }

                if (eat('^'.code)) x = x.pow(parseFactor())

                return x
            }
        }.parse()
    }

    // Format helper to display clean decimals without scientific notation if possible
    fun formatResult(value: Double, precision: Int = 6): String {
        if (value.isNaN()) return "NaN"
        if (value.isInfinite()) return "Infinity"
        
        // Remove trailing zeros
        val rounded = (value * 10.0.pow(precision)).roundToLong() / 10.0.pow(precision)
        return if (rounded == rounded.toLong().toDouble()) {
            rounded.toLong().toString()
        } else {
            rounded.toString()
        }
    }
}
