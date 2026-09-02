package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CalculatorHistoryEntity::class,
        FavoriteCityEntity::class,
        CachedWeatherEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun calculatorHistoryDao(): CalculatorHistoryDao
    abstract fun favoriteCityDao(): FavoriteCityDao
    abstract fun cachedWeatherDao(): CachedWeatherDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kurdish_utility.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Prepopulate default major Kurdish cities
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getInstance(context).favoriteCityDao()
                                dao.insertFavorite(
                                    FavoriteCityEntity(
                                        name = "Erbil",
                                        kurdishName = "هەولێر",
                                        country = "Iraq",
                                        latitude = 36.1911,
                                        longitude = 44.0091,
                                        isDefault = true
                                    )
                                )
                                dao.insertFavorite(
                                    FavoriteCityEntity(
                                        name = "Sulaymaniyah",
                                        kurdishName = "سلێمانی",
                                        country = "Iraq",
                                        latitude = 35.5668,
                                        longitude = 45.4222
                                    )
                                )
                                dao.insertFavorite(
                                    FavoriteCityEntity(
                                        name = "Duhok",
                                        kurdishName = "دهۆک",
                                        country = "Iraq",
                                        latitude = 36.8679,
                                        longitude = 42.9886
                                    )
                                )
                                dao.insertFavorite(
                                    FavoriteCityEntity(
                                        name = "Halabja",
                                        kurdishName = "هەڵەبجە",
                                        country = "Iraq",
                                        latitude = 35.1778,
                                        longitude = 45.9861
                                    )
                                )
                                dao.insertFavorite(
                                    FavoriteCityEntity(
                                        name = "Kirkuk",
                                        kurdishName = "کەرکووک",
                                        country = "Iraq",
                                        latitude = 35.4681,
                                        longitude = 44.3922
                                    )
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
