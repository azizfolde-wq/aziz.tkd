package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calculator_history")
data class CalculatorHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_cities")
data class FavoriteCityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val kurdishName: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val isDefault: Boolean = false
)

@Entity(tableName = "cached_weather")
data class CachedWeatherEntity(
    @PrimaryKey
    val cityName: String,
    val kurdishName: String,
    val temperatureC: Double,
    val minTempC: Double,
    val maxTempC: Double,
    val weatherCode: Int,
    val conditionKurdish: String,
    val humidity: Int,
    val windSpeedKmh: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)
