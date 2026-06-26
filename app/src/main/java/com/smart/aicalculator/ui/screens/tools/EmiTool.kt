package com.smart.aicalculator.ui.screens.tools

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smart.aicalculator.data.db.AppDatabase
import com.smart.aicalculator.data.db.HistoryEntity
import com.smart.aicalculator.ui.components.CoralButton
import com.smart.aicalculator.ui.components.SecondaryButton
import com.smart.aicalculator.ui.components.SmartCard
import com.smart.aicalculator.ui.theme.UrbanistFontFamily
import kotlinx.coroutines.launch
import kotlin.math.pow

@Composable
fun EmiTool(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context.applicationContext) }
    val historyDao = db.historyDao()

    var principalStr by rememberSaveable { mutableStateOf("") }
    var interestRateStr by rememberSaveable { mutableStateOf("") }
    var tenureStr by rememberSaveable { mutableStateOf("") }
    var isTenureYears by rememberSaveable { mutableStateOf(true) }

    var calculated by rememberSaveable { mutableStateOf(false) }
    var monthlyEmi by rememberSaveable { mutableStateOf(0.0) }
    var totalInterest by rememberSaveable { mutableStateOf(0.0) }
    var totalPayable by rememberSaveable { mutableStateOf(0.0) }

    fun format(value: Double): String {
        return if (value.isNaN() || value.isInfinite()) {
            "0"
        } else if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format("%.2f", value)
        }
    }

    fun calculate() {
        val p = principalStr.toDoubleOrNull()
        val rAnn = interestRateStr.toDoubleOrNull()
        val tVal = tenureStr.toDoubleOrNull()

        if (p == null || p <= 0) {
            Toast.makeText(context, "Please enter a valid loan amount", Toast.LENGTH_SHORT).show()
            return
        }
        if (rAnn == null || rAnn <= 0) {
            Toast.makeText(context, "Please enter a valid interest rate", Toast.LENGTH_SHORT).show()
            return
        }
        if (tVal == null || tVal <= 0) {
            Toast.makeText(context, "Please enter a valid tenure", Toast.LENGTH_SHORT).show()
            return
        }

        val r = rAnn / 12.0 / 100.0
        val n = if (isTenureYears) (tVal * 12.0).toInt() else tVal.toInt()

        if (r == 0.0) {
            monthlyEmi = p / n
            totalPayable = p
            totalInterest = 0.0
        } else {
            val emiVal = (p * r * (1.0 + r).pow(n)) / ((1.0 + r).pow(n) - 1.0)
            monthlyEmi = emiVal
            totalPayable = emiVal * n
            totalInterest = totalPayable - p
        }
        calculated = true

        coroutineScope.launch {
            val expr = "EMI Loan ₹${format(p)} at ${format(rAnn)}% for ${format(tVal)} ${if (isTenureYears) "Years" else "Months"}"
            val res = "EMI: ₹${format(monthlyEmi)} (Total Interest: ₹${format(totalInterest)})"
            historyDao.insertHistory(
                HistoryEntity(
                    expression = expr,
                    result = res,
                    timestamp = System.currentTimeMillis(),
                    source = "Tool"
                )
            )
        }
    }

    fun reset() {
        principalStr = ""
        interestRateStr = ""
        tenureStr = ""
        isTenureYears = true
        calculated = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SmartCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Loan Amount
                OutlinedTextField(
                    value = principalStr,
                    onValueChange = { principalStr = it },
                    label = { Text("Loan Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Interest Rate
                OutlinedTextField(
                    value = interestRateStr,
                    onValueChange = { interestRateStr = it },
                    label = { Text("Annual Interest Rate (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Tenure & Unit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = tenureStr,
                        onValueChange = { tenureStr = it },
                        label = { Text("Tenure") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Years/Months switch
                    AppSegmentedControl(
                        items = listOf("Years", "Months"),
                        selectedIndex = if (isTenureYears) 0 else 1,
                        onSelectedChange = { isTenureYears = it == 0 },
                        modifier = Modifier.weight(1.5f),
                        height = 56.dp
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryButton(
                        text = "Reset",
                        onClick = { reset() },
                        modifier = Modifier.weight(1f)
                    )
                    CoralButton(
                        text = "Calculate",
                        onClick = { calculate() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Result Card
        if (calculated) {
            ToolResultCard(
                title = "EMI Details",
                onCopy = {
                    val text = "Monthly EMI: ₹${format(monthlyEmi)}\nTotal Interest: ₹${format(totalInterest)}\nTotal Payable: ₹${format(totalPayable)}"
                    clipboardManager.setText(AnnotatedString(text))
                    Toast.makeText(context, "Copied EMI breakdown", Toast.LENGTH_SHORT).show()
                }
            ) {
                ResultHeadline("₹${format(monthlyEmi)} / mo")
                ResultRow("Loan Principal", "₹${format(principalStr.toDoubleOrNull() ?: 0.0)}")
                ResultRow("Total Interest Payable", "₹${format(totalInterest)}")
                ResultRow("Total Payable (Principal + Interest)", "₹${format(totalPayable)}")
                ResultDivider()
                ResultRow(
                    label = "Monthly EMI",
                    value = "₹${format(monthlyEmi)} / mo",
                    emphasized = true
                )
            }
        }
    }
}
