package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.database.CalculatorHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CalculatorRepository(private val database: AppDatabase) {

    val historyList: Flow<List<CalculatorHistoryEntity>> =
        database.calculatorHistoryDao().getAllHistory()

    suspend fun saveHistory(expression: String, result: String) = withContext(Dispatchers.IO) {
        database.calculatorHistoryDao().insertHistory(
            CalculatorHistoryEntity(
                expression = expression,
                result = result,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteHistoryItem(id: Long) = withContext(Dispatchers.IO) {
        database.calculatorHistoryDao().deleteHistoryById(id)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        database.calculatorHistoryDao().clearAllHistory()
    }
}
