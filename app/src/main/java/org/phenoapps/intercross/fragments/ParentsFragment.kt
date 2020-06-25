package org.phenoapps.intercross.fragments

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.ParentsAdapter
import org.phenoapps.intercross.data.ParentsRepository
import org.phenoapps.intercross.data.PollenGroupRepository
import org.phenoapps.intercross.data.models.BaseParent
import org.phenoapps.intercross.data.models.Parent
import org.phenoapps.intercross.data.models.PollenGroup
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

    private val parentList: ParentsListViewModel by viewModels {
        ParentsListViewModelFactory(ParentsRepository.getInstance(db.parentsDao()))
    }

    private lateinit var mMaleAdapter: ParentsAdapter

    private lateinit var mFemaleAdapter: ParentsAdapter

    private var mNextMaleSelection = true
    private var mNextFemaleSelection = true

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

        viewModel.updateSelection(0)
        groupList.updateSelection(0)

        mMaleAdapter = ParentsAdapter(viewModel, groupList)
        mFemaleAdapter = ParentsAdapter(viewModel, groupList)

        femaleRecycler.adapter = mFemaleAdapter
        femaleRecycler.layoutManager = LinearLayoutManager(ctx)

        maleRecycler.adapter = mMaleAdapter
        maleRecycler.layoutManager = LinearLayoutManager(ctx)

        //TODO Trevor: What happens when wishlist import includes different code ids with same name, similarly for cross events

        viewModel.parents.observe(viewLifecycleOwner, Observer { parents ->

            val addedMales = ArrayList<BaseParent>()

            groupList.groups.observe(viewLifecycleOwner, Observer { groups ->

                addedMales.clear()

                addedMales.addAll(groups.distinctBy { it.codeId })

                mMaleAdapter.submitList(addedMales+(parents
                        .filter { p -> p.sex == 1 }
                        .sortedBy { p -> p.name}))

            })

            mMaleAdapter.submitList(addedMales+(parents
                    .filter { p -> p.sex == 1 }
                    .sortedBy { p -> p.name }))

            mFemaleAdapter.submitList(parents
                    .filter { p -> p.sex == 0 }
                    .sortedBy { p -> p.name })

        })

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
                        }
                        "Male" -> {
                            maleRecycler.visibility=View.VISIBLE
                            femaleRecycler.visibility=View.GONE
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
        newButton.setOnClickListener {

            when (tabLayout.getTabAt(0)?.isSelected) {

                true -> Navigation.findNavController(mBinding.root)
                        .navigate(ParentsFragmentDirections.actionParentsToCreateEvent(0))

                else -> Navigation.findNavController(mBinding.root)
                        .navigate(ParentsFragmentDirections.actionParentsToCreateEvent(1))
            }

        }

        /**
         * Delete should erase just the current tab
         * Group deletion: ll entries that have the selected group ids must be purged from DB.
         */
        deleteButton.setOnClickListener {

            if (tabLayout.getTabAt(0)?.isSelected == true) {

                val outParents: List<Parent> = mFemaleAdapter
                        .currentList.filterIsInstance(Parent::class.java)

                viewModel.delete(*(outParents
                        .filter { p -> p.selected }).toTypedArray())

            } else {

                //TODO chaney refactor
                val outParents = mMaleAdapter.currentList.filterIsInstance(Parent::class.java)

                val outGroups = mMaleAdapter.currentList.filterIsInstance(PollenGroup::class.java)

                groupList.deleteByCode(outGroups
                        .filter { group -> group.selected }
                        .map { g -> g.codeId }.toList())

                viewModel.delete(*outParents.filter { parent -> parent.selected }.toTypedArray())

            }

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


                    if (tabLayout.getTabAt(0)?.isSelected == true) {

                        parentList.update(
                                *(mFemaleAdapter.currentList
                                        .filterIsInstance(Parent::class.java)
                                        .map { mom -> mom.apply { mom.selected = mNextFemaleSelection } }
                                        .sortedBy { mom -> mom.name }
                                        .toTypedArray())
                        )

                        mNextFemaleSelection = !mNextFemaleSelection

                        mFemaleAdapter.notifyDataSetChanged()


                    } else {

                        parentList.update(
                                *(mMaleAdapter.currentList
                                        .filterIsInstance(Parent::class.java)
                                        .map { dad -> dad.apply { dad.selected = mNextMaleSelection } }
                                        .toTypedArray())
                        )

                        groupList.update(
                                *(mMaleAdapter.currentList
                                        .filterIsInstance(PollenGroup::class.java)
                                        .map { group -> group.apply { group.selected = mNextMaleSelection } }
                                        .toTypedArray())
                        )

                        mNextMaleSelection = !mNextMaleSelection

                        mMaleAdapter.notifyDataSetChanged()
                    }
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