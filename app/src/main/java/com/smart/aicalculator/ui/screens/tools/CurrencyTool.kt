package com.smart.aicalculator.ui.screens.tools

import android.text.format.DateUtils
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smart.aicalculator.data.db.AppDatabase
import com.smart.aicalculator.data.db.HistoryEntity
import com.smart.aicalculator.ui.components.SmartCard
import com.smart.aicalculator.ui.theme.AppTheme
import com.smart.aicalculator.ui.theme.UrbanistFontFamily
import kotlinx.coroutines.launch

// Common currencies offered in the picker (the API returns these and many more).
private val CURRENCY_NAMES = listOf(
    "USD" to "US Dollar",
    "EUR" to "Euro",
    "GBP" to "British Pound",
    "INR" to "Indian Rupee",
    "JPY" to "Japanese Yen",
    "AUD" to "Australian Dollar",
    "CAD" to "Canadian Dollar",
    "CNY" to "Chinese Yuan",
    "AED" to "UAE Dirham",
    "SGD" to "Singapore Dollar",
    "CHF" to "Swiss Franc",
    "NZD" to "New Zealand Dollar",
    "HKD" to "Hong Kong Dollar",
    "ZAR" to "South African Rand"
)

@Composable
fun CurrencyTool(
    onBackClick: () -> Unit,
    viewModel: CurrencyViewModel = viewModel()
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context.applicationContext) }

    val rates by viewModel.rates.collectAsState()
    val status by viewModel.status.collectAsState()
    val updatedAt by viewModel.updatedAt.collectAsState()

    var amount by rememberSaveable { mutableStateOf("") }
    var fromCode by rememberSaveable { mutableStateOf("USD") }
    var toCode by rememberSaveable { mutableStateOf("INR") }
    var manualMode by rememberSaveable { mutableStateOf(false) }
    var manualRate by rememberSaveable { mutableStateOf("") }

    val amountValue = amount.toDoubleOrNull()
    val result: Double? = when {
        amountValue == null -> null
        manualMode -> manualRate.toDoubleOrNull()?.let { amountValue * it }
        else -> viewModel.convert(amountValue, fromCode, toCode)
    }
    val unitRate: Double? =
        if (manualMode) manualRate.toDoubleOrNull() else viewModel.rateFor(fromCode, toCode)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ToolInputCard {
            ToolNumberField(amount, { amount = it }, "Amount")

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CurrencyDropdown("From", fromCode, { fromCode = it }, Modifier.weight(1f))
                IconButton(onClick = { val t = fromCode; fromCode = toCode; toCode = t }) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = "Swap currencies", tint = MaterialTheme.colorScheme.primary)
                }
                CurrencyDropdown("To", toCode, { toCode = it }, Modifier.weight(1f))
            }

            // Status + refresh
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    StatusChip(status)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when {
                            status == CurrencyViewModel.Status.LOADING -> "Fetching latest rates…"
                            updatedAt > 0L -> "Updated " + DateUtils.getRelativeTimeSpanString(
                                updatedAt, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS
                            )
                            else -> "Not updated yet"
                        },
                        fontFamily = UrbanistFontFamily,
                        fontSize = 12.sp,
                        color = AppTheme.colors.textTertiary
                    )
                }
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh rates", tint = MaterialTheme.colorScheme.primary)
                }
            }

            if (manualMode) {
                ToolNumberField(
                    value = manualRate,
                    onValueChange = { manualRate = it },
                    label = "Rate: 1 $fromCode = ? $toCode"
                )
            }
        }

        // Error state with manual fallback
        if (status == CurrencyViewModel.Status.ERROR && !manualMode) {
            SmartCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                border = false
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Couldn't fetch live rates",
                        fontFamily = UrbanistFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Check your connection and tap refresh, or enter a rate manually.",
                        fontFamily = UrbanistFontFamily,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = AppTheme.colors.coralOnTint
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = { manualMode = true },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Enter rate manually", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Result
        if (result != null) {
            ToolResultCard(
                title = "Converted Amount",
                onCopy = {
                    clipboardManager.setText(AnnotatedString("${formatMoney(result)} $toCode"))
                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                }
            ) {
                ResultHeadline("${formatMoney(result)} $toCode")
                ResultDivider()
                if (unitRate != null) {
                    ResultRow("Rate", "1 $fromCode = ${formatNumber(unitRate)} $toCode")
                }
                ResultRow(
                    "Source",
                    when {
                        manualMode -> "Manual rate"
                        status == CurrencyViewModel.Status.LIVE -> "Live rate"
                        status == CurrencyViewModel.Status.CACHED -> "Cached (offline)"
                        else -> "—"
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            db.historyDao().insertHistory(
                                HistoryEntity(
                                    expression = "${formatNumber(amountValue!!)} $fromCode → $toCode",
                                    result = "${formatMoney(result)} $toCode",
                                    timestamp = System.currentTimeMillis(),
                                    source = "Tool"
                                )
                            )
                            Toast.makeText(context, "Saved to history", Toast.LENGTH_SHORT).show()
                        }
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Save to history", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: CurrencyViewModel.Status) {
    val (label, color) = when (status) {
        CurrencyViewModel.Status.LOADING -> "Updating…" to AppTheme.colors.textTertiary
        CurrencyViewModel.Status.LIVE -> "Live rates" to AppTheme.colors.success
        CurrencyViewModel.Status.CACHED -> "Offline · cached" to AppTheme.colors.warning
        CurrencyViewModel.Status.ERROR -> "Update failed" to MaterialTheme.colorScheme.primary
    }
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.14f)) {
        Text(
            text = label,
            fontFamily = UrbanistFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyDropdown(
    label: String,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = AppTheme.colors.border,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = AppTheme.colors.textTertiary,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            textStyle = TextStyle(
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            CURRENCY_NAMES.forEach { (code, name) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            "$code — $name",
                            fontFamily = UrbanistFontFamily,
                            fontWeight = if (code == selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onSelect(code)
                        expanded = false
                    }
                )
            }
        }
    }
}
