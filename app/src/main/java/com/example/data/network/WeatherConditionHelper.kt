package com.example.data.network

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class WeatherConditionInfo(
    val titleKurdish: String,
    val titleEnglish: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val gradientColors: List<Color>
)

object WeatherConditionHelper {

    fun getCondition(code: Int, isDay: Boolean = true): WeatherConditionInfo {
        return when (code) {
            0 -> WeatherConditionInfo(
                titleKurdish = if (isDay) "ئاسمانی ساماڵ" else "شەوێکی ساماڵ",
                titleEnglish = if (isDay) "Sunny / Clear" else "Clear Night",
                icon = Icons.Default.WbSunny,
                primaryColor = Color(0xFFF59E0B),
                gradientColors = if (isDay) listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                else listOf(Color(0xFF1E1B4B), Color(0xFF0F172A))
            )
            1, 2 -> WeatherConditionInfo(
                titleKurdish = "کەمێک هەوراوی",
                titleEnglish = "Partly Cloudy",
                icon = Icons.Default.WbCloudy,
                primaryColor = Color(0xFF38BDF8),
                gradientColors = listOf(Color(0xFF0284C7), Color(0xFF0369A1))
            )
            3 -> WeatherConditionInfo(
                titleKurdish = "هەوراوی تەواو",
                titleEnglish = "Overcast",
                icon = Icons.Default.Cloud,
                primaryColor = Color(0xFF64748B),
                gradientColors = listOf(Color(0xFF475569), Color(0xFF334155))
            )
            45, 48 -> WeatherConditionInfo(
                titleKurdish = "تەماوی",
                titleEnglish = "Foggy",
                icon = Icons.Default.Air,
                primaryColor = Color(0xFF94A3B8),
                gradientColors = listOf(Color(0xFF64748B), Color(0xFF475569))
            )
            51, 53, 55 -> WeatherConditionInfo(
                titleKurdish = "نمە باران",
                titleEnglish = "Drizzle",
                icon = Icons.Default.Grain,
                primaryColor = Color(0xFF06B6D4),
                gradientColors = listOf(Color(0xFF0891B2), Color(0xFF0E7490))
            )
            61, 63, 65 -> WeatherConditionInfo(
                titleKurdish = "باراناوی",
                titleEnglish = "Rainy",
                icon = Icons.Default.Grain,
                primaryColor = Color(0xFF0284C7),
                gradientColors = listOf(Color(0xFF0369A1), Color(0xFF1E3A8A))
            )
            66, 67, 71, 73, 75, 77 -> WeatherConditionInfo(
                titleKurdish = "بەفربارین",
                titleEnglish = "Snow",
                icon = Icons.Default.Grain,
                primaryColor = Color(0xFF93C5FD),
                gradientColors = listOf(Color(0xFF60A5FA), Color(0xFF3B82F6))
            )
            80, 81, 82 -> WeatherConditionInfo(
                titleKurdish = "تاوە باران",
                titleEnglish = "Rain Showers",
                icon = Icons.Default.Grain,
                primaryColor = Color(0xFF2563EB),
                gradientColors = listOf(Color(0xFF1D4ED8), Color(0xFF1E40AF))
            )
            85, 86 -> WeatherConditionInfo(
                titleKurdish = "تاوە بەفر",
                titleEnglish = "Snow Showers",
                icon = Icons.Default.Grain,
                primaryColor = Color(0xFF60A5FA),
                gradientColors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
            )
            95, 96, 99 -> WeatherConditionInfo(
                titleKurdish = "برووسکە و ڕەشەبا",
                titleEnglish = "Thunderstorm",
                icon = Icons.Default.Thunderstorm,
                primaryColor = Color(0xFF7C3AED),
                gradientColors = listOf(Color(0xFF6D28D9), Color(0xFF4C1D95))
            )
            else -> WeatherConditionInfo(
                titleKurdish = "ساماڵ",
                titleEnglish = "Clear",
                icon = Icons.Default.WbSunny,
                primaryColor = Color(0xFFF59E0B),
                gradientColors = listOf(Color(0xFFD97706), Color(0xFFB45309))
            )
        }
    }
}
