package com.expense.tracker

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class CategoryBudget(val name: String, val consumed: Double, val budget: Double)

data class SheetStats(
    val dayOfMonth: Int,
    val expenseTillNow: Double,
    val estimated: Double,
    val onceAMonthSum: Double,
    val dailySum: Double,
    val adjustedRate: Double,
    val adjustedEstimate: Double,
    val totalBudget: Double,
    val totalConsumed: Double,
    val categories: List<CategoryBudget>
)

object ExpenseRepository {

    private const val PREFS_NAME = "expense_prefs"
    private const val KEY_WEB_APP_URL = "web_app_url"
    private const val KEY_SHEET_NAME = "sheet_name"

    const val DEFAULT_WEB_APP_URL = "https://script.google.com/macros/s/YOUR_SCRIPT_ID/exec"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getWebAppUrl(context: Context): String {
        return getPrefs(context).getString(KEY_WEB_APP_URL, DEFAULT_WEB_APP_URL) ?: DEFAULT_WEB_APP_URL
    }

    fun saveWebAppUrl(context: Context, url: String) {
        getPrefs(context).edit().putString(KEY_WEB_APP_URL, url.trim()).apply()
    }

    fun getSheetName(context: Context): String {
        val saved = getPrefs(context).getString(KEY_SHEET_NAME, "") ?: ""
        if (saved.isNotEmpty()) return saved
        val sdf = SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
        return sdf.format(Date())
    }

    fun saveSheetName(context: Context, name: String) {
        getPrefs(context).edit().putString(KEY_SHEET_NAME, name.trim()).apply()
    }

    suspend fun fetchStats(context: Context): Result<SheetStats> = withContext(Dispatchers.IO) {
        try {
            val url = getWebAppUrl(context)
            val sheetName = getSheetName(context)
            val requestUrl = "$url?action=getStats&sheetName=$sheetName"

            val request = Request.Builder().url(requestUrl).get().build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            val json = JSONObject(responseBody)
            if (json.optString("status") == "success") {
                val stats = parseStatsJson(json.getJSONObject("stats"))
                Result.success(stats)
            } else {
                Result.failure(Exception(json.optString("message", "Failed to fetch stats")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addExpense(
        context: Context,
        amount: Double,
        category: String,
        note: String,
        isMonthly: Boolean
    ): Result<Pair<String, SheetStats?>> = withContext(Dispatchers.IO) {
        try {
            val url = getWebAppUrl(context)
            val targetSheet = getSheetName(context)

            val json = JSONObject().apply {
                put("amount", amount)
                put("category", category)
                put("note", note)
                put("isMonthly", isMonthly)
                put("sheetName", targetSheet)
            }

            val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder().url(url).post(body).build()

            val response = client.newCall(request).execute()
            val resStr = response.body?.string() ?: ""
            val resJson = JSONObject(resStr)

            if (resJson.optString("status") == "success") {
                val stats = if (resJson.has("stats")) parseStatsJson(resJson.getJSONObject("stats")) else null
                Result.success(Pair(resJson.optString("message"), stats))
            } else {
                Result.failure(Exception(resJson.optString("message", "Error submitting expense")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseStatsJson(obj: JSONObject): SheetStats {
        val catList = mutableListOf<CategoryBudget>()
        val arr = obj.optJSONArray("categories")
        if (arr != null) {
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                catList.add(
                    CategoryBudget(
                        name = item.optString("name"),
                        consumed = item.optDouble("consumed", 0.0),
                        budget = item.optDouble("budget", 0.0)
                    )
                )
            }
        }

        return SheetStats(
            dayOfMonth = obj.optInt("dayOfMonth", 1),
            expenseTillNow = obj.optDouble("expenseTillNow", 0.0),
            estimated = obj.optDouble("estimated", 0.0),
            onceAMonthSum = obj.optDouble("onceAMonthSum", 0.0),
            dailySum = obj.optDouble("dailySum", 0.0),
            adjustedRate = obj.optDouble("adjustedRate", 0.0),
            adjustedEstimate = obj.optDouble("adjustedEstimate", 0.0),
            totalBudget = obj.optDouble("totalBudget", 60000.0),
            totalConsumed = obj.optDouble("totalConsumed", 0.0),
            categories = catList
        )
    }
}