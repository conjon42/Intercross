package org.phenoapps.intercross.fragments

import android.graphics.Color
import android.widget.ArrayAdapter
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.tabs.TabLayout
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.SummaryAdapter
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.ParentsRepository
import org.phenoapps.intercross.data.models.CrossType
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.Parent
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.ParentsListViewModel
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.ParentsListViewModelFactory
import org.phenoapps.intercross.databinding.FragmentDataSummaryBinding
import java.util.*

/**
 * @author: Chaney
 *
 * Summary Fragment displays accumulated data using a pie chart and recycler view.
 * Summary fragment can be chosen from the main menu (currently the nav drawer)
 * This fragment has a tab layout to select three different categories:
 * 1. Sex: this category displays total males, females and unknowns (these are crosses) while m/fs are parents
 * 2. Type: accumulated cross types e.g Biparental, Open, Self
 * 3. Meta: accumulated meta data fields (currently only fruits, seeds and flowers)
 *
 * If counted data is 0 then it is not displayed. This may result in a blank tab, improvements might include a default message when there's no data.
 * TODO: the graph library allows clicking different sections of the piechart, maybe this should change the displayed data somehow
 */
class SummaryFragment : IntercrossBaseFragment<FragmentDataSummaryBinding>(R.layout.fragment_data_summary) {

    private val eventsModel: EventListViewModel by viewModels {
        EventsListViewModelFactory(EventsRepository.getInstance(db.eventsDao()))
    }

    private val parentsModel: ParentsListViewModel by viewModels {
        ParentsListViewModelFactory(ParentsRepository.getInstance(db.parentsDao()))
    }

    private lateinit var mEvents: List<Event>
    private lateinit var mParents: List<Parent>

    //a quick wrapper function for tab selection
    private fun tabSelected(onSelect: (TabLayout.Tab?) -> Unit) = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
           onSelect(tab)
        }
        override fun onTabUnselected(tab: TabLayout.Tab?) {}
        override fun onTabReselected(tab: TabLayout.Tab?) {}
    }

    override fun FragmentDataSummaryBinding.afterCreateView() {

        //initialize pie chart parameters, this is mostly taken from the github examples
        setupPieChart()

        //listen to events and parents once and then displays the data
        //if somehow events are injected after afterCreateView, the graph won't update
        //but that could only happen if someone used the database inspector
        startObservers()

        //val sex = getString(R.string.sex)
        val type = getString(R.string.type)
        val meta = getString(R.string.meta)

        //calls the set data on the respective data selected from the tab view
        dataSummaryTabLayout.addOnTabSelectedListener(tabSelected { tab ->

            setData(when (tab?.text) {
                type -> setTypeData()
                meta -> setMetaData()
                else -> setSexData()
            })
        })
    }

    //used to load label/value pair data into the adapter's view holder
    open class ListEntry(open var label: String, open var value: Float)

    //extension function for live data to only observe once when the data is not null
    private fun <T> LiveData<T>.observeOnce(observer: Observer<T>) {
        observe(viewLifecycleOwner, object : Observer<T> {
            override fun onChanged(t: T?) {
                t?.let { data ->
                    observer.onChanged(data)
                    removeObserver(this)
                }
            }
        })
    }

    /**
     * Cascade-load event and parent data, once the data is loaded trigger the first tab data to load.
     */
    private fun FragmentDataSummaryBinding.startObservers() {

        eventsModel.events.observeOnce { data ->

            mEvents = data

            parentsModel.parents.observeOnce { parents ->

                mParents = parents

                setData(setSexData())
            }
        }
    }

    //initializes view parameters s.a recycler view and pie chart
    private fun FragmentDataSummaryBinding.setupPieChart() {

        dataSummaryRecyclerView.layoutManager = LinearLayoutManager(context).apply {
            orientation = LinearLayoutManager.VERTICAL
        }

        dataSummaryRecyclerView.adapter = SummaryAdapter()

        dataSummaryPieChart.setUsePercentValues(true)
        dataSummaryPieChart.description.isEnabled = false
        dataSummaryPieChart.setExtraOffsets(5f, 10f, 5f, 5f)

        dataSummaryPieChart.dragDecelerationFrictionCoef = 0.95f

        //chart.setCenterTextTypeface(tfLight)
        //chart.centerText = generateCenterSpannableText()

        dataSummaryPieChart.isDrawHoleEnabled = true
        dataSummaryPieChart.setHoleColor(Color.WHITE)

        dataSummaryPieChart.setTransparentCircleColor(Color.WHITE)
        dataSummaryPieChart.setTransparentCircleAlpha(110)

        dataSummaryPieChart.holeRadius = 58f
        dataSummaryPieChart.transparentCircleRadius = 61f

        dataSummaryPieChart.setDrawCenterText(true)

        dataSummaryPieChart.rotationAngle = 0f
        // enable rotation of the chart by touch
        // enable rotation of the chart by touch
        dataSummaryPieChart.isRotationEnabled = true
        dataSummaryPieChart.isHighlightPerTapEnabled = true

        // add a selection listener
        //chart.setOnChartValueSelectedListener(this)

        val l: Legend = dataSummaryPieChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.xEntrySpace = 7f
        l.yEntrySpace = 0f
        l.yOffset = 0f

        // entry label styling

        // entry label styling
        dataSummaryPieChart.setEntryLabelColor(Color.BLACK)
        //chart.setEntryLabelTypeface(tfRegular)
        dataSummaryPieChart.setEntryLabelTextSize(12f)
    }

    private fun setSexData(): PieDataSet = PieDataSet(ArrayList<PieEntry>().apply {

        if (::mParents.isInitialized) {
            val males = mParents.count { it.sex == 1 }.toFloat()
            if (males > 0f) add(PieEntry(males, "Males"))
            val females = mParents.count { it.sex == 0 }.toFloat()
            if (females > 0f) add(PieEntry(females, "Females"))
        }
        if (::mEvents.isInitialized) {
            if (::mParents.isInitialized) {
                val unk = mEvents.size.toFloat() + mParents.count { it.sex == -1 }.toFloat()
                if (unk > 0f) add(PieEntry(unk, "Unknown"))
            } else {
                val unk = mEvents.size.toFloat()
                if (unk > 0f) add(PieEntry(unk, "Unknown"))
            }
        }
    },"Sex Statistics")

    private fun setTypeData(): PieDataSet = PieDataSet(ArrayList<PieEntry>().apply {

        val biparental = getString(R.string.cross_type_biparental)
        val open = getString(R.string.cross_type_open)
        val self = getString(R.string.cross_type_self)
        val unknown = getString(R.string.cross_type_unknown)
        val poly = getString(R.string.cross_type_poly)

        if (::mEvents.isInitialized)
            for (type in CrossType.values()) {
                val items = mEvents.count { it.type == type }.toFloat()
                if (items > 0f) add(
                    PieEntry(
                        items,
                        when (type) {
                            CrossType.BIPARENTAL -> biparental
                            CrossType.OPEN -> open
                            CrossType.POLY -> poly
                            CrossType.SELF -> self
                            CrossType.UNKNOWN -> unknown
                        }
                    )
                )
            }
    },"Cross Type Statistics")

    private fun setMetaData(): PieDataSet = PieDataSet(ArrayList<PieEntry>().apply {

        if (::mEvents.isInitialized) {
//            val fruits = mEvents.map { it.metaData.fruits }.sum().toFloat()
//            val seeds = mEvents.map { it.metaData.seeds }.sum().toFloat()
//            val flowers = mEvents.map { it.metaData.flowers }.sum().toFloat()
//            if (fruits > 0f) add(PieEntry(fruits, getString(R.string.crosses_export_fruits_header)))
//            if (seeds > 0f) add(PieEntry(seeds, getString(R.string.crosses_export_seeds_header)))
//            if (flowers > 0f) add(
//                PieEntry(
//                    flowers,
//                    getString(R.string.crosses_export_flowers_header)
//                )
//            )
        }
    },"Meta Data Statistics")

    //sets the accumulated data into the piechart and recycler view, along with some misc. parameter setting
    private fun FragmentDataSummaryBinding.setData(dataset: PieDataSet) {

        activity?.currentFocus?.clearFocus()

        dataSummaryPieChart.animateY(1400, Easing.EaseInOutQuad)

        dataset.setDrawIcons(false)
        dataset.sliceSpace = 3f
        //dataset.iconsOffset = MPPointF(0f, 40f)
        dataset.selectionShift = 5f

        // add a lot of colors
        val colors = ArrayList<Int>()
        for (c in ColorTemplate.VORDIPLOM_COLORS) colors.add(c)
        for (c in ColorTemplate.JOYFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.COLORFUL_COLORS) colors.add(c)
        for (c in ColorTemplate.LIBERTY_COLORS) colors.add(c)
        for (c in ColorTemplate.PASTEL_COLORS) colors.add(c)
        colors.add(ColorTemplate.getHoloBlue())
        dataset.colors = colors
        //dataSet.setSelectionShift(0f);
        val data = PieData(dataset)
        data.setValueFormatter(PercentFormatter())
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.BLACK)
        //data.setValueTypeface(tfLight)
        dataSummaryPieChart.data = data

        // undo all highlights
        dataSummaryPieChart.highlightValues(null)
        dataSummaryPieChart.invalidate()

        (dataSummaryRecyclerView.adapter as? SummaryAdapter)?.submitList(
            dataset.values?.map { ListEntry(it.label, it.value) }
        )
    }
}