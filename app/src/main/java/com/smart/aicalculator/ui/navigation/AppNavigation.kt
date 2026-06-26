package com.smart.aicalculator.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smart.aicalculator.data.AppSettingsStore
import com.smart.aicalculator.ui.screens.main.MainScreen
import com.smart.aicalculator.ui.screens.onboarding.LanguageScreen
import com.smart.aicalculator.ui.screens.settings.SettingsScreen
import com.smart.aicalculator.ui.screens.splash.SplashScreen
import com.smart.aicalculator.ui.screens.tools.ToolDetailScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    appSettingsStore: AppSettingsStore,
    isSetupCompleted: Boolean?,
    currentLanguage: String
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    // NOTE: `currentLanguage` is now received from the caller (MainActivity)
    // which already collects appSettingsStore.appLanguage. Previously this
    // composable collected it independently, creating a duplicate DataStore
    // listener on every navigation recomposition.

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                isSetupCompleted = isSetupCompleted,
                onNavigateNext = { route ->
                    navController.navigate(route) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // First-launch language selection (shown straight after the splash).
        composable("language_setup") {
            LanguageScreen(
                initialLanguageCode = "system",
                isFromSettings = false,
                onLanguageSelected = { languageCode ->
                    coroutineScope.launch {
                        // Continue on the first-launch language screen saves BOTH
                        // the chosen language and the setup-completed flag, so this
                        // screen is never shown again on subsequent launches.
                        appSettingsStore.setAppLanguage(languageCode)
                        appSettingsStore.setSetupCompleted(true)
                        navController.navigate("main") {
                            popUpTo("language_setup") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(
            route = "main?tab={tab}",
            arguments = listOf(navArgument("tab") { defaultValue = "calculator" })
        ) { backStackEntry ->
            val tab = backStackEntry.arguments?.getString("tab") ?: "calculator"
            MainScreen(
                initialTab = tab,
                onNavigateToToolDetail = { toolId ->
                    navController.navigate("tool_detail/$toolId")
                },
                onOpenSettings = { navController.navigate("settings") }
            )
        }

        composable("settings") {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onLanguageClick = { navController.navigate("language_settings") }
            )
        }

        // Language picker opened from Settings (reusable screen)
        composable("language_settings") {
            LanguageScreen(
                initialLanguageCode = currentLanguage,
                isFromSettings = true,
                onBackClick = { navController.popBackStack() },
                onLanguageSelected = { languageCode ->
                    coroutineScope.launch {
                        appSettingsStore.setAppLanguage(languageCode)
                        navController.popBackStack()
                    }
                }
            )
        }

        composable(
            route = "tool_detail/{toolId}",
            arguments = listOf(navArgument("toolId") { type = NavType.StringType })
        ) { backStackEntry ->
            val toolId = backStackEntry.arguments?.getString("toolId") ?: ""
            ToolDetailScreen(
                toolId = toolId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
