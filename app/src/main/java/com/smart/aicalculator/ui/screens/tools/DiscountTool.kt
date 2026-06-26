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
fun DiscountTool(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context.applicationContext) }
    val historyDao = db.historyDao()

    var originalPrice by rememberSaveable { mutableStateOf("") }
    var discountPercent by rememberSaveable { mutableStateOf("") }

    var calculated by rememberSaveable { mutableStateOf(false) }
    var savedAmount by rememberSaveable { mutableStateOf(0.0) }
    var finalPrice by rememberSaveable { mutableStateOf(0.0) }

    fun format(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format("%.2f", value)
        }
    }

    fun calculate() {
        val original = originalPrice.toDoubleOrNull()
        val percent = discountPercent.toDoubleOrNull()

        if (original == null || original <= 0) {
            Toast.makeText(context, "Please enter a valid original price", Toast.LENGTH_SHORT).show()
            return
        }
        if (percent == null || percent < 0 || percent > 100) {
            Toast.makeText(context, "Please enter a valid discount percentage (0-100)", Toast.LENGTH_SHORT).show()
            return
        }

        savedAmount = original * (percent / 100.0)
        finalPrice = original - savedAmount
        calculated = true

        coroutineScope.launch {
            val expr = "Discount ${format(percent)}% on ₹${format(original)}"
            val res = "Price: ₹${format(finalPrice)} (Saved: ₹${format(savedAmount)})"
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
        originalPrice = ""
        discountPercent = ""
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
                // Original Price
                OutlinedTextField(
                    value = originalPrice,
                    onValueChange = { originalPrice = it },
                    label = { Text("Original Price (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Discount Percent
                OutlinedTextField(
                    value = discountPercent,
                    onValueChange = { discountPercent = it },
                    label = { Text("Discount (%)") },
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
                title = "Calculation Result",
                onCopy = {
                    val text = "Original: ₹${originalPrice}\nSaved: ₹${format(savedAmount)}\nFinal: ₹${format(finalPrice)}"
                    clipboardManager.setText(AnnotatedString(text))
                    Toast.makeText(context, "Copied results", Toast.LENGTH_SHORT).show()
                }
            ) {
                ResultHeadline("₹${format(finalPrice)}")
                ResultRow("Original Price", "₹${originalPrice}")
                ResultRow("You Save", "₹${format(savedAmount)}")
                ResultDivider()
                ResultRow(
                    label = "Discounted Price",
                    value = "₹${format(finalPrice)}",
                    emphasized = true
                )
            }
        }
    }
}
