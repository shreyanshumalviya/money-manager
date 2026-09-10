package com.expense.tracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var etWebAppUrl: TextInputEditText
    private lateinit var etSheetName: TextInputEditText
    private lateinit var btnSaveConfig: Button
    private lateinit var btnTestWebhook: Button
    private lateinit var btnOpenQuickAdd: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etWebAppUrl = findViewById(R.id.etWebAppUrl)
        etSheetName = findViewById(R.id.etSheetName)
        btnSaveConfig = findViewById(R.id.btnSaveConfig)
        btnTestWebhook = findViewById(R.id.btnTestWebhook)
        btnOpenQuickAdd = findViewById(R.id.btnOpenQuickAdd)

        // Load saved preferences
        val currentUrl = ExpenseRepository.getWebAppUrl(this)
        if (currentUrl != ExpenseRepository.DEFAULT_WEB_APP_URL) {
            etWebAppUrl.setText(currentUrl)
        }
        etSheetName.setText(ExpenseRepository.getSheetName(this))

        btnSaveConfig.setOnClickListener {
            val url = etWebAppUrl.text.toString().trim()
            val sheetName = etSheetName.text.toString().trim()

            ExpenseRepository.saveWebAppUrl(this, url)
            ExpenseRepository.saveSheetName(this, sheetName)

            Toast.makeText(this, "Settings saved successfully!", Toast.LENGTH_SHORT).show()
        }

        btnTestWebhook.setOnClickListener {
            val url = etWebAppUrl.text.toString().trim()
            if (url.isEmpty()) {
                Toast.makeText(this, "Please enter a valid Apps Script URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnTestWebhook.isEnabled = false
            btnTestWebhook.text = "Testing..."

            lifecycleScope.launch {
                val result = ExpenseRepository.addExpense(
                    context = this@MainActivity,
                    amount = 1.0,
                    category = "Food",
                    note = "Test Ping from App",
                    isMonthly = false
                )
                btnTestWebhook.isEnabled = true
                btnTestWebhook.text = "Test Ping"

                result.onSuccess {
                    Toast.makeText(this@MainActivity, "Connection Successful! Test entry sent.", Toast.LENGTH_LONG).show()
                }.onFailure { err ->
                    Toast.makeText(this@MainActivity, "Error: ${err.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        btnOpenQuickAdd.setOnClickListener {
            val intent = Intent(this, QuickAddActivity::class.java)
            startActivity(intent)
        }
    }
}
