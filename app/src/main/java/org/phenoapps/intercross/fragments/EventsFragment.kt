package org.phenoapps.intercross.fragments

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
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_events.*
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.EventsAdapter
import org.phenoapps.intercross.data.Settings
import org.phenoapps.intercross.data.Wishlist
import org.phenoapps.intercross.databinding.FragmentEventsBinding
import org.phenoapps.intercross.util.FileUtil
import org.phenoapps.intercross.util.SnackbarQueue
import java.util.*

class EventsFragment : IntercrossBaseFragment() {

    private lateinit var mBinding: FragmentEventsBinding
    private lateinit var mAdapter: EventsAdapter

    private lateinit var mWishlist: List<Wishlist>
    private lateinit var mFocused: View

    private var mSettings = Settings()

    var mOrder: Int = 0
    var mAllowBlank: Boolean = false
    var mCollectData = true

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val orderKey = "org.phenoapps.intercross.CROSS_ORDER"
        val blankKey = "org.phenoapps.intercross.BLANK_MALE_ID"
        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        mOrder = (pref.getString(orderKey, "0") ?: "0").toInt()
        mAllowBlank = pref.getBoolean(blankKey, false)
        mCollectData = pref.getBoolean(SettingsFragment.COLLECT_INFO, true)

        pref.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            when(key) {
                orderKey -> mOrder = (sharedPreferences.getString(key, "0") ?: "0").toInt()
                blankKey -> mAllowBlank = sharedPreferences.getBoolean(key, false)
                SettingsFragment.COLLECT_INFO -> mCollectData = sharedPreferences.getBoolean(key, true)
            }
        }

        mBinding = FragmentEventsBinding
                .inflate(inflater, container, false)

        mAdapter = EventsAdapter(mBinding.root.context)

        mBinding.recyclerView.adapter = mAdapter


        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val event = mAdapter.currentList[viewHolder.adapterPosition]
                mEventsListViewModel.delete(event)
                mSnackbar.push(SnackbarQueue.SnackJob(mBinding.root, "${event.eventDbId}", "Undo") {
                    mEventsListViewModel.addCrossEvent(event)
                })

            }
        }).attachToRecyclerView(mBinding.recyclerView)

        //setup UI

        setHasOptionsMenu(true)

        //single text watcher class to check if all fields are non-empty to enable the save button
        val emptyGuard = object : TextWatcher {

            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                mBinding.saveButton.isEnabled = isInputValid()
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

        with(mBinding) {

            secondText.addTextChangedListener(emptyGuard)
            firstText.addTextChangedListener(emptyGuard)
            editTextCross.addTextChangedListener(emptyGuard)

            firstText.onFocusChangeListener = focusListener
            secondText.onFocusChangeListener = focusListener
            editTextCross.onFocusChangeListener = focusListener

            firstText.setOnEditorActionListener(TextView.OnEditorActionListener { _, i, _ ->
                if (i == EditorInfo.IME_ACTION_DONE) {
                    if ((mBinding.firstText.text ?: "").isNotEmpty()) mBinding.secondText.requestFocus()
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

            saveButton.isEnabled = isInputValid()

            mBinding.button.setOnClickListener {

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

            mBinding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

            clearButton.setOnClickListener {
                //editTextCross.setText("")
                firstText.setText("")
                secondText.setText("")
                firstText.requestFocus()
            }

            when (mOrder) {
                0 -> {
                    firstText.hint = "Female ID:"
                    secondText.hint = "Male ID:"
                }
                1 -> {
                    firstText.hint = "Male ID:"
                    secondText.hint = "Female ID:"
                }
            }
        }

        startObservers()

        return mBinding.root
    }

    private fun startObservers() {

        mEventsListViewModel.events.observe(viewLifecycleOwner, Observer { result ->
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
                        mBinding.editTextCross.setText("${mSettings.prefix}${mSettings.number.toString().padStart(mSettings.pad, '0')}${mSettings.suffix}")
                    }
                    mSettings.isUUID -> {
                        mBinding.editTextCross.setText(UUID.randomUUID().toString())
                    }
                    else -> mBinding.editTextCross.setText("")
                }
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

    private fun isInputValid(): Boolean {

        val male: String
        val female: String
        val cross: String = mBinding.editTextCross.text.toString()
        if (mOrder == 0) {
            female = mBinding.firstText.text.toString()
            male = mBinding.secondText.text.toString()
        } else {
            male = mBinding.firstText.text.toString()
            female = mBinding.secondText.text.toString()
        }

        //calculate how full the save button should be
        var numFilled = 0
        if (mAllowBlank) numFilled++
        else if (male.isNotBlank()) numFilled++
        if (female.isNotBlank()) numFilled++
        if (cross.isNotBlank()) numFilled++

        //change save button fill percentage using corresponding xml shapes
        mBinding.saveButton.background = ContextCompat.getDrawable(requireContext(),
            when (numFilled) {
                0,1,2 -> R.drawable.button_save_empty
                //1 -> R.drawable.button_save_third
                //2 -> R.drawable.button_save_two_thirds
                else -> R.drawable.button_save_full
            })

        return ((male.isNotEmpty() || mAllowBlank) && female.isNotEmpty()
                && (cross.isNotEmpty() || (mSettings.isUUID || mSettings.isPattern)))
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

    private fun askUserNewExperimentName() {

        val value = mBinding.editTextCross.text.toString()

        lateinit var male: String
        lateinit var female: String
        when (mOrder) {
            0 -> {
                female = (mBinding.firstText.text ?: "").toString()
                male = (mBinding.secondText.text ?: "").toString()
            }
            1 -> {
                male = (mBinding.firstText.text ?: "").toString()
                female = (mBinding.secondText.text ?: "").toString()
            }
        }

        if (value.isNotEmpty() && (male.isNotEmpty() || mAllowBlank)) {

            if (male.isEmpty()) male = "blank"
            //val first = (mBinding.firstText.text ?: "")
            //val second = (mBinding.secondText.text ?: "")

            mEventsListViewModel.addCrossEvent(value, female, male)

            FileUtil(requireContext()).ringNotification(true)
            checkWishlist(female, male, value)

            Handler().postDelayed(Runnable {
                mBinding.recyclerView.scrollToPosition(0)
            }, 250)

        } else {
            mSnackbar.push(SnackbarQueue.SnackJob(mBinding.root, "You must enter a cross name."))
            //FileUtil(requireContext()).ringNotification(false)
        }
    }

    private fun checkWishlist(f: String, m: String, x: String) {

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

        mBinding.firstText.setText("")
        mBinding.secondText.setText("")
        mBinding.editTextCross.setText("")

        when {
            mSettings.isPattern -> {
                mSettings.number += 1
                mSettingsViewModel.update(mSettings)
                mBinding.editTextCross.setText("${mSettings.prefix}${mSettings.number.toString().padStart(mSettings.pad, '0')}${mSettings.suffix}")

            }
            mSettings.isUUID -> {
                mBinding.editTextCross.setText(UUID.randomUUID().toString())
            }
        }
        if (current >= min && min != 0) {
            FileUtil(requireContext()).ringNotification(true)
            mSnackbar.push(SnackbarQueue.SnackJob(mBinding.root, "Wishlist complete for $f and $m : $current/$min"))
        } else mSnackbar.push(SnackbarQueue.SnackJob(mBinding.root, "New Cross Event! $x added."))

        mBinding.firstText.requestFocus()
    }
}