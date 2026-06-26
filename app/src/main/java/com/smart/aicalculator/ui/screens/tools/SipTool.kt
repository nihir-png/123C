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
import kotlin.math.pow

@Composable
fun SipTool(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context.applicationContext) }
    val historyDao = db.historyDao()

    var monthlyStr by rememberSaveable { mutableStateOf("") }
    var rateStr by rememberSaveable { mutableStateOf("12") }
    var yearsStr by rememberSaveable { mutableStateOf("") }

    var calculated by rememberSaveable { mutableStateOf(false) }
    var invested by rememberSaveable { mutableStateOf(0.0) }
    var futureValue by rememberSaveable { mutableStateOf(0.0) }
    var returns by rememberSaveable { mutableStateOf(0.0) }

    fun calculate() {
        val p = monthlyStr.toDoubleOrNull()
        val annualRate = rateStr.toDoubleOrNull()
        val years = yearsStr.toDoubleOrNull()

        if (p == null || p <= 0) {
            Toast.makeText(context, "Enter a valid monthly investment", Toast.LENGTH_SHORT).show()
            return
        }
        if (annualRate == null || annualRate < 0) {
            Toast.makeText(context, "Enter a valid return rate", Toast.LENGTH_SHORT).show()
            return
        }
        if (years == null || years <= 0) {
            Toast.makeText(context, "Enter a valid period in years", Toast.LENGTH_SHORT).show()
            return
        }

        val months = (years * 12).toInt()
        val i = annualRate / 12.0 / 100.0
        futureValue = if (i == 0.0) {
            p * months
        } else {
            p * (((1 + i).pow(months) - 1) / i) * (1 + i)
        }
        invested = p * months
        returns = futureValue - invested
        calculated = true

        coroutineScope.launch {
            historyDao.insertHistory(
                HistoryEntity(
                    expression = "SIP ₹${formatMoney(p)}/mo at ${formatNumber(annualRate)}% for ${formatNumber(years)}y",
                    result = "Value: ₹${formatMoney(futureValue)}",
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
            ToolNumberField(monthlyStr, { monthlyStr = it }, "Monthly Investment", prefix = "₹ ")
            ToolNumberField(rateStr, { rateStr = it }, "Expected Return Rate", suffix = "% p.a.")
            ToolNumberField(yearsStr, { yearsStr = it }, "Investment Period", suffix = "years")
            ToolActions(
                onReset = {
                    monthlyStr = ""; rateStr = "12"; yearsStr = ""; calculated = false
                },
                onCalculate = { calculate() }
            )
        }

        if (calculated) {
            ToolResultCard(
                title = "Estimated Value",
                onCopy = {
                    clipboardManager.setText(
                        AnnotatedString(
                            "Invested: ₹${formatMoney(invested)}\n" +
                                "Est. returns: ₹${formatMoney(returns)}\n" +
                                "Total value: ₹${formatMoney(futureValue)}"
                        )
                    )
                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                }
            ) {
                ResultHeadline("₹${formatMoney(futureValue)}")
                ResultDivider()
                ResultRow("Invested amount", "₹${formatMoney(invested)}")
                ResultRow("Estimated returns", "₹${formatMoney(returns)}")
                ResultDivider()
                ResultRow("Total value", "₹${formatMoney(futureValue)}", emphasized = true)
            }
        }
    }
}
