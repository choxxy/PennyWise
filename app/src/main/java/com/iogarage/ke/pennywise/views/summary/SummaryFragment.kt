package com.iogarage.ke.pennywise.views.summary

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.LargeValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.iogarage.ke.pennywise.R
import com.iogarage.ke.pennywise.data.dao.TransactionDao
import com.iogarage.ke.pennywise.databinding.FragmentSummaryBinding
import com.iogarage.ke.pennywise.tabs.CardFragmentPagerAdapter
import com.iogarage.ke.pennywise.tabs.ShadowTransformer
import com.iogarage.ke.pennywise.util.viewBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * A simple [Fragment] subclass.
 * Use the [SummaryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class SummaryFragment : Fragment(), OnChartValueSelectedListener {
    protected var mTfRegular: Typeface? = null
    protected var mTfLight: Typeface? = null
    private var transactionDao: TransactionDao? = null

    private val binding by viewBinding(FragmentSummaryBinding::bind)


    private val mViewPager: ViewPager? = null
    private val mCardShadowTransformer: ShadowTransformer? = null
    private val mFragmentCardAdapter: CardFragmentPagerAdapter? = null
    private val mFragmentCardShadowTransformer: ShadowTransformer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        if (arguments != null) {
        }
      //  transactionDao = (activity as PennyMain?)!!.transactionDao
      //  mTfRegular = Typeface.createFromAsset(activity!!.assets, "fonts/OpenSans-Regular.ttf")
      //  mTfLight = Typeface.createFromAsset(activity!!.assets, "fonts/OpenSans-Light.ttf")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_summary, container, false)
        val chart: BarChart = view.findViewById<BarChart>(R.id.barChart)
        setupChart(chart)
        setData(chart)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        // TODO Add your menu entries here
        inflater.inflate(R.menu.summary_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_life_time -> {}
            R.id.action_last_six_months -> {}
            R.id.action_last_three_months -> {}
            R.id.action_this_month -> {}
        }
        return true
    }

    private fun setupChart(chart: BarChart) {

        // scaling can now only be done on x- and y-axis separately
        chart.setPinchZoom(false)
        chart.setDrawBarShadow(false)
        chart.setDrawGridBackground(false)
        val l: Legend = chart.getLegend()
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP)
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT)
        l.setOrientation(Legend.LegendOrientation.VERTICAL)
        l.setDrawInside(true)
        l.setTypeface(mTfLight)
        l.setYOffset(0f)
        l.setXOffset(10f)
        l.setYEntrySpace(0f)
        l.setTextSize(8f)
        val xAxis: XAxis = chart.getXAxis()
        xAxis.setTypeface(mTfLight)
        xAxis.setGranularity(1f)
        xAxis.setCenterAxisLabels(true)
        xAxis.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toInt().toString()
            }
        })
        val leftAxis: YAxis = chart.getAxisLeft()
        leftAxis.setTypeface(mTfLight)
        leftAxis.setValueFormatter(LargeValueFormatter())
        leftAxis.setDrawGridLines(false)
        leftAxis.setSpaceTop(35f)
        leftAxis.setAxisMinimum(0f) // this replaces setStartAtZero(true)
        chart.getAxisRight().setEnabled(false)
    }

    private fun setData(chart: BarChart) {
        val groupSpace = 0.08f
        val barSpace = 0.06f // x4 DataSet
        val barWidth = 0.4f // x4 DataSet
        // (0.2 + 0.03) * 4 + 0.08 = 1.00 -> interval per "group"
        val groupCount = 2
        val startYear = 1980
        val endYear = startYear + groupCount
        val yVals1: ArrayList<BarEntry> = ArrayList<BarEntry>()
        val yVals2: ArrayList<BarEntry> = ArrayList<BarEntry>()
        val randomMultiplier = 100000f
        for (i in startYear until endYear) {
            yVals1.add(BarEntry(i.toFloat(), (Math.random() * randomMultiplier).toFloat()))
            yVals2.add(BarEntry(i.toFloat(), (Math.random() * randomMultiplier).toFloat()))
        }
        val set1: BarDataSet
        val set2: BarDataSet
        if (chart.getData() != null && chart.getData().getDataSetCount() > 0) {
            set1 = chart.getData().getDataSetByIndex(0) as BarDataSet
            set2 = chart.getData().getDataSetByIndex(1) as BarDataSet
            set1.setValues(yVals1)
            set2.setValues(yVals2)
            chart.getData().notifyDataChanged()
            chart.notifyDataSetChanged()
        } else {
            // create 2 DataSets
            set1 = BarDataSet(yVals1, "Company A")
            set1.setColor(Color.rgb(104, 241, 175))
            set2 = BarDataSet(yVals2, "Company B")
            set2.setColor(Color.rgb(164, 228, 251))
            val data = BarData(set1, set2)
            data.setValueFormatter(LargeValueFormatter())
            data.setValueTypeface(mTfLight)
            chart.setData(data)
        }

        // specify the width each bar should have
        chart.getBarData().setBarWidth(barWidth)

        // restrict the x-axis range
        chart.getXAxis().setAxisMinimum(startYear.toFloat())

        // barData.getGroupWith(...) is a helper that calculates the width each group needs based on the provided parameters
        chart.getXAxis().setAxisMaximum(
            startYear + chart.getBarData().getGroupWidth(groupSpace, barSpace) * groupCount
        )
        chart.groupBars(startYear.toFloat(), groupSpace, barSpace)
        chart.invalidate()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onValueSelected(e: Entry, h: Highlight) {}
    override fun onNothingSelected() {}

    companion object {
        // TODO: Rename and change types and number of parameters
        fun newInstance(): SummaryFragment {
            val fragment = SummaryFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }

        fun dpToPixels(dp: Int, context: Context): Float {
            return dp * context.resources.displayMetrics.density
        }
    }
}