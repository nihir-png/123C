package com.smart.aicalculator.ui.screens.history

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smart.aicalculator.R
import com.smart.aicalculator.data.db.HistoryEntity
import com.smart.aicalculator.ui.components.AppChip
import com.smart.aicalculator.ui.components.AppHeader
import com.smart.aicalculator.ui.components.EmptyState
import com.smart.aicalculator.ui.components.SmartCard
import com.smart.aicalculator.ui.theme.AppTheme
import com.smart.aicalculator.ui.theme.UrbanistFontFamily

// Filter VALUES match the source strings stored in the DB (kept in English so
// matching is locale-independent); only the labels shown are localized.
private val SOURCE_FILTERS = listOf("All", "Calculator", "Smart Solve", "Tool")

/** Localized label for a source/filter value. */
@Composable
private fun sourceLabel(value: String): String = when (value) {
    "All" -> stringResource(R.string.filter_all)
    "Calculator" -> stringResource(R.string.tab_calculator)
    "Smart Solve" -> stringResource(R.string.smart_solve_title)
    "Tool" -> stringResource(R.string.source_tool)
    else -> value
}

@Composable
fun HistoryScreen(
    onOpenSettings: () -> Unit = {},
    viewModel: HistoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val history by viewModel.history.collectAsState()

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf("All") }
    var showClearDialog by remember { mutableStateOf(false) }

    val filtered by remember {
        derivedStateOf {
            history.filter { item ->
                (selectedFilter == "All" || item.source == selectedFilter) &&
                    (searchQuery.isBlank() ||
                        item.expression.contains(searchQuery, ignoreCase = true) ||
                        item.result.contains(searchQuery, ignoreCase = true))
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAll()
                    showClearDialog = false
                }) { Text(stringResource(R.string.clear_all), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.secondary)
                }
            },
            title = { Text(stringResource(R.string.clear_history_q), fontFamily = UrbanistFontFamily, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.clear_history_msg)) },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        AppHeader(
            title = stringResource(R.string.tab_history),
            subtitle = if (history.isEmpty()) stringResource(R.string.history_subtitle_empty)
            else stringResource(R.string.history_subtitle_count, history.size)
        ) {
            if (history.isNotEmpty()) {
                IconButton(onClick = { showClearDialog = true }) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = stringResource(R.string.clear_all), tint = MaterialTheme.colorScheme.onBackground)
                }
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.action_settings), tint = MaterialTheme.colorScheme.onBackground)
            }
        }

        when {
            history.isEmpty() -> {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    EmptyState(
                        icon = Icons.Default.History,
                        title = stringResource(R.string.history_empty_title),
                        subtitle = stringResource(R.string.history_empty_desc)
                    )
                }
            }

            else -> {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(R.string.history_search), fontSize = 15.sp, color = AppTheme.colors.textTertiary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AppTheme.colors.textTertiary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear), tint = AppTheme.colors.textTertiary)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = AppTheme.colors.border,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SOURCE_FILTERS.forEach { filter ->
                        AppChip(
                            text = sourceLabel(filter),
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        EmptyState(
                            icon = Icons.Default.SearchOff,
                            title = stringResource(R.string.history_no_match_title),
                            subtitle = stringResource(R.string.history_no_match_desc)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(filtered, key = { it.id }) { item ->
                            HistoryCard(
                                item = item,
                                onCopy = { viewModel.copy(context, item) },
                                onDelete = { viewModel.delete(item.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    item: HistoryEntity,
    onCopy: () -> Unit,
    onDelete: () -> Unit
) {
    SmartCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 6.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SourcePill(source = item.source)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = item.expression,
                    fontFamily = UrbanistFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.result,
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = DateUtils.getRelativeTimeSpanString(
                        item.timestamp,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    ).toString(),
                    fontFamily = UrbanistFontFamily,
                    fontSize = 12.sp,
                    color = AppTheme.colors.textTertiary
                )
            }

            Column {
                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = AppTheme.colors.textTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = AppTheme.colors.textTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SourcePill(source: String) {
    val color = when (source) {
        "Calculator" -> AppTheme.colors.info
        "Smart Solve" -> MaterialTheme.colorScheme.primary
        "Tool" -> AppTheme.colors.success
        else -> MaterialTheme.colorScheme.secondary
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.13f)
    ) {
        Text(
            text = sourceLabel(source),
            fontFamily = UrbanistFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.3.sp,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
