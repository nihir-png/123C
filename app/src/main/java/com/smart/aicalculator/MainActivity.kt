package com.smart.aicalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.smart.aicalculator.data.AppSettingsStore
import com.smart.aicalculator.ui.navigation.AppNavigation
import com.smart.aicalculator.ui.theme.AICalculatorTheme
import com.smart.aicalculator.util.LanguageHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val appSettingsStore = AppSettingsStore(applicationContext)

        setContent {
            val themeMode by appSettingsStore.themeMode.collectAsState(initial = "system")
            // initial = null means "DataStore hasn't loaded yet". The splash waits
            // for a real (non-null) value before deciding where to go, so a returning
            // user is never mis-routed to the language screen on the initial frame.
            val isSetupCompleted by appSettingsStore.isSetupCompleted.collectAsState(initial = null)
            val appLanguage by appSettingsStore.appLanguage.collectAsState(initial = "system")

            val darkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            val context = LocalContext.current
            val localeContext = remember(context, appLanguage) {
                LanguageHelper.wrapContext(context, appLanguage)
            }

            CompositionLocalProvider(
                LocalContext provides localeContext,
                LocalConfiguration provides localeContext.resources.configuration
            ) {
                AICalculatorTheme(darkTheme = darkTheme) {
                    Surface(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AppNavigation(
                            appSettingsStore = appSettingsStore,
                            isSetupCompleted = isSetupCompleted,
                            currentLanguage = appLanguage
                        )
                    }
                }
            }
        }
    }
}
