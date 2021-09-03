package org.phenoapps.intercross.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.ActionMenuView
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
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
import org.phenoapps.intercross.util.Dialogs

class ParentsFragment: IntercrossBaseFragment<FragmentParentsBinding>(R.layout.fragment_parents),
    CoroutineScope by MainScope() {

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
                            fragParentsSelectAllCb.isChecked = !mNextFemaleSelection
                        }
                        "Male" -> {
                            maleRecycler.visibility=View.VISIBLE
                            femaleRecycler.visibility=View.GONE
                            fragParentsSelectAllCb.isChecked = !mNextMaleSelection
                        }
                    }

                    viewModel.parents.observe(viewLifecycleOwner, { parents ->

                        groupList.groups.observe(viewLifecycleOwner, { groups ->

                            mBinding.updateSelectionText(
                                parents.filter { it.selected },
                                groups.filter { it.selected })

                        })
                    })
                }
            }
        })

        /*
        On startup, read arguments and determine the tab
         */
        tabLayout.getTabAt(tabFocus)?.select()

        eventsModel.events.observe(viewLifecycleOwner) { parents ->

            parents?.let {

                mCrosses = it

            }
        }

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

            mBinding.updateSelectionText(parents.filter { it.selected })

        })

        /*
        Go to Pollen Manager fragment for male group data-entry
         */
        fragParentsNewParentBtn.setOnClickListener {

            when (tabLayout.getTabAt(0)?.isSelected) {

                true -> Navigation.findNavController(mBinding.root)
                        .navigate(ParentsFragmentDirections.actionParentsToCreateEvent(0))

                else -> Navigation.findNavController(mBinding.root)
                        .navigate(ParentsFragmentDirections.actionParentsToCreateEvent(1))
            }

        }

        fragParentsSelectAllCb.setOnClickListener {

            mBinding.selectAll()

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

        setupToolbar()

    }

    private fun FragmentParentsBinding.setupToolbar() {

        val menu = fragParentsTb.findViewById<ActionMenuView>(R.id.frag_parents_action_menu_view)

        menu?.setOnMenuItemClickListener {
            onOptionsItemSelected(it)
        }

        activity?.menuInflater?.let {
            onCreateOptionsMenu(menu.menu, it)
        }
    }

    private fun FragmentParentsBinding.updateSelectionText(parents: List<Parent>, groups: List<PollenGroup>? = null) {

        val selectedSex = if (tabLayout.getTabAt(0)?.isSelected != false) 0 else 1

        var count = parents.filter { it.sex == selectedSex }.count()
        if (selectedSex == 1) count += groups?.count() ?: 0

        val tv = fragParentsTb.findViewById<TextView>(R.id.frag_parents_toolbar_count_tv)

        when (count) {
            0 -> {
                tv.visibility = View.GONE
                updateMenuButtons(expanded = false)
            }
            else -> {
                tv.text = count.toString()
                tv.visibility = View.VISIBLE
                updateMenuButtons(expanded = true)
            }
        }
    }

    private fun updateMenuButtons(expanded: Boolean = false) {
        arrayOf(R.id.action_parents_delete, R.id.action_parents_print).forEach {
            mBinding.fragParentsTb.findViewById<ActionMenuView>(R.id.frag_parents_action_menu_view)
                ?.menu?.findItem(it)?.isVisible = expanded
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.parents_toolbar, menu)
    }

    /**
     * Toggles the selected field for all parents in the current rv.
     */
    private fun FragmentParentsBinding.selectAll() {

        if (tabLayout.getTabAt(0)?.isSelected == true) {

            parentList.update(
                *(mFemaleAdapter.currentList
                    .filterIsInstance(Parent::class.java)
                    .map { mom -> mom.apply { mom.selected = mNextFemaleSelection } }
                    .sortedBy { mom -> mom.name }
                    .toTypedArray())
            )

            mNextFemaleSelection = !mNextFemaleSelection

            mFemaleAdapter.notifyItemRangeChanged(0, mFemaleAdapter.itemCount)


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
                    .map { g -> g.apply { selected = mNextMaleSelection } }
                    .toTypedArray())
            )

            mNextMaleSelection = !mNextMaleSelection

            mMaleAdapter.notifyItemRangeChanged(0, mMaleAdapter.itemCount)
        }
    }

    /**
     * Show a dialog to confirm deletion to the user.
     * If the user selects OK then this function either deletes males or females from the database.
     * If a parent is used in a cross then it will not be deleted and a message is shown.
     * Similarly poly groups will not be deleted if used as a parent.
     * The count could also add the number of males when a poly is selected but as of now it just deletes/prints the group id.
     */
    private fun FragmentParentsBinding.deleteParents() {

        context?.let { ctx ->

            Dialogs.onOk(AlertDialog.Builder(ctx),
                getString(R.string.frag_parent_delete_selected_title),
                getString(android.R.string.cancel),
                getString(android.R.string.ok),
                getString(R.string.frag_parent_confirm_delete_message)) {

                if (tabLayout.getTabAt(0)?.isSelected == true) {

                    val out: List<Parent> = mFemaleAdapter.currentList.filterIsInstance(Parent::class.java)
                        .filter { p -> p.selected }

                    //find all parents with crosses (that are selected)
                    val parentOfCrossed = out.filter { p -> mCrosses.any { crossed -> p.codeId == crossed.femaleObsUnitDbId } }

                    viewModel.delete(*(out-parentOfCrossed).toTypedArray())

                    if (!parentOfCrossed.isEmpty()) {

                        Toast.makeText(context, R.string.frag_parents_parents_not_deleted_reason,
                            Toast.LENGTH_SHORT).show()
                    }

                } else {

                    val outParents = mMaleAdapter.currentList.filterIsInstance(Parent::class.java)
                        .filter { p -> p.selected }

                    val outGroups = mMaleAdapter.currentList.filterIsInstance(PollenGroup::class.java)
                        .filter { g -> g.selected }

                    val parentOfCrossed = outParents.filter { p -> mCrosses.any { crossed -> p.codeId == crossed.maleObsUnitDbId } }
                    val parentOfGroup = outGroups.filter { p -> mCrosses.any { crossed -> p.codeId == crossed.maleObsUnitDbId } }

                    viewModel.delete(*(outParents-parentOfCrossed).toTypedArray())

                    groupList.deleteByCode(((outGroups-parentOfGroup).map { g -> g.codeId }))

                    if (parentOfCrossed.isNotEmpty() || parentOfGroup.isNotEmpty()) {

                        Toast.makeText(
                            context, R.string.frag_parents_parents_not_deleted_reason,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun FragmentParentsBinding.printParents() {

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

    override fun onResume() {
        super.onResume()

        mBinding.bottomNavBar.selectedItemId = R.id.action_nav_parents
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {

            R.id.action_import -> {

                (activity as? MainActivity)?.launchImport()

            }

            R.id.action_parents_delete -> {
                mBinding.deleteParents()
            }

            R.id.action_parents_print -> {
                mBinding.printParents()
            }

            android.R.id.home -> {
                findNavController().popBackStack()
            }
            else -> true
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

                    (activity as MainActivity).showExportDialog {

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