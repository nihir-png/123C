package com.smart.aicalculator.ui.screens.tools

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smart.aicalculator.ui.components.CoralButton
import com.smart.aicalculator.ui.components.SecondaryButton
import com.smart.aicalculator.ui.components.SmartCard
import com.smart.aicalculator.ui.theme.AppTheme
import com.smart.aicalculator.ui.theme.UrbanistFontFamily
import java.util.Locale

/** Format a number cleanly: drop trailing zeros, 2 decimals otherwise. */
fun formatNumber(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "0"
    return if (value == value.toLong().toDouble()) value.toLong().toString()
    else String.format(Locale.US, "%.2f", value)
}

/** Format with thousands separators (for money). */
fun formatMoney(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "0"
    val rounded = Math.round(value * 100.0) / 100.0
    return if (rounded == rounded.toLong().toDouble())
        String.format(Locale.US, "%,d", rounded.toLong())
    else String.format(Locale.US, "%,.2f", rounded)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    prefix: String? = null,
    suffix: String? = null,
    decimal: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        prefix = if (prefix != null) {
            { Text(prefix, fontWeight = FontWeight.SemiBold) }
        } else null,
        suffix = if (suffix != null) {
            { Text(suffix, color = AppTheme.colors.textTertiary) }
        } else null,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number
        ),
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = AppTheme.colors.border,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = AppTheme.colors.textTertiary,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

/**
 * One polished, reusable segmented control used across every tool screen
 * (GST, EMI, Saving, Percentage, Unit Converter, …).
 *
 * Design (why it stays balanced on every device):
 *  - A stable outer Box with a FIXED height and an even 4.dp grey padding on
 *    ALL four sides — so top/bottom/left/right spacing is always visually equal.
 *  - The inner white pill is sized to EXACTLY one segment (inner width / count)
 *    and full inner height, so it never touches the container edges and keeps
 *    the same 4.dp gap in every position.
 *  - Segment width comes from the measured inner width (BoxWithConstraints), so
 *    the pill slides to `segmentWidth * selectedIndex` — exact, never a guessed
 *    pixel offset that drifts on different screen widths / font scales.
 *  - Labels are equal-weight, centered, single-line with ellipsis so longer
 *    labels (e.g. "X is what % of Y") never break the layout.
 *
 * Visual style is intentionally kept: light grey container, white selected
 * pill, coral (primary) selected text.
 *
 * @param items 2 or 3 segment labels.
 * @param selectedIndex currently selected segment.
 * @param onSelectedChange invoked with the tapped segment index.
 * @param height control height — keep compact (default 44.dp); inline controls
 *   sitting next to a text field can pass 56.dp to align with it.
 */
@Composable
fun AppSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 44.dp
) {
    if (items.isEmpty()) return
    val count = items.size
    val safeIndex = selectedIndex.coerceIn(0, count - 1)

    BoxWithConstraints(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp) // even 4.dp grey padding on all four sides
    ) {
        // maxWidth is the INNER width (container minus the 4.dp padding); each
        // segment is exactly an equal fraction so the pill aligns perfectly.
        val segmentWidth = maxWidth / count
        val pillOffset by animateDpAsState(
            targetValue = segmentWidth * safeIndex,
            animationSpec = tween(220),
            label = "segmentPillOffset"
        )

        // Sliding white pill — exactly one segment wide, full inner height.
        Box(
            modifier = Modifier
                .offset(x = pillOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
        )

        // Equal-weight, centered labels drawn on top of the pill.
        Row(modifier = Modifier.fillMaxSize()) {
            items.forEachIndexed { index, label ->
                val selected = index == safeIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(
                            selected = selected,
                            role = Role.Tab,
                            onClick = { if (index != safeIndex) onSelectedChange(index) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontFamily = UrbanistFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        color = if (selected) MaterialTheme.colorScheme.primary
                        else AppTheme.colors.textTertiary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

/** Input card wrapper with consistent padding/spacing. */
@Composable
fun ToolInputCard(content: @Composable ColumnScope.() -> Unit) {
    SmartCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

/** Reset + Calculate action row. */
@Composable
fun ToolActions(
    onReset: () -> Unit,
    onCalculate: () -> Unit,
    calculateLabel: String = "Calculate"
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SecondaryButton(text = "Reset", onClick = onReset, modifier = Modifier.weight(1f))
        CoralButton(text = calculateLabel, onClick = onCalculate, modifier = Modifier.weight(1f))
    }
}

/** Coral-tinted result card with a title, optional copy action, and content. */
@Composable
fun ToolResultCard(
    title: String,
    modifier: Modifier = Modifier,
    onCopy: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    SmartCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
        border = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.8.sp,
                    color = AppTheme.colors.coralOnTint
                )
                if (onCopy != null) {
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy result",
                            tint = AppTheme.colors.coralOnTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            content()
        }
    }
}

/** The headline value inside a result card. */
@Composable
fun ResultHeadline(value: String) {
    Text(
        text = value,
        fontFamily = UrbanistFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        color = MaterialTheme.colorScheme.onPrimaryContainer
    )
}

/** A label/value breakdown row. */
@Composable
fun ResultRow(
    label: String,
    value: String,
    emphasized: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = UrbanistFontFamily,
            fontSize = if (emphasized) 15.sp else 14.sp,
            fontWeight = if (emphasized) FontWeight.Bold else FontWeight.Medium,
            color = if (emphasized) MaterialTheme.colorScheme.onPrimaryContainer else AppTheme.colors.coralOnTint
        )
        Text(
            text = value,
            fontFamily = UrbanistFontFamily,
            fontSize = if (emphasized) 18.sp else 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun ResultDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
}
