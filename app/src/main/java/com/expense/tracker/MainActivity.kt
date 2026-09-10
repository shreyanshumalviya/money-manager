package com.expense.tracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var etWebAppUrl: TextInputEditText
    private lateinit var etSheetName: TextInputEditText
    private lateinit var btnSaveConfig: Button
    private lateinit var btnFetchStats: MaterialButton
    private lateinit var btnOpenQuickAdd: Button
    private lateinit var pieChartView: PieChartView

    // Metric text views
    private lateinit var tvTotalConsumed: TextView
    private lateinit var tvOnceAMonth: TextView
    private lateinit var tvAdjustedRate: TextView
    private lateinit var tvAdjustedEstimate: TextView
    private lateinit var layoutCategoryDetails: LinearLayout
    private lateinit var statsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etWebAppUrl = findViewById(R.id.etWebAppUrl)
        etSheetName = findViewById(R.id.etSheetName)
        btnSaveConfig = findViewById(R.id.btnSaveConfig)
        btnFetchStats = findViewById(R.id.btnFetchStats)
        btnOpenQuickAdd = findViewById(R.id.btnOpenQuickAdd)
        pieChartView = findViewById(R.id.pieChartView)

        tvTotalConsumed = findViewById(R.id.tvTotalConsumed)
        tvOnceAMonth = findViewById(R.id.tvOnceAMonth)
        tvAdjustedRate = findViewById(R.id.tvAdjustedRate)
        tvAdjustedEstimate = findViewById(R.id.tvAdjustedEstimate)
        layoutCategoryDetails = findViewById(R.id.layoutCategoryDetails)
        statsContainer = findViewById(R.id.statsContainer)

        val currentUrl = ExpenseRepository.getWebAppUrl(this)
        if (currentUrl != ExpenseRepository.DEFAULT_WEB_APP_URL) {
            etWebAppUrl.setText(currentUrl)
        }
        etSheetName.setText(ExpenseRepository.getSheetName(this))

        btnSaveConfig.setOnClickListener {
            ExpenseRepository.saveWebAppUrl(this, etWebAppUrl.text.toString().trim())
            ExpenseRepository.saveSheetName(this, etSheetName.text.toString().trim())
            Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show()
        }

        btnFetchStats.setOnClickListener {
            fetchStatsNow()
        }

        btnOpenQuickAdd.setOnClickListener {
            startActivity(Intent(this, QuickAddActivity::class.java))
        }

        // Auto-fetch on app open
        if (currentUrl != ExpenseRepository.DEFAULT_WEB_APP_URL) {
            fetchStatsNow()
        }
    }

    private fun fetchStatsNow() {
        btnFetchStats.isEnabled = false
        btnFetchStats.text = "Fetching..."

        lifecycleScope.launch {
            val result = ExpenseRepository.fetchStats(this@MainActivity)
            btnFetchStats.isEnabled = true
            btnFetchStats.text = "Refresh Sheet Stats"

            result.onSuccess { stats ->
                updateStatsUI(stats)
            }.onFailure { err ->
                Toast.makeText(this@MainActivity, "Error: ${err.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateStatsUI(stats: SheetStats) {
        statsContainer.visibility = View.VISIBLE
        tvTotalConsumed.text = "₹${stats.totalConsumed.toInt()}"
        tvOnceAMonth.text = "₹${stats.onceAMonthSum.toInt()}"
        tvAdjustedRate.text = "₹${stats.adjustedRate.toInt()}/day"
        tvAdjustedEstimate.text = "₹${stats.adjustedEstimate.toInt()}"

        // Update Pie Chart
        val chartData = stats.categories.map { Pair(it.name, it.consumed) }
        pieChartView.setData(chartData)

        // Populate Category Details list
        layoutCategoryDetails.removeAllViews()
        for (cat in stats.categories) {
            val row = layoutInflater.inflate(R.layout.item_category_stat, layoutCategoryDetails, false)
            val tvName = row.findViewById<TextView>(R.id.tvCatName)
            val tvAmounts = row.findViewById<TextView>(R.id.tvCatAmount)
            val percent = if (cat.budget > 0) ((cat.consumed / cat.budget) * 100).toInt() else 0

            tvName.text = cat.name
            tvAmounts.text = "₹${cat.consumed.toInt()} / ₹${cat.budget.toInt()} ($percent%)"
            layoutCategoryDetails.addView(row)
        }
    }
}