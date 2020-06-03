package org.phenoapps.intercross.fragments

import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.EventsAdapter
import org.phenoapps.intercross.data.EventName
import org.phenoapps.intercross.data.Events
import org.phenoapps.intercross.data.PollenGroup
import org.phenoapps.intercross.data.Wishlist
import org.phenoapps.intercross.databinding.FragmentEventsBinding
import org.phenoapps.intercross.util.DateUtil
import org.phenoapps.intercross.util.FileUtil
import org.phenoapps.intercross.util.SnackbarQueue
import java.util.*

class EventsFragment : IntercrossBaseFragment<FragmentEventsBinding>(R.layout.fragment_events), LifecycleObserver {

    private lateinit var mAdapter: EventsAdapter

    private lateinit var mWishlist: List<Wishlist>
    private lateinit var mGroups: List<PollenGroup>

    private lateinit var mFocused: View

    private var mLastOpened: Long = 0L

    override fun FragmentEventsBinding.afterCreateView() {

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this@EventsFragment)

        setupRecyclerView()

        setupTextInput()

        setupButtons()

        setHasOptionsMenu(true)

        val order = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString(SettingsFragment.ORDER, "0")
        when (order) {
            "0" -> {
                firstText.hint = "Female ID:"
                secondText.hint = "Male ID:"
            }
            "1" -> {
                firstText.hint = "Male ID:"
                secondText.hint = "Female ID:"
            }
        }

        startObservers()
    }

    private fun FragmentEventsBinding.setupRecyclerView() {

        //recyclerView lists EventsAdapter(requireContext())

        mAdapter = EventsAdapter(root.context)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recyclerView.adapter = mAdapter

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val event = mAdapter.currentList[viewHolder.adapterPosition]
                mEventsListViewModel.delete(event)
                mSnackbar.push(SnackbarQueue.SnackJob(root, "${event.eventDbId}", "Undo") {
                    submitCrossEvent(event.apply { id = null })
                })

            }
        }).attachToRecyclerView(recyclerView)
    }

    private fun submitCrossEvent(e: Events) {
        mGroups.let {
            val groups = it.map { it.uuid }
            if (e.maleOBsUnitDbId in groups) {
                e.isPoly = true
            }
        }
        mEventsListViewModel.addCrossEvent(e)
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
            if (hasFocus) mFocused = v
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
                findNavController().navigate(R.id.barcode_scan_fragment,
                        Bundle().apply {
                            putString("mode", "single")
                        }
                )
        }

        saveButton.setOnClickListener {
            askUserNewExperimentName()
        }


        clearButton.setOnClickListener {
            //editTextCross.setText("")
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

    private fun FragmentEventsBinding.askUserNewExperimentName() {

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
            //val first = (mBinding.firstText.text ?: "")
            //val second = (mBinding.secondText.text ?: "")


            val tPerson = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .getString("org.phenoapps.intercross.PERSON", "")

            val experiment = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .getString("org.phenoapps.intercross.EXPERIMENT", "")

            //assert(person == tPerson)

            submitCrossEvent(Events(null, value, EventName.POLLINATION.itemType, female, male, null, DateUtil().getTime(), tPerson, experiment))


            FileUtil(requireContext()).ringNotification(true)
            checkWishlist(female, male, value)

            Handler().postDelayed(Runnable {
                recyclerView.scrollToPosition(0)
            }, 250)

        } else {
            mSnackbar.push(SnackbarQueue.SnackJob(root, "You must enter a cross name."))
            //FileUtil(requireContext()).ringNotification(false)
        }
    }

    private fun FragmentEventsBinding.checkWishlist(f: String, m: String, x: String) {

        var isOnList = false
        var min = 0
        var current = 0
        mWishlist.forEach {
            if (it.femaleDbId == f && it.maleDbId == m) {
                isOnList = true
                min = it.wishMin
                current++
            }
        }
        if (isOnList) {
            mAdapter.currentList.forEach {
                if (it.femaleObsUnitDbId == f && it.maleOBsUnitDbId == m) {
                    current++
                }
            }
        }

        firstText.setText("")
        secondText.setText("")
        editTextCross.setText("")

        when {
            mSettings.isPattern -> {
                mSettings.number += 1
                mSettingsViewModel.update(mSettings)
                editTextCross.setText("${mSettings.prefix}${mSettings.number.toString().padStart(mSettings.pad, '0')}${mSettings.suffix}")

            }
            mSettings.isUUID -> {
                editTextCross.setText(UUID.randomUUID().toString())
            }
        }
        if (current >= min && min != 0) {
            FileUtil(requireContext()).ringNotification(true)
            mSnackbar.push(SnackbarQueue.SnackJob(root, "Wishlist complete for $f and $m : $current/$min"))
        } else mSnackbar.push(SnackbarQueue.SnackJob(root, "New Cross Event! $x added."))

        firstText.requestFocus()
    }

    private fun FragmentEventsBinding.startObservers() {

        mEventsListViewModel.crosses.observe(viewLifecycleOwner, Observer { result ->
            result?.let {
                mAdapter.submitList(it.reversed())
            }
        })

        mWishlistViewModel.wishlist.observe(viewLifecycleOwner, Observer{
            it?.let {
                mWishlist = it
            }
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

        mSettingsViewModel.settings.observe(viewLifecycleOwner, Observer {
            it?.let {
                mSettings = it
                when {
                    mSettings.isPattern -> {
                        editTextCross.setText("${mSettings.prefix}${mSettings.number.toString().padStart(mSettings.pad, '0')}${mSettings.suffix}")
                    }
                    mSettings.isUUID -> {
                        editTextCross.setText(UUID.randomUUID().toString())
                    }
                    else -> editTextCross.setText("")
                }
            }
        })

        mPollenManagerViewModel.groups.observe(viewLifecycleOwner, Observer {
            it?.let {
                mGroups = it
            }
        })
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

    private fun askIfSamePerson() {

        val builder = AlertDialog.Builder(requireContext()).apply {

            setNegativeButton("Change Person") { _, _ ->
                findNavController().navigate(R.id.settings_fragment, Bundle().apply {
                    putString("org.phenoapps.intercross.ASK_PERSON", "true")
                })
            }

            setPositiveButton("Yes") { _, _ ->
                //welcome back
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun afterReturn() {
        val timeLength = System.nanoTime() - mLastOpened
        Log.d("TIME", timeLength.toString())
        //24*60*60 = 86400s timeLength is in nanoseconds
        if ((timeLength > 864e11 && mLastOpened != 0L)) {
            askIfSamePerson()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun afterClosed() {
        mLastOpened = System.nanoTime()
    }

}