package com.example.ui.screens.weather

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.CachedWeatherEntity
import com.example.data.database.FavoriteCityEntity
import com.example.data.network.GeocodingResult
import com.example.data.network.WeatherResponse
import com.example.data.repository.SettingsRepository
import com.example.data.repository.TempUnit
import com.example.data.repository.WeatherRepository
import com.example.data.repository.WeatherResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WeatherUiState(
    val searchQuery: String = "",
    val searchResults: List<GeocodingResult> = emptyList(),
    val isSearching: Boolean = false,
    val selectedCityName: String = "هەولێر",
    val selectedCityEnName: String = "Erbil",
    val selectedCountry: String = "Iraq",
    val currentLat: Double = 36.1911,
    val currentLon: Double = 44.0091,
    val weatherData: WeatherResponse? = null,
    val cachedWeather: CachedWeatherEntity? = null,
    val isLoading: Boolean = false,
    val errorMessageKurdish: String? = null,
    val tempUnit: TempUnit = TempUnit.CELSIUS,
    val useKurdishDigits: Boolean = false,
    val favoriteCities: List<FavoriteCityEntity> = emptyList(),
    val isCurrentCityFavorite: Boolean = true
)

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val settingsRepo = SettingsRepository(application)
    private val weatherRepo = WeatherRepository(db)

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                settingsRepo.tempUnit,
                settingsRepo.useKurdishDigits,
                weatherRepo.favoriteCities
            ) { unit, kurdishDigits, favorites ->
                _uiState.update { state ->
                    val isFav = favorites.any { it.name.equals(state.selectedCityEnName, ignoreCase = true) }
                    state.copy(
                        tempUnit = unit,
                        useKurdishDigits = kurdishDigits,
                        favoriteCities = favorites,
                        isCurrentCityFavorite = isFav
                    )
                }
            }.collect()
        }

        // Initial load for Erbil
        loadWeather(
            cityNameKurdish = "هەولێر",
            cityNameEnglish = "Erbil",
            country = "کوردستان - عێراق",
            lat = 36.1911,
            lon = 44.0091
        )
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            delay(400) // Debounce
            val results = weatherRepo.searchCities(query)
            _uiState.update { it.copy(searchResults = results, isSearching = false) }
        }
    }

    fun selectCity(
        cityNameKurdish: String,
        cityNameEnglish: String,
        country: String,
        lat: Double,
        lon: Double
    ) {
        _uiState.update {
            it.copy(
                searchQuery = "",
                searchResults = emptyList(),
                selectedCityName = cityNameKurdish,
                selectedCityEnName = cityNameEnglish,
                selectedCountry = country,
                currentLat = lat,
                currentLon = lon
            )
        }
        loadWeather(cityNameKurdish, cityNameEnglish, country, lat, lon)
    }

    fun refreshWeather() {
        val state = _uiState.value
        loadWeather(
            state.selectedCityName,
            state.selectedCityEnName,
            state.selectedCountry,
            state.currentLat,
            state.currentLon
        )
    }

    private fun loadWeather(
        cityNameKurdish: String,
        cityNameEnglish: String,
        country: String,
        lat: Double,
        lon: Double
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessageKurdish = null) }
            when (val result = weatherRepo.getWeatherData(cityNameEnglish, cityNameKurdish, lat, lon)) {
                is WeatherResult.Success -> {
                    val isFav = _uiState.value.favoriteCities.any { it.name.equals(cityNameEnglish, ignoreCase = true) }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            weatherData = result.data,
                            cachedWeather = null,
                            errorMessageKurdish = null,
                            isCurrentCityFavorite = isFav
                        )
                    }
                }
                is WeatherResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            cachedWeather = result.cachedData,
                            errorMessageKurdish = result.messageKurdish
                        )
                    }
                }
                WeatherResult.Loading -> Unit
            }
        }
    }

    fun toggleFavoriteCurrentCity() {
        viewModelScope.launch {
            val state = _uiState.value
            val existing = state.favoriteCities.find { it.name.equals(state.selectedCityEnName, ignoreCase = true) }
            if (existing != null) {
                weatherRepo.removeFavorite(existing.id)
            } else {
                weatherRepo.addFavorite(
                    FavoriteCityEntity(
                        name = state.selectedCityEnName,
                        kurdishName = state.selectedCityName,
                        country = state.selectedCountry,
                        latitude = state.currentLat,
                        longitude = state.currentLon
                    )
                )
            }
        }
    }

    fun toggleTempUnit() {
        val nextUnit = if (_uiState.value.tempUnit == TempUnit.CELSIUS) TempUnit.FAHRENHEIT else TempUnit.CELSIUS
        settingsRepo.setTempUnit(nextUnit)
    }

    fun formatTemp(celsius: Double): String {
        val converted = if (_uiState.value.tempUnit == TempUnit.FAHRENHEIT) {
            (celsius * 9 / 5) + 32
        } else {
            celsius
        }
        val symbol = if (_uiState.value.tempUnit == TempUnit.FAHRENHEIT) "°F" else "°C"
        val formatted = "${Math.round(converted)}$symbol"
        return formatted
    }
}
