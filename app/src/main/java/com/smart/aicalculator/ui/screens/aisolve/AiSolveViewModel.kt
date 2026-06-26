package com.smart.aicalculator.ui.screens.aisolve

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smart.aicalculator.data.db.AppDatabase
import com.smart.aicalculator.data.db.HistoryEntity
import com.smart.aicalculator.solver.LocalSolver
import com.smart.aicalculator.solver.SolveResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SmartSolveUiState {
    object Idle : SmartSolveUiState
    data class Success(val result: SolveResult) : SmartSolveUiState
    data class Unsupported(val message: String) : SmartSolveUiState
}

/**
 * Fully offline natural-language solver. Powered by [LocalSolver] only — no
 * network, no API. Recognizes GST/VAT, discount, bill split, percentage and
 * simple linear equations stated in plain words.
 */
class AiSolveViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val historyDao = db.historyDao()

    private val _uiState = MutableStateFlow<SmartSolveUiState>(SmartSolveUiState.Idle)
    val uiState: StateFlow<SmartSolveUiState> = _uiState.asStateFlow()

    private val _inputQuery = MutableStateFlow("")
    val inputQuery: StateFlow<String> = _inputQuery.asStateFlow()

    fun updateInputQuery(query: String) {
        _inputQuery.value = query
    }

    fun solveQuery(query: String) {
        if (query.isBlank()) return

        val localResult = LocalSolver.solve(query)
        if (localResult != null) {
            _uiState.value = SmartSolveUiState.Success(localResult)
            saveToHistory(localResult)
        } else {
            _uiState.value = SmartSolveUiState.Unsupported(
                "Couldn't read that one yet. Try a phrasing like “18% GST on 2500”, " +
                    "“10% discount on 1999”, “15% of 3500”, “split 2400 between 4”, or “solve 2x + 5 = 15”."
            )
        }
    }

    private fun saveToHistory(result: SolveResult) {
        viewModelScope.launch {
            try {
                historyDao.insertHistory(
                    HistoryEntity(
                        expression = result.query,
                        result = result.finalAnswer,
                        timestamp = System.currentTimeMillis(),
                        source = "Smart Solve"
                    )
                )
            } catch (e: Exception) {
                // Silence DB history failures
            }
        }
    }

    fun copyToClipboard(context: Context, result: SolveResult) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val textToCopy = buildString {
            append("Query: ${result.query}\n")
            append("Answer: ${result.finalAnswer}\n\n")
            append("Steps:\n")
            result.steps.forEachIndexed { index, step ->
                append("${index + 1}. $step\n")
            }
        }
        val clip = ClipData.newPlainText("Smart Solve Result", textToCopy)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied explanation to clipboard", Toast.LENGTH_SHORT).show()
    }
}
