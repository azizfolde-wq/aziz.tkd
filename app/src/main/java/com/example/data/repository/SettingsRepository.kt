package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class TempUnit {
    CELSIUS,
    FAHRENHEIT
}

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

    private val _is24Hour = MutableStateFlow(prefs.getBoolean(KEY_IS_24_HOUR, true))
    val is24Hour: StateFlow<Boolean> = _is24Hour.asStateFlow()

    private val _tempUnit = MutableStateFlow(
        if (prefs.getString(KEY_TEMP_UNIT, "C") == "F") TempUnit.FAHRENHEIT else TempUnit.CELSIUS
    )
    val tempUnit: StateFlow<TempUnit> = _tempUnit.asStateFlow()

    private val _themeMode = MutableStateFlow(
        when (prefs.getString(KEY_THEME_MODE, "SYSTEM")) {
            "LIGHT" -> AppThemeMode.LIGHT
            "DARK" -> AppThemeMode.DARK
            else -> AppThemeMode.SYSTEM
        }
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _useKurdishDigits = MutableStateFlow(prefs.getBoolean(KEY_KURDISH_DIGITS, false))
    val useKurdishDigits: StateFlow<Boolean> = _useKurdishDigits.asStateFlow()

    private val _isMilitaryTime = MutableStateFlow(prefs.getBoolean(KEY_MILITARY_TIME, false))
    val isMilitaryTime: StateFlow<Boolean> = _isMilitaryTime.asStateFlow()

    private val _kurdishCalendarFormat = MutableStateFlow(prefs.getString(KEY_CALENDAR_FORMAT, "FULL") ?: "FULL")
    val kurdishCalendarFormat: StateFlow<String> = _kurdishCalendarFormat.asStateFlow()

    private val _appLanguage = MutableStateFlow(prefs.getString(KEY_APP_LANG, "ku") ?: "ku")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    fun set24HourFormat(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_IS_24_HOUR, enabled).apply()
        _is24Hour.value = enabled
    }

    fun setMilitaryTime(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MILITARY_TIME, enabled).apply()
        _isMilitaryTime.value = enabled
    }

    fun setTempUnit(unit: TempUnit) {
        prefs.edit().putString(KEY_TEMP_UNIT, if (unit == TempUnit.FAHRENHEIT) "F" else "C").apply()
        _tempUnit.value = unit
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    fun setUseKurdishDigits(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_KURDISH_DIGITS, enabled).apply()
        _useKurdishDigits.value = enabled
    }

    fun setKurdishCalendarFormat(format: String) {
        prefs.edit().putString(KEY_CALENDAR_FORMAT, format).apply()
        _kurdishCalendarFormat.value = format
    }

    fun setAppLanguage(lang: String) {
        prefs.edit().putString(KEY_APP_LANG, lang).apply()
        _appLanguage.value = lang
    }

    companion object {
        private const val KEY_IS_24_HOUR = "key_is_24_hour"
        private const val KEY_MILITARY_TIME = "key_military_time"
        private const val KEY_TEMP_UNIT = "key_temp_unit"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_KURDISH_DIGITS = "key_kurdish_digits"
        private const val KEY_CALENDAR_FORMAT = "key_calendar_format"
        private const val KEY_APP_LANG = "key_app_lang"
    }
}
