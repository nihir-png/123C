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
import kotlin.math.pow

@Composable
fun BmiTool(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context.applicationContext) }
    val historyDao = db.historyDao()

    var weightStr by rememberSaveable { mutableStateOf("") }
    var heightStr by rememberSaveable { mutableStateOf("") }

    var calculated by rememberSaveable { mutableStateOf(false) }
    var bmiResult by rememberSaveable { mutableStateOf(0.0) }
    var bmiCategory by rememberSaveable { mutableStateOf("") }
    var categoryColor by remember { mutableStateOf(Color.Gray) }

    fun format(value: Double): String {
        return if (value.isNaN() || value.isInfinite()) "0" else String.format("%.1f", value)
    }

    fun calculate() {
        val w = weightStr.toDoubleOrNull()
        val h = heightStr.toDoubleOrNull()

        if (w == null || w <= 0) {
            Toast.makeText(context, "Please enter a valid weight in kg", Toast.LENGTH_SHORT).show()
            return
        }
        if (h == null || h <= 0) {
            Toast.makeText(context, "Please enter a valid height in cm", Toast.LENGTH_SHORT).show()
            return
        }

        val hMeters = h / 100.0
        bmiResult = w / hMeters.pow(2)

        val (cat, color) = when {
            bmiResult < 18.5 -> Pair("Underweight", Color(0xFF03A9F4)) // Blue
            bmiResult >= 18.5 && bmiResult < 25.0 -> Pair("Normal", Color(0xFF4CAF50)) // Green
            bmiResult >= 25.0 && bmiResult < 30.0 -> Pair("Overweight", Color(0xFFFF9800)) // Orange
            else -> Pair("Obese", Color(0xFFF44336)) // Red
        }
        bmiCategory = cat
        categoryColor = color
        calculated = true

        coroutineScope.launch {
            val expr = "BMI: Weight ${format(w)}kg, Height ${format(h)}cm"
            val res = "BMI: ${format(bmiResult)} ($cat)"
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
        weightStr = ""
        heightStr = ""
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
                // Weight
                OutlinedTextField(
                    value = weightStr,
                    onValueChange = { weightStr = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Height
                OutlinedTextField(
                    value = heightStr,
                    onValueChange = { heightStr = it },
                    label = { Text("Height (cm)") },
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
                title = "BMI Assessment",
                onCopy = {
                    val text = "Weight: ${weightStr}kg\nHeight: ${heightStr}cm\nBMI: ${format(bmiResult)}\nCategory: $bmiCategory"
                    clipboardManager.setText(AnnotatedString(text))
                    Toast.makeText(context, "Copied BMI summary", Toast.LENGTH_SHORT).show()
                }
            ) {
                ResultHeadline(format(bmiResult))

                Text(
                    text = bmiCategory.uppercase(),
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = categoryColor
                )

                ResultRow("Category", bmiCategory)

                Text(
                    text = when (bmiCategory) {
                        "Underweight" -> "A BMI of less than 18.5 indicates you are underweight. You may need to gain weight safely."
                        "Normal" -> "A BMI of 18.5 to 24.9 indicates you are at a healthy weight for your height."
                        "Overweight" -> "A BMI of 25 to 29.9 is considered overweight. Consider checking lifestyle options."
                        else -> "A BMI of 30 or more is considered obese. Consult a medical professional for guidance."
                    },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}
