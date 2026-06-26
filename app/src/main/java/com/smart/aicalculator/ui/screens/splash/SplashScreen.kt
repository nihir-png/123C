package com.smart.aicalculator.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smart.aicalculator.R
import com.smart.aicalculator.ui.theme.UrbanistFontFamily
import kotlinx.coroutines.delay

/**
 * Clean, premium splash screen.
 *
 * Shows the real app icon (custom_app_icon.png) and the "Smart Calculator"
 * wordmark on the light theme background — minimal, elegant, no clutter.
 *
 * Launch flow: Splash -> (Language on first launch | Home if setup is done).
 */
@Composable
fun SplashScreen(
    isSetupCompleted: Boolean?,
    onNavigateNext: (String) -> Unit
) {
    val scale = remember { Animatable(0.85f) }

    // rememberUpdatedState keeps the latest flag value visible to the long-lived
    // LaunchedEffect coroutine. Without it the effect would capture the value from
    // the first frame (still loading -> false) and route to the language screen on
    // every launch, even for returning users.
    val setupCompleted = rememberUpdatedState(isSetupCompleted)

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 700)
        )
        delay(1100)
        // Wait until DataStore has actually loaded the flag (non-null), so the
        // decision is based on the persisted value, not the initial placeholder.
        val completed = snapshotFlow { setupCompleted.value }.filterNotNull().first()
        // First launch -> language selection; afterwards go straight to Home.
        if (completed) {
            onNavigateNext("main")
        } else {
            onNavigateNext("language_setup")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.scale(scale.value)
        ) {
            // App icon — the exact launcher artwork, softly elevated for a
            // premium "floating" feel.
            Image(
                painter = painterResource(id = R.drawable.custom_app_icon),
                contentDescription = null,
                modifier = Modifier
                    .size(104.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(26.dp),
                        clip = false
                    )
                    .clip(RoundedCornerShape(26.dp))
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Smart Calculator",
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = UrbanistFontFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Calculator & Smart Tools",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = UrbanistFontFamily,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
