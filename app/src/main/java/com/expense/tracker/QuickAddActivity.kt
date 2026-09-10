package com.expense.tracker

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class QuickAddActivity : AppCompatActivity() {

    // Categories matching your sheet tabs: Food, Travel, Subscriptions, Luxury Purchase, Household, Rent, for others
    private val categories = arrayOf(
        "Food",
        "Travel",
        "Subscriptions",
        "Luxury Purchase",
        "Household",
        "Rent",
        "for others"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_add)

        val etAmount = findViewById<TextInputEditText>(R.id.etAmount)
        val etNote = findViewById<TextInputEditText>(R.id.etNote)
        val spinnerCategory = findViewById<Spinner>(R.id.spinnerCategory)
        val cbMonthly = findViewById<CheckBox>(R.id.cbMonthly)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        val btnCancel = findViewById<Button>(R.id.btnCancel)
        val tvSheetTarget = findViewById<TextView>(R.id.tvSheetTarget)

        val targetSheet = ExpenseRepository.getSheetName(this)
        tvSheetTarget.text = "Logging to: $targetSheet"

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        spinnerCategory.adapter = adapter

        btnCancel.setOnClickListener {
            finish()
        }

        btnSubmit.setOnClickListener {
            val amountStr = etAmount.text.toString().trim()
            if (amountStr.isEmpty()) {
                etAmount.error = "Please enter amount"
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0) {
                etAmount.error = "Invalid amount"
                return@setOnClickListener
            }

            val category = spinnerCategory.selectedItem.toString()
            val note = etNote.text.toString().trim()
            val isMonthly = cbMonthly.isChecked

            btnSubmit.isEnabled = false
            btnSubmit.text = "Adding..."

            lifecycleScope.launch {
                val result = ExpenseRepository.addExpense(
                    context = applicationContext,
                    amount = amount,
                    category = category,
                    note = note,
                    isMonthly = isMonthly
                )

                result.onSuccess { msg ->
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                    finish()
                }.onFailure { err ->
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Retry"
                    Toast.makeText(applicationContext, "Failed: ${err.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
