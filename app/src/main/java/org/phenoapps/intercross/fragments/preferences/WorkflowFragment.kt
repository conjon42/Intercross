package org.phenoapps.intercross.fragments.preferences

import android.os.Bundle
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
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.Meta
import org.phenoapps.intercross.data.models.MetadataValues
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.MetaValuesViewModel
import org.phenoapps.intercross.data.viewmodels.MetadataViewModel
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.MetaValuesViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.MetadataViewModelFactory
import org.phenoapps.intercross.dialogs.MetadataCreatorDialog
import org.phenoapps.intercross.interfaces.MetadataManager
import org.phenoapps.intercross.util.Dialogs
import org.phenoapps.intercross.util.KeyUtil

class WorkflowFragment : ToolbarPreferenceFragment(
    R.xml.workflow_preferences, R.string.root_workflow), MetadataManager, CoroutineScope by MainScope() {

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

    private val mKeyUtil by lazy {
        KeyUtil(context)
    }

    private var mEvents: List<Event> = ArrayList()
    private lateinit var mMetaValuesList: List<MetadataValues>
    private lateinit var mMetaList: List<Meta>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        eventsModel.events.observe(viewLifecycleOwner) {
            mEvents = it
        }

        metaValuesViewModel.metaValues.observe(viewLifecycleOwner) {
            it?.let {
                mMetaValuesList = it
            }
        }

        metadataViewModel.metadata.observe(viewLifecycleOwner) {
            it?.let {
                mMetaList = it
            }
        }

        //ensure metadata creation / setting defaults preference is invisible by default
        context?.let { ctx ->

            val metadataPref = findPreference<Preference>(mKeyUtil.workMetaKey)
            val defaultsPref = findPreference<Preference>(mKeyUtil.workMetaDefaultsKey)

            val isCollect = mPref.getBoolean(mKeyUtil.workCollectKey, false)

            defaultsPref?.isVisible = isCollect
            metadataPref?.isVisible = isCollect

        }

        //when collect info is changed, update visibility of metadata preference
        with (findPreference<SwitchPreference>(mKeyUtil.workCollectKey)) {
            this?.let {

                setOnPreferenceChangeListener { preference, newValue ->

                    findPreference<Preference>(mKeyUtil.workMetaKey)?.let { metadataPref ->
                        metadataPref.isVisible = newValue as? Boolean ?: false
                    }

                    findPreference<Preference>(mKeyUtil.workMetaDefaultsKey)?.let { metadataPref ->
                        metadataPref.isVisible = newValue as? Boolean ?: false
                    }
                    true
                }
            }
        }

        //setup click listener to handle metadata creation when pressed
        with (findPreference<Preference>(mKeyUtil.workMetaKey)) {
            this?.let {

                setOnPreferenceClickListener {

                    context?.let { ctx ->

                        MetadataCreatorDialog(ctx, this@WorkflowFragment).show()

                    }

                    true
                }
            }
        }

        //setup click listener to handle metadata creation when pressed
        with (findPreference<Preference>(mKeyUtil.workMetaDefaultsKey)) {
            this?.let {

                setOnPreferenceClickListener {

                    findNavController().navigate(WorkflowFragmentDirections
                        .actionFromWorkflowToMetadataList())

//                    context?.let { ctx ->
//
//                        val defaults = mMetaList.map { it.defaultValue }
//
//                        val properties = mMetaList.map { it.property }
//
//                        val viewed = properties.zip(defaults).map { "${it.first} -> ${it.second}" }
//                            .toTypedArray()
//
//                        if (properties.isNotEmpty()) {
//
//                            AlertDialog.Builder(ctx).setSingleChoiceItems(viewed, 0) { dialog, item ->
//
//                                val default = mMetaList.find { it.property == properties[item] }?.defaultValue ?: 1
//
//                                MetadataDefaultEditorDialog(ctx,
//                                    mMetaList[item].id ?: -1L,
//                                    properties[item],
//                                    default,
//                                    this@WorkflowFragment).show()
//
//                                dialog.dismiss()
//
//                            }.show()
//
//                        } else Toast.makeText(ctx, R.string.fragment_settings_no_metadata_exists, Toast.LENGTH_SHORT).show()
//                    }

                    true
                }
            }
        }
    }

    //asks the user to delete the property,
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
                val mid = metadataViewModel.insert(
                    Meta(property, value)
                )

                mEvents.forEach {
                    metaValuesViewModel.insert(
                        MetadataValues(it.id?.toInt() ?: -1, mid.toInt(), value)
                    )
                }
            }
        }
    }

    //updates a property with a new default value
    private fun updateMetadataDefault(rowId: Long, value: Int, property: String) {

        launch {
            withContext(Dispatchers.IO) {
                metadataViewModel.update(
                    Meta(property, value, rowId)
                )
            }
        }
    }

    //deletes the given property from the metdata string
    private fun deleteMetadata(property: String, rowid: Long) {

        launch {
            withContext(Dispatchers.IO) {
                metadataViewModel.delete(
                    Meta(property, id = rowid)
                )

                mMetaValuesList.filter { it.metaId == rowid.toInt() }.forEach {
                    metaValuesViewModel.delete(it)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setBackButtonToolbar()
        (activity as AppCompatActivity).supportActionBar?.show()
    }
}
