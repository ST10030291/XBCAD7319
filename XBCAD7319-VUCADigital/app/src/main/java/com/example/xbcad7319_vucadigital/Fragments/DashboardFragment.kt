package com.example.xbcad7319_vucadigital.Fragments

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.xbcad7319_vucadigital.Activites.DashboardActivity
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.db.SupabaseHelper
import com.example.xbcad7319_vucadigital.models.CustomLineChartMarkerView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardFragment : Fragment() {

    private lateinit var customerCountTextView: TextView
    private lateinit var opportunitiesCountTextView: TextView
    private lateinit var dailyFilterButton: Button
    private lateinit var weeklyFilterButton: Button
    private lateinit var monthlyFilterButton: Button
    private lateinit var yearlyFilterButton: Button
    private lateinit var viewProductsBtn: CardView
    private lateinit var viewAnalyticsBtn: CardView
    private lateinit var lineChart: LineChart
    private val monthLabels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    private val dayLabels = List(31) { (it + 1).toString() }
    private val weekLabels = List(5) { "Week ${it + 1}" }
    private val yearLabels = listOf("2024")
    private val supabaseHelper = SupabaseHelper()

    override fun onResume() {
        super.onResume()
        val dashboardActivity = activity as? DashboardActivity
        dashboardActivity?.binding?.apply {
            bottomNavigation.visibility = View.VISIBLE
            plusBtn.visibility = View.GONE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser

        //Initialize views
        InitElements(view)

        // Display a welcome message with the users name for added personalization
        val userEmail = user?.email
        val userName = userEmail?.substringBefore("@") ?: "User"
        view.findViewById<TextView>(R.id.username_tv).text = "Welcome, $userName"

        lifecycleScope.launch {
            // Initial data fetching on the background thread
            val initialFilteredTasks = withContext(Dispatchers.IO) {
                supabaseHelper.fetchTasksByFilter("month")
            }

            fetchAndDisplayCustomerCount()
            fetchAndDisplayOpportunitiesCount()
            selectButton(monthlyFilterButton)
            setupLineChart(monthLabels, initialFilteredTasks)

            // Set onClickListeners
            viewProductsBtn.setOnClickListener { openProductsFragment() }
            viewAnalyticsBtn.setOnClickListener { openAnalyticsFragment() }

            // Set up filter buttons
            setupFilterButton(monthlyFilterButton, "month", monthLabels)
            setupFilterButton(dailyFilterButton, "day", dayLabels)
            setupFilterButton(weeklyFilterButton, "week", weekLabels)
            setupFilterButton(yearlyFilterButton, "year", yearLabels)
        }
    }
    // StackOverflow post
    // Title: How to use LifecycleScope to execute coroutine
    // Posted by: Arpit Shukla
    // Available at: https://stackoverflow.com/questions/70058423/how-to-use-lifecyclescope-to-execute-coroutine
    private fun setupFilterButton(button: Button, filterType: String, labels: List<String>) {
        button.setOnClickListener {
            lifecycleScope.launch {
                // Fetch tasks on the background thread
                val tasks = withContext(Dispatchers.IO) {
                    supabaseHelper.fetchTasksByFilter(filterType)
                }

                // Update UI on the main thread
                setupLineChart(labels, tasks)
                selectButton(button)
            }
        }
    }

    private fun InitElements(view: View){
        customerCountTextView = view.findViewById(R.id.customerCount)
        opportunitiesCountTextView = view.findViewById(R.id.opportunityCount)
        viewProductsBtn = view.findViewById(R.id.viewProducts_btn)
        viewAnalyticsBtn = view.findViewById(R.id.viewAnalytics_btn)
        lineChart = view.findViewById(R.id.lineChart1)
        dailyFilterButton = view.findViewById(R.id.DailyFilter)
        weeklyFilterButton = view.findViewById(R.id.WeeklyFilter)
        monthlyFilterButton= view.findViewById(R.id.MonthlyFilter)
        yearlyFilterButton = view.findViewById(R.id.YearlyFilter)

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    //Changes the filter button selected theme
    private fun selectButton(selectedButton: Button) {
        monthlyFilterButton.isSelected = selectedButton == monthlyFilterButton
        dailyFilterButton.isSelected = selectedButton == dailyFilterButton
        weeklyFilterButton.isSelected = selectedButton == weeklyFilterButton
        yearlyFilterButton.isSelected = selectedButton == yearlyFilterButton
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
    // StackOverflow post
    // Title: Create Line Chart in Android using MPAndroidChart library
    // Posted by: AmrDeveloper
    // Available at: https://stackoverflow.com/questions/64709234/create-line-chart-in-android-using-mpandroidchart-library
    private fun setupLineChart(xAxisLabels: List<String>, values: List<Int>) {
        // Parse the x and y values
        val entries = List(xAxisLabels.size) { index ->
            Entry(index.toFloat(), values.getOrElse(index) { 0 }.toFloat())
        }

        // Create the Line chart data set
        val lineDataSet = LineDataSet(entries, "Number of Tasks").apply {
            color = 0xFFFF7F50.toInt()
            valueTextColor = ColorTemplate.COLORFUL_COLORS[1]
            valueTextSize = 0f
            lineWidth = 3f
            circleRadius = 1f
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            setDrawCircles(false)
        }

        val lineData = LineData(lineDataSet)
        lineChart.data = lineData

        // Customize the Y axis
        lineChart.axisLeft.apply {
            gridColor = Color.TRANSPARENT
            gridLineWidth = 0f
            axisLineColor = Color.BLACK
            axisLineWidth = 1f

            setDrawGridLines(false)

            typeface = Typeface.DEFAULT_BOLD
        }

        lineChart.axisRight.isEnabled = false

        // Customize the X axis
        lineChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = object : ValueFormatter() {
                override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                    val index = value.toInt()
                    return if (index in xAxisLabels.indices) {
                        xAxisLabels[index]
                    } else {
                        ""
                    }
                }
            }

            axisLineColor = Color.BLACK
            axisLineWidth = 1f

            setDrawGridLines(false)

            typeface = Typeface.DEFAULT_BOLD
        }

        // Set up the custom marker view
        val markerView = CustomLineChartMarkerView(lineChart.context, lineChart)
        lineChart.marker = markerView

        lineChart.description.isEnabled = false
        lineChart.animateX(1000)
        lineChart.invalidate()
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
