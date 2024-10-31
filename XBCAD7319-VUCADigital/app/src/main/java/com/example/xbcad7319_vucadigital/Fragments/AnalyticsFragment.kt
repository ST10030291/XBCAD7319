package com.example.xbcad7319_vucadigital.Fragments

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.xbcad7319_vucadigital.Activites.DashboardActivity
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.db.SupabaseHelper
import com.example.xbcad7319_vucadigital.models.CustomBarChartMarkerView
import com.example.xbcad7319_vucadigital.models.CustomLineChartMarkerView
import com.example.xbcad7319_vucadigital.models.CustomerModel
import com.example.xbcad7319_vucadigital.models.OpportunityModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnalyticsFragment : Fragment() {

    private val sbHelper = SupabaseHelper()
    private lateinit var barChart: BarChart
    private lateinit var lineChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_analytics, container, false)
        barChart = view.findViewById(R.id.barChart)
        lineChart = view.findViewById(R.id.lineChart2)

        getOpportunities()

        getCustomers()

        return view
    }

    // StackOverflow post
    // Title: How to use LifecycleScope to execute coroutine
    // Posted by: Arpit Shukla
    // Available at: https://stackoverflow.com/questions/70058423/how-to-use-lifecyclescope-to-execute-coroutine
    private fun getOpportunities() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val opportunities = sbHelper.getAllOpportunities()
                withContext(Dispatchers.Main) {
                    displayBarChart(opportunities)
                }
            } catch (e: Exception) {
                Log.e("TaskLoadError", "Error loading tasks", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Couldn't load tasks from DB", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayBarChart(opportunities: List<OpportunityModel>) {
        val stageData = getStageData(opportunities)
        val barEntries = createBarEntries(stageData)
        setupBarChart(barEntries, stageData.keys.toList())
    }

    // StackOverflow post
    // Title: Kotlin - from a list of Maps, to a map grouped by key
    // Posted by: Constantin Chernishov
    // Available at: https://stackoverflow.com/questions/53433108/kotlin-from-a-list-of-maps-to-a-map-grouped-by-key
    private fun getStageData(opportunities: List<OpportunityModel>): Map<String, Double> {
        return opportunities.groupBy { it.Stage }
            .mapValues { entry -> entry.value.sumOf { it.TotalValue } }
    }

    // StackOverflow post
    // Title: Why kotlin map.indexed returns List<Any> insteand of List<MyObject>
    // Posted by: Mikezx6r
    // Available at: https://stackoverflow.com/questions/53841789/why-kotlin-map-indexed-returns-listany-insteand-of-listmyobject
    private fun createBarEntries(stageData: Map<String, Double>): List<BarEntry> {
        return stageData.entries.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value.toFloat())
        }
    }

    // GitHub code
    // Repo name: MPAndroidChart
    // Pushed by: danielgindi
    // Available at: https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/main/java/com/xxmassdeveloper/mpchartexample/BarChartActivityMultiDataset.java
    private fun setupBarChart(barEntries: List<BarEntry>, labels: List<String>) {
        val barDataSet = BarDataSet(barEntries, "").apply {
            color = Color.parseColor("#E8715C")
            valueTextColor = Color.BLACK
            valueTextSize = 16f
            // Disable value labels above bars
            setDrawValues(false)
        }

        val barData = BarData(barDataSet).apply {
            barWidth = 0.1f
        }

        barChart.data = barData

        // Remove the description
        barChart.legend.isEnabled = false

        // Enable scrolling and set a minimum scale
        barChart.setTouchEnabled(true)
        barChart.isDragEnabled = true
        barChart.setScaleEnabled(true)
        barChart.setPinchZoom(true)

        // Setup X-axis
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = object : IndexAxisValueFormatter(labels.map { it.uppercase() }) {
                override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                    return super.getAxisLabel(value, axis)
                }
            }
            granularity = 1f
            isGranularityEnabled = true
            axisLineColor = Color.BLACK
            axisLineWidth = 1f
            setDrawGridLines(false)

            // Make X-axis labels bold
            typeface = Typeface.DEFAULT_BOLD
        }

        // Setup Y-axis (left side only)
        barChart.axisLeft.apply {
            isEnabled = true
            axisLineColor = Color.BLACK
            axisLineWidth = 1f
            gridColor = Color.TRANSPARENT
            setDrawGridLines(false)

            // Make Y-axis labels bold
            typeface = Typeface.DEFAULT_BOLD
        }

        barChart.axisRight.isEnabled = false

        // Remove grid lines from the chart area
        barChart.setGridBackgroundColor(Color.TRANSPARENT)

        barChart.setBackgroundColor(Color.WHITE)

        barChart.description.isEnabled = false

        // Set up the custom marker view
        val markerView = CustomBarChartMarkerView(barChart.context, barChart)
        barChart.marker = markerView

        // Refresh the chart
        barChart.invalidate()
        barChart.animateY(1000)
    }

    private fun displayLineChart(customers: List<CustomerModel>) {
        val customerTypeData = getCustomerTypeData(customers)
        val lineEntries = createLineEntries(customerTypeData)
        setupLineChart(lineEntries, customerTypeData.keys.toList())
    }

    // StackOverflow post
    // Title: Kotlin - from a list of Maps, to a map grouped by key
    // Posted by: Constantin Chernishov
    // Available at: https://stackoverflow.com/questions/53433108/kotlin-from-a-list-of-maps-to-a-map-grouped-by-key
    private fun getCustomerTypeData(customers: List<CustomerModel>): Map<String, Double> {
        // Group by CustomerType
        return customers.groupBy { it.CustomerType }
            // Count customers in each type
            .mapValues { entry -> entry.value.size.toDouble() }
    }

    // StackOverflow post
    // Title: Why kotlin map.indexed returns List<Any> insteand of List<MyObject>
    // Posted by: Mikezx6r
    // Available at: https://stackoverflow.com/questions/53841789/why-kotlin-map-indexed-returns-listany-insteand-of-listmyobject
    private fun createLineEntries(customerTypeData: Map<String, Double>): List<Entry> {
        return customerTypeData.entries.mapIndexed { index, entry ->
            Entry(index.toFloat(), entry.value.toFloat())
        }
    }

    // StackOverflow post
    // Title: How to use LifecycleScope to execute coroutine
    // Posted by: Arpit Shukla
    // Available at: https://stackoverflow.com/questions/70058423/how-to-use-lifecyclescope-to-execute-coroutine
    private fun getCustomers() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val customers = sbHelper.getAllCustomers()
                withContext(Dispatchers.Main) {
                    displayLineChart(customers)
                }
            } catch (e: Exception) {
                Log.e("TaskLoadError", "Error loading customers", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Couldn't load customers from DB", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // StackOverflow post
    // Title: Create Line Chart in Android using MPAndroidChart library
    // Posted by: AmrDeveloper
    // Available at: https://stackoverflow.com/questions/64709234/create-line-chart-in-android-using-mpandroidchart-library
    private fun setupLineChart(entries: List<Entry>, labels: List<String>) {
        val lineDataSet = LineDataSet(entries, "").apply {
            color = Color.parseColor("#E8715C")
            valueTextColor = Color.BLACK
            valueTextSize = 16f
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            lineWidth = 3f
            setDrawValues(false)
            setDrawCircles(true)
            setCircleColor(Color.parseColor("#E8715C"))
            circleRadius = 4f
        }

        val lineData = LineData(lineDataSet)
        lineChart.data = lineData

        // Remove the description
        lineChart.legend.isEnabled = false

        // Enable scrolling and set a minimum scale
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(true)
        lineChart.setPinchZoom(true)

        // Setup X-axis
        lineChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = object : IndexAxisValueFormatter(labels.map { it.uppercase() }) {
                override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                    // Adjusting the first and last label positions
                    return when (value.toInt()) {
                        0 -> "    ${super.getAxisLabel(value, axis)}" // Move first label to the right
                        labels.size - 1 -> "${super.getAxisLabel(value, axis)}      " // Move last label to the left
                        else -> super.getAxisLabel(value, axis)
                    }
                }
            }

            // Ensure all labels are displayed
            granularity = 1f
            isGranularityEnabled = true
            axisLineColor = Color.BLACK
            axisLineWidth = 1f
            setDrawGridLines(false)

            // Make X-axis labels bold
            typeface = Typeface.DEFAULT_BOLD
        }

        // Customize the Y axis
        lineChart.axisLeft.apply {
            // Ensure Y-axis is enabled
            isEnabled = true
            gridColor = Color.TRANSPARENT
            gridLineWidth = 0f

            axisLineColor = Color.BLACK
            axisLineWidth = 1f
            setDrawGridLines(false)

            textSize = 11f
            // Make Y-axis labels bold
            typeface = Typeface.DEFAULT_BOLD
        }

        // Disable right Y-axis
        lineChart.axisRight.isEnabled = false

        // Remove grid lines from the chart area
        lineChart.setGridBackgroundColor(Color.TRANSPARENT)
        lineChart.setBackgroundColor(Color.WHITE)
        lineChart.description.isEnabled = false

        // Set up the custom marker view
        val markerView = CustomLineChartMarkerView(lineChart.context, lineChart)
        lineChart.marker = markerView

        // Refresh the chart
        lineChart.invalidate()
        lineChart.animateY(1000)
    }

    override fun onResume() {
        super.onResume()
        val dashboardActivity = activity as? DashboardActivity
        dashboardActivity?.binding?.apply {
            bottomNavigation.visibility = View.VISIBLE
            plusBtn.visibility = View.VISIBLE
        }
    }
}