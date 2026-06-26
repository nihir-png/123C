package com.smart.aicalculator.ui.screens.tools

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.smart.aicalculator.data.db.AppDatabase
import com.smart.aicalculator.data.db.HistoryEntity
import com.smart.aicalculator.ui.theme.AppTheme
import com.smart.aicalculator.ui.theme.UrbanistFontFamily
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgeTool(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context.applicationContext) }
    val historyDao = db.historyDao()

    var dobMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var showPicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US) }
    val dob: LocalDate? = remember(dobMillis) {
        dobMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = dobMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis <= System.currentTimeMillis()
            }
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dobMillis = pickerState.selectedDateMillis
                    showPicker = false
                    coroutineScope.launch {
                        val d = dobMillis?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
                        if (d != null) {
                            val p = Period.between(d, LocalDate.now())
                            historyDao.insertHistory(
                                HistoryEntity(
                                    expression = "Age from ${d.format(dateFormatter)}",
                                    result = "${p.years}y ${p.months}m ${p.days}d",
                                    timestamp = System.currentTimeMillis(),
                                    source = "Tool"
                                )
                            )
                        }
                    }
                }) { Text("OK", color = MaterialTheme.colorScheme.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.secondary)
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ToolInputCard {
            Text(
                text = "Date of Birth",
                fontFamily = UrbanistFontFamily,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            OutlinedTextField(
                value = dob?.format(dateFormatter) ?: "",
                onValueChange = {},
                readOnly = true,
                enabled = false,
                placeholder = { Text("Select your date of birth", color = AppTheme.colors.textTertiary) },
                trailingIcon = {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPicker = true },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = AppTheme.colors.border,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    disabledTextColor = MaterialTheme.colorScheme.onBackground,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.primary,
                    disabledPlaceholderColor = AppTheme.colors.textTertiary
                )
            )
        }

        if (dob != null) {
            AgeResult(dob = dob, dateFormatter = dateFormatter, clipboardManager = clipboardManager, context = context)
        }
    }
}

@Composable
private fun AgeResult(
    dob: LocalDate,
    dateFormatter: DateTimeFormatter,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    context: android.content.Context
) {
    val today = remember { LocalDate.now() }
    val period = remember(dob) { Period.between(dob, today) }
    val totalDays = remember(dob) { ChronoUnit.DAYS.between(dob, today) }
    val totalMonths = remember(dob) { ChronoUnit.MONTHS.between(dob, today) }
    val totalWeeks = totalDays / 7

    val nextBirthday = remember(dob) {
        var nb = dob.withYear(today.year)
        if (!nb.isAfter(today)) nb = nb.plusYears(1)
        nb
    }
    val daysToBirthday = remember(dob) { ChronoUnit.DAYS.between(today, nextBirthday) }
    val bornDay = remember(dob) { dob.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US) }

    ToolResultCard(
        title = "Your Age",
        onCopy = {
            clipboardManager.setText(
                AnnotatedString("${period.years} years, ${period.months} months, ${period.days} days")
            )
            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
        }
    ) {
        ResultHeadline("${period.years}y  ${period.months}m  ${period.days}d")
        ResultDivider()
        ResultRow("In months", "${formatMoney(totalMonths.toDouble())}")
        ResultRow("In weeks", "${formatMoney(totalWeeks.toDouble())}")
        ResultRow("In days", "${formatMoney(totalDays.toDouble())}")
        ResultDivider()
        ResultRow("Born on", bornDay)
        ResultRow(
            "Next birthday",
            if (daysToBirthday == 0L) "Today! 🎉" else "in $daysToBirthday days",
            emphasized = true
        )
    }
}
