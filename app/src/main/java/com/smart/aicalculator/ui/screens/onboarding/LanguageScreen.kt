package com.smart.aicalculator.ui.screens.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smart.aicalculator.R
import com.smart.aicalculator.ui.components.CoralButton
import com.smart.aicalculator.ui.theme.AppTheme
import com.smart.aicalculator.ui.theme.UrbanistFontFamily
import com.smart.aicalculator.util.LanguageHelper
import com.smart.aicalculator.util.LanguageOption

/**
 * Reusable language selection screen.
 *
 * Used in two contexts:
 * 1. Onboarding — no back button, "Continue" finishes setup.
 * 2. Settings  — has a back button, "Save" applies the change.
 *
 * @param initialLanguageCode The currently persisted language code.
 * @param isFromSettings      When true, shows a back arrow and uses "Save" as CTA.
 * @param onBackClick         Called when back arrow is tapped (settings mode).
 * @param onLanguageSelected  Called with the chosen language code when user taps CTA.
 */
@Composable
fun LanguageScreen(
    initialLanguageCode: String = "system",
    isFromSettings: Boolean = false,
    onBackClick: () -> Unit = {},
    onLanguageSelected: (String) -> Unit
) {
    var selectedCode by remember { mutableStateOf(initialLanguageCode) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // --- Top bar ---
        if (isFromSettings) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = stringResource(R.string.settings_language),
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- Header icon + title + subtitle (onboarding mode only) ---
        if (!isFromSettings) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Globe icon badge
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.language_choose_title),
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.language_choose_sub),
                    fontFamily = UrbanistFontFamily,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.secondary,
                    lineHeight = 21.sp
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // --- Language list ---
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isFromSettings) {
                Spacer(modifier = Modifier.height(4.dp))
            }

            LanguageHelper.supportedLanguages.forEach { language ->
                LanguageCard(
                    language = language,
                    isSelected = selectedCode == language.code,
                    onClick = { selectedCode = language.code }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // --- Bottom CTA ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CoralButton(
                text = if (isFromSettings) stringResource(R.string.save) else stringResource(R.string.continue_label),
                onClick = { onLanguageSelected(selectedCode) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LanguageCard(
    language: LanguageOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
        else AppTheme.colors.border,
        animationSpec = tween(200),
        label = "langBorder"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface,
        animationSpec = tween(200),
        label = "langBg"
    )

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Native label / emoji
            Text(
                text = language.nativeName,
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.width(80.dp)
            )

            // Display name
            Text(
                text = if (language.code == "system") stringResource(R.string.language_system) else language.displayName,
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )

            // Check indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .then(
                            Modifier.background(
                                color = Color.Transparent,
                                shape = CircleShape
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(
                                color = Color.Transparent,
                                shape = CircleShape
                            )
                            .then(
                                Modifier.background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape
                                )
                            )
                    )
                }
            }
        }
    }
}
