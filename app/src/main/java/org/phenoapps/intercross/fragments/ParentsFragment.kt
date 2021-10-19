package org.phenoapps.intercross.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import org.phenoapps.intercross.activities.MainActivity
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.ParentsAdapter
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.ParentsRepository
import org.phenoapps.intercross.data.PollenGroupRepository
import org.phenoapps.intercross.data.models.BaseParent
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.Parent
import org.phenoapps.intercross.data.models.PollenGroup
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.ParentsListViewModel
import org.phenoapps.intercross.data.viewmodels.PollenGroupListViewModel
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.ParentsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.PollenGroupListViewModelFactory
import org.phenoapps.intercross.databinding.FragmentParentsBinding
import org.phenoapps.intercross.util.BluetoothUtil
import org.phenoapps.intercross.util.SnackbarQueue

class ParentsFragment: IntercrossBaseFragment<FragmentParentsBinding>(R.layout.fragment_parents) {

    private val viewModel: ParentsListViewModel by viewModels {
        ParentsListViewModelFactory(ParentsRepository.getInstance(db.parentsDao()))
    }

    private val eventsModel: EventListViewModel by viewModels {
        EventsListViewModelFactory(EventsRepository.getInstance(db.eventsDao()))
    }

    private val groupList: PollenGroupListViewModel by viewModels {
        PollenGroupListViewModelFactory(PollenGroupRepository.getInstance(db.pollenGroupDao()))
    }

    private val parentList: ParentsListViewModel by viewModels {
        ParentsListViewModelFactory(ParentsRepository.getInstance(db.parentsDao()))
    }

    private var mCrosses: List<Event> = ArrayList()

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

        eventsModel.events.observe(viewLifecycleOwner) { parents ->

            parents?.let {

                mCrosses = it

            }
        }

        //TODO Trevor: What happens when wishlist import includes different code ids with same name, similarly for cross events

        viewModel.parents.observe(viewLifecycleOwner, { parents ->

            val addedMales = ArrayList<BaseParent>()

            groupList.groups.observe(viewLifecycleOwner, { groups ->

                addedMales.clear()

                addedMales.addAll(groups.distinctBy { it.codeId })

                mMaleAdapter.submitList(addedMales+(parents
                    .filter { p -> p.sex == 1 }
                    .distinctBy { p -> p.codeId }
                    .sortedBy { p -> p.name}))

            })

            mMaleAdapter.submitList(addedMales+(parents
                .filter { p -> p.sex == 1 }
                .distinctBy { p -> p.codeId }
                .sortedBy { p -> p.name }))

            mFemaleAdapter.submitList(parents
                .filter { p -> p.sex == 0 }
                .distinctBy { p -> p.codeId }
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

            //variable that tracks whether user tried to delete parents of a cross, this will display a message.
            var triedToDelete = false

            if (tabLayout.getTabAt(0)?.isSelected == true) {

                val out: List<Parent> = mFemaleAdapter.currentList.filterIsInstance(Parent::class.java)
                        .filter { p -> p.selected }


                //don't delete parents that have been crossed.
                if (mCrosses.isNotEmpty()) {

                    //find all parents with crosses (that are selected)
                    val parentOfCrossed = out.filter { p -> mCrosses.any { crossed -> p.codeId == crossed.femaleObsUnitDbId } }

                    //if the result is empty, just delete the original array, otherwise remove the parents and delete from original
                    if (parentOfCrossed.isEmpty()) {

                        viewModel.delete(*out.toTypedArray())

                    } else {

                        triedToDelete = true

                        viewModel.delete(*(out-parentOfCrossed).toTypedArray())
                    }

                } else {

                    viewModel.delete(*out.toTypedArray())

                }

            } else {

                val outParents = mMaleAdapter.currentList.filterIsInstance(Parent::class.java)
                        .filter { p -> p.selected }

                val outGroups = mMaleAdapter.currentList.filterIsInstance(PollenGroup::class.java)
                        .filter { g -> g.selected }

                if (mCrosses.isNotEmpty()) {

                    val parentOfCrossed = outParents.filter { p -> mCrosses.any { crossed -> p.codeId == crossed.maleObsUnitDbId } }
                    val parentOfGroup = outGroups.filter { p -> mCrosses.any { crossed -> p.codeId == crossed.maleObsUnitDbId } }

                    if (parentOfCrossed.isEmpty()) {

                        viewModel.delete(*outParents.toTypedArray())

                    } else {

                        triedToDelete = true

                        viewModel.delete(*(outParents-parentOfCrossed).toTypedArray())
                    }

                    if (parentOfGroup.isEmpty()) {

                        groupList.deleteByCode(outGroups.map { g -> g.codeId })

                    } else {

                        triedToDelete = true

                        groupList.deleteByCode(((outGroups-parentOfGroup).map { g -> g.codeId }))
                    }

                } else {

                    viewModel.delete(*outParents.toTypedArray())

                    groupList.deleteByCode(outGroups.map { g -> g.codeId })

                }
            }

            if (triedToDelete) {

                mSnackbar.push(SnackbarQueue.SnackJob(mBinding.root, getString(R.string.parents_of_crosses_not_deleted)))

            }
        }

        printButton.setOnClickListener {

//            val experiment = PreferenceManager.getDefaultSharedPreferences(requireContext())
//                    .getString("org.phenoapps.intercross.EXPERIMENT", "")
//
//            val person = PreferenceManager.getDefaultSharedPreferences(requireContext())
//                    .getString("org.phenoapps.intercross.PERSON", "")

            if (tabLayout.getTabAt(0)?.isSelected == true) {

                val outParents = mFemaleAdapter.currentList.filterIsInstance(Parent::class.java)

                BluetoothUtil().print(requireContext(), outParents.filter { p -> p.selected }.toTypedArray())

            } else {

                val outParents = mMaleAdapter.currentList
                        .filterIsInstance(Parent::class.java)
                        .filter { p -> p.selected }

                val outAll = outParents + mMaleAdapter.currentList
                        .filterIsInstance(PollenGroup::class.java)
                        .filter { p -> p.selected }
                        .map { group -> Parent(group.codeId, 1, group.name)}

                BluetoothUtil().print(requireContext(), outAll.toTypedArray())

            }
        }


//        val gdc = GestureDetectorCompat(context, gestureListener)
//
//        maleRecycler.setOnTouchListener { _, motionEvent ->
//            gdc.onTouchEvent(motionEvent)
//        }
//
//        femaleRecycler.setOnTouchListener({ _, motionEvent ->
//            gdc.onTouchEvent(motionEvent)
//        })

        bottomNavBar.selectedItemId = R.id.action_nav_parents

        setupBottomNavBar()

    }

    override fun onResume() {
        super.onResume()

        mBinding.bottomNavBar.selectedItemId = R.id.action_nav_parents
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.parents_toolbar, menu)

        //Bug in Gradle system (?) for some reason this icon is transformed to black fill
        //in drawable-anydpi-v21
        menu.findItem(R.id.action_select_all).icon?.setTint(Color.WHITE)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        with (mBinding) {

            when(item.itemId) {

                R.id.action_import -> {

                    (activity as? MainActivity)?.launchImport()

                }
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
                else -> true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun FragmentParentsBinding.setupBottomNavBar() {

        bottomNavBar.setOnNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.action_nav_settings -> {

                    findNavController().navigate(R.id.global_action_to_settings_fragment)
                }
                R.id.action_nav_export -> {

                    (activity as MainActivity).showImportOrExportDialog {

                        bottomNavBar.selectedItemId = R.id.action_nav_parents
                    }

                }
                R.id.action_nav_home -> {

                    findNavController().navigate(ParentsFragmentDirections.globalActionToEvents())

                }
                R.id.action_nav_cross_count -> {

                    findNavController().navigate(ParentsFragmentDirections.globalActionToCrossCount())
                }
            }

            true
        }
    }

    private fun FragmentParentsBinding.swipeLeft() {

        tabLayout.getTabAt(1)?.select()

    }

    private fun FragmentParentsBinding.swipeRight() {

        tabLayout.getTabAt(0)?.select()

    }
}