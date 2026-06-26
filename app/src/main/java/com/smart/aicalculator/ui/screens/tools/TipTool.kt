package com.smart.aicalculator.ui.screens.tools

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
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

@Composable
fun TipTool(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context.applicationContext) }
    val historyDao = db.historyDao()

    var billAmount by rememberSaveable { mutableStateOf("") }
    var tipPercent by rememberSaveable { mutableStateOf("10") }
    var splitPeople by rememberSaveable { mutableStateOf("1") }

    var calculated by rememberSaveable { mutableStateOf(false) }
    var totalTip by rememberSaveable { mutableStateOf(0.0) }
    var totalBill by rememberSaveable { mutableStateOf(0.0) }
    var tipPerPerson by rememberSaveable { mutableStateOf(0.0) }
    var billPerPerson by rememberSaveable { mutableStateOf(0.0) }

    fun format(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format("%.2f", value)
        }
    }

    fun calculate() {
        val bill = billAmount.toDoubleOrNull()
        val percent = tipPercent.toDoubleOrNull()
        val people = splitPeople.toIntOrNull()

        if (bill == null || bill <= 0) {
            Toast.makeText(context, "Please enter a valid bill amount", Toast.LENGTH_SHORT).show()
            return
        }
        if (percent == null || percent < 0) {
            Toast.makeText(context, "Please enter a valid tip percentage", Toast.LENGTH_SHORT).show()
            return
        }
        if (people == null || people <= 0) {
            Toast.makeText(context, "Please enter a valid number of people", Toast.LENGTH_SHORT).show()
            return
        }

        totalTip = bill * (percent / 100.0)
        totalBill = bill + totalTip
        tipPerPerson = totalTip / people
        billPerPerson = totalBill / people
        calculated = true

        coroutineScope.launch {
            val expr = "Split ₹${format(bill)} with ${format(percent)}% tip among $people people"
            val res = "Per Person: ₹${format(billPerPerson)} (Tip: ₹${format(tipPerPerson)})"
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
        billAmount = ""
        tipPercent = "10"
        splitPeople = "1"
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
                // Bill Amount
                OutlinedTextField(
                    value = billAmount,
                    onValueChange = { billAmount = it },
                    label = { Text("Bill Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Tip Percent
                OutlinedTextField(
                    value = tipPercent,
                    onValueChange = { tipPercent = it },
                    label = { Text("Tip Percentage (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Split People
                OutlinedTextField(
                    value = splitPeople,
                    onValueChange = { splitPeople = it },
                    label = { Text("Split Between (People)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

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
                title = "Split Summary",
                onCopy = {
                    val text = "Total Tip: ₹${format(totalTip)}\nTotal Bill: ₹${format(totalBill)}\nTip per Person: ₹${format(tipPerPerson)}\nTotal per Person: ₹${format(billPerPerson)}"
                    clipboardManager.setText(AnnotatedString(text))
                    Toast.makeText(context, "Copied results", Toast.LENGTH_SHORT).show()
                }
            ) {
                ResultHeadline("₹${format(billPerPerson)}")
                ResultRow("Total Tip (Overall)", "₹${format(totalTip)}")
                ResultRow("Total Bill (Overall)", "₹${format(totalBill)}")
                ResultDivider()
                ResultRow("Tip per Person", "₹${format(tipPerPerson)}")
                ResultRow(
                    label = "Amount per Person",
                    value = "₹${format(billPerPerson)}",
                    emphasized = true
                )
            }
        }
    }
}
