package com.expense.tracker

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class QuickAddActivity : AppCompatActivity() {

    private val categories = arrayOf(
        "Food", "Travel", "Subscriptions", "Luxury Purchase", "Household", "Rent", "for others"
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

        tvSheetTarget.text = "Logging to: ${ExpenseRepository.getSheetName(this)}"
        spinnerCategory.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        btnCancel.setOnClickListener { finish() }

        btnSubmit.setOnClickListener {
            val amountStr = etAmount.text.toString().trim()
            if (amountStr.isEmpty()) {
                etAmount.error = "Enter amount"
                return@setOnClickListener
            }

            val amount = amountStr.toDouble()
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

                result.onSuccess { pair ->
                    val stats = pair.second
                    if (stats != null) {
                        showSuccessDialog(amount, category, stats)
                    } else {
                        Toast.makeText(applicationContext, "Added ₹$amount to $category", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }.onFailure { err ->
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Retry"
                    Toast.makeText(applicationContext, "Error: ${err.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showSuccessDialog(amount: Double, category: String, stats: SheetStats) {
        val msg = """
            Entry: ₹${amount.toInt()} ($category)
            
            • Total Consumed: ₹${stats.totalConsumed.toInt()}
            • Once a Month: ₹${stats.onceAMonthSum.toInt()}
            • Adjusted Rate: ₹${stats.adjustedRate.toInt()}/day
            • Month End Estimate: ₹${stats.adjustedEstimate.toInt()}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Expense Added!")
            .setMessage(msg)
            .setPositiveButton("Done") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }
}