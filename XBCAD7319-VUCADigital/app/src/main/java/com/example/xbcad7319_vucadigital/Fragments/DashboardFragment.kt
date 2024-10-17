package com.example.xbcad7319_vucadigital.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.db.SupabaseHelper
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardFragment : Fragment() {

    private lateinit var customerCountTextView: TextView
    private lateinit var opportunitiesCountTextView: TextView
    private lateinit var viewProductsBtn: CardView
    private lateinit var viewAnalyticsBtn: CardView
    private lateinit var lineChart: LineChart
    private val supabaseHelper = SupabaseHelper()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize
        customerCountTextView = view.findViewById(R.id.customerCount)
        opportunitiesCountTextView = view.findViewById(R.id.opportunityCount)
        viewProductsBtn = view.findViewById(R.id.viewProducts_btn)
        viewAnalyticsBtn = view.findViewById(R.id.viewAnalytics_btn)
        lineChart = view.findViewById(R.id.lineChart1)

        // Set onClickListeners
        viewProductsBtn.setOnClickListener {
            openProductsFragment()
        }

        viewAnalyticsBtn.setOnClickListener {
            openAnalyticsFragment()
        }

        // Fetch and display customer/opportunities count
        fetchAndDisplayCustomerCount()
        fetchAndDisplayOpportunitiesCount()

        setupLineChart()
    }

    private fun fetchAndDisplayCustomerCount() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val customerCount = supabaseHelper.getCustomerCount()
                withContext(Dispatchers.Main) {
                    customerCountTextView.text = "$customerCount"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    customerCountTextView.text = "Null"
                }
            }
        }
    }

    private fun fetchAndDisplayOpportunitiesCount() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val opportunitiesCount = supabaseHelper.getOpportunityCount()
                withContext(Dispatchers.Main) {
                    opportunitiesCountTextView.text = "$opportunitiesCount"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    opportunitiesCountTextView.text = "Null"
                }
            }
        }
    }

    private fun setupLineChart() {
        // Create fake data for the line chart
        val xValues = listOf(1f, 2f, 3f, 4f, 5f) // Example X values (e.g., months)
        val yValues = listOf(10f, 20f, 15f, 25f, 30f) // Example Y values (e.g., budget amounts)

        val entries = xValues.mapIndexed { index, xValue ->
            Entry(xValue, yValues[index]) // Create entries from fake data
        }

        // Create a dataset
        val lineDataSet = LineDataSet(entries, "Monthly Data").apply {
            color = ColorTemplate.COLORFUL_COLORS[0]
            valueTextColor = ColorTemplate.COLORFUL_COLORS[1]
            valueTextSize = 12f
            lineWidth = 2f
            circleRadius = 5f
            setCircleColor(ColorTemplate.COLORFUL_COLORS[2])
        }

        // Create LineData object and set it to the chart
        val lineData = LineData(lineDataSet)
        lineChart.data = lineData

        lineChart.axisLeft.axisMinimum = 0f // Set minimum Y value
        lineChart.axisRight.isEnabled = false // Hide the right Y-axis
        lineChart.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM // Set X-axis position
        lineChart.description.isEnabled = false // Hide the description
        lineChart.animateX(2000) // Animate the chart
        lineChart.invalidate() // Refresh the chart
    }

    private fun openProductsFragment() {
        val fragment = ProductsFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun openAnalyticsFragment() {
        val fragment = AnalyticsFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
