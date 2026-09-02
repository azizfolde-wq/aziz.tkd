package com.example.ui.screens.clock

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.calendar.KurdishCalendarHelper
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

enum class ClockTab {
    DIGITAL_CLOCK,
    MILITARY_CLOCK,
    STOPWATCH,
    TIMER,
    WORLD_CLOCK
}

data class WorldCityTime(
    val cityNameKurdish: String,
    val cityNameEnglish: String,
    val timeZoneId: String,
    val timeFormatted: String = "",
    val timeDiff: String = ""
)

data class ClockUiState(
    val currentTab: ClockTab = ClockTab.DIGITAL_CLOCK,
    val timeFormatted: String = "",
    val secondsText: String = "",
    val hoursMinutesText: String = "",
    val amPmText: String = "",
    val kurdishDateFormatted: String = "",
    val gregorianDateFormatted: String = "",
    val dayOfWeek: String = "",
    val is24Hour: Boolean = true,
    val isMilitaryTime: Boolean = false,
    val useKurdishDigits: Boolean = false,
    
    // Military Time specific live fields (1200:15)
    val militaryTimeFormatted: String = "1200:15",
    val militaryHoursMinutes: String = "1200",
    val militarySeconds: String = "15",
    val militaryZuluFormatted: String = "",
    val militaryPhoneticKurdish: String = "",
    val militaryPhoneticEnglish: String = "",
    val militaryZoneName: String = "Charlie (UTC+3 - Kurdistan)",

    // Stopwatch
    val isStopwatchRunning: Boolean = false,
    val stopwatchElapsedMillis: Long = 0L,
    val stopwatchLaps: List<String> = emptyList(),

    // Timer
    val isTimerRunning: Boolean = false,
    val timerTotalSeconds: Int = 300, // 5 min default
    val timerRemainingSeconds: Int = 300,
    val timerProgress: Float = 1f,

    // World Clock
    val worldCities: List<WorldCityTime> = listOf(
        WorldCityTime("هەولێر (کوردستان)", "Erbil", "Asia/Baghdad"),
        WorldCityTime("سلێمانی (کوردستان)", "Sulaymaniyah", "Asia/Baghdad"),
        WorldCityTime("لەندەن (بەریتانیا)", "London", "Europe/London"),
        WorldCityTime("نیویۆرک (ئەمریکا)", "New York", "America/New_York"),
        WorldCityTime("تۆکیۆ (ژاپۆن)", "Tokyo", "Asia/Tokyo"),
        WorldCityTime("دوبەی (ئیمارات)", "Dubai", "Asia/Dubai"),
        WorldCityTime("پاریس (فەرەنسا)", "Paris", "Europe/Paris"),
        WorldCityTime("ئەستەنبوڵ (تورکیا)", "Istanbul", "Europe/Istanbul")
    )
)

class ClockViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepo = SettingsRepository(application)
    private val _uiState = MutableStateFlow(ClockUiState())
    val uiState: StateFlow<ClockUiState> = _uiState.asStateFlow()

    private var stopwatchJob: Job? = null
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                settingsRepo.is24Hour,
                settingsRepo.isMilitaryTime,
                settingsRepo.useKurdishDigits
            ) { is24, isMilitary, kurdishDigits ->
                _uiState.update {
                    it.copy(
                        is24Hour = is24,
                        isMilitaryTime = isMilitary,
                        useKurdishDigits = kurdishDigits
                    )
                }
            }.collect()
        }

        // Live Clock ticker (runs automatically every 1 second)
        viewModelScope.launch {
            while (isActive) {
                updateLiveClock()
                updateWorldClocks()
                delay(1000)
            }
        }
    }

    private fun updateLiveClock() {
        val now = Calendar.getInstance()
        val is24 = _uiState.value.is24Hour
        val isMilitary = _uiState.value.isMilitaryTime
        val useKurdishDigits = _uiState.value.useKurdishDigits

        val pattern = if (isMilitary) "HHmm:ss" else if (is24) "HH:mm:ss" else "hh:mm:ss"
        val sdf = SimpleDateFormat(pattern, Locale.US)
        var timeStr = sdf.format(now.time)

        val hmPattern = if (isMilitary) "HHmm" else if (is24) "HH:mm" else "hh:mm"
        val hmSdf = SimpleDateFormat(hmPattern, Locale.US)
        var hmStr = hmSdf.format(now.time)

        val secSdf = SimpleDateFormat("ss", Locale.US)
        var secStr = secSdf.format(now.time)

        // Military Time Formats (HHmm:ss, e.g. 1200:15)
        val milSdf = SimpleDateFormat("HHmm:ss", Locale.US)
        val milHmSdf = SimpleDateFormat("HHmm", Locale.US)
        val zuluSdf = SimpleDateFormat("HHmm:ss 'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        var rawMilTime = milSdf.format(now.time)
        var rawMilHm = milHmSdf.format(now.time)
        var rawMilSec = secSdf.format(now.time)
        var rawZuluTime = zuluSdf.format(now.time)

        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)
        val second = now.get(Calendar.SECOND)

        val (kuPhonetic, enPhonetic) = formatMilitaryPhonetics(hour, minute, second)

        var amPm = ""
        if (!is24 && !isMilitary) {
            amPm = if (hour < 12) "ب.ن (AM)" else "د.ن (PM)"
        }

        if (useKurdishDigits) {
            timeStr = KurdishCalendarHelper.toKurdishDigits(timeStr)
            hmStr = KurdishCalendarHelper.toKurdishDigits(hmStr)
            secStr = KurdishCalendarHelper.toKurdishDigits(secStr)
            rawMilTime = KurdishCalendarHelper.toKurdishDigits(rawMilTime)
            rawMilHm = KurdishCalendarHelper.toKurdishDigits(rawMilHm)
            rawMilSec = KurdishCalendarHelper.toKurdishDigits(rawMilSec)
            rawZuluTime = KurdishCalendarHelper.toKurdishDigits(rawZuluTime)
        }

        val kDate = KurdishCalendarHelper.getKurdishDate(now).formatStandard()
        val gDate = KurdishCalendarHelper.getGregorianFormattedKurdish(now)
        val day = KurdishCalendarHelper.getDayOfWeekKurdish(now)

        _uiState.update {
            it.copy(
                timeFormatted = timeStr,
                hoursMinutesText = hmStr,
                secondsText = secStr,
                amPmText = amPm,
                militaryTimeFormatted = rawMilTime,
                militaryHoursMinutes = rawMilHm,
                militarySeconds = rawMilSec,
                militaryZuluFormatted = rawZuluTime,
                militaryPhoneticKurdish = kuPhonetic,
                militaryPhoneticEnglish = enPhonetic,
                kurdishDateFormatted = if (useKurdishDigits) KurdishCalendarHelper.toKurdishDigits(kDate) else kDate,
                gregorianDateFormatted = if (useKurdishDigits) KurdishCalendarHelper.toKurdishDigits(gDate) else gDate,
                dayOfWeek = day
            )
        }
    }

    private fun formatMilitaryPhonetics(hour: Int, minute: Int, second: Int): Pair<String, String> {
        val kurdishHourWords = arrayOf(
            "سفڕ سەد", "سفڕ یەک سەد", "سفڕ دوو سەد", "سفڕ سێ سەد", "سفڕ چوار سەد",
            "سفڕ پێنج سەد", "سفڕ شەش سەد", "سفڕ حەوت سەد", "سفڕ هەشت سەد", "سفڕ نۆ سەد",
            "دە سەد", "یازدە سەد", "دوازدە سەد", "سێزدە سەد", "چواردە سەد",
            "پازدە سەد", "شازدە سەد", "حەڤدە سەد", "هەژدە سەد", "نۆزدە سەد",
            "بیست سەد", "بیست و یەک سەد", "بیست و دوو سەد", "بیست و سێ سەد"
        )

        val englishHourWords = arrayOf(
            "Zero Hundred", "Zero One Hundred", "Zero Two Hundred", "Zero Three Hundred", "Zero Four Hundred",
            "Zero Five Hundred", "Zero Six Hundred", "Zero Seven Hundred", "Zero Eight Hundred", "Zero Nine Hundred",
            "Ten Hundred", "Eleven Hundred", "Twelve Hundred", "Thirteen Hundred", "Fourteen Hundred",
            "Fifteen Hundred", "Sixteen Hundred", "Seventeen Hundred", "Eighteen Hundred", "Nineteen Hundred",
            "Twenty Hundred", "Twenty-One Hundred", "Twenty-Two Hundred", "Twenty-Three Hundred"
        )

        val kuH = if (hour in 0..23) kurdishHourWords[hour] else "$hour سەد"
        val enH = if (hour in 0..23) englishHourWords[hour] else "$hour Hundred"

        val kuText = if (minute == 0) {
            "$kuH و $second چرکە"
        } else {
            "$kuH و $minute خولەک و $second چرکە"
        }

        val enText = if (minute == 0) {
            "$enH Hours, $second Seconds"
        } else {
            String.format(Locale.US, "%s %02d, %d Seconds", enH, minute, second)
        }

        return Pair(kuText, enText)
    }

    private fun updateWorldClocks() {
        val is24 = _uiState.value.is24Hour
        val useKurdishDigits = _uiState.value.useKurdishDigits
        val pattern = if (is24) "HH:mm:ss" else "hh:mm:ss a"

        val updated = _uiState.value.worldCities.map { city ->
            val cal = Calendar.getInstance(TimeZone.getTimeZone(city.timeZoneId))
            val sdf = SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone(city.timeZoneId)
            }
            var t = sdf.format(cal.time)
            if (useKurdishDigits) t = KurdishCalendarHelper.toKurdishDigits(t)
            city.copy(timeFormatted = t)
        }

        _uiState.update { it.copy(worldCities = updated) }
    }

    fun setTab(tab: ClockTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    // Stopwatch actions
    fun toggleStopwatch() {
        if (_uiState.value.isStopwatchRunning) {
            stopwatchJob?.cancel()
            _uiState.update { it.copy(isStopwatchRunning = false) }
        } else {
            _uiState.update { it.copy(isStopwatchRunning = true) }
            val startTime = System.currentTimeMillis() - _uiState.value.stopwatchElapsedMillis
            stopwatchJob = viewModelScope.launch {
                while (isActive) {
                    val elapsed = System.currentTimeMillis() - startTime
                    _uiState.update { it.copy(stopwatchElapsedMillis = elapsed) }
                    delay(30)
                }
            }
        }
    }

    fun lapStopwatch() {
        val currentElapsed = _uiState.value.stopwatchElapsedMillis
        val formatted = formatStopwatch(currentElapsed)
        _uiState.update {
            it.copy(stopwatchLaps = listOf(formatted) + it.stopwatchLaps)
        }
    }

    fun resetStopwatch() {
        stopwatchJob?.cancel()
        _uiState.update {
            it.copy(
                isStopwatchRunning = false,
                stopwatchElapsedMillis = 0L,
                stopwatchLaps = emptyList()
            )
        }
    }

    fun formatStopwatch(millis: Long): String {
        val m = (millis / 1000) / 60
        val s = (millis / 1000) % 60
        val ms = (millis % 1000) / 10
        val formatted = String.format(Locale.US, "%02d:%02d.%02d", m, s, ms)
        return if (_uiState.value.useKurdishDigits) KurdishCalendarHelper.toKurdishDigits(formatted) else formatted
    }

    // Timer actions
    fun setTimerDuration(minutes: Int, seconds: Int) {
        val total = (minutes * 60) + seconds
        if (total > 0) {
            _uiState.update {
                it.copy(
                    timerTotalSeconds = total,
                    timerRemainingSeconds = total,
                    timerProgress = 1f
                )
            }
        }
    }

    fun toggleTimer() {
        if (_uiState.value.isTimerRunning) {
            timerJob?.cancel()
            _uiState.update { it.copy(isTimerRunning = false) }
        } else {
            if (_uiState.value.timerRemainingSeconds <= 0) {
                _uiState.update {
                    it.copy(
                        timerRemainingSeconds = it.timerTotalSeconds,
                        timerProgress = 1f
                    )
                }
            }
            _uiState.update { it.copy(isTimerRunning = true) }
            timerJob = viewModelScope.launch {
                while (isActive && _uiState.value.timerRemainingSeconds > 0) {
                    delay(1000)
                    _uiState.update {
                        val rem = it.timerRemainingSeconds - 1
                        val prog = if (it.timerTotalSeconds > 0) rem.toFloat() / it.timerTotalSeconds.toFloat() else 0f
                        it.copy(
                            timerRemainingSeconds = rem,
                            timerProgress = prog.coerceIn(0f, 1f),
                            isTimerRunning = rem > 0
                        )
                    }
                }
            }
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                isTimerRunning = false,
                timerRemainingSeconds = it.timerTotalSeconds,
                timerProgress = 1f
            )
        }
    }

    fun formatTimer(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        val formatted = String.format(Locale.US, "%02d:%02d", m, s)
        return if (_uiState.value.useKurdishDigits) KurdishCalendarHelper.toKurdishDigits(formatted) else formatted
    }
}
