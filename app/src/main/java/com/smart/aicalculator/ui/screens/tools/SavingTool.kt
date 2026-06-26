package com.smart.aicalculator.ui.screens.tools

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.smart.aicalculator.data.db.AppDatabase
import com.smart.aicalculator.data.db.HistoryEntity
import kotlinx.coroutines.launch
import kotlin.math.ceil

@Composable
fun SavingTool(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context.applicationContext) }
    val historyDao = db.historyDao()

    var goalStr by rememberSaveable { mutableStateOf("") }
    var savedStr by rememberSaveable { mutableStateOf("") }
    var durationStr by rememberSaveable { mutableStateOf("") }
    var unitIndex by rememberSaveable { mutableStateOf(0) } // 0 = Years, 1 = Months

    var calculated by rememberSaveable { mutableStateOf(false) }
    var remaining by rememberSaveable { mutableStateOf(0.0) }
    var months by rememberSaveable { mutableStateOf(0) }
    var perMonth by rememberSaveable { mutableStateOf(0.0) }
    var reached by rememberSaveable { mutableStateOf(false) }

    fun calculate() {
        val goal = goalStr.toDoubleOrNull()
        val saved = savedStr.toDoubleOrNull() ?: 0.0
        val duration = durationStr.toDoubleOrNull()

        if (goal == null || goal <= 0) {
            Toast.makeText(context, "Enter a valid goal amount", Toast.LENGTH_SHORT).show()
            return
        }
        if (duration == null || duration <= 0) {
            Toast.makeText(context, "Enter a valid duration", Toast.LENGTH_SHORT).show()
            return
        }

        months = if (unitIndex == 0) (duration * 12).toInt() else duration.toInt()
        if (months <= 0) months = 1
        remaining = (goal - saved).coerceAtLeast(0.0)
        reached = saved >= goal
        perMonth = if (reached) 0.0 else remaining / months
        calculated = true

        coroutineScope.launch {
            historyDao.insertHistory(
                HistoryEntity(
                    expression = "Save ₹${formatMoney(goal)} in $months mo",
                    result = if (reached) "Goal already reached" else "₹${formatMoney(perMonth)}/mo",
                    timestamp = System.currentTimeMillis(),
                    source = "Tool"
                )
            )
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
            ToolNumberField(goalStr, { goalStr = it }, "Goal Amount", prefix = "₹ ")
            ToolNumberField(savedStr, { savedStr = it }, "Already Saved (optional)", prefix = "₹ ")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                ToolNumberField(
                    value = durationStr,
                    onValueChange = { durationStr = it },
                    label = "Duration",
                    modifier = Modifier.weight(1.2f),
                    decimal = false
                )
                AppSegmentedControl(
                    items = listOf("Years", "Months"),
                    selectedIndex = unitIndex,
                    onSelectedChange = { unitIndex = it },
                    modifier = Modifier.weight(1.5f),
                    height = 56.dp
                )
            }
            ToolActions(
                onReset = {
                    goalStr = ""; savedStr = ""; durationStr = ""; unitIndex = 0; calculated = false
                },
                onCalculate = { calculate() }
            )
        }

        if (calculated) {
            ToolResultCard(
                title = if (reached) "Goal Reached" else "Save Per Month",
                onCopy = {
                    val text = if (reached) "Goal already reached"
                    else "Save ₹${formatMoney(perMonth)}/mo for $months months"
                    clipboardManager.setText(AnnotatedString(text))
                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                }
            ) {
                if (reached) {
                    ResultHeadline("You're already there! 🎉")
                } else {
                    ResultHeadline("₹${formatMoney(ceil(perMonth))}/mo")
                    ResultDivider()
                    ResultRow("Amount still needed", "₹${formatMoney(remaining)}")
                    ResultRow("Number of months", "$months")
                    ResultDivider()
                    ResultRow("Save each month", "₹${formatMoney(perMonth)}", emphasized = true)
                }
            }
        }
    }
}
