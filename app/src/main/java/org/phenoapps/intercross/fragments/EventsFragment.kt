package org.phenoapps.intercross.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_events.*
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.EventsAdapter
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.ParentsRepository
import org.phenoapps.intercross.data.SettingsRepository
import org.phenoapps.intercross.data.WishlistRepository
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.Parent
import org.phenoapps.intercross.data.models.Settings
import org.phenoapps.intercross.data.models.Wishlist
import org.phenoapps.intercross.data.viewmodels.*
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.ParentsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.SettingsViewModelFactory
import org.phenoapps.intercross.databinding.FragmentEventsBinding
import org.phenoapps.intercross.util.DateUtil
import org.phenoapps.intercross.util.FileUtil
import org.phenoapps.intercross.util.SnackbarQueue
import java.util.*

class EventsFragment : IntercrossBaseFragment<FragmentEventsBinding>(R.layout.fragment_events) {

    private val viewModel: EventListViewModel by viewModels {
        EventsListViewModelFactory(EventsRepository.getInstance(db.eventsDao()))
    }

    private val settingsModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(SettingsRepository.getInstance(db.settingsDao()))
    }

    private val parentsList: ParentsListViewModel by viewModels {
        ParentsListViewModelFactory(ParentsRepository.getInstance(db.parentsDao()))
    }

    private var mSettings: Settings = Settings()

    private val mAdapter: EventsAdapter = EventsAdapter()

    private lateinit var mWishlistStore: WishlistViewModel
    private lateinit var mSharedViewModel: CrossSharedViewModel

    private lateinit var mWishlist: List<Wishlist>

    private fun getFirstOrder(context: Context): String {

        val order = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(SettingsFragment.ORDER, "0")

        return when (order) {

            "0" -> context.getString(R.string.FemaleID)

            else -> context.getString(R.string.MaleID)

        }
    }

    private fun getSecondOrder(context: Context): String {

        val order = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(SettingsFragment.ORDER, "0")

        return when (order) {

            "1" -> context.getString(R.string.FemaleID)

            else -> context.getString(R.string.MaleID)

        }
    }

    override fun FragmentEventsBinding.afterCreateView() {

        recyclerView.adapter = EventsAdapter()

        recyclerView.layoutManager = LinearLayoutManager(context)

        mWishlistStore = WishlistViewModel(WishlistRepository.getInstance(db.wishlistDao()))

        mSharedViewModel = CrossSharedViewModel()

        firstHint = getFirstOrder(requireContext())

        secondHint = getSecondOrder(requireContext())

        viewModel.events.observe(viewLifecycleOwner, Observer {

            it?.let {

                (recyclerView.adapter as? EventsAdapter)?.submitList(it)

                recyclerView.adapter?.notifyDataSetChanged()
            }
        })

        settingsModel.settings.observeForever {

            it?.let {

                mSettings = it

                mBinding.settings = it

            }
        }

        mWishlistStore.wishlist.observe(viewLifecycleOwner, Observer {

            mWishlist = it
        })

        mSharedViewModel.lastScan.observe(viewLifecycleOwner, Observer {

            it?.let {

                if (it.isNotEmpty()) {

                    if ((firstText.text ?: "").isEmpty()) firstText.setText(it)
                    else if ((secondText.text ?: "").isEmpty()) {

                        secondText.setText(it)
                        if ((mSettings.isPattern || mSettings.isUUID)) askUserNewExperimentName()

                    }
                    else if ((editTextCross.text ?: "").isEmpty()) {

                        editTextCross.setText(it)
                        askUserNewExperimentName()

                    }

                    mSharedViewModel.lastScan.value = ""
                }
            }
        })

        executePendingBindings()

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        setupUI()

    }

    private fun FragmentEventsBinding.setupUI() {

        setupRecyclerView()

        setupTextInput()

        setupButtons()

        setHasOptionsMenu(true)

    }

    private fun FragmentEventsBinding.setupRecyclerView() {

        //setup recycler adapter
        recyclerView.adapter = EventsAdapter()

        //setup on item swipe to delete
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                (recyclerView.adapter as EventsAdapter)
                        .currentList[viewHolder.adapterPosition].also { event ->

                    event.id?.let {

                        viewModel.deleteById(eid = it)

                        mSnackbar.push(SnackbarQueue.SnackJob(root, event.readableName, "Undo") {

                            submitCrossEvent(event.apply { id = null })
                        })
                    }


                }
            }
        }).attachToRecyclerView(recyclerView)
    }

    private fun submitCrossEvent(e: Event) {

        parentsList.insertIgnore(
                Parent(e.femaleObsUnitDbId, 0), Parent(e.maleObsUnitDbId, 1)
        )

        viewModel.insert(e)

    }

    private fun resetDataEntry() {

        firstText.setText("")

        secondText.setText("")

        editTextCross.setText("")

        when {

            mSettings.isPattern -> {

                editTextCross.setText(mSettings.pattern)
            }

            mSettings.isUUID -> {

                editTextCross.setText(UUID.randomUUID().toString())
            }
        }
    }

    private fun FragmentEventsBinding.setupTextInput() {

        //single text watcher class to check if all fields are non-empty to enable the save button
        val emptyGuard = object : TextWatcher {

            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

                saveButton.isEnabled = isInputValid()
            }

            override fun afterTextChanged(editable: Editable) {

            }
        }

        val focusListener = View.OnFocusChangeListener { v, hasFocus ->
            //if (hasFocus) mFocused = v
            if (hasFocus && (PreferenceManager.getDefaultSharedPreferences(requireContext())
                            .getString("org.phenoapps.intercross.PERSON", "") ?: "").isBlank()) {
                askUserForPerson()
            }
        }

        secondText.addTextChangedListener(emptyGuard)
        firstText.addTextChangedListener(emptyGuard)
        editTextCross.addTextChangedListener(emptyGuard)

        firstText.onFocusChangeListener = focusListener
        secondText.onFocusChangeListener = focusListener
        editTextCross.onFocusChangeListener = focusListener

        firstText.setOnEditorActionListener(TextView.OnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                if ((firstText.text ?: "").isNotEmpty()) secondText.requestFocus()
                return@OnEditorActionListener true
            }
            false
        })

        //if auto generation is enabled save after the second text is submitted
        secondText.setOnEditorActionListener(TextView.OnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                if (!(mSettings.isPattern || mSettings.isUUID) && (secondText.text ?: "").isNotEmpty()) {
                    editTextCross.requestFocus()
                } else {
                    askUserNewExperimentName()
                }
                return@OnEditorActionListener true
            }
            false
        })

        editTextCross.setOnEditorActionListener(TextView.OnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                askUserNewExperimentName()
                return@OnEditorActionListener true
            }
            false
        })

    }

    private fun FragmentEventsBinding.setupButtons() {

        saveButton.isEnabled = isInputValid()

        button.setOnClickListener {

            if ((PreferenceManager.getDefaultSharedPreferences(requireContext())
                            .getString("org.phenoapps.intercross.PERSON", "") ?: "").isBlank()) {
                askUserForPerson()
            } else
                findNavController()
                        .navigate(EventsFragmentDirections
                        .actionToBarcodeScanFragment())
        }

        saveButton.setOnClickListener {

            askUserNewExperimentName()
        }

        clearButton.setOnClickListener {

            firstText.setText("")

            secondText.setText("")

            val person = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .getString("org.phenoapps.intercross.PERSON", "") ?: ""

            if (person.isNotBlank()) firstText.requestFocus()
        }
    }

    private fun FragmentEventsBinding.isInputValid(): Boolean {

        val allowBlank = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString(SettingsFragment.BLANK, "0")
        val order = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString(SettingsFragment.ORDER, "0")
        val male: String
        val female: String
        val cross: String = editTextCross.text.toString()
        if (order == "0") {
            female = firstText.text.toString()
            male = secondText.text.toString()
        } else {
            male = firstText.text.toString()
            female = secondText.text.toString()
        }

        //calculate how full the save button should be
        var numFilled = 0
        if (allowBlank == "1") numFilled++
        else if (male.isNotBlank()) numFilled++
        if (female.isNotBlank()) numFilled++
        if (cross.isNotBlank()) numFilled++

        //change save button fill percentage using corresponding xml shapes
        saveButton.background = ContextCompat.getDrawable(requireContext(),
                when (numFilled) {
                    0,1,2 -> R.drawable.button_save_empty
                    //1 -> R.drawable.button_save_third
                    //2 -> R.drawable.button_save_two_thirds
                    else -> R.drawable.button_save_full
                })

        return ((male.isNotEmpty() || (allowBlank == "1")) && female.isNotEmpty()
                && (cross.isNotEmpty() || (mSettings.isUUID || mSettings.isPattern)))
    }

    private fun askUserNewExperimentName() {

        val value = editTextCross.text.toString()

        val allowBlank = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString(SettingsFragment.BLANK, "0")
        val order = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString(SettingsFragment.ORDER, "0")

        lateinit var male: String
        lateinit var female: String

        when (order) {
            "0" -> {
                female = (firstText.text ?: "").toString()
                male = (secondText.text ?: "").toString()
            }
            "1" -> {
                male = (firstText.text ?: "").toString()
                female = (secondText.text ?: "").toString()
            }
        }

        if (value.isNotEmpty() && (male.isNotEmpty() || allowBlank == "1")) {

            if (male.isEmpty()) male = "blank"

            val tPerson = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .getString("org.phenoapps.intercross.PERSON", "")

            val experiment = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .getString("org.phenoapps.intercross.EXPERIMENT", "")

            submitCrossEvent(Event(value, female, male, value, DateUtil().getTime(), tPerson
                    ?: "?", experiment ?: "?"))

            FileUtil(requireContext()).ringNotification(true)

            checkWishlist(female, male, value)

            resetDataEntry()

            if (mSettings.isPattern) {

                settingsModel.update(mSettings.apply {

                    number += 1
                })
            }

            firstText.requestFocus()

            Handler().postDelayed({
                recyclerView.scrollToPosition(0)
            }, 250)

        } else {
            mSnackbar.push(SnackbarQueue.SnackJob(mBinding.root, "You must enter a cross name."))
            //FileUtil(requireContext()).ringNotification(false)
        }
    }

    private fun checkWishlist(f: String, m: String, x: String) {

        mWishlist.find { it.femaleDbId == f && it.maleDbId == m}?.let { item ->

            //TODO: Add Alert Dialog when min or max is achieved.

            val current = item.wishCurrent + 1

            //wishlist item has been found, item should be updated and visualized
            mWishlistStore.update(item.apply {

                wishCurrent = current
            })

            if (current >= item.wishMin && item.wishMin != 0) {

                FileUtil(requireContext()).ringNotification(true)

                mSnackbar.push(SnackbarQueue.SnackJob(mBinding.root, "Wishlist complete for $f and $m : $current/${item.wishMin}"))

            } else mSnackbar.push(SnackbarQueue.SnackJob(mBinding.root, "New Cross Event! $x added."))
        }
    }

    private fun askUserForPerson() {

        mBinding.constraintLayoutParent.requestFocus()

        val builder = AlertDialog.Builder(requireContext()).apply {

            setNegativeButton("Cancel") { _, _ ->
                mSnackbar.push(SnackbarQueue.SnackJob(mBinding.root, "Person must be set before crosses can be made."))
            }

            setPositiveButton("Set Person") { _, _ ->
                findNavController().navigate(R.id.settings_fragment, Bundle().apply {
                    putString("org.phenoapps.intercross.ASK_PERSON", "true")
                })
            }
        }

        builder.setTitle("Person must be set before crosses can be made.")
        builder.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.activity_main_toolbar, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {

            R.id.action_search_barcode -> {

                findNavController().navigate(R.id.barcode_scan_fragment,
                        Bundle().apply {
                            putString("mode", "search")
                        }
                )
            }
            R.id.action_continuous_barcode -> {

                if ((PreferenceManager.getDefaultSharedPreferences(requireContext())
                                .getString("org.phenoapps.intercross.PERSON", "") ?: "").isBlank()) {
                    askUserForPerson()
                } else
                    findNavController().navigate(R.id.barcode_scan_fragment,
                            Bundle().apply {
                                putString("mode", "continuous")
                            }
                    )
            }
        }
        return super.onOptionsItemSelected(item)
    }
}