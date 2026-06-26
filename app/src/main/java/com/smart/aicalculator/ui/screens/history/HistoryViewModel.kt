package com.smart.aicalculator.ui.screens.history

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smart.aicalculator.data.db.AppDatabase
import com.smart.aicalculator.data.db.HistoryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val historyDao = AppDatabase.getDatabase(application).historyDao()

    val history: StateFlow<List<HistoryEntity>> = historyDao.getAllHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun delete(id: Long) {
        viewModelScope.launch { historyDao.deleteHistoryItem(id) }
    }

    fun clearAll() {
        viewModelScope.launch { historyDao.clearAllHistory() }
    }

    fun copy(context: Context, entity: HistoryEntity) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = "${entity.expression} = ${entity.result}"
        clipboard.setPrimaryClip(ClipData.newPlainText("Calculation", text))
        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
    }
}
