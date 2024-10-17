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
import com.google.firebase.auth.FirebaseAuth
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

        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return

        // Initialize
        customerCountTextView = view.findViewById(R.id.customerCount)
        opportunitiesCountTextView = view.findViewById(R.id.opportunityCount)
        viewProductsBtn = view.findViewById(R.id.viewProducts_btn)
        viewAnalyticsBtn = view.findViewById(R.id.viewAnalytics_btn)
        lineChart = view.findViewById(R.id.lineChart1)

        // Display a welcome message with the users name for added personalization
        val userEmail = user?.email
        val userName = userEmail?.substringBefore("@") ?: "User"
        view.findViewById<TextView>(R.id.username_tv).text = "Welcome, $userName"

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
        // Create data for the line chart
        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val yValues = listOf(10f, 20f, 15f, 25f, 30f, 40f, 50f, 60f, 70f, 80f, 90f, 100f) // Example Y values

        val entries = months.mapIndexed { index, month ->
            Entry(index.toFloat(), yValues[index])
        }

        // Create a dataset
        val lineDataSet = LineDataSet(entries, "Monthly Data").apply {
            color = 0xFFFF7F50.toInt() // Coral color
            valueTextColor = ColorTemplate.COLORFUL_COLORS[1]
            valueTextSize = 0f
            lineWidth = 3f
            circleRadius = 0f
        }

        // Create LineData object and set it to the chart
        val lineData = LineData(lineDataSet)
        lineChart.data = lineData

        // Customize Y-axis
        lineChart.axisLeft.axisMinimum = 0f // Set minimum Y value
        lineChart.axisLeft.axisMaximum = 100f // Set maximum Y value
        lineChart.axisLeft.setLabelCount(10, false) // Set label count for Y-axis
        lineChart.axisLeft.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: com.github.mikephil.charting.components.AxisBase?): String {
                return if (value % 10 == 0f) value.toInt().toString() else ""
            }
        }

        // Hide grid lines for Y-axis
        lineChart.axisLeft.gridColor = android.graphics.Color.TRANSPARENT // Hide Y-axis grid lines
        lineChart.axisLeft.gridLineWidth = 0f // Set Y-axis grid line width to 0

        lineChart.axisRight.isEnabled = false // Hide the right Y-axis

        // Customize X-axis
        lineChart.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM // Set X-axis position
        lineChart.xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(months) // Set custom labels for X-axis
        lineChart.xAxis.setLabelCount(months.size, true) // Ensure all months are displayed
        lineChart.xAxis.granularity = 1f // Minimum interval for X-axis

        // Hide grid lines for X-axis
        lineChart.xAxis.gridColor = android.graphics.Color.TRANSPARENT // Hide X-axis grid lines
        lineChart.xAxis.gridLineWidth = 0f // Set X-axis grid line width to 0

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
