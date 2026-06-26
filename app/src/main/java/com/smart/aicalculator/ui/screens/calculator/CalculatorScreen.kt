package com.smart.aicalculator.ui.screens.calculator

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smart.aicalculator.R
import com.smart.aicalculator.data.AppSettingsStore
import com.smart.aicalculator.ui.components.CalculatorKeyButton
import com.smart.aicalculator.ui.theme.AppTheme
import com.smart.aicalculator.ui.theme.UrbanistFontFamily

private val OPERATORS = setOf("÷", "×", "-", "+", "^")
private val SCI_FUNCTIONS = setOf("sin", "cos", "tan", "log", "ln", "sqrt", "π", "e")
private val UTILITY = setOf("(", ")", "( )", "%", "DEL")

/** A keypad button. [label] is what the user sees; [value] is the token sent
 *  to the calculator engine (lets us show √ / xʸ while keeping engine tokens). */
private data class Key(val label: String, val value: String = label)

private fun k(label: String, value: String = label) = Key(label, value)

// Basic mode: no brackets, has DEL backspace
private val SIMPLE_KEYS = listOf(
    listOf(k("AC"), k("%"), k("DEL"), k("÷")),
    listOf(k("7"), k("8"), k("9"), k("×")),
    listOf(k("4"), k("5"), k("6"), k("-")),
    listOf(k("1"), k("2"), k("3"), k("+")),
    listOf(k("±"), k("0"), k("."), k("="))
)

// Scientific mode: has brackets, trig, etc.
private val SCIENTIFIC_KEYS = listOf(
    listOf(k("sin"), k("cos"), k("tan"), k("log"), k("ln")),
    listOf(k("AC"), k("( )"), k("xʸ", "^"), k("DEL"), k("÷")),
    listOf(k("7"), k("8"), k("9"), k("√", "sqrt"), k("×")),
    listOf(k("4"), k("5"), k("6"), k("π"), k("-")),
    listOf(k("1"), k("2"), k("3"), k("e"), k("+")),
    listOf(k("±"), k("0"), k("."), k("%"), k("="))
)

/**
 * Style holder — plain class, no @Composable cost.
 */
private data class KeyStyle(
    val bg: Color,
    val fg: Color,
    val fontSize: TextUnit,
    val fontWeight: FontWeight
)

/**
 * Pre-compute a map of key→style for the current theme colors.
 * Called once per recomposition (cached via remember), NOT per-button.
 */
@Composable
private fun rememberKeyStyles(): Map<String, KeyStyle> {
    val cs = MaterialTheme.colorScheme
    return remember(cs) {
        val equal = KeyStyle(cs.primary, Color.White, 30.sp, FontWeight.Bold)
        val ac = KeyStyle(cs.surface, cs.primary, 22.sp, FontWeight.Bold)
        val del = KeyStyle(cs.surface, cs.primary, 18.sp, FontWeight.Bold)
        val negate = KeyStyle(cs.surface, cs.secondary, 24.sp, FontWeight.SemiBold)
        val operator = KeyStyle(cs.primaryContainer, cs.primary, 28.sp, FontWeight.Bold)
        val sciFunc = KeyStyle(cs.surface, cs.secondary, 17.sp, FontWeight.SemiBold)
        val utility = KeyStyle(cs.surface, cs.secondary, 20.sp, FontWeight.SemiBold)
        val digit = KeyStyle(cs.surface, cs.onBackground, 26.sp, FontWeight.SemiBold)

        buildMap {
            put("=", equal)
            put("AC", ac)
            put("DEL", del)
            put("±", negate)
            OPERATORS.forEach { put(it, operator) }
            SCI_FUNCTIONS.forEach { put(it, sciFunc) }
            UTILITY.forEach { put(it, utility) }
            // Digits and other keys fall through to `digit` via getOrDefault
        }
    }
}

@Composable
fun CalculatorScreen(
    onOpenSettings: () -> Unit = {},
    viewModel: CalculatorViewModel = viewModel()
) {
    val context = LocalContext.current
    val expression by viewModel.expression.collectAsState()
    val preview by viewModel.preview.collectAsState()
    val isScientificMode by viewModel.isScientificMode.collectAsState()

    // Cache settingsStore so it survives recomposition (the screen now also
    // survives tab switches thanks to MainScreen's visibility approach).
    val settingsStore = remember(context) { AppSettingsStore(context) }
    val hapticsEnabled by settingsStore.isVibrationEnabled.collectAsState(initial = true)

    // Pre-compute all key styles in one pass — avoids calling a @Composable
    // inside the per-button loop.
    val keyStyles = rememberKeyStyles()
    val defaultStyle = KeyStyle(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.onBackground,
        26.sp,
        FontWeight.SemiBold
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top bar: title + Basic/Scientific switch + settings.
        // Layout contract (so the settings icon never shrinks in Sci mode):
        //  - title takes the leftover space (weight) and ellipsizes if needed,
        //  - the Basic/Sci switch keeps a stable fixed width,
        //  - the settings icon has a fixed 48dp touch target with a 24dp glyph.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.tab_calculator),
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            ModeSwitch(
                isScientific = isScientificMode,
                onSelect = { wantScientific ->
                    if (wantScientific != isScientificMode) viewModel.toggleScientificMode()
                }
            )

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Display area (borderless, airy)
        CalculatorDisplay(
            expression = expression,
            preview = preview,
            onCopy = { viewModel.copyToClipboard(context) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Keypad
        Keypad(
            keys = if (isScientificMode) SCIENTIFIC_KEYS else SIMPLE_KEYS,
            keyStyles = keyStyles,
            defaultStyle = defaultStyle,
            onKeyPress = viewModel::onKeyPress,
            hapticsEnabled = hapticsEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (isScientificMode) 2.9f else 2.5f)
        )

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun CalculatorDisplay(
    expression: String,
    preview: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val exprFontSize = when {
        expression.length > 18 -> 32.sp
        expression.length > 11 -> 42.sp
        else -> 54.sp
    }

    // Keep the most recently typed characters visible on long expressions.
    val exprScrollState = rememberScrollState()
    LaunchedEffect(expression) {
        exprScrollState.scrollTo(exprScrollState.maxValue)
    }

    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        // Typed expression / result — the primary focus
        Text(
            text = expression.ifEmpty { "0" },
            fontFamily = UrbanistFontFamily,
            fontSize = exprFontSize,
            fontWeight = FontWeight.SemiBold,
            color = if (expression.isEmpty()) AppTheme.colors.textTertiary else MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(exprScrollState)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Live result preview + copy
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (preview.isNotEmpty() || expression.isNotEmpty()) {
                Surface(
                    onClick = onCopy,
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.height(36.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy result",
                            tint = AppTheme.colors.textTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.size(36.dp))
            }

            Text(
                text = if (preview.isNotEmpty()) "= $preview" else "",
                fontFamily = UrbanistFontFamily,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
            )
        }
    }
}

@Composable
private fun Keypad(
    keys: List<List<Key>>,
    keyStyles: Map<String, KeyStyle>,
    defaultStyle: KeyStyle,
    onKeyPress: (String) -> Unit,
    hapticsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        keys.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { key ->
                    // Lookup from pre-computed map — no @Composable call per button.
                    val style = keyStyles[key.value] ?: defaultStyle
                    CalculatorKeyButton(
                        text = key.label,
                        onClick = { onKeyPress(key.value) },
                        backgroundColor = style.bg,
                        textColor = style.fg,
                        fontSize = style.fontSize,
                        fontWeight = style.fontWeight,
                        hapticFeedbackEnabled = hapticsEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeSwitch(
    isScientific: Boolean,
    onSelect: (Boolean) -> Unit
) {
    // Fixed-size segmented control. A fixed width keeps the two segments exactly
    // equal and stops the control resizing between Basic/Sci (the old version
    // sized each segment to its text, so "Basic" and "Sci" were unequal).
    BoxWithConstraints(
        modifier = Modifier
            .width(152.dp)
            .height(40.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(50)
            )
            .padding(4.dp) // even 4.dp grey padding on all four sides
    ) {
        // maxWidth here is the INNER width (container minus the 4.dp padding),
        // so each segment is exactly half and the pill slides exactly 50%.
        val segmentWidth = maxWidth / 2
        val pillOffset by animateDpAsState(
            targetValue = if (isScientific) segmentWidth else 0.dp,
            animationSpec = tween(220),
            label = "modePillOffset"
        )

        // Sliding white pill: exactly half the inner width, full inner height,
        // so it keeps an even 4.dp gap from the grey container in both states
        // and never touches the outer edges.
        Box(
            modifier = Modifier
                .offset(x = pillOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(50)
                )
        )

        // Two equal-weight, centered labels drawn on top of the pill.
        Row(modifier = Modifier.fillMaxSize()) {
            ModeSwitchSegment(
                label = stringResource(R.string.mode_basic),
                selected = !isScientific,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(false) }
            )
            ModeSwitchSegment(
                label = stringResource(R.string.mode_sci),
                selected = isScientific,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(true) }
            )
        }
    }
}

@Composable
private fun ModeSwitchSegment(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .selectable(
                selected = selected,
                role = Role.Tab,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = UrbanistFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = if (selected) MaterialTheme.colorScheme.primary else AppTheme.colors.textTertiary
        )
    }
}
