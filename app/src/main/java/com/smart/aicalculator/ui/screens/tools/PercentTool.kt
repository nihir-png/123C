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

@Composable
fun PercentTool(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context.applicationContext) }
    val historyDao = db.historyDao()

    var valueX by rememberSaveable { mutableStateOf("") }
    var valueY by rememberSaveable { mutableStateOf("") }
    var isModeOf by rememberSaveable { mutableStateOf(true) } // true: "X% of Y", false: "X is what % of Y"

    var calculated by rememberSaveable { mutableStateOf(false) }
    var resultVal by rememberSaveable { mutableStateOf(0.0) }

    fun format(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format("%.2f", value)
        }
    }

    fun calculate() {
        val x = valueX.toDoubleOrNull()
        val y = valueY.toDoubleOrNull()

        if (x == null) {
            Toast.makeText(context, "Please enter a valid X value", Toast.LENGTH_SHORT).show()
            return
        }
        if (y == null || y == 0.0) {
            Toast.makeText(context, "Please enter a valid Y value", Toast.LENGTH_SHORT).show()
            return
        }

        if (isModeOf) {
            resultVal = y * (x / 100.0)
        } else {
            resultVal = (x / y) * 100.0
        }
        calculated = true

        coroutineScope.launch {
            val expr = if (isModeOf) "${format(x)}% of ${format(y)}" else "${format(x)} is what % of ${format(y)}"
            val res = if (isModeOf) format(resultVal) else "${format(resultVal)}%"
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
        valueX = ""
        valueY = ""
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
                // Mode Switch
                AppSegmentedControl(
                    items = listOf("X% of Y", "X is what % of Y"),
                    selectedIndex = if (isModeOf) 0 else 1,
                    onSelectedChange = { isModeOf = it == 0 },
                    modifier = Modifier.fillMaxWidth()
                )

                // X Input
                OutlinedTextField(
                    value = valueX,
                    onValueChange = { valueX = it },
                    label = { Text(if (isModeOf) "Percentage X (%)" else "Value X") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Y Input
                OutlinedTextField(
                    value = valueY,
                    onValueChange = { valueY = it },
                    label = { Text("Total Amount Y") },
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
                title = "Result",
                onCopy = {
                    val text = if (isModeOf) {
                        "${valueX}% of ${valueY} = ${format(resultVal)}"
                    } else {
                        "${valueX} is ${format(resultVal)}% of ${valueY}"
                    }
                    clipboardManager.setText(AnnotatedString(text))
                    Toast.makeText(context, "Copied results", Toast.LENGTH_SHORT).show()
                }
            ) {
                ResultHeadline(
                    if (isModeOf) {
                        format(resultVal)
                    } else {
                        "${format(resultVal)}%"
                    }
                )
                ResultRow(
                    label = if (isModeOf) "${valueX}% of ${valueY}" else "${valueX} of ${valueY}",
                    value = if (isModeOf) format(resultVal) else "${format(resultVal)}%",
                    emphasized = true
                )
            }
        }
    }
}
