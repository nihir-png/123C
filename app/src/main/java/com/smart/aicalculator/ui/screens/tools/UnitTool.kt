package com.smart.aicalculator.ui.screens.tools

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.smart.aicalculator.ui.components.SmartCard
import com.smart.aicalculator.ui.theme.UrbanistFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitTool(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context.applicationContext) }
    val historyDao = db.historyDao()

    var activeTab by rememberSaveable { mutableStateOf("Length") } // "Length", "Weight", "Temperature"
    var inputValue by rememberSaveable { mutableStateOf("1") }

    val lengthUnits = listOf("Meter (m)", "Kilometer (km)", "Centimeter (cm)", "Inch (in)", "Foot (ft)")
    val weightUnits = listOf("Kilogram (kg)", "Gram (g)", "Pound (lb)", "Ounce (oz)")
    val tempUnits = listOf("Celsius (°C)", "Fahrenheit (°F)", "Kelvin (K)")

    val currentUnits = when (activeTab) {
        "Weight" -> weightUnits
        "Temperature" -> tempUnits
        else -> lengthUnits
    }

    var fromUnit by remember { mutableStateOf(currentUnits[0]) }
    var toUnit by remember { mutableStateOf(currentUnits[1]) }

    // Reset units when tab changes
    LaunchedEffect(activeTab) {
        fromUnit = currentUnits[0]
        toUnit = currentUnits[1]
    }

    var fromMenuExpanded by remember { mutableStateOf(false) }
    var toMenuExpanded by remember { mutableStateOf(false) }

    fun format(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format("%.4f", value).trimEnd('0').trimEnd('.')
        }
    }

    // Convert length to meters
    fun toMeters(value: Double, unit: String): Double {
        return when (unit) {
            "Kilometer (km)" -> value * 1000.0
            "Centimeter (cm)" -> value / 100.0
            "Inch (in)" -> value * 0.0254
            "Foot (ft)" -> value * 0.3048
            else -> value
        }
    }

    // Convert meters to target unit
    fun fromMeters(value: Double, unit: String): Double {
        return when (unit) {
            "Kilometer (km)" -> value / 1000.0
            "Centimeter (cm)" -> value * 100.0
            "Inch (in)" -> value / 0.0254
            "Foot (ft)" -> value / 0.3048
            else -> value
        }
    }

    // Convert weight to kg
    fun toKg(value: Double, unit: String): Double {
        return when (unit) {
            "Gram (g)" -> value / 1000.0
            "Pound (lb)" -> value * 0.45359237
            "Ounce (oz)" -> value * 0.028349523
            else -> value
        }
    }

    // Convert kg to target unit
    fun fromKg(value: Double, unit: String): Double {
        return when (unit) {
            "Gram (g)" -> value * 1000.0
            "Pound (lb)" -> value / 0.45359237
            "Ounce (oz)" -> value / 0.028349523
            else -> value
        }
    }

    // Temperature Conversion
    fun convertTemperature(value: Double, from: String, to: String): Double {
        val inCelsius = when (from) {
            "Fahrenheit (°F)" -> (value - 32.0) * 5.0 / 9.0
            "Kelvin (K)" -> value - 273.15
            else -> value
        }
        return when (to) {
            "Fahrenheit (°F)" -> inCelsius * 9.0 / 5.0 + 32.0
            "Kelvin (K)" -> inCelsius + 273.15
            else -> inCelsius
        }
    }

    val outputValue = remember(inputValue, fromUnit, toUnit, activeTab) {
        val num = inputValue.toDoubleOrNull() ?: 0.0
        val converted = when (activeTab) {
            "Length" -> {
                val inMeters = toMeters(num, fromUnit)
                fromMeters(inMeters, toUnit)
            }
            "Weight" -> {
                val inKg = toKg(num, fromUnit)
                fromKg(inKg, toUnit)
            }
            "Temperature" -> {
                convertTemperature(num, fromUnit, toUnit)
            }
            else -> num
        }
        converted
    }

    fun saveHistory() {
        val num = inputValue.toDoubleOrNull()
        if (num == null) return
        coroutineScope.launch {
            val expr = "Convert ${format(num)} $fromUnit to $toUnit"
            val res = "${format(outputValue)} $toUnit"
            historyDao.insertHistory(
                HistoryEntity(
                    expression = expr,
                    result = res,
                    timestamp = System.currentTimeMillis(),
                    source = "Tool"
                )
            )
            Toast.makeText(context, "Saved conversion to History", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Selector
        val unitTabs = listOf("Length", "Weight", "Temperature")
        AppSegmentedControl(
            items = unitTabs,
            selectedIndex = unitTabs.indexOf(activeTab).coerceAtLeast(0),
            onSelectedChange = { activeTab = unitTabs[it] },
            modifier = Modifier.fillMaxWidth()
        )

        // Converter Card
        SmartCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // From Unit Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = fromMenuExpanded,
                        onExpandedChange = { fromMenuExpanded = !fromMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = fromUnit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("From Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = fromMenuExpanded,
                            onDismissRequest = { fromMenuExpanded = false }
                        ) {
                            currentUnits.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        fromUnit = unit
                                        fromMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Input Value
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    label = { Text("Value") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // To Unit Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = toMenuExpanded,
                        onExpandedChange = { toMenuExpanded = !toMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = toUnit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("To Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toMenuExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = toMenuExpanded,
                            onDismissRequest = { toMenuExpanded = false }
                        ) {
                            currentUnits.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        toUnit = unit
                                        toMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Result Card
        ToolResultCard(
            title = "Converted Value",
            onCopy = {
                val text = "${inputValue} ${fromUnit} = ${format(outputValue)} ${toUnit}"
                clipboardManager.setText(AnnotatedString(text))
                Toast.makeText(context, "Copied conversion text", Toast.LENGTH_SHORT).show()
            }
        ) {
            ResultRow("From", "${inputValue.ifEmpty { "0" }} $fromUnit")
            ResultHeadline("${format(outputValue)} $toUnit")
            ResultDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { saveHistory() }) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save to history",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Save to History",
                        fontFamily = UrbanistFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
