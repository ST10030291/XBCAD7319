package com.example.xbcad7319_vucadigital.models
//
//import android.graphics.Canvas
//import android.graphics.Paint
//import com.github.mikephil.charting.charts.BarChart
//import com.github.mikephil.charting.renderer.BarChartRenderer
//import com.github.mikephil.charting.utils.ViewPortHandler
//import com.github.mikephil.charting.components.YAxis
//import com.github.mikephil.charting.components.XAxis
//
//class CustomBarChartRenderer(
//    private val chart: BarChart,
//    animator: com.github.mikephil.charting.animation.ChartAnimator,
//    viewPortHandler: ViewPortHandler
//) : BarChartRenderer(chart, animator, viewPortHandler) {
//
//    private val linePaint = Paint().apply {
//        color = android.graphics.Color.BLACK // Line color
//        strokeWidth = 2f // Line width
//        style = Paint.Style.STROKE
//    }
//
//    override fun drawExtras(c: Canvas) {
//        super.drawExtras(c)
//
//        // Draw additional lines outside Y-axis labels
//        val yAxis: YAxis = chart.axisLeft
//        for (i in 0 until yAxis.labelCount) {
//            // Calculate position based on the current Y value
//            val yLabelPosition = yAxis.labelPosition(yAxis.entries[i])
//            c.drawLine(
//                -10f, // Start line 10 pixels to the left of the Y-axis
//                yLabelPosition,
//                0f, // End line at the Y-axis
//                yLabelPosition,
//                linePaint
//            )
//        }
//
//        // Draw additional lines outside X-axis labels
//        val xAxis: XAxis = chart.xAxis
//        for (i in 0 until xAxis.labelCount) {
//            // Calculate position based on the current X value
//            val xLabelPosition = xAxis.getPosition(i.toFloat()).x
//            c.drawLine(
//                xLabelPosition,
//                chart.height.toFloat(), // Start line at the bottom of the chart
//                xLabelPosition,
//                chart.height.toFloat() + 10f, // Extend line 10 pixels down
//                linePaint
//            )
//        }
//    }
//}
