package com.example.moodup.ui.profile.analysis

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.moodup.R
import com.example.moodup.databinding.FragmentAnalysisBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class AnalysisFragment : Fragment() {

    private lateinit var binding: FragmentAnalysisBinding
    private val analysisViewModel: AnalysisViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAnalysisBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.analysisViewModel = analysisViewModel

        arrowButtonListener()
        showButtonListener()

        return binding.root
    }

    private fun arrowButtonListener() {
        analysisViewModel.onArrowButtonClicked.observe(viewLifecycleOwner) { isClicked ->
            if (isClicked) {
                findNavController().navigate(R.id.action_analysisFragment_to_profileFragment)
                analysisViewModel.onArrowButtonClicked.value = false
            }
        }
    }

    private fun showButtonListener() {
        analysisViewModel.onShowButtonClicked.observe(viewLifecycleOwner) { isClicked ->
            if (isClicked) {
                analysisViewModel.fetchWeeklyScores()
                observeWeeklyScores()
                analysisViewModel.onShowButtonClicked.value = false
            }

        }
    }

    private fun observeWeeklyScores() {
        analysisViewModel.weeklyScores.observe(viewLifecycleOwner) { scores ->
            Log.d("AnalysisFragment", "Weekly scores: $scores")
            updateBarChart(scores)
        }
    }

    private fun updateBarChart(scores: Map<String, Map<String, Double>>) {
        val barChart: BarChart = binding.barChart

        val entries = scores.entries.mapIndexed { index, entry ->
            val positive = entry.value["positivePercentage"] ?: 0.0
            val negative = entry.value["negativePercentage"] ?: 0.0
            BarEntry(index.toFloat(), floatArrayOf(positive.toFloat(), negative.toFloat()))
        }

        val barDataSet = BarDataSet(entries, "Daily Scores")
        barDataSet.colors =
            listOf(ColorTemplate.VORDIPLOM_COLORS[1], ColorTemplate.LIBERTY_COLORS[4])
        barDataSet.stackLabels = arrayOf("Positive", "Negative")

        val description = Description()
        description.text = "Emotions Over One Week: Positive vs Negative"
        barChart.description = description

        val barData = BarData(barDataSet)
        barChart.data = barData

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(getDaysOfWeek(scores.keys.toList()))
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        barChart.axisLeft.setDrawGridLines(false)

        barChart.axisLeft.setDrawLabels(true)
        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisRight.isEnabled = false

        barChart.invalidate()
    }

    private fun getDaysOfWeek(days: List<String>): List<String> {
        return days
    }


}
