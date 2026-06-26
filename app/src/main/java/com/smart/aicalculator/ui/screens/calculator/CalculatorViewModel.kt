package com.smart.aicalculator.ui.screens.calculator

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smart.aicalculator.data.db.AppDatabase
import com.smart.aicalculator.data.db.HistoryEntity
import com.smart.aicalculator.util.MathParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val historyDao = db.historyDao()

    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _preview = MutableStateFlow("")
    val preview: StateFlow<String> = _preview.asStateFlow()

    private val _isScientificMode = MutableStateFlow(false)
    val isScientificMode: StateFlow<Boolean> = _isScientificMode.asStateFlow()

    init {
        viewModelScope.launch {
            _expression.collectLatest { expr ->
                evaluateRealtime(expr)
            }
        }
    }

    fun toggleScientificMode() {
        _isScientificMode.value = !_isScientificMode.value
    }

    fun setScientificMode(value: Boolean) {
        _isScientificMode.value = value
    }

    fun onKeyPress(key: String) {
        val current = _expression.value
        when (key) {
            "AC" -> {
                _expression.value = ""
                _preview.value = ""
            }
            "DEL" -> {
                if (current.isNotEmpty()) {
                    // Smart delete for functions
                    val functions = listOf("sin(", "cos(", "tan(", "log(", "ln(", "sqrt(")
                    var deleted = false
                    for (func in functions) {
                        if (current.endsWith(func)) {
                            _expression.value = current.substring(0, current.length - func.length)
                            deleted = true
                            break
                        }
                    }
                    if (!deleted) {
                        _expression.value = current.substring(0, current.length - 1)
                    }
                }
            }
            "=" -> {
                if (current.isNotEmpty()) {
                    try {
                        val resultVal = MathParser.evaluate(current)
                        val formatted = MathParser.formatResult(resultVal)
                        if (formatted != "NaN" && formatted != "Infinity") {
                            // Save to database
                            viewModelScope.launch {
                                historyDao.insertHistory(
                                    HistoryEntity(
                                        expression = current,
                                        result = formatted,
                                        timestamp = System.currentTimeMillis(),
                                        source = "Calculator"
                                    )
                                )
                            }
                            _expression.value = formatted
                            _preview.value = ""
                        } else {
                            _preview.value = "Error"
                        }
                    } catch (e: Exception) {
                        _preview.value = "Error"
                    }
                }
            }
            "sin", "cos", "tan", "log", "ln", "sqrt" -> {
                _expression.value = current + "$key("
            }
            "π" -> {
                _expression.value = current + "π"
            }
            "e" -> {
                _expression.value = current + "e"
            }
            "+", "-", "×", "÷", "^" -> {
                if (current.isNotEmpty()) {
                    val lastChar = current.last().toString()
                    val operators = listOf("+", "-", "×", "÷", "^")
                    if (operators.contains(lastChar)) {
                        // Replace last operator
                        _expression.value = current.substring(0, current.length - 1) + key
                    } else {
                        _expression.value = current + key
                    }
                } else if (key == "-") {
                    // Allow unary minus at start
                    _expression.value = "-"
                }
            }
            "±" -> {
                if (current.isNotEmpty()) {
                    if (current.startsWith("-")) {
                        _expression.value = current.removePrefix("-")
                    } else {
                        _expression.value = "-$current"
                    }
                }
            }
            "( )" -> {
                if (current.isNotEmpty()) {
                    val openCount = current.count { it == '(' }
                    val closeCount = current.count { it == ')' }
                    val lastChar = current.last()
                    if (openCount > closeCount && (lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e' || lastChar == '%')) {
                        _expression.value = current + ")"
                    } else {
                        _expression.value = current + "("
                    }
                } else {
                    _expression.value = "("
                }
            }
            else -> {
                // Digits, decimal, brackets, percentage
                _expression.value = current + key
            }
        }
    }

    private fun evaluateRealtime(expr: String) {
        if (expr.isEmpty()) {
            _preview.value = ""
            return
        }

        // Avoid parsing if it ends with an operator or opening parenthesis
        val lastChar = expr.last().toString()
        val pendingSymbols = listOf("+", "-", "×", "÷", "^", "(", "sin(", "cos(", "tan(", "log(", "ln(", "sqrt(")
        if (pendingSymbols.contains(lastChar) || expr.endsWith("sin") || expr.endsWith("cos") || expr.endsWith("tan") || expr.endsWith("log") || expr.endsWith("ln") || expr.endsWith("sqrt")) {
            return
        }

        try {
            val res = MathParser.evaluate(expr)
            val formatted = MathParser.formatResult(res)
            if (formatted != "NaN" && formatted != "Infinity" && formatted != expr) {
                _preview.value = formatted
            } else {
                _preview.value = ""
            }
        } catch (e: Exception) {
            // Silence real-time errors to keep UI clean
            _preview.value = ""
        }
    }

    fun copyToClipboard(context: Context) {
        val textToCopy = if (_preview.value.isNotEmpty()) _preview.value else _expression.value
        if (textToCopy.isNotEmpty()) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Calculation", textToCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied: $textToCopy", Toast.LENGTH_SHORT).show()
        }
    }
}
