package com.habitbeads.app

import android.content.Context

private const val PreferencesName = "habit_beads_preferences"
private const val ThemeChoiceKey = "theme_choice"
private const val ShowBeadNumbersKey = "show_bead_numbers"

fun loadSavedThemeName(context: Context): String {
    return context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
        .getString(ThemeChoiceKey, "Warm") ?: "Warm"
}

fun saveThemeName(context: Context, themeName: String) {
    context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
        .edit()
        .putString(ThemeChoiceKey, themeName)
        .apply()
}

fun loadShowBeadNumbers(context: Context): Boolean {
    return context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
        .getBoolean(ShowBeadNumbersKey, false)
}

fun saveShowBeadNumbers(context: Context, showNumbers: Boolean) {
    context.getSharedPreferences(PreferencesName, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(ShowBeadNumbersKey, showNumbers)
        .apply()
}
