package com.example.xbcad7319_vucadigital.models

import android.content.Context
import android.widget.TextView
import com.example.xbcad7319_vucadigital.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart

// StackOverflow post
// Titled: Customize the marker - MPAndroidChart
// Posted by: David Rawson
// Available at: https://stackoverflow.com/questions/41458190/customize-the-marker-mpandroidchart
class CustomBarChartMarkerView(
    context: Context,
    private val barChart: BarChart // Add BarChart as a parameter
) : MarkerView(context, R.layout.custom_marker_view) {

    private val tvContent: TextView = findViewById(R.id.marker_text)

    // Called when the marker is created
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e != null) {
            val selectedValue = "Value: ${e.y}"
            tvContent.text = selectedValue

            // Adjust the marker offset based on the position of the entry
            if (e.x >= barChart.xAxis.axisMaximum - 1) { // Check if near the right edge
                setOffset(-width.toFloat(), -height.toFloat()) // Move marker left
            } else {
                setOffset(0f, -height.toFloat()) // Default offset
            }

        }
        super.refreshContent(e, highlight)
    }
}
// StackOverflow post
// Titled: Customize the marker - MPAndroidChart
// Posted by: David Rawson
// Available at: https://stackoverflow.com/questions/41458190/customize-the-marker-mpandroidchart
class CustomLineChartMarkerView(
    context: Context,
    private val lineChart: LineChart // Add BarChart as a parameter
) : MarkerView(context, R.layout.custom_marker_view) {

    private val tvContent: TextView = findViewById(R.id.marker_text)

    // Called when the marker is created
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e != null) {
            val selectedValue = "Value: ${e.y}"
            tvContent.text = selectedValue

            // Adjust the marker offset based on the position of the entry
            if (e.x >= lineChart.xAxis.axisMaximum - 1) { // Check if near the right edge
                setOffset(-width.toFloat(), -height.toFloat()) // Move marker left
            } else {
                setOffset(0f, -height.toFloat()) // Default offset
            }
        }
        super.refreshContent(e, highlight)
    }
}

