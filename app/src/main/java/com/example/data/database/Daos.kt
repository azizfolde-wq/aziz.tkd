package com.example.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculatorHistoryDao {
    @Query("SELECT * FROM calculator_history ORDER BY timestamp DESC LIMIT 100")
    fun getAllHistory(): Flow<List<CalculatorHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: CalculatorHistoryEntity): Long

    @Query("DELETE FROM calculator_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM calculator_history")
    suspend fun clearAllHistory()
}

@Dao
interface FavoriteCityDao {
    @Query("SELECT * FROM favorite_cities ORDER BY id ASC")
    fun getAllFavorites(): Flow<List<FavoriteCityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(city: FavoriteCityEntity): Long

    @Query("DELETE FROM favorite_cities WHERE id = :id")
    suspend fun deleteFavoriteById(id: Long)

    @Query("DELETE FROM favorite_cities WHERE name = :name")
    suspend fun deleteFavoriteByName(name: String)

    @Query("SELECT COUNT(*) FROM favorite_cities")
    suspend fun getFavoritesCount(): Int
}

@Dao
interface CachedWeatherDao {
    @Query("SELECT * FROM cached_weather WHERE cityName = :cityName LIMIT 1")
    suspend fun getCachedWeather(cityName: String): CachedWeatherEntity?

    @Query("SELECT * FROM cached_weather ORDER BY lastUpdated DESC LIMIT 1")
    fun getLatestCachedWeather(): Flow<CachedWeatherEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCachedWeather(weather: CachedWeatherEntity)

    @Query("DELETE FROM cached_weather")
    suspend fun clearWeatherCache()
}
