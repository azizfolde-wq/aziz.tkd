package com.example.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.repository.AppThemeMode
import com.example.data.repository.CalculatorRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.TempUnit
import com.example.data.repository.WeatherRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val is24Hour: Boolean = true,
    val isMilitaryTime: Boolean = false,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val tempUnit: TempUnit = TempUnit.CELSIUS,
    val useKurdishDigits: Boolean = false,
    val kurdishCalendarFormat: String = "FULL",
    val appLanguage: String = "ku",
    val historyCount: Int = 0,
    val favoritesCount: Int = 0,
    val messageSnackbar: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val settingsRepo = SettingsRepository(application)
    private val calculatorRepo = CalculatorRepository(db)
    private val weatherRepo = WeatherRepository(db)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsRepo.is24Hour,
                settingsRepo.isMilitaryTime,
                settingsRepo.themeMode,
                settingsRepo.tempUnit,
                settingsRepo.useKurdishDigits
            ) { is24, military, theme, unit, kurdishDigits ->
                _uiState.update {
                    it.copy(
                        is24Hour = is24,
                        isMilitaryTime = military,
                        themeMode = theme,
                        tempUnit = unit,
                        useKurdishDigits = kurdishDigits
                    )
                }
            }.collect()
        }

        viewModelScope.launch {
            settingsRepo.kurdishCalendarFormat.collect { format ->
                _uiState.update { it.copy(kurdishCalendarFormat = format) }
            }
        }

        viewModelScope.launch {
            calculatorRepo.historyList.collect { list ->
                _uiState.update { it.copy(historyCount = list.size) }
            }
        }

        viewModelScope.launch {
            weatherRepo.favoriteCities.collect { list ->
                _uiState.update { it.copy(favoritesCount = list.size) }
            }
        }
    }

    fun set24Hour(enabled: Boolean) {
        settingsRepo.set24HourFormat(enabled)
    }

    fun setMilitaryTime(enabled: Boolean) {
        settingsRepo.setMilitaryTime(enabled)
    }

    fun setThemeMode(mode: AppThemeMode) {
        settingsRepo.setThemeMode(mode)
    }

    fun setTempUnit(unit: TempUnit) {
        settingsRepo.setTempUnit(unit)
    }

    fun setUseKurdishDigits(enabled: Boolean) {
        settingsRepo.setUseKurdishDigits(enabled)
    }

    fun setCalendarFormat(format: String) {
        settingsRepo.setKurdishCalendarFormat(format)
    }

    fun clearCalculatorHistory() {
        viewModelScope.launch {
            calculatorRepo.clearHistory()
            _uiState.update { it.copy(messageSnackbar = "مێژووی ژمێرەر بە سەرکەوتوویی سڕایەوە") }
        }
    }

    fun clearWeatherCache() {
        viewModelScope.launch {
            db.cachedWeatherDao().clearWeatherCache()
            _uiState.update { it.copy(messageSnackbar = "داتای پاشەکەوتکراوی کەشوهەوا سڕایەوە") }
        }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(messageSnackbar = null) }
    }
}
