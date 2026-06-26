package com.smart.aicalculator.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smart.aicalculator.R
import com.smart.aicalculator.data.AppSettingsStore
import com.smart.aicalculator.data.db.AppDatabase
import com.smart.aicalculator.ui.components.AppHeader
import com.smart.aicalculator.ui.components.SectionHeader
import com.smart.aicalculator.ui.components.SmartCard
import com.smart.aicalculator.ui.screens.tools.AppSegmentedControl
import com.smart.aicalculator.ui.theme.AppTheme
import com.smart.aicalculator.ui.theme.UrbanistFontFamily
import com.smart.aicalculator.util.LanguageHelper
import kotlinx.coroutines.launch

private val THEME_MODES = listOf("light", "dark", "system")

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLanguageClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val store = remember { AppSettingsStore(context) }
    val db = remember { AppDatabase.getDatabase(context.applicationContext) }

    val themeMode by store.themeMode.collectAsState(initial = "system")
    val vibrationEnabled by store.isVibrationEnabled.collectAsState(initial = true)
    val appLanguage by store.appLanguage.collectAsState(initial = "system")

    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { db.historyDao().clearAllHistory() }
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
    ) {
        AppHeader(title = stringResource(R.string.settings_title), onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Appearance
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(title = stringResource(R.string.settings_appearance))
                SmartCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.settings_theme),
                            fontFamily = UrbanistFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AppSegmentedControl(
                            items = listOf(
                                stringResource(R.string.theme_light),
                                stringResource(R.string.theme_dark),
                                stringResource(R.string.theme_system)
                            ),
                            selectedIndex = THEME_MODES.indexOf(themeMode).coerceAtLeast(0),
                            onSelectedChange = { index ->
                                scope.launch { store.setThemeMode(THEME_MODES[index]) }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            height = 48.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                SmartCard(modifier = Modifier.fillMaxWidth()) {
                    SettingNavRow(
                        icon = Icons.Default.Language,
                        title = stringResource(R.string.settings_language),
                        subtitle = if (appLanguage == "system") stringResource(R.string.language_system)
                        else LanguageHelper.getDisplayName(appLanguage),
                        onClick = onLanguageClick
                    )
                }
            }

            // Preferences
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(title = stringResource(R.string.settings_preferences))
                SmartCard(modifier = Modifier.fillMaxWidth()) {
                    SettingToggleRow(
                        icon = Icons.Default.Vibration,
                        title = stringResource(R.string.settings_haptic_title),
                        subtitle = stringResource(R.string.settings_haptic_sub),
                        checked = vibrationEnabled,
                        onCheckedChange = { scope.launch { store.setVibrationEnabled(it) } }
                    )
                }
            }

            // Data
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(title = stringResource(R.string.settings_data))
                SmartCard(modifier = Modifier.fillMaxWidth()) {
                    SettingActionRow(
                        icon = Icons.Default.DeleteOutline,
                        title = stringResource(R.string.settings_clear_title),
                        subtitle = stringResource(R.string.settings_clear_sub),
                        destructive = true,
                        onClick = { showClearDialog = true }
                    )
                }
            }

            // About
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(title = stringResource(R.string.settings_about))
                SmartCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = stringResource(R.string.about_title),
                            fontFamily = UrbanistFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.about_version),
                            fontFamily = UrbanistFontFamily,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.about_desc),
                            fontFamily = UrbanistFontFamily,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun SettingToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingIcon(icon)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            SettingTitle(title)
            SettingSubtitle(subtitle)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = AppTheme.colors.border
            )
        )
    }
}

@Composable
private fun SettingActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    destructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingIcon(icon, tint = if (destructive) MaterialTheme.colorScheme.primary else null)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            SettingTitle(title)
            SettingSubtitle(subtitle)
        }
        TextButton(onClick = onClick) {
            Text(stringResource(R.string.clear), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SettingIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color? = null
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint ?: MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingTitle(text: String) {
    Text(
        text = text,
        fontFamily = UrbanistFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun SettingSubtitle(text: String) {
    Text(
        text = text,
        fontFamily = UrbanistFontFamily,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.secondary
    )
}

@Composable
private fun SettingNavRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingIcon(icon)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            SettingTitle(title)
            SettingSubtitle(subtitle)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(22.dp)
        )
    }
}
