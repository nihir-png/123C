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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smart.aicalculator.data.db.AppDatabase
import com.smart.aicalculator.data.db.HistoryEntity
import com.smart.aicalculator.ui.components.CoralButton
import com.smart.aicalculator.ui.components.SecondaryButton
import com.smart.aicalculator.ui.components.SmartCard
import com.smart.aicalculator.ui.theme.UrbanistFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GstTool(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context.applicationContext) }
    val historyDao = db.historyDao()

    var amount by rememberSaveable { mutableStateOf("") }
    var gstRate by rememberSaveable { mutableStateOf("18") }
    var isAddGst by rememberSaveable { mutableStateOf(true) }

    var calculated by rememberSaveable { mutableStateOf(false) }
    var resultBase by rememberSaveable { mutableStateOf(0.0) }
    var resultTax by rememberSaveable { mutableStateOf(0.0) }
    var resultTotal by rememberSaveable { mutableStateOf(0.0) }

    fun format(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format("%.2f", value)
        }
    }

    fun calculate() {
        val amtVal = amount.toDoubleOrNull()
        val rateVal = gstRate.toDoubleOrNull()

        if (amtVal == null || amtVal <= 0) {
            Toast.makeText(context, "Please enter a valid base amount", Toast.LENGTH_SHORT).show()
            return
        }
        if (rateVal == null || rateVal < 0) {
            Toast.makeText(context, "Please enter a valid tax percentage", Toast.LENGTH_SHORT).show()
            return
        }

        if (isAddGst) {
            resultBase = amtVal
            resultTax = amtVal * (rateVal / 100.0)
            resultTotal = amtVal + resultTax
        } else {
            resultTotal = amtVal
            resultBase = amtVal / (1.0 + (rateVal / 100.0))
            resultTax = amtVal - resultBase
        }
        calculated = true

        // Save to database history
        coroutineScope.launch {
            val expr = "GST ${if (isAddGst) "+" else "-"}${format(rateVal)}% on ₹${format(amtVal)}"
            val res = "Total: ₹${format(resultTotal)} (Tax: ₹${format(resultTax)})"
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
        amount = ""
        gstRate = "18"
        isAddGst = true
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
                // Add / Remove Toggle
                AppSegmentedControl(
                    items = listOf("Add GST", "Remove GST"),
                    selectedIndex = if (isAddGst) 0 else 1,
                    onSelectedChange = { isAddGst = it == 0 },
                    modifier = Modifier.fillMaxWidth()
                )

                // Amount Input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Base Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // GST Rate Input
                OutlinedTextField(
                    value = gstRate,
                    onValueChange = { gstRate = it },
                    label = { Text("GST Rate (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Quick rate chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("5", "12", "18", "28").forEach { rate ->
                        val isSelected = gstRate == rate
                        FilterChip(
                            selected = isSelected,
                            onClick = { gstRate = rate },
                            label = { Text("$rate%") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
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
                title = "Result Breakdown",
                onCopy = {
                    val text = "Base: ₹${format(resultBase)}\nTax: ₹${format(resultTax)}\nTotal: ₹${format(resultTotal)}"
                    clipboardManager.setText(AnnotatedString(text))
                    Toast.makeText(context, "Copied results", Toast.LENGTH_SHORT).show()
                }
            ) {
                ResultHeadline("₹${format(resultTotal)}")
                ResultRow("Base Amount", "₹${format(resultBase)}")
                ResultRow("GST Amount (${gstRate}%)", "₹${format(resultTax)}")
                ResultDivider()
                ResultRow(
                    label = if (isAddGst) "Total (Gross)" else "Total (Net)",
                    value = "₹${format(resultTotal)}",
                    emphasized = true
                )
            }
        }
    }
}
