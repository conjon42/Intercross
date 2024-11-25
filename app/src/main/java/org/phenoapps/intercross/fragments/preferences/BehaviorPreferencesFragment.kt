package org.phenoapps.intercross.fragments.preferences

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.phenoapps.intercross.R
import org.phenoapps.intercross.activities.MainActivity
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.IntercrossDatabase
import org.phenoapps.intercross.data.MetaValuesRepository
import org.phenoapps.intercross.data.MetadataRepository
import org.phenoapps.intercross.data.SettingsRepository
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.Meta
import org.phenoapps.intercross.data.models.MetadataValues
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.MetaValuesViewModel
import org.phenoapps.intercross.data.viewmodels.MetadataViewModel
import org.phenoapps.intercross.data.viewmodels.SettingsViewModel
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.MetaValuesViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.MetadataViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.SettingsViewModelFactory
import org.phenoapps.intercross.dialogs.MetadataCreatorDialog
import org.phenoapps.intercross.interfaces.MetadataManager
import org.phenoapps.intercross.util.Dialogs
import org.phenoapps.intercross.util.KeyUtil

class BehaviorPreferencesFragment : BasePreferenceFragment(R.xml.behavior_preferences, R.string.root_behavior), MetadataManager, CoroutineScope by MainScope() {

    private val TAG = "BehaviorPreferences"

    private val mKeyUtil by lazy {
        KeyUtil(context)
    }

    private val settingsModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(SettingsRepository
            .getInstance(IntercrossDatabase.getInstance(requireContext()).settingsDao()))
    }

    private val metaValuesViewModel: MetaValuesViewModel by viewModels {
        MetaValuesViewModelFactory(MetaValuesRepository
            .getInstance(IntercrossDatabase.getInstance(requireContext()).metaValuesDao()))
    }

    private val metadataViewModel: MetadataViewModel by viewModels {
        MetadataViewModelFactory(MetadataRepository
            .getInstance(IntercrossDatabase.getInstance(requireContext()).metadataDao()))
    }

    private val eventsModel: EventListViewModel by viewModels {
        EventsListViewModelFactory(EventsRepository
            .getInstance(IntercrossDatabase.getInstance(requireContext()).eventsDao()))
    }

    private val mPref by lazy {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    private var mEvents: List<Event> = ArrayList()
    private lateinit var mMetaValuesList: List<MetadataValues>
    private lateinit var mMetaList: List<Meta>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupNamingPreferences()
        setupWorkflowPreferences()
    }

    private fun setupNamingPreferences() {
        settingsModel.settings.observeForever { settings ->
            settings?.let {
                findPreference<Preference>(mKeyUtil.nameCreatePatternKey)?.apply {
                    summary = when {
                        settings.isPattern -> "Pattern"
                        !settings.isUUID && !settings.isPattern -> "None"
                        else -> "UUID"
                    }
                }
            }
        }

        findPreference<Preference>(mKeyUtil.nameCreatePatternKey)?.setOnPreferenceClickListener {
            findNavController().navigate(BehaviorPreferencesFragmentDirections.actionBehaviorPreferencesFragmentToPatternFragment())
            true
        }
    }

    private fun setupWorkflowPreferences() {
        eventsModel.events.observe(viewLifecycleOwner) { mEvents = it }
        metaValuesViewModel.metaValues.observe(viewLifecycleOwner) { it?.let { mMetaValuesList = it } }
        metadataViewModel.metadata.observe(viewLifecycleOwner) { it?.let { mMetaList = it } }

        setupMetadataPreferences()
    }

    private fun setupMetadataPreferences() {
        try {
            val metadataPref = findPreference<Preference>(mKeyUtil.workMetaKey)
            val defaultsPref = findPreference<Preference>(mKeyUtil.workMetaDefaultsKey)
            val isCollect = mPref.getBoolean(mKeyUtil.workCollectKey, false)

            defaultsPref?.isVisible = isCollect
            metadataPref?.isVisible = isCollect

            findPreference<SwitchPreference>(mKeyUtil.workCollectKey)?.setOnPreferenceChangeListener { _, newValue ->
                val isCollectEnabled = newValue as? Boolean ?: false
                metadataPref?.isVisible = isCollectEnabled
                defaultsPref?.isVisible = isCollectEnabled
                true
            }

            metadataPref?.setOnPreferenceClickListener {
                context?.let { ctx -> MetadataCreatorDialog(ctx, this@BehaviorPreferencesFragment).show() }
                true
            }

            defaultsPref?.setOnPreferenceClickListener {
                findNavController().navigate(BehaviorPreferencesFragmentDirections.actionBehaviorPreferencesFragmentToMetadataList())
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in setupMetadataPreferences: ${e.message}", e)
        }
    }

    override fun onMetadataLongClicked(rowid: Long, property: String) {
        context?.let { ctx ->
            Dialogs.onOk(
                AlertDialog.Builder(ctx),
                title = getString(R.string.dialog_confirm_remove_metadata),
                cancel = getString(android.R.string.cancel),
                ok = getString(android.R.string.ok),
                message = getString(R.string.dialog_confirm_remove_for_all)) {
                deleteMetadata(property, rowid)
            }
        }
    }

    override fun onMetadataDefaultUpdated(rowId: Long, property: String, value: Int) {
        updateMetadataDefault(rowId, value, property)
    }

    //adds the new property to all crosses in the database
    override fun onMetadataCreated(property: String, value: String) {
        createNewMetadata(value.toInt(), property)
    }

    //adds the new default value and property to the metadata string
    private fun createNewMetadata(value: Int, property: String) {
        launch {
            withContext(Dispatchers.IO) {
                //insert a new row
                val mid = metadataViewModel.insert(Meta(property, value))
                mEvents.forEach {
                    metaValuesViewModel.insert(MetadataValues(it.id?.toInt() ?: -1, mid.toInt(), value))
                }
            }
        }
    }

    //updates a property with a new default value
    private fun updateMetadataDefault(rowId: Long, value: Int, property: String) {
        launch {
            withContext(Dispatchers.IO) {
                metadataViewModel.update(Meta(property, value, rowId))
            }
        }
    }

    //deletes the given property from the metadata string
    private fun deleteMetadata(property: String, rowid: Long) {
        launch {
            withContext(Dispatchers.IO) {
                metadataViewModel.delete(Meta(property, id = rowid))
                mMetaValuesList.filter { it.metaId == rowid.toInt() }.forEach {
                    metaValuesViewModel.delete(it)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setToolbar(getString(R.string.prefs_behavior_title))
    }
}
