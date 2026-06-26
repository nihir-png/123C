package com.smart.aicalculator.ui.screens.tools

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smart.aicalculator.data.AppSettingsStore
import com.smart.aicalculator.data.currency.ExchangeRateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class CurrencyViewModel(application: Application) : AndroidViewModel(application) {

    enum class Status { LOADING, LIVE, CACHED, ERROR }

    private val store = AppSettingsStore(application)

    private val _rates = MutableStateFlow<Map<String, Double>>(emptyMap())
    val rates: StateFlow<Map<String, Double>> = _rates.asStateFlow()

    private val _updatedAt = MutableStateFlow(0L)
    val updatedAt: StateFlow<Long> = _updatedAt.asStateFlow()

    private val _status = MutableStateFlow(Status.LOADING)
    val status: StateFlow<Status> = _status.asStateFlow()

    init {
        viewModelScope.launch {
            // 1) Show cached rates immediately (works offline).
            val cachedMap = ExchangeRateRepository.deserialize(store.cachedCurrencyRates.first())
            if (cachedMap.isNotEmpty()) {
                _rates.value = cachedMap
                _updatedAt.value = store.currencyRatesUpdatedAt.first()
                _status.value = Status.CACHED
            }
            // 2) Try to refresh with live rates.
            refresh()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            if (_rates.value.isEmpty()) _status.value = Status.LOADING
            try {
                val result = ExchangeRateRepository.fetchLatest()
                _rates.value = result.rates
                _updatedAt.value = result.updatedAt
                _status.value = Status.LIVE
                store.setCurrencyRates(
                    ExchangeRateRepository.serialize(result.rates),
                    result.updatedAt
                )
            } catch (e: Exception) {
                // Offline / failure: keep cached rates if we have them.
                _status.value = if (_rates.value.isEmpty()) Status.ERROR else Status.CACHED
            }
        }
    }

    /** Convert via the USD pivot. Returns null if a rate is unavailable. */
    fun convert(amount: Double, from: String, to: String): Double? {
        val r = _rates.value
        val f = r[from] ?: return null
        val t = r[to] ?: return null
        if (f == 0.0) return null
        return amount / f * t
    }

    fun rateFor(from: String, to: String): Double? = convert(1.0, from, to)
}
