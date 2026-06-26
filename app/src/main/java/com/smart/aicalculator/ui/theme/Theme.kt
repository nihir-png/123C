package com.smart.aicalculator.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = CoralPrimary,
    onPrimary = Color.White,
    primaryContainer = CoralBgTint,
    onPrimaryContainer = CoralOnTint,
    secondary = TextSecondary,
    onSecondary = Color.White,
    secondaryContainer = SurfaceMutedLight,
    onSecondaryContainer = OnMutedLight,
    background = BgLight,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceMutedLight,
    onSurfaceVariant = OnMutedLight,
    outline = BorderLight,
    outlineVariant = BorderLight,
    error = CoralDark
)

private val DarkColorScheme = darkColorScheme(
    primary = CoralPrimary,
    onPrimary = Color.White,
    primaryContainer = CoralBgTintDark,
    onPrimaryContainer = CoralOnTintDark,
    secondary = TextSecondaryDark,
    onSecondary = Color.White,
    secondaryContainer = SurfaceMutedDark,
    onSecondaryContainer = OnMutedDark,
    background = BgDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceMutedDark,
    onSurfaceVariant = OnMutedDark,
    outline = BorderDark,
    outlineVariant = BorderDark,
    error = CoralLight
)

/**
 * Tokens Material 3 does not model directly (tertiary/hint text, hairline
 * borders, semantic status colors). Accessed via [AppTheme.colors].
 */
data class ExtraColors(
    val textTertiary: Color,
    val border: Color,
    val surfaceMuted: Color,
    val onMuted: Color,
    val coralOnTint: Color,
    val success: Color,
    val warning: Color,
    val info: Color
)

private val LightExtraColors = ExtraColors(
    textTertiary = TextTertiary,
    border = BorderLight,
    surfaceMuted = SurfaceMutedLight,
    onMuted = OnMutedLight,
    coralOnTint = CoralOnTint,
    success = SuccessGreen,
    warning = WarningAmber,
    info = InfoBlue
)

private val DarkExtraColors = ExtraColors(
    textTertiary = TextTertiaryDark,
    border = BorderDark,
    surfaceMuted = SurfaceMutedDark,
    onMuted = OnMutedDark,
    coralOnTint = CoralOnTintDark,
    success = SuccessGreen,
    warning = WarningAmber,
    info = InfoBlue
)

val LocalExtraColors = staticCompositionLocalOf { LightExtraColors }

object AppTheme {
    val colors: ExtraColors
        @Composable
        get() = LocalExtraColors.current
}

@Composable
fun AICalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extraColors = if (darkTheme) DarkExtraColors else LightExtraColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()

            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalExtraColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
