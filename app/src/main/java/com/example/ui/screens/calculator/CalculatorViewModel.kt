package com.example.ui.screens.calculator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.CalculatorHistoryEntity
import com.example.data.repository.CalculatorRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val expression: String = "",
    val liveResult: String = "",
    val errorMessage: String? = null,
    val isDegreeMode: Boolean = true,
    val isScientificExpanded: Boolean = true,
    val isHistoryOpen: Boolean = false,
    val historyList: List<CalculatorHistoryEntity> = emptyList()
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val calculatorRepo = CalculatorRepository(db)

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            calculatorRepo.historyList.collect { list ->
                _uiState.update { it.copy(historyList = list) }
            }
        }
    }

    fun onInput(char: String) {
        val current = _uiState.value.expression
        val updated = current + char
        _uiState.update { it.copy(expression = updated, errorMessage = null) }
        calculateLiveResult(updated)
    }

    fun onFunction(func: String) {
        val current = _uiState.value.expression
        val updated = "$current$func("
        _uiState.update { it.copy(expression = updated, errorMessage = null) }
        calculateLiveResult(updated)
    }

    fun onBackspace() {
        val current = _uiState.value.expression
        if (current.isNotEmpty()) {
            val updated = current.dropLast(1)
            _uiState.update { it.copy(expression = updated, errorMessage = null) }
            calculateLiveResult(updated)
        }
    }

    fun onClear() {
        _uiState.update {
            it.copy(
                expression = "",
                liveResult = "",
                errorMessage = null
            )
        }
    }

    fun onToggleSign() {
        val current = _uiState.value.expression
        if (current.isEmpty()) return
        if (current.startsWith("-")) {
            val updated = current.removePrefix("-")
            _uiState.update { it.copy(expression = updated) }
            calculateLiveResult(updated)
        } else {
            val updated = "-($current)"
            _uiState.update { it.copy(expression = updated) }
            calculateLiveResult(updated)
        }
    }

    fun onEquals() {
        val expr = _uiState.value.expression
        if (expr.isBlank()) return

        val evaluator = MathEvaluator(isDegreeMode = _uiState.value.isDegreeMode)
        when (val res = evaluator.evaluate(expr)) {
            is MathEvaluator.EvaluationResult.Success -> {
                _uiState.update {
                    it.copy(
                        expression = res.formattedText,
                        liveResult = "",
                        errorMessage = null
                    )
                }
                viewModelScope.launch {
                    calculatorRepo.saveHistory(expr, res.formattedText)
                }
            }
            is MathEvaluator.EvaluationResult.Error -> {
                _uiState.update {
                    it.copy(
                        errorMessage = res.messageKurdish
                    )
                }
            }
        }
    }

    private fun calculateLiveResult(expr: String) {
        if (expr.isBlank()) {
            _uiState.update { it.copy(liveResult = "") }
            return
        }
        val evaluator = MathEvaluator(isDegreeMode = _uiState.value.isDegreeMode)
        when (val res = evaluator.evaluate(expr)) {
            is MathEvaluator.EvaluationResult.Success -> {
                _uiState.update { it.copy(liveResult = res.formattedText) }
            }
            is MathEvaluator.EvaluationResult.Error -> {
                // Keep live result empty or silent during typing
            }
        }
    }

    fun toggleDegreeMode() {
        val newMode = !_uiState.value.isDegreeMode
        _uiState.update { it.copy(isDegreeMode = newMode) }
        calculateLiveResult(_uiState.value.expression)
    }

    fun toggleScientificKeypad() {
        _uiState.update { it.copy(isScientificExpanded = !it.isScientificExpanded) }
    }

    fun toggleHistorySheet() {
        _uiState.update { it.copy(isHistoryOpen = !it.isHistoryOpen) }
    }

    fun useHistoryItem(item: CalculatorHistoryEntity, useResult: Boolean) {
        val target = if (useResult) item.result else item.expression
        _uiState.update {
            it.copy(
                expression = target,
                isHistoryOpen = false,
                errorMessage = null
            )
        }
        calculateLiveResult(target)
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            calculatorRepo.deleteHistoryItem(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            calculatorRepo.clearHistory()
        }
    }
}
