package com.smart.aicalculator.ui.screens.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smart.aicalculator.R
import com.smart.aicalculator.ui.screens.calculator.CalculatorScreen
import com.smart.aicalculator.ui.screens.calculator.CalculatorViewModel
import com.smart.aicalculator.ui.screens.tools.ToolsScreen
import com.smart.aicalculator.ui.screens.history.HistoryScreen
import com.smart.aicalculator.ui.screens.history.HistoryViewModel
import com.smart.aicalculator.ui.theme.AppTheme
import com.smart.aicalculator.ui.theme.UrbanistFontFamily

sealed class MainTab(val route: String, val titleRes: Int, val icon: ImageVector) {
    object Calculator : MainTab("calculator", R.string.tab_calculator, Icons.Default.Calculate)
    object Tools : MainTab("tools", R.string.tab_tools, Icons.Default.Handyman)
    object History : MainTab("history", R.string.tab_history, Icons.Default.History)
}

// Stable, file-level list — never recreated during recomposition.
private val TABS = listOf(
    MainTab.Calculator,
    MainTab.Tools,
    MainTab.History
)

@Composable
fun MainScreen(
    initialTab: String = MainTab.Calculator.route,
    onNavigateToToolDetail: ((String) -> Unit)? = null,
    onOpenSettings: () -> Unit = {}
) {
    var currentTab by rememberSaveable { mutableStateOf(initialTab) }

    // Hoist ViewModels at MainScreen level so they SURVIVE tab switches.
    // Previously they were scoped inside the when-block and recreated every time.
    val calculatorViewModel: CalculatorViewModel = viewModel(key = "calc_main")
    val historyViewModel: HistoryViewModel = viewModel(key = "history_main")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            PremiumBottomNavigation(
                tabs = TABS,
                currentRoute = currentTab,
                onTabSelected = { route -> currentTab = route }
            )
        }
    ) { innerPadding ->
        // Render ONLY the active tab. The Calculator/History ViewModels are
        // hoisted above (keyed), so their data survives tab switches AND
        // back-navigation from tool screens — nothing is rebuilt. Each tab's
        // own scroll position and rememberSaveable inputs are restored when it
        // re-enters composition. This keeps composition light: switching tabs
        // or returning from a tool only composes a single screen, instead of
        // re-laying-out all three at once (which caused the back/switch lag).
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                MainTab.Tools.route -> ToolsScreen(
                    onToolClick = { toolId -> onNavigateToToolDetail?.invoke(toolId) },
                    onOpenSettings = onOpenSettings
                )

                MainTab.History.route -> HistoryScreen(
                    onOpenSettings = onOpenSettings,
                    viewModel = historyViewModel
                )

                else -> CalculatorScreen(
                    onOpenSettings = onOpenSettings,
                    viewModel = calculatorViewModel
                )
            }
        }
    }
}

@Composable
fun PremiumBottomNavigation(
    tabs: List<MainTab>,
    currentRoute: String,
    onTabSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column {
            // Hairline top border for crisp separation from content.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AppTheme.colors.border)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(80.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    BottomNavItem(
                        tab = tab,
                        selected = currentRoute == tab.route,
                        onClick = { onTabSelected(tab.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavItem(
    tab: MainTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else AppTheme.colors.textTertiary,
        animationSpec = tween(200),
        label = "navColor"
    )
    val pillColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(200),
        label = "navPill"
    )
    val pillWidth by animateDpAsState(
        targetValue = if (selected) 64.dp else 48.dp,
        animationSpec = tween(200),
        label = "navPillWidth"
    )

    val title = stringResource(tab.titleRes)
    // Outer weighted cell: ONLY for spacing/centering — it is NOT clickable, so
    // taps in the empty space around and between tabs are ignored.
    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Compact clickable block that wraps only the icon + label. The touch
        // target is the icon/label area (pill + label, comfortably > 48dp tall),
        // NOT the full-width cell. A min-height plus centered arrangement gives
        // the label enough vertical room so it is never clipped; horizontal
        // padding keeps the ripple/label clear of the rounded corners.
        Column(
            modifier = Modifier
                .heightIn(min = 60.dp)
                .clip(RoundedCornerShape(16.dp))
                .selectable(
                    selected = selected,
                    role = Role.Tab,
                    onClick = onClick
                )
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .width(pillWidth)
                    .height(36.dp)
                    .clip(RoundedCornerShape(50))
                    .background(pillColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = title,
                    tint = contentColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = contentColor,
                fontSize = 13.sp,
                maxLines = 1,
                fontFamily = UrbanistFontFamily,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
