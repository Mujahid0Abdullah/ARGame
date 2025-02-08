package com.achelmas.numart

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

class LanguageManager {

    companion object {
        fun setLocaleLanguage(context: Context , language: String) {
            val locale = Locale(language)
            Locale.setDefault(locale)

            val config = Configuration()
            config.locale = locale
            context.resources.updateConfiguration(config, context.resources.displayMetrics)

            // Save the selected language
            saveSelectedLanguage(context, language)
        }

        fun setLocal(context: Context) {
            val selectedLanguage = loadSelectedLanguage(context)
            setLocaleLanguage(context, selectedLanguage ?: "")
        }

        fun loadLocale(context: Context) {
            setLocal(context)
        }

        fun saveSelectedLanguage(context: Context , language: String) {
            val preferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
            val editor = preferences.edit()
            editor.putString("Selected_Language", language)
            editor.apply()
        }

        fun loadSelectedLanguage(context: Context) : String? {
            val preferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
            return preferences.getString("Selected_Language", null)
        }
    }

}