package com.smart.aicalculator.solver

import java.util.regex.Pattern
import kotlin.math.roundToInt

data class SolveResult(
    val query: String,
    val finalAnswer: String,
    val steps: List<String>,
    val source: String = "AI Solve"
)

object LocalSolver {

    private fun format(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format("%.2f", value)
        }
    }

    fun solve(query: String): SolveResult? {
        val clean = query.trim().lowercase()

        // 1. GST / VAT Calculator: e.g. "18% GST on ₹2500"
        val gstPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*%\\s*(?:gst|vat)\\s*(?:on|of)?\\s*(?:₹|\\$|rs|inr)?\\s*(\\d+(?:\\.\\d+)?)")
        val gstMatcher = gstPattern.matcher(clean)
        if (gstMatcher.find()) {
            val percentage = gstMatcher.group(1)!!.toDouble()
            val amount = gstMatcher.group(2)!!.toDouble()
            val gstAmount = amount * (percentage / 100.0)
            val total = amount + gstAmount

            return SolveResult(
                query = query,
                finalAnswer = "Total: ₹${format(total)} (GST: ₹${format(gstAmount)})",
                steps = listOf(
                    "Identify the base amount: ₹${format(amount)} and the GST rate: ${format(percentage)}%",
                    "Calculate the GST tax amount: ₹${format(amount)} × (${format(percentage)} / 100) = ₹${format(gstAmount)}",
                    "Add the GST tax amount to the base amount: ₹${format(amount)} + ₹${format(gstAmount)} = ₹${format(total)}"
                )
            )
        }

        // 2. Discount Calculator: e.g. "10% discount on 1999" or "10% off on 1999"
        val discountPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*%\\s*(?:discount|off)\\s*(?:on|of)?\\s*(?:₹|\\$|rs|inr)?\\s*(\\d+(?:\\.\\d+)?)")
        val discountMatcher = discountPattern.matcher(clean)
        if (discountMatcher.find()) {
            val percentage = discountMatcher.group(1)!!.toDouble()
            val amount = discountMatcher.group(2)!!.toDouble()
            val discountAmount = amount * (percentage / 100.0)
            val finalPrice = amount - discountAmount

            return SolveResult(
                query = query,
                finalAnswer = "Final Price: ₹${format(finalPrice)} (Saved: ₹${format(discountAmount)})",
                steps = listOf(
                    "Identify the original price: ₹${format(amount)} and the discount rate: ${format(percentage)}%",
                    "Calculate the savings (discount amount): ₹${format(amount)} × (${format(percentage)} / 100) = ₹${format(discountAmount)}",
                    "Subtract the savings from the original price: ₹${format(amount)} - ₹${format(discountAmount)} = ₹${format(finalPrice)}"
                )
            )
        }

        // 3. Bill Split: e.g. "Split ₹2400 between 4 people"
        val splitPattern = Pattern.compile("(?:split|divide)\\s*(?:₹|\\$|rs|inr)?\\s*(\\d+(?:\\.\\d+)?)\\s*(?:between|among|for)?\\s*(\\d+)\\s*(?:people|persons|friends|guests)?")
        val splitMatcher = splitPattern.matcher(clean)
        if (splitMatcher.find()) {
            val amount = splitMatcher.group(1)!!.toDouble()
            val people = splitMatcher.group(2)!!.toInt()
            if (people <= 0) return null
            val perPerson = amount / people

            return SolveResult(
                query = query,
                finalAnswer = "₹${format(perPerson)} per person",
                steps = listOf(
                    "Identify the total bill amount: ₹${format(amount)} and the number of people: $people",
                    "Divide the total bill by the number of people: ₹${format(amount)} ÷ $people = ₹${format(perPerson)} per person"
                )
            )
        }

        // 4. Percentage: e.g. "15% of 3500"
        val percentPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*%\\s*(?:of)\\s*(?:₹|\\$|rs|inr)?\\s*(\\d+(?:\\.\\d+)?)")
        val percentMatcher = percentPattern.matcher(clean)
        if (percentMatcher.find()) {
            val percentage = percentMatcher.group(1)!!.toDouble()
            val amount = percentMatcher.group(2)!!.toDouble()
            val result = amount * (percentage / 100.0)

            return SolveResult(
                query = query,
                finalAnswer = format(result),
                steps = listOf(
                    "Convert the percentage to a decimal fraction: ${format(percentage)}% = ${format(percentage)} / 100 = ${format(percentage / 100.0)}",
                    "Multiply the fraction by the amount: ${format(percentage / 100.0)} × ${format(amount)} = ${format(result)}"
                )
            )
        }

        // 5. Simple Algebraic Equation: e.g. "Solve 2x + 5 = 15" or "Solve 2x - 5 = 15" or "Solve x + 3 = 10"
        val eqPattern = Pattern.compile("(?:solve\\s+)?(\\d*)\\s*([a-zA-Z])\\s*([-+])\\s*(\\d+(?:\\.\\d+)?)\\s*=\\s*(\\d+(?:\\.\\d+)?)")
        val eqMatcher = eqPattern.matcher(clean)
        if (eqMatcher.find()) {
            val coeffStr = eqMatcher.group(1)
            val variableName = eqMatcher.group(2)!!
            val operator = eqMatcher.group(3)!!
            val b = eqMatcher.group(4)!!.toDouble()
            val c = eqMatcher.group(5)!!.toDouble()

            val coeff = if (coeffStr.isNullOrEmpty()) 1.0 else coeffStr.toDouble()
            val rhs = if (operator == "+") c - b else c + b
            val x = rhs / coeff

            val step1 = "Given equation: ${if (coeff == 1.0) "" else format(coeff)}$variableName $operator ${format(b)} = ${format(c)}"
            val step2 = if (operator == "+") {
                "Subtract ${format(b)} from both sides: ${if (coeff == 1.0) "" else format(coeff)}$variableName = ${format(c)} - ${format(b)} = ${format(rhs)}"
            } else {
                "Add ${format(b)} to both sides: ${if (coeff == 1.0) "" else format(coeff)}$variableName = ${format(c)} + ${format(b)} = ${format(rhs)}"
            }
            val step3 = if (coeff != 1.0) {
                "Divide both sides by ${format(coeff)}: $variableName = ${format(rhs)} ÷ ${format(coeff)} = ${format(x)}"
            } else {
                "Result: $variableName = ${format(x)}"
            }

            return SolveResult(
                query = query,
                finalAnswer = "$variableName = ${format(x)}",
                steps = listOf(step1, step2, step3)
            )
        }

        return null
    }
}
