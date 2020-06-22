package org.phenoapps.intercross.fragments

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.ParentsAdapter
import org.phenoapps.intercross.data.ParentsRepository
import org.phenoapps.intercross.data.PollenGroupRepository
import org.phenoapps.intercross.data.models.Parent
import org.phenoapps.intercross.data.viewmodels.EventProducer
import org.phenoapps.intercross.data.viewmodels.ParentsListViewModel
import org.phenoapps.intercross.data.viewmodels.PollenGroupListViewModel
import org.phenoapps.intercross.data.viewmodels.factory.ParentsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.PollenGroupListViewModelFactory
import org.phenoapps.intercross.databinding.FragmentParentsBinding


class ParentsFragment: IntercrossBaseFragment<FragmentParentsBinding>(R.layout.fragment_parents) {

    private val viewModel: ParentsListViewModel by viewModels {
        ParentsListViewModelFactory(ParentsRepository.getInstance(db.parentsDao()))
    }

    private val groupList: PollenGroupListViewModel by viewModels {
        PollenGroupListViewModelFactory(PollenGroupRepository.getInstance(db.pollenGroupDao()))
    }

    val parentList: ParentsListViewModel by viewModels {
        ParentsListViewModelFactory(ParentsRepository.getInstance(db.parentsDao()))
    }


    private lateinit var mMaleAdapter: ParentsAdapter

    private lateinit var mFemaleAdapter: ParentsAdapter


    //simple gesture listener to detect left and right swipes,
    //on a detected swipe the viewed gender will change
    //private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {

//        override fun onFling(e1: MotionEvent?, e2: MotionEvent?,
//                             velocityX: Float, velocityY: Float): Boolean {
//
//            e1?.let {
//                e2?.let {
//                    val dx = e1.x - e2.x
//                    val x = abs(dx)
//
//                    if (x in 100.0..1000.0) {
//                        if (dx > 0) {
//                            //swipe to left
//                            FragmentParentsBinding.swipeLeft()
//                        } else {
//                            //swipe right
//                            swipeRight()
//                        }
//                    }
//
//                    return true
//                }
//            }
//            return false
//        }
    //}

    override fun FragmentParentsBinding.afterCreateView() {

        val ctx = requireContext()

        val tabFocus = arguments?.getInt("malesFirst") ?: 0

        mMaleAdapter = ParentsAdapter(viewModel)
        mFemaleAdapter = ParentsAdapter(viewModel)

        femaleRecycler.adapter = mFemaleAdapter
        femaleRecycler.layoutManager = LinearLayoutManager(ctx)

        maleRecycler.adapter = mMaleAdapter
        maleRecycler.layoutManager = LinearLayoutManager(ctx)

        viewModel.parents.observeForever { parents ->

            val addedMales = ArrayList<Parent>()

            groupList.groups.observeForever { groups ->

                //aggregate groups TODO replace with new query
                addedMales.addAll(groups
                        .distinctBy { it.codeId }
                        .map { g-> Parent(g.codeId, 1, g.name) })

                mMaleAdapter.submitList(addedMales+parents.filter { p -> p.sex == 1 })

            }

            mMaleAdapter.submitList(addedMales+(parents.filter { p -> p.sex == 1 }.reversed()))

            mFemaleAdapter.submitList(parents.filter { p -> p.sex == 0 }.reversed())

        }

        /**
         * Submit id/name pair to Parents table
         */
        submitParent.setOnClickListener {

            //TODO add text sanitization / checking

            val sex = if (tabLayout.getTabAt(0)?.isSelected != false) 0 else 1

            parentList.insert(Parent(codeEditText.text.toString(), sex, nameEditText.text.toString()))

            //clear edit texts
            nameEditText.setText("")

            codeEditText.setText("")
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {

                tab?.let {

                    when(it.text) {

                        "Female" -> {
                            femaleRecycler.visibility=View.VISIBLE
                            maleRecycler.visibility=View.GONE
                            newMaleButton.visibility=View.GONE
                        }
                        "Male" -> {
                            maleRecycler.visibility=View.VISIBLE
                            femaleRecycler.visibility=View.GONE
                            newMaleButton.visibility=View.VISIBLE
                        }
                    }
                }
            }
        })

        /*
        On startup, read arguments and determine the tab
         */
        tabLayout.getTabAt(tabFocus)?.select()

        /*
        Go to Pollen Manager fragment for male group data-entry
         */
        newMaleButton.setOnClickListener {

            Navigation.findNavController(mBinding.root)
                    .navigate(ParentsFragmentDirections.globalActionToPollenManagerFragment())
        }

        deleteButton.setOnClickListener {

            //TODO Ask if delete should erase both male and female selected, or just the current tab?
            //TODO add poly cross delete, all entries that have the selected group ids must be purged from DB.
            //delete only selected tab

            viewModel.delete(*(mFemaleAdapter.currentList + mMaleAdapter.currentList)
                    .filter { it.selected }
                    .toTypedArray())

        }

        printButton.setOnClickListener {

            val experiment = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .getString("org.phenoapps.intercross.EXPERIMENT", "")

            val person = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .getString("org.phenoapps.intercross.PERSON", "")

//            val events = (mMaleAdapter.currentList + mFemaleAdapter.currentList)
//                    .filter { it.isSelected }
//                    .toTypedArray()
//
//            if (events.isNotEmpty()) {
//
//                //TODO add message saying printing females and males
//                BluetoothUtil().print(requireContext(), events)
//            }
        }


//        val gdc = GestureDetectorCompat(context, gestureListener)
//
//        //todo create custom view and override performClick()
//        maleRecycler.setOnTouchListener { _, motionEvent ->
//            gdc.onTouchEvent(motionEvent)
//        }
//
//        femaleRecycler.setOnTouchListener({ _, motionEvent ->
//            gdc.onTouchEvent(motionEvent)
//        })

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.parents_toolbar, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        with (mBinding) {

            when(item.itemId) {

                R.id.action_select_all -> {

                    //TODO update all male/female or just the selected tab?
                    //TODO reverse all selections or just make them all true? no inverting
//                    if (tabLayout2.getTabAt(0)?.isSelected == true) {
//
//                        mEventStore.update(
//                                *(mFemaleAdapter.currentList)
//                                        .map { it.apply { it.isSelected = true }}
//                                        .toTypedArray()
//
//                        )
//
//                        mFemaleAdapter.notifyDataSetChanged()
//
//                    } else {
//
//                        mEventStore.update(
//                                *(mMaleAdapter.currentList)
//                                        .map { it.apply { it.isSelected = true }}
//                                        .toTypedArray()
//
//                        )
//
//                        mMaleAdapter.notifyDataSetChanged()
//                    }
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun FragmentParentsBinding.swipeLeft() {

        tabLayout.getTabAt(1)?.select()

    }

    private fun FragmentParentsBinding.swipeRight() {

        tabLayout.getTabAt(0)?.select()

    }
}