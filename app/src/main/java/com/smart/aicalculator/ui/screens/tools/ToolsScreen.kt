package com.smart.aicalculator.ui.screens.tools

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smart.aicalculator.R
import com.smart.aicalculator.ui.components.AppHeader
import com.smart.aicalculator.ui.theme.AppTheme
import com.smart.aicalculator.ui.theme.UrbanistFontFamily

data class ToolItem(
    val id: String,
    val titleRes: Int,
    val descRes: Int,
    val icon: ImageVector
)

private data class ToolCategory(
    val nameRes: Int,
    val accent: Color,
    val tools: List<ToolItem>
)

// Soft, premium category accents (kept muted, not bright).
private val FinanceAccent = Color(0xFF3E63DD)   // soft indigo-blue
private val MathAccent = Color(0xFF0E9384)      // soft teal
private val HealthAccent = Color(0xFF9A4DB8)    // soft violet

private const val SMART_SOLVE_ID = "smartsolve"

private val CATEGORIES = listOf(
    ToolCategory(
        R.string.category_finance, FinanceAccent,
        listOf(
            ToolItem("gst", R.string.tool_gst_title, R.string.tool_gst_desc, Icons.AutoMirrored.Filled.ReceiptLong),
            ToolItem("discount", R.string.tool_discount_title, R.string.tool_discount_desc, Icons.Default.LocalOffer),
            ToolItem("tip", R.string.tool_tip_title, R.string.tool_tip_desc, Icons.Default.RoomService),
            ToolItem("emi", R.string.tool_emi_title, R.string.tool_emi_desc, Icons.Default.AccountBalance),
            ToolItem("saving", R.string.tool_saving_title, R.string.tool_saving_desc, Icons.Default.Savings),
            ToolItem("sip", R.string.tool_sip_title, R.string.tool_sip_desc, Icons.AutoMirrored.Filled.TrendingUp),
            ToolItem("currency", R.string.tool_currency_title, R.string.tool_currency_desc, Icons.Default.CurrencyExchange)
        )
    ),
    ToolCategory(
        R.string.category_math, MathAccent,
        listOf(
            ToolItem("percent", R.string.tool_percent_title, R.string.tool_percent_desc, Icons.Default.Percent),
            ToolItem("unit", R.string.tool_unit_title, R.string.tool_unit_desc, Icons.AutoMirrored.Filled.CompareArrows),
            ToolItem("age", R.string.tool_age_title, R.string.tool_age_desc, Icons.Default.Cake)
        )
    ),
    ToolCategory(
        R.string.category_health, HealthAccent,
        listOf(
            ToolItem("bmi", R.string.tool_bmi_title, R.string.tool_bmi_desc, Icons.Default.MonitorWeight)
        )
    )
)

@Composable
fun ToolsScreen(
    onToolClick: (String) -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        AppHeader(
            title = stringResource(R.string.tab_tools),
            subtitle = stringResource(R.string.tools_subtitle)
        ) {
            IconButton(onClick = onOpenSettings) {
                Icon(
                    Icons.Outlined.Settings,
                    contentDescription = stringResource(R.string.action_settings),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            SmartSolveBanner(onClick = { onToolClick(SMART_SOLVE_ID) })

            CATEGORIES.forEach { category ->
                CategorySection(category = category, onToolClick = onToolClick)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun CategorySection(
    category: ToolCategory,
    onToolClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Plain section title — clean text only, no colored background pill.
        // Color stays in the cards (ToolGridCard), not behind the label.
        Text(
            text = stringResource(category.nameRes),
            fontFamily = UrbanistFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 2.dp, top = 4.dp)
        )

        category.tools.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { tool ->
                    ToolGridCard(
                        tool = tool,
                        accent = category.accent,
                        onClick = { onToolClick(tool.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ToolGridCard(
    tool: ToolItem,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(148.dp),
        shape = RoundedCornerShape(22.dp),
        color = accent.copy(alpha = 0.10f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(tool.titleRes),
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1
            )
            Text(
                text = stringResource(tool.descRes),
                fontFamily = UrbanistFontFamily,
                fontSize = 12.sp,
                lineHeight = 15.sp,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 2
            )
        }
    }
}

/**
 * Featured Smart Solve card. Calm, soft coral tint that matches the category
 * card system — a featured tool, not a loud banner.
 */
@Composable
private fun SmartSolveBanner(onClick: () -> Unit) {
    val accent = MaterialTheme.colorScheme.primary
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = accent.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.16f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.smart_solve_title),
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = stringResource(R.string.smart_solve_desc),
                    fontFamily = UrbanistFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Calm "Try now →" CTA pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = accent.copy(alpha = 0.14f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.smart_solve_cta),
                            fontFamily = UrbanistFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = accent
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}
