package org.phenoapps.intercross.fragments

import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_events.*
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.EventsAdapter
import org.phenoapps.intercross.data.*
import org.phenoapps.intercross.databinding.FragmentEventsBinding
import org.phenoapps.intercross.viewmodels.CrossSharedViewModel
import org.phenoapps.intercross.viewmodels.EventsListViewModel
import org.phenoapps.intercross.viewmodels.SettingsViewModel
import org.phenoapps.intercross.viewmodels.WishlistViewModel
import java.util.*

class EventsFragment : Fragment() {

    private lateinit var mBinding: FragmentEventsBinding

    private lateinit var mAdapter: EventsAdapter

    private lateinit var mEventsListViewModel: EventsListViewModel

    private lateinit var mSharedViewModel: CrossSharedViewModel

    private lateinit var mSettingsViewModel: SettingsViewModel

    private lateinit var mWishlistViewModel: WishlistViewModel

    private lateinit var mWishlist: List<Wishlist>

    private var mSettings = Settings()

    private var mAllowBlank = false

    private var mOrder = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.run {
            with(ViewModelProviders.of(this)) {
                mSharedViewModel = this.get(CrossSharedViewModel::class.java)
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        mSettingsViewModel = ViewModelProviders.of(this,
                object : ViewModelProvider.NewInstanceFactory() {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return SettingsViewModel(SettingsRepository.getInstance(
                                IntercrossDatabase.getInstance(requireContext()).settingsDao())) as T

                    }
                }).get(SettingsViewModel::class.java)

        mWishlistViewModel = ViewModelProviders.of(this,
                object : ViewModelProvider.NewInstanceFactory() {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return WishlistViewModel(WishlistRepository.getInstance(
                                IntercrossDatabase.getInstance(requireContext()).wishlistDao())) as T

                    }
                }).get(WishlistViewModel::class.java)

        mWishlistViewModel.wishlist.observe(viewLifecycleOwner, Observer{
            it?.let {
                mWishlist = it
            }
        })

        mBinding = FragmentEventsBinding
                .inflate(inflater, container, false)

        mAdapter = EventsAdapter(mBinding.root.context)

        mBinding.recyclerView.adapter = mAdapter

        val db = IntercrossDatabase.getInstance(requireContext())

        mEventsListViewModel = ViewModelProviders.of(this,
                object : ViewModelProvider.NewInstanceFactory() {

                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return EventsListViewModel(
                                EventsRepository.getInstance(db.eventsDao())) as T

                    }
                }
        ).get(EventsListViewModel::class.java)

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                mEventsListViewModel.delete(mAdapter.currentList[viewHolder.adapterPosition])

            }
        }).attachToRecyclerView(mBinding.recyclerView)

        mEventsListViewModel.events.observe(viewLifecycleOwner, Observer { result ->
            result?.let {
                mAdapter.submitList(it.reversed())
            }
        })

        val orderKey = "org.phenoapps.intercross.CROSS_ORDER"
        val blankKey = "org.phenoapps.intercross.BLANK_MALE_ID"
        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
        mOrder = (pref.getString(orderKey, "0") ?: "0").toInt()
        mAllowBlank = pref.getBoolean(blankKey, false)
        pref.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            when(key) {
                orderKey -> mOrder = (sharedPreferences.getString(key, "0") ?: "0").toInt()
                blankKey -> mAllowBlank = sharedPreferences.getBoolean(key, false)
            }
        }

        mSharedViewModel.lastScan.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it.isNotEmpty()) {
                    if ((firstText.text ?: "").isEmpty()) firstText.setText(it)
                    else if ((secondText.text
                                    ?: "").isEmpty() && !mAllowBlank) secondText.setText(it)
                    else if ((editTextCross.text ?: "").isEmpty()) editTextCross.setText(it)

                    if (isInputValid()) {
                        mSharedViewModel.lastScan.value = ""
                        askUserNewExperimentName()
                    }
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

        val focusListener = View.OnFocusChangeListener { _, p1 ->
            if (p1 && (PreferenceManager.getDefaultSharedPreferences(requireContext())
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
                    when {
                        mOrder == 0 && mAllowBlank && (mBinding.firstText.text ?: "").isNotEmpty()
                                && (mSettings.isPattern || mSettings.isUUID) ->
                            askUserNewExperimentName()
                        mAllowBlank && (mBinding.firstText.text ?: "").isNotEmpty() &&
                                !(mSettings.isPattern || mSettings.isUUID) -> editTextCross.requestFocus()
                        (firstText.text ?: "").isNotEmpty() -> secondText.requestFocus()
                    }
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


        return mBinding.root
    }

    private fun askUserForPerson() {

        mBinding.constraintLayoutParent.requestFocus()

        val builder = AlertDialog.Builder(requireContext()).apply {

            setNegativeButton("Cancel") { _, _ ->
                Snackbar.make(mBinding.root,
                        "Person must be set before crosses can be made.", Snackbar.LENGTH_SHORT).show()
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

        //calculate how how full the save button should be
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

        if (value.isNotEmpty()) {

            val first = (mBinding.firstText.text ?: "")
            val second = (mBinding.secondText.text ?: "")

            when (mOrder) {
                0 -> {
                    if (first.isNotEmpty() && (second.isNotEmpty() || mAllowBlank)) {
                        mEventsListViewModel.addCrossEvent(mBinding.editTextCross.text.toString(),
                                mBinding.firstText.text.toString(), mBinding.secondText.text.toString())
                        checkWishlist(first.toString(), second.toString(), value)
                    } else Snackbar.make(mBinding.root,
                            "Parents must be defined.", Snackbar.LENGTH_SHORT).show()

                }
                1 -> {
                    if ((mBinding.secondText.text ?: "").isNotEmpty() &&
                            ((mBinding.firstText.text ?: "").isNotEmpty() || mAllowBlank)) {
                        mEventsListViewModel.addCrossEvent(mBinding.editTextCross.text.toString(),
                                mBinding.secondText.text.toString(), mBinding.firstText.text.toString())
                        checkWishlist(second.toString(), first.toString(), value)
                    }
                    else Snackbar.make(mBinding.root,
                            "Parents must be defined.", Snackbar.LENGTH_SHORT).show()
                }
            }

        } else {
            Snackbar.make(mBinding.root,
                    "You must enter a cross name.", Snackbar.LENGTH_LONG).show()
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
        if (current >= min) {
            Snackbar.make(mBinding.root, "Wishlist complete for $f and $m : $current/$min", Snackbar.LENGTH_LONG).show()
        } else Snackbar.make(mBinding.root,
               "New Cross Event! $x added.", Snackbar.LENGTH_SHORT).show()

        mBinding.firstText.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        //mBinding.editTextCross.setText("${mSettings.prefix}${mSettings.number.toString().padStart(mSettings.pad, '0')}${mSettings.suffix}")

    }
}