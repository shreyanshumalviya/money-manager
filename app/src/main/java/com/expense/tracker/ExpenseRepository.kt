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

object ExpenseRepository {

    private const val PREFS_NAME = "expense_prefs"
    private const val KEY_WEB_APP_URL = "web_app_url"
    private const val KEY_SHEET_NAME = "sheet_name"

    // Default placeholder URL (user can set in app UI or replace here)
    const val DEFAULT_WEB_APP_URL = "https://script.google.com/macros/s/YOUR_SCRIPT_ID/exec"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
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
        
        // Dynamic fallback to current month & year (e.g. Sept 2026)
        val sdf = SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
        return sdf.format(Date())
    }

    fun saveSheetName(context: Context, name: String) {
        getPrefs(context).edit().putString(KEY_SHEET_NAME, name.trim()).apply()
    }

    suspend fun addExpense(
        context: Context,
        amount: Double,
        category: String,
        note: String,
        isMonthly: Boolean
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = getWebAppUrl(context)
            if (url.isEmpty() || url == DEFAULT_WEB_APP_URL) {
                return@withContext Result.failure(Exception("Please configure your Google Apps Script URL in settings first!"))
            }

            val targetSheet = getSheetName(context)

            val json = JSONObject().apply {
                put("amount", amount)
                put("category", category)
                put("note", note)
                put("isMonthly", isMonthly)
                put("sheetName", targetSheet)
            }

            val body = json.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success("Added ₹$amount to $category ($targetSheet)")
            } else {
                Result.failure(Exception("Server returned HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
