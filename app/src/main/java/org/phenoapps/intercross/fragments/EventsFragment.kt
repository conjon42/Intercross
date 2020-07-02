package org.phenoapps.intercross.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_events.*
import org.phenoapps.intercross.BuildConfig
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.EventsAdapter
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.ParentsRepository
import org.phenoapps.intercross.data.SettingsRepository
import org.phenoapps.intercross.data.WishlistRepository
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.Parent
import org.phenoapps.intercross.data.models.Settings
import org.phenoapps.intercross.data.models.WishlistView
import org.phenoapps.intercross.data.viewmodels.CrossSharedViewModel
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.ParentsListViewModel
import org.phenoapps.intercross.data.viewmodels.SettingsViewModel
import org.phenoapps.intercross.data.viewmodels.WishlistViewModel
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.ParentsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.SettingsViewModelFactory
import org.phenoapps.intercross.databinding.FragmentEventsBinding
import org.phenoapps.intercross.util.CrossUtil
import org.phenoapps.intercross.util.Dialogs
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

    private lateinit var mParents: List<Parent>

    private var mSettings: Settings = Settings()

    private var mEvents: List<Event> = ArrayList()

    private var mEventsEmpty = true

    private lateinit var mWishlistStore: WishlistViewModel

    private val mSharedViewModel: CrossSharedViewModel by activityViewModels()

    private lateinit var mWishlistProgress: List<WishlistView>

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

        if ("demo" in BuildConfig.FLAVOR) {

            val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

            pref.edit().putString("org.phenoapps.intercross.PERSON", "Developer").apply()

        }

        val error = getString(R.string.ErrorCodeExists)

        recyclerView.adapter = EventsAdapter()

        recyclerView.layoutManager = LinearLayoutManager(context)

        mWishlistStore = WishlistViewModel(WishlistRepository.getInstance(db.wishlistDao()))

        firstHint = getFirstOrder(requireContext())

        secondHint = getSecondOrder(requireContext())

        parentsList.parents.observe(viewLifecycleOwner, Observer {

            it?.let {

                mParents = it

            }
        })

        viewModel.events.observe(viewLifecycleOwner, Observer {

            it?.let {

                mEvents = it

                mEventsEmpty = it.isEmpty()

                editTextCross.addTextChangedListener {

                    var codes = mEvents.map { event -> event.eventDbId } + mParents.map { parent -> parent.codeId }.distinct()

                    if (editTextCross.text.toString() in codes) {

                        if (crossTextHolder.error == null) crossTextHolder.error = error

                    } else crossTextHolder.error = null

                }

                (recyclerView.adapter as? EventsAdapter)?.submitList(it)

                recyclerView.adapter?.notifyDataSetChanged()
            }
        })

        settingsModel.settings.observe(viewLifecycleOwner, Observer {

            it?.let {

                mSettings = it

                mBinding.settings = it

            }
        })

        mWishlistStore.crossblock.observe(viewLifecycleOwner, Observer {

            it?.let {

                mWishlistProgress = it.filter { wish -> wish.wishType == "cross" }
            }

        })

        mSharedViewModel.lastScan.observe(viewLifecycleOwner, Observer {

            it?.let {

                if (it.isNotEmpty()) {

                    if ((firstText.text ?: "").isBlank()) {

                        firstText.setText(it)

                    }
                    else if ((secondText.text ?: "").isBlank()) {

                        secondText.setText(it)

                        if ((mSettings.isPattern || mSettings.isUUID)) {

                            askUserNewExperimentName()

                        }

                    }
                    else if ((editTextCross.text ?: "").isBlank()) {

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

        val undoString = getString(R.string.undo)

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

                        mSnackbar.push(SnackbarQueue.SnackJob(root, event.eventDbId, undoString) {

                            CrossUtil(requireContext()).submitCrossEvent(
                                    event.femaleObsUnitDbId, event.maleObsUnitDbId,
                                    event.eventDbId, mSettings, settingsModel, viewModel,
                                    mParents, parentsList, mWishlistProgress
                            )

                        })
                    }


                }
            }
        }).attachToRecyclerView(recyclerView)
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
            } else {

                findNavController().navigate(EventsFragmentDirections.actionToBarcodeScanFragment())

            }
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

            val crossIds = mEvents.map { event -> event.eventDbId }

            if (!crossIds.any { id -> id == value }) {

                val parent = mParents.find { parent -> parent.codeId == value }

                if (parent != null) {

                    Dialogs.notify(AlertDialog.Builder(requireContext()), getString(R.string.cross_id_already_exists_as_parent))

                } else {

                    CrossUtil(requireContext()).submitCrossEvent(female, male, value, mSettings, settingsModel, viewModel, mParents, parentsList, mWishlistProgress)

                }

                FileUtil(requireContext()).ringNotification(true)

                resetDataEntry()

                firstText.requestFocus()

                Handler().postDelayed({
                    recyclerView.scrollToPosition(0)
                }, 250)

            } else Dialogs.notify(AlertDialog.Builder(requireContext()), getString(R.string.cross_id_already_exists_as_event))

        } else {
            mSnackbar.push(SnackbarQueue.SnackJob(mBinding.root, getString(R.string.you_must_enter_cross_name)))
            FileUtil(requireContext()).ringNotification(false)
        }
    }

    private fun askUserForPerson() {

        mBinding.constraintLayoutParent.requestFocus()

        val person = getString(R.string.person)
        val personMustBeSet = getString(R.string.person_must_be_set)
        val setPerson = getString(R.string.set_person)

        val builder = AlertDialog.Builder(requireContext()).apply {
            
            setNegativeButton(person) { _, _ ->
                mSnackbar.push(SnackbarQueue.SnackJob(mBinding.root, personMustBeSet))
            }

            setPositiveButton(setPerson) { _, _ ->
                findNavController().navigate(R.id.settings_fragment, Bundle().apply {
                    putString("org.phenoapps.intercross.ASK_PERSON", "true")
                })
            }
        }

        builder.setTitle(personMustBeSet)
        builder.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.activity_main_toolbar, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {

            R.id.action_search_barcode -> {

                findNavController().navigate(EventsFragmentDirections.actionToBarcodeScanFragment(1))

            }
            R.id.action_continuous_barcode -> {

                if ((PreferenceManager.getDefaultSharedPreferences(requireContext())
                                .getString("org.phenoapps.intercross.PERSON", "") ?: "").isBlank()) {
                    askUserForPerson()
                } else {

                    findNavController().navigate(EventsFragmentDirections.actionToBarcodeScanFragment(2))

                }

            }
        }
        return super.onOptionsItemSelected(item)
    }
}