package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.database.CachedWeatherEntity
import com.example.data.database.FavoriteCityEntity
import com.example.data.network.GeocodingResult
import com.example.data.network.NetworkClient
import com.example.data.network.WeatherConditionHelper
import com.example.data.network.WeatherResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

sealed class WeatherResult<out T> {
    data class Success<T>(val data: T, val isFromCache: Boolean = false) : WeatherResult<T>()
    data class Error(val messageKurdish: String, val cachedData: CachedWeatherEntity? = null) : WeatherResult<Nothing>()
    object Loading : WeatherResult<Nothing>()
}

class WeatherRepository(private val database: AppDatabase) {

    val favoriteCities: Flow<List<FavoriteCityEntity>> =
        database.favoriteCityDao().getAllFavorites()

    suspend fun searchCities(query: String): List<GeocodingResult> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        try {
            val response = NetworkClient.geocodingApi.searchCity(name = query.trim())
            response.results ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getWeatherData(
        cityName: String,
        kurdishName: String,
        latitude: Double,
        longitude: Double
    ): WeatherResult<WeatherResponse> = withContext(Dispatchers.IO) {
        try {
            val response = NetworkClient.weatherApi.getForecast(
                latitude = latitude,
                longitude = longitude
            )

            // Cache response in Room
            val current = response.currentWeather
            val daily = response.daily
            if (current != null) {
                val minT = daily?.temperature2mMin?.firstOrNull() ?: (current.temperature - 5)
                val maxT = daily?.temperature2mMax?.firstOrNull() ?: (current.temperature + 5)
                val cond = WeatherConditionHelper.getCondition(current.weatherCode, current.isDay == 1)

                val entity = CachedWeatherEntity(
                    cityName = cityName,
                    kurdishName = kurdishName,
                    temperatureC = current.temperature,
                    minTempC = minT,
                    maxTempC = maxT,
                    weatherCode = current.weatherCode,
                    conditionKurdish = cond.titleKurdish,
                    humidity = response.hourly?.relativeHumidity2m?.firstOrNull() ?: 45,
                    windSpeedKmh = current.windSpeed,
                    lastUpdated = System.currentTimeMillis()
                )
                database.cachedWeatherDao().saveCachedWeather(entity)
            }

            WeatherResult.Success(response)
        } catch (e: Exception) {
            // Retrieve cached data if available
            val cached = database.cachedWeatherDao().getCachedWeather(cityName)
            if (cached != null) {
                WeatherResult.Error(
                    messageKurdish = "پەیوەندی ئینتەرنێت نییە، داتای پاشەکەوتکراو پیشاندەدرێت",
                    cachedData = cached
                )
            } else {
                WeatherResult.Error(
                    messageKurdish = "نەتوانرا زانیاری کەشوهەوا باربکرێت، تکایە ئینتەرنێت بپشکنە",
                    cachedData = null
                )
            }
        }
    }

    suspend fun addFavorite(city: FavoriteCityEntity) = withContext(Dispatchers.IO) {
        database.favoriteCityDao().insertFavorite(city)
    }

    suspend fun removeFavorite(id: Long) = withContext(Dispatchers.IO) {
        database.favoriteCityDao().deleteFavoriteById(id)
    }

    suspend fun getCachedWeather(cityName: String): CachedWeatherEntity? = withContext(Dispatchers.IO) {
        database.cachedWeatherDao().getCachedWeather(cityName)
    }
}
