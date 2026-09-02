package com.example.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.calendar.KurdishCalendarHelper
import com.example.data.database.AppDatabase
import com.example.data.database.CachedWeatherEntity
import com.example.data.database.FavoriteCityEntity
import com.example.data.network.WeatherResponse
import com.example.data.repository.SettingsRepository
import com.example.data.repository.TempUnit
import com.example.data.repository.WeatherRepository
import com.example.data.repository.WeatherResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class HomeUiState(
    val timeFormatted: String = "",
    val militaryTimeFormatted: String = "1200:15",
    val amPmText: String = "",
    val dayOfWeek: String = "",
    val kurdishDateText: String = "",
    val gregorianDateText: String = "",
    val is24Hour: Boolean = true,
    val isMilitaryTime: Boolean = false,
    val useKurdishDigits: Boolean = false,
    val selectedCityName: String = "هەولێر",
    val selectedCityEnName: String = "Erbil",
    val weatherLoading: Boolean = false,
    val currentWeather: WeatherResponse? = null,
    val cachedWeather: CachedWeatherEntity? = null,
    val weatherErrorMessage: String? = null,
    val tempUnit: TempUnit = TempUnit.CELSIUS,
    val favoriteCities: List<FavoriteCityEntity> = emptyList()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val settingsRepo = SettingsRepository(application)
    private val weatherRepo = WeatherRepository(db)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // Observe preferences
        viewModelScope.launch {
            combine(
                settingsRepo.is24Hour,
                settingsRepo.isMilitaryTime,
                settingsRepo.tempUnit,
                settingsRepo.useKurdishDigits,
                weatherRepo.favoriteCities
            ) { is24, isMilitary, tempUnit, kurdishDigits, favorites ->
                _uiState.update {
                    it.copy(
                        is24Hour = is24,
                        isMilitaryTime = isMilitary,
                        tempUnit = tempUnit,
                        useKurdishDigits = kurdishDigits,
                        favoriteCities = favorites
                    )
                }
            }.collect()
        }

        // Start live ticker
        viewModelScope.launch {
            while (isActive) {
                updateTimeAndDate()
                delay(1000)
            }
        }

        // Load initial weather for Erbil
        loadWeatherForCity("Erbil", "هەولێر", 36.1911, 44.0091)
    }

    private fun updateTimeAndDate() {
        val now = Calendar.getInstance()
        val is24 = _uiState.value.is24Hour
        val isMilitary = _uiState.value.isMilitaryTime
        val useKurdishDigits = _uiState.value.useKurdishDigits

        val pattern = if (isMilitary) "HHmm:ss" else if (is24) "HH:mm:ss" else "hh:mm:ss"
        val sdf = SimpleDateFormat(pattern, Locale.US)
        var timeStr = sdf.format(now.time)

        val milSdf = SimpleDateFormat("HHmm:ss", Locale.US)
        var milTimeStr = milSdf.format(now.time)

        var amPm = ""
        if (!is24 && !isMilitary) {
            val hour = now.get(Calendar.HOUR_OF_DAY)
            amPm = if (hour < 12) "ب.ن" else "د.ن" // بەیانی/دواینیوەڕۆ (AM/PM in Kurdish)
        }

        if (useKurdishDigits) {
            timeStr = KurdishCalendarHelper.toKurdishDigits(timeStr)
            milTimeStr = KurdishCalendarHelper.toKurdishDigits(milTimeStr)
        }

        val kDate = KurdishCalendarHelper.getKurdishDate(now)
        val kDateFormatted = kDate.formatStandard() // e.g., "11 خەرمانان / 2726"
        val gDateFormatted = KurdishCalendarHelper.getGregorianFormattedKurdish(now)
        val dayName = KurdishCalendarHelper.getDayOfWeekKurdish(now)

        _uiState.update {
            it.copy(
                timeFormatted = timeStr,
                militaryTimeFormatted = milTimeStr,
                amPmText = amPm,
                dayOfWeek = dayName,
                kurdishDateText = if (useKurdishDigits) KurdishCalendarHelper.toKurdishDigits(kDateFormatted) else kDateFormatted,
                gregorianDateText = if (useKurdishDigits) KurdishCalendarHelper.toKurdishDigits(gDateFormatted) else gDateFormatted
            )
        }
    }

    fun selectCity(city: FavoriteCityEntity) {
        _uiState.update {
            it.copy(
                selectedCityName = city.kurdishName,
                selectedCityEnName = city.name
            )
        }
        loadWeatherForCity(city.name, city.kurdishName, city.latitude, city.longitude)
    }

    fun refreshWeather() {
        val currentCity = _uiState.value.favoriteCities.find { it.name == _uiState.value.selectedCityEnName }
        if (currentCity != null) {
            loadWeatherForCity(currentCity.name, currentCity.kurdishName, currentCity.latitude, currentCity.longitude)
        } else {
            loadWeatherForCity("Erbil", "هەولێر", 36.1911, 44.0091)
        }
    }

    private fun loadWeatherForCity(name: String, kurdishName: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(weatherLoading = true, weatherErrorMessage = null) }
            when (val result = weatherRepo.getWeatherData(name, kurdishName, lat, lon)) {
                is WeatherResult.Success -> {
                    _uiState.update {
                        it.copy(
                            weatherLoading = false,
                            currentWeather = result.data,
                            cachedWeather = null,
                            weatherErrorMessage = null
                        )
                    }
                }
                is WeatherResult.Error -> {
                    _uiState.update {
                        it.copy(
                            weatherLoading = false,
                            cachedWeather = result.cachedData,
                            weatherErrorMessage = result.messageKurdish
                        )
                    }
                }
                WeatherResult.Loading -> Unit
            }
        }
    }
}
