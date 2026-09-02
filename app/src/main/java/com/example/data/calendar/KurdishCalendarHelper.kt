package com.example.data.calendar

import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Utility for converting Gregorian dates to Kurdish (Solar Hijri / Jalali) dates
 * and formatting them with authentic Sorani Kurdish day and month names.
 */
object KurdishCalendarHelper {

    // Kurdish month names (Kurdish Solar Hijri / ڕۆژژمێری کوردی)
    val kurdishMonths = listOf(
        "نەورۆز",     // 1. Newroz (Farvardin) - 31 days
        "گوڵان",      // 2. Gulan (Ordibehesht) - 31 days
        "جۆزەردان",   // 3. Jozardan (Khordad) - 31 days
        "پووشپەڕ",    // 4. Pûşper (Tir) - 31 days
        "گەلاوێژ",    // 5. Gelawêj (Mordad) - 31 days
        "خەرمانان",   // 6. Xermanan (Shahrivar) - 31 days
        "ڕەزبەر",     // 7. Rezber (Mehr) - 30 days
        "گەڵارێزان",   // 8. Gellarêzan / Xezellwer (Aban) - 30 days
        "سەرماوەز",   // 9. Sermawez (Azar) - 30 days
        "بەفرانبار",  // 10. Befranbar (Dey) - 30 days
        "ڕێبەندان",   // 11. Rêbendan (Bahman) - 30 days
        "ڕەشەمە"      // 12. Reşeme (Esfand) - 29/30 days
    )

    // Kurdish day names of the week
    val kurdishDaysOfWeek = mapOf(
        Calendar.SATURDAY to "شەممە",
        Calendar.SUNDAY to "یەکشەممە",
        Calendar.MONDAY to "دووشەممە",
        Calendar.TUESDAY to "سێشەممە",
        Calendar.WEDNESDAY to "چوارشەممە",
        Calendar.THURSDAY to "پێنجشەممە",
        Calendar.FRIDAY to "هەینی"
    )

    // Gregorian month names in Kurdish
    val gregorianMonthsKurdish = listOf(
        "کانوونی دووەم", // Jan
        "شوبات",         // Feb
        "ئازار",         // Mar
        "نیسان",         // Apr
        "ئایار",         // May
        "حوزەیران",      // Jun
        "تەممووز",       // Jul
        "ئاب",           // Aug
        "ئەیلوول",       // Sep
        "تشرینی یەکەم",  // Oct
        "تشرینی دووەم",  // Nov
        "کانوونی یەکەم"  // Dec
    )

    data class KurdishDate(
        val year: Int,
        val month: Int, // 1-12
        val day: Int,   // 1-31
        val monthName: String,
        val dayName: String
    ) {
        fun formatStandard(): String = "$day $monthName / $year"
        fun formatWithDay(): String = "$dayName، $day $monthName $year"
    }

    /**
     * Converts a Gregorian date (year, month, day) to Solar Hijri date,
     * and adds 1321 years to the Solar Hijri year to get the official Kurdish Calendar Year (e.g. 1405 + 1321 = 2726).
     */
    fun getKurdishDate(calendar: Calendar = Calendar.getInstance()): KurdishDate {
        val gy = calendar.get(Calendar.YEAR)
        val gm = calendar.get(Calendar.MONTH) + 1 // 1-12
        val gd = calendar.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        val gDaysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        if (isGregorianLeapYear(gy)) {
            gDaysInMonth[2] = 29
        }

        var gy2 = gy - 1600
        var gm2 = gm - 1
        var gd2 = gd - 1

        var gDayNo = 365 * gy2 + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400
        for (i in 1..gm2) {
            gDayNo += gDaysInMonth[i]
        }
        gDayNo += gd2

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        var jd = 0

        if (jDayNo < 186) {
            jm = 1 + (jDayNo / 31)
            jd = 1 + (jDayNo % 31)
        } else {
            jm = 7 + ((jDayNo - 186) / 30)
            jd = 1 + ((jDayNo - 186) % 30)
        }

        // Kurdish year = Solar Hijri year (jy) + 1321
        val kurdishYear = jy + 1321
        val monthName = kurdishMonths.getOrElse(jm - 1) { "" }
        val dayName = kurdishDaysOfWeek[dayOfWeek] ?: ""

        return KurdishDate(
            year = kurdishYear,
            month = jm,
            day = jd,
            monthName = monthName,
            dayName = dayName
        )
    }

    private fun isGregorianLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    fun getGregorianFormattedKurdish(calendar: Calendar = Calendar.getInstance()): String {
        val gy = calendar.get(Calendar.YEAR)
        val gm = calendar.get(Calendar.MONTH) // 0-11
        val gd = calendar.get(Calendar.DAY_OF_MONTH)
        val monthName = gregorianMonthsKurdish.getOrElse(gm) { "" }
        return "$gd $monthName $gy"
    }

    fun getDayOfWeekKurdish(calendar: Calendar = Calendar.getInstance()): String {
        return kurdishDaysOfWeek[calendar.get(Calendar.DAY_OF_WEEK)] ?: ""
    }

    /**
     * Converts western digits 0-9 to Eastern Arabic/Kurdish digits ٠-٩ if enabled
     */
    fun toKurdishDigits(numberStr: String): String {
        val kurdishDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        val builder = StringBuilder()
        for (ch in numberStr) {
            if (ch in '0'..'9') {
                builder.append(kurdishDigits[ch - '0'])
            } else {
                builder.append(ch)
            }
        }
        return builder.toString()
    }
}
