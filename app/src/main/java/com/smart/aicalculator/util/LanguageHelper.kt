package com.smart.aicalculator.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * Describes a single supported language option.
 */
data class LanguageOption(
    val code: String,
    val displayName: String,
    val nativeName: String
)

object LanguageHelper {

    /** All supported languages. "system" = follow device setting. */
    val supportedLanguages = listOf(
        LanguageOption("system", "System Default", "📱"),
        LanguageOption("en", "English", "English"),
        LanguageOption("hi", "Hindi", "हिन्दी"),
        LanguageOption("es", "Spanish", "Español"),
        LanguageOption("fr", "French", "Français"),
        LanguageOption("de", "German", "Deutsch"),
        LanguageOption("pt", "Portuguese", "Português")
    )

    /** Return the display name for a language code (e.g. "en" → "English"). */
    fun getDisplayName(code: String): String {
        return supportedLanguages.find { it.code == code }?.displayName ?: "System Default"
    }

    /**
     * Wrap a [Context] so its resources use the chosen locale.
     * When [languageCode] is "system" the device locale is kept unchanged.
     */
    fun wrapContext(context: Context, languageCode: String): Context {
        val locale = if (languageCode == "system") {
            android.content.res.Resources.getSystem().configuration.locales.get(0)
        } else {
            Locale(languageCode)
        }
        
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}
