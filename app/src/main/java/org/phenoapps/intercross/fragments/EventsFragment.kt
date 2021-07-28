package org.phenoapps.intercross.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import org.phenoapps.intercross.BuildConfig
import org.phenoapps.intercross.MainActivity
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
import org.phenoapps.intercross.data.viewmodels.*
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.ParentsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.SettingsViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.WishlistViewModelFactory
import org.phenoapps.intercross.databinding.FragmentEventsBinding
import org.phenoapps.intercross.fragments.preferences.ToolbarPreferenceFragment
import org.phenoapps.intercross.util.*
import java.util.*
import kotlin.math.roundToInt


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

    private val wishStore: WishlistViewModel by viewModels {
        WishlistViewModelFactory(WishlistRepository.getInstance(db.wishlistDao()))
    }

    private var mParents: List<Parent> = ArrayList()

    private var mSettings: Settings = Settings()

    private var mEvents: List<Event> = ArrayList()

    private var mEventsEmpty = true

    private val mSharedViewModel: CrossSharedViewModel by activityViewModels()

    private var mWishlistProgress: List<WishlistView> = ArrayList()

    private var mFocused: View? = null

    private val scope = CoroutineScope(Dispatchers.IO)

    private val mPref by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    private val mKeyUtil by lazy {
        KeyUtil(context)
    }

    private fun getFirstOrder(context: Context): String {

        val maleFirst = mPref.getBoolean(mKeyUtil.nameCrossOrderKey, false)

        return if (maleFirst) context.getString(R.string.MaleID) else context.getString(R.string.FemaleID)
    }

    private fun getSecondOrder(context: Context): String {

        val maleFirst = mPref.getBoolean(mKeyUtil.nameCrossOrderKey, false)

        return if (maleFirst) context.getString(R.string.FemaleID) else context.getString(R.string.MaleID)

    }

    override fun FragmentEventsBinding.afterCreateView() {

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if ("demo" in BuildConfig.BUILD_TYPE) {

            pref.edit().putString("org.phenoapps.intercross.PERSON", "Developer").apply()

        }

        if (pref.getBoolean("first_load", true)) {

            settingsModel.insert(mSettings
                    .apply {
                        isUUID = true
                    })

            pref.edit().putBoolean("first_load", false).apply()
        }

        if (mSettings.isUUID) {

            mBinding.editTextCross.setText(UUID.randomUUID().toString())

        }

        recyclerView.adapter = EventsAdapter(this@EventsFragment, viewModel)

        recyclerView.layoutManager = LinearLayoutManager(context)

        firstHint = getFirstOrder(requireContext())

        secondHint = getSecondOrder(requireContext())

        startObservers()

        bottomNavBar.selectedItemId = R.id.action_nav_home

        (activity as MainActivity).supportActionBar?.hide()

        setupUI()

    }

    override fun onResume() {
        super.onResume()

        mBinding.bottomNavBar.selectedItemId = R.id.action_nav_home

    }

    private fun startObservers() {

        val error = getString(R.string.ErrorCodeExists)

        val isCommutative = mPref.getBoolean(mKeyUtil.workCommutativeKey, false)

        parentsList.parents.observe(viewLifecycleOwner, {

            it?.let {

                mParents = it

            }
        })

        viewModel.events.observe(viewLifecycleOwner, {

            it?.let {

                mEvents = it

                mEventsEmpty = it.isEmpty()

                mBinding.editTextCross.addTextChangedListener {

                    val value = mBinding.editTextCross.text.toString()

                    if (value.isNotBlank()) {

                        val codes = mEvents.map { event -> event.eventDbId } + mParents.map { parent -> parent.codeId }.distinct()

                        if (mBinding.editTextCross.text.toString() in codes) {

                            if (mBinding.crossTextHolder.error == null) mBinding.crossTextHolder.error = error

                        } else mBinding.crossTextHolder.error = null

                    } else {

                        mBinding.crossTextHolder.error = null

                    }
                }

                (mBinding.recyclerView.adapter as? EventsAdapter)?.submitList(it)
            }
        })

        settingsModel.settings.observe(viewLifecycleOwner, {

            it?.let {

                mSettings = it

                mBinding.settings = it

            }
        })

        if (isCommutative) {

            wishStore.commutativeWishes.observe(viewLifecycleOwner, {

                it?.let {

                    mWishlistProgress = it.filter { wish -> wish.wishType == "cross" }
                }

            })

        } else {

            wishStore.wishes.observe(viewLifecycleOwner, {

                it?.let {

                    mWishlistProgress = it.filter { wish -> wish.wishType == "cross" }
                }

            })
        }

        mSharedViewModel.lastScan.observe(viewLifecycleOwner, {

            it?.let {

                if (it.isNotBlank()) {

                    //Log.d("IntercrossNextScan", mFocused?.id.toString())

                    when (mFocused?.id) {

                        mBinding.firstText.id -> {

                            afterFirstText(it)

                        }
                        mBinding.secondText.id -> {

                            afterSecondText(it)

                        }
                        mBinding.editTextCross.id -> {

                            afterThirdText(it)

                        }
                        else -> {

                            /**
                             * if nothing is focused check each text field and apply current settings
                             */
                            val first = mBinding.firstText.text.toString()
                            val second = mBinding.secondText.text.toString()
                            //val third = editTextCross.text.toString()

                            val maleFirst = mPref.getBoolean(mKeyUtil.nameCrossOrderKey, false)

                            val blank = mPref.getBoolean(mKeyUtil.nameBlankMaleKey, false)

                            //first check first text, if male first and allow blank males then skip to second text
                            if (first.isBlank() && !(maleFirst && blank)) {

                                afterFirstText(it)

                            } else if (second.isBlank() && !(!maleFirst && blank)) {

                                afterSecondText(it)

                            } else afterThirdText(it)
                        }
                    }

                    mSharedViewModel.lastScan.value = ""
                }
            }
        })
    }

    private fun afterFirstText(value: String) {

        mBinding.firstText.setText(value)

        mBinding.secondText.requestFocus()
    }

    private fun afterSecondText(value: String) {

        val maleFirst = mPref.getBoolean(mKeyUtil.nameCrossOrderKey, false)

        val blank = mPref.getBoolean(mKeyUtil.nameBlankMaleKey, false)

        mBinding.secondText.setText(value)

        //check if female first, then check if string is empty to show error message
        if (!maleFirst) {

            if (value.isEmpty() && !blank) {

                mSnackbar.push(SnackbarQueue.SnackJob(mBinding.root, getString(R.string.you_must_enter_male_name)))

                return
            }

        }

        if ((mSettings.isPattern || mSettings.isUUID)) {

            askUserNewExperimentName()

        } else mBinding.editTextCross.requestFocus()
    }

    private fun afterThirdText(value: String) {

        mBinding.editTextCross.setText(value)

        askUserNewExperimentName()
    }

    private fun FragmentEventsBinding.setupUI() {

        setupRecyclerView()

        setupTextInput()

        setupButtons()

        setupBottomNavBar()

        setHasOptionsMenu(true)

    }

    private fun FragmentEventsBinding.setupBottomNavBar() {

        bottomNavBar.setOnNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.action_nav_settings -> {

                    findNavController().navigate(R.id.global_action_to_settings_fragment)
                }
                R.id.action_nav_parents -> {

                    findNavController().navigate(EventsFragmentDirections.globalActionToParents())

                }
                R.id.action_nav_export -> {

                    (activity as MainActivity).showImportOrExportDialog {

                        bottomNavBar.selectedItemId = R.id.action_nav_home

                    }
                }
                R.id.action_nav_cross_count -> {

                    findNavController().navigate(EventsFragmentDirections.actionToCrossCountFragment())

                }
            }

            true
        }
    }

    private fun FragmentEventsBinding.setupRecyclerView() {

        //setup recycler adapter
        recyclerView.adapter = EventsAdapter(this@EventsFragment, viewModel)

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

                            scope.launch {
                                CrossUtil(requireContext()).submitCrossEvent(mBinding.root,
                                    event.femaleObsUnitDbId, event.maleObsUnitDbId,
                                    event.eventDbId, mSettings, settingsModel, viewModel,
                                    mParents, parentsList, mWishlistProgress
                                )
                            }
                        })
                    }
                }
            }
        }).attachToRecyclerView(recyclerView)
    }

    private fun resetDataEntry() {

        mBinding.firstText.setText("")

        mBinding.secondText.setText("")

        mBinding.editTextCross.setText("")

        when {

            mSettings.isPattern -> {

                mBinding.editTextCross.setText(mSettings.pattern)
            }

            mSettings.isUUID -> {

                mBinding.editTextCross.setText(UUID.randomUUID().toString())
            }
        }

        mBinding.firstText.requestFocus()
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

            if (hasFocus && (mPref.getString(mKeyUtil.profPersonKey, "") ?: "").isBlank()) {
                askUserForPerson()
            }

        }

        //https://stackoverflow.com/questions/3425932/detecting-when-user-has-dismissed-the-soft-keyboard
        activity?.addKeyboardToggleListener { shown ->

            if (shown) bottomNavBar.visibility = View.GONE
            else bottomNavBar.visibility = View.VISIBLE
        }

        secondText.addTextChangedListener(emptyGuard)
        firstText.addTextChangedListener(emptyGuard)
        editTextCross.addTextChangedListener(emptyGuard)

        firstText.onFocusChangeListener = focusListener
        secondText.onFocusChangeListener = focusListener
        editTextCross.onFocusChangeListener = focusListener

        firstText.setOnEditorActionListener(TextView.OnEditorActionListener { _, i, _ ->

            if (i == EditorInfo.IME_ACTION_DONE) {

                val value = firstText.text.toString()

                afterFirstText(value)

                return@OnEditorActionListener true
            }

            false
        })

        //if auto generation is enabled save after the second text is submitted
        secondText.setOnEditorActionListener(TextView.OnEditorActionListener { _, i, _ ->

            if (i == EditorInfo.IME_ACTION_DONE) {

                val value = secondText.text.toString()

                afterSecondText(value)

                return@OnEditorActionListener true
            }

            false
        })

        editTextCross.setOnEditorActionListener(TextView.OnEditorActionListener { _, i, _ ->

            if (i == EditorInfo.IME_ACTION_DONE) {

                val value = editTextCross.text.toString()

                if (value.isNotBlank()) {

                    afterThirdText(value)

                }

                return@OnEditorActionListener true
            }

            false
        })

    }

    open class KeyboardToggleListener(
        private val root: View?,
        private val onKeyboardToggleAction: (shown: Boolean) -> Unit
    ) : ViewTreeObserver.OnGlobalLayoutListener {
        private var shown = false
        override fun onGlobalLayout() {
            root?.run {
                val heightDiff = rootView.height - height
                val thresh = 550
                val keyboardShown = heightDiff > thresh
                if (shown != keyboardShown) {
                    onKeyboardToggleAction.invoke(keyboardShown)
                    shown = keyboardShown
                }
            }
        }

        private fun View.dpToPix(dp: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics).roundToInt()

    }

    fun Activity.addKeyboardToggleListener(onKeyboardToggleAction: (shown: Boolean) -> Unit): KeyboardToggleListener? {
        val root = findViewById<View>(android.R.id.content)
        val listener = KeyboardToggleListener(root, onKeyboardToggleAction)
        return root?.viewTreeObserver?.run {
            addOnGlobalLayoutListener(listener)
            listener
        }
    }

    private fun FragmentEventsBinding.setupButtons() {

        saveButton.isEnabled = isInputValid()

        button.setOnClickListener {

            if (mPref.getString(mKeyUtil.profPersonKey, "").isNullOrBlank()) {
                askUserForPerson()
            } else {

                findNavController().navigate(EventsFragmentDirections.actionToBarcodeScanFragment())

            }
        }

        button.setOnLongClickListener {

            if (mPref.getString(mKeyUtil.profPersonKey, "").isNullOrBlank()) {
                askUserForPerson()
            } else {

                findNavController().navigate(EventsFragmentDirections.actionToBarcodeScanFragment(2))

            }

            true
        }

        fragmentEventsSearchButton.setOnClickListener {

            findNavController().navigate(EventsFragmentDirections.actionToBarcodeScanFragment(1))

        }

        saveButton.setOnClickListener {

            askUserNewExperimentName()
        }

        clearButton.setOnClickListener {

            firstText.setText("")

            secondText.setText("")

            val person = mPref.getString(mKeyUtil.profPersonKey, "") ?: ""

            if (person.isNotBlank()) firstText.requestFocus()
        }
    }

    private fun FragmentEventsBinding.isInputValid(): Boolean {

        val maleFirst = mPref.getBoolean(mKeyUtil.nameCrossOrderKey, false)

        val blank = mPref.getBoolean(mKeyUtil.nameBlankMaleKey, false)

        val male: String
        val female: String
        val cross: String = editTextCross.text.toString()
        if (!maleFirst) {
            female = firstText.text.toString()
            male = secondText.text.toString()
        } else {
            male = firstText.text.toString()
            female = secondText.text.toString()
        }

        //calculate how full the save button should be
        var numFilled = 0
        if (blank) numFilled++
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

        return ((male.isNotBlank() || blank) && female.isNotBlank()
                && (cross.isNotBlank() || (mSettings.isUUID || mSettings.isPattern)))
    }

    private fun askUserNewExperimentName() {

        val value = mBinding.editTextCross.text.toString()

        val maleFirst = mPref.getBoolean(mKeyUtil.nameCrossOrderKey, false)

        val blank = mPref.getBoolean(mKeyUtil.nameBlankMaleKey, false)

        lateinit var male: String
        lateinit var female: String

        if (!maleFirst) {
            female = (mBinding.firstText.text ?: "").toString()
            male = (mBinding.secondText.text ?: "").toString()
        } else {
            male = (mBinding.firstText.text ?: "").toString()
            female = (mBinding.secondText.text ?: "").toString()
        }

        if (value.isNotBlank() && (male.isNotBlank() || blank) && female.isNotBlank()) {

            if (male.isBlank()) male = "blank"

            val crossIds = mEvents.map { event -> event.eventDbId }

            if (!crossIds.any { id -> id == value }) {

                val parent = mParents.find { parent -> parent.codeId == value }

                if (parent != null) {

                    Dialogs.notify(AlertDialog.Builder(requireContext()), getString(R.string.cross_id_already_exists_as_parent))

                } else {

                    scope.launch {

                        context?.let { ctx ->

                            with(CrossUtil(ctx)) {

                                val eid = submitCrossEvent(
                                        mBinding.root,
                                        female,
                                        male,
                                        value,
                                        mSettings,
                                        settingsModel,
                                        viewModel,
                                        mParents,
                                        parentsList,
                                        mWishlistProgress
                                    )

                                activity?.runOnUiThread {

                                    checkPrefToOpenCrossEvent(findNavController(),
                                                EventsFragmentDirections.actionToEventFragment(eid))
                                }
                            }
                        }
                    }
                }

                resetDataEntry()

                Handler().postDelayed({
                    mBinding.recyclerView.scrollToPosition(0)
                }, 250)

            } else Dialogs.notify(AlertDialog.Builder(requireContext()), getString(R.string.cross_id_already_exists_as_event))

        } else {

            if (female.isBlank()) {

                mSnackbar.push(SnackbarQueue.SnackJob(mBinding.root, getString(R.string.you_must_enter_female_name)))

                if (!maleFirst) {
                    mBinding.firstText.requestFocus()
                } else mBinding.secondText.requestFocus()

            } else if (value.isBlank()) {

                mSnackbar.push(SnackbarQueue.SnackJob(mBinding.root, getString(R.string.you_must_enter_cross_name)))

                mBinding.editTextCross.requestFocus()

            } else {

                mSnackbar.push(SnackbarQueue.SnackJob(mBinding.root, getString(R.string.you_must_enter_male_name)))

                //request focus on the edit text that is missing
                if (maleFirst) {
                    mBinding.firstText.requestFocus()
                } else mBinding.secondText.requestFocus()
            }

            FileUtil(requireContext()).ringNotification(false)

        }
    }

    private fun askUserForPerson() {

        mBinding.constraintLayoutParent.requestFocus()

        val cancel = getString(android.R.string.cancel)
        val personMustBeSet = getString(R.string.person_must_be_set)
        val setPerson = getString(R.string.set_person)

        val builder = AlertDialog.Builder(requireContext()).apply {

            setNegativeButton(cancel) { _, _ ->
                mSnackbar.push(SnackbarQueue.SnackJob(mBinding.root, personMustBeSet))
            }

            setPositiveButton(setPerson) { _, _ ->
                findNavController().navigate(EventsFragmentDirections
                    .actionFromEventsToPreferences(true))
            }
        }

        builder.setTitle(personMustBeSet)
        builder.show()
    }

}