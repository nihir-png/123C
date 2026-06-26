package com.smart.aicalculator.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.smart.aicalculator.R
import com.smart.aicalculator.ui.components.AppHeader
import com.smart.aicalculator.ui.screens.aisolve.AiSolveScreen

@Composable
fun ToolDetailScreen(
    toolId: String,
    onBackClick: () -> Unit
) {
    val titleRes = when (toolId) {
        "smartsolve" -> R.string.smart_solve_title
        "gst" -> R.string.tool_gst_title
        "discount" -> R.string.tool_discount_title
        "tip" -> R.string.tool_tip_title
        "emi" -> R.string.tool_emi_title
        "percent" -> R.string.tool_percent_title
        "bmi" -> R.string.tool_bmi_title
        "unit" -> R.string.tool_unit_title
        "saving" -> R.string.tool_saving_title
        "sip" -> R.string.tool_sip_title
        "age" -> R.string.tool_age_title
        "currency" -> R.string.tool_currency_title
        else -> R.string.tab_tools
    }
    val toolTitle = stringResource(titleRes)

    Scaffold(
        topBar = {
            AppHeader(
                title = toolTitle,
                onBackClick = onBackClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (toolId) {
                "smartsolve" -> AiSolveScreen(showHeader = false)
                "gst" -> GstTool(onBackClick)
                "discount" -> DiscountTool(onBackClick)
                "tip" -> TipTool(onBackClick)
                "emi" -> EmiTool(onBackClick)
                "percent" -> PercentTool(onBackClick)
                "bmi" -> BmiTool(onBackClick)
                "unit" -> UnitTool(onBackClick)
                "currency" -> CurrencyTool(onBackClick)
                "sip" -> SipTool(onBackClick)
                "saving" -> SavingTool(onBackClick)
                "age" -> AgeTool(onBackClick)
                else -> ComingSoonTool(toolTitle)
            }
        }
    }
}
