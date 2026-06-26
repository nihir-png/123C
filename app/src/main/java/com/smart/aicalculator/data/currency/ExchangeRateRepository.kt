package com.smart.aicalculator.data.currency

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Fetches live exchange rates from a free, no-API-key public endpoint
 * (open.er-api.com). All rates are returned relative to USD, so any pair can
 * be converted locally via the USD pivot. No key, no paid tier, no analytics.
 */
object ExchangeRateRepository {

    private const val ENDPOINT = "https://open.er-api.com/v6/latest/USD"

    data class RatesResult(
        val rates: Map<String, Double>, // units per 1 USD
        val updatedAt: Long              // epoch millis
    )

    suspend fun fetchLatest(): RatesResult = withContext(Dispatchers.IO) {
        val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
        }
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IllegalStateException("HTTP ${connection.responseCode}")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            if (json.optString("result") != "success") {
                throw IllegalStateException("API returned an error")
            }
            val ratesJson = json.getJSONObject("rates")
            val rates = HashMap<String, Double>(ratesJson.length())
            val keys = ratesJson.keys()
            while (keys.hasNext()) {
                val code = keys.next()
                rates[code] = ratesJson.getDouble(code)
            }
            val updatedAt = json.optLong("time_last_update_unix", 0L)
                .let { if (it > 0) it * 1000L else System.currentTimeMillis() }
            RatesResult(rates, updatedAt)
        } finally {
            connection.disconnect()
        }
    }

    /** Serialize rates to "CODE=rate;CODE=rate" for local caching. */
    fun serialize(rates: Map<String, Double>): String =
        rates.entries.joinToString(";") { "${it.key}=${it.value}" }

    fun deserialize(raw: String): Map<String, Double> {
        if (raw.isBlank()) return emptyMap()
        return raw.split(";").mapNotNull { entry ->
            val parts = entry.split("=")
            val code = parts.getOrNull(0)
            val value = parts.getOrNull(1)?.toDoubleOrNull()
            if (code != null && value != null) code to value else null
        }.toMap()
    }
}
