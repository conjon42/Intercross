package org.phenoapps.intercross.fragments

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.fragment_event_detail.*
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.MetadataAdapter
import org.phenoapps.intercross.adapters.models.MetadataModel
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.MetaValuesRepository
import org.phenoapps.intercross.data.MetadataRepository
import org.phenoapps.intercross.data.WishlistRepository
import org.phenoapps.intercross.data.dao.EventsDao
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.databinding.FragmentEventDetailBinding
import org.phenoapps.intercross.interfaces.MetadataManager
import org.phenoapps.intercross.data.models.MetadataValues
import org.phenoapps.intercross.data.models.Meta
import org.phenoapps.intercross.data.models.WishlistView
import org.phenoapps.intercross.data.viewmodels.*
import org.phenoapps.intercross.data.viewmodels.factory.*
import org.phenoapps.intercross.util.*

class EventDetailFragment:
    IntercrossBaseFragment<FragmentEventDetailBinding>(R.layout.fragment_event_detail),
    MetadataManager {

    private lateinit var mEvent: Event
    private lateinit var mMetaValuesList: List<MetadataValues>
    private lateinit var mMetaList: List<Meta>
    private lateinit var mWishlist: List<WishlistView>

    private val eventsList: EventListViewModel by viewModels {
        EventsListViewModelFactory(EventsRepository.getInstance(db.eventsDao()))
    }

    private val metaValuesViewModel: MetaValuesViewModel by viewModels {
        MetaValuesViewModelFactory(MetaValuesRepository.getInstance(db.metaValuesDao()))
    }

    private val metadataViewModel: MetadataViewModel by viewModels {
        MetadataViewModelFactory(MetadataRepository.getInstance(db.metadataDao()))
    }

    private val wishList: WishlistViewModel by viewModels {
        WishlistViewModelFactory(WishlistRepository.getInstance(db.wishlistDao()))
    }

    private val mPref by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    private val mKeyUtil by lazy {
        KeyUtil(context)
    }

    private lateinit var eventDetailViewModel: EventDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    private fun getMetaDataVisibility(context: Context): Int {

        //determine if meta data collection is enabled
        val collect: Boolean = mPref.getBoolean(mKeyUtil.workCollectKey, false)

        return if (collect) View.VISIBLE else View.GONE

    }

    override fun FragmentEventDetailBinding.afterCreateView() {

        arguments?.getLong("eid")?.let { rowid ->

            if (rowid == -1L){

                FileUtil(requireContext()).ringNotification(false)

                findNavController().popBackStack()
            }

            val viewModel: EventDetailViewModel by viewModels {
                EventDetailViewModelFactory(EventsRepository.getInstance(db.eventsDao()), rowid)
            }

            eventDetailViewModel = viewModel

            metaDataVisibility = getMetaDataVisibility(requireContext())

            eventDetailMetadataRecyclerView.adapter = MetadataAdapter(this@EventDetailFragment.context
                ?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager,
                this@EventDetailFragment)

            refreshObservers()
        }
    }

    //loads the metadata into the ui
    //this doesn't listen forever to avoid circular recursive updates
    private fun refreshMetadata() {

        val eid = mEvent.id?.toInt() ?: -1
        metadataViewModel.metadata.observeOnce(viewLifecycleOwner) { metadata ->

            eventDetailViewModel.metadata.observeOnce(viewLifecycleOwner) { values ->

                //merge the metadata properties with either the default values or saved values
                val actualMeta = arrayListOf<EventsDao.CrossMetadata>()
                for (data in metadata) {
                    if (values.any { it.eid == eid && data.property == it.property }) {
                        values.find { data.property == it.property }?.let {
                            actualMeta.add(EventsDao.CrossMetadata(
                                eid, data.property, it.value
                            ))
                        }
                    } else actualMeta.add(
                        EventsDao.CrossMetadata(eid, data.property, data.defaultValue))
                }

                (mBinding.eventDetailMetadataRecyclerView.adapter as MetadataAdapter)
                    .submitList(actualMeta
                        .map { MetadataModel(it.property, it.value.toString()) }
                        .sortedBy { it.property })

                mBinding.eventDetailMetadataRecyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun FragmentEventDetailBinding.refreshObservers() {

        wishList.wishes.observe(viewLifecycleOwner) { crossblock ->
            mWishlist = crossblock
        }

        if (::eventDetailViewModel.isInitialized) {

            eventDetailViewModel.event.observe(viewLifecycleOwner) {

                it?.let {

                    mEvent = it

                    event = it

                    refreshMetadata()

                    eventDetailLayout.event = it

                    eventDetailLayout.timestamp = if ("_" in it.timestamp) {

                        it.timestamp.split("_")[0]

                    } else it.timestamp
                }
            }

            eventDetailViewModel.parents.observe(viewLifecycleOwner) { data ->

                data?.let { parents ->

                    eventDetailLayout.female = parents.momReadableName

                    eventDetailLayout.male = parents.dadReadableName

                    momName = parents.momReadableName

                    dadName = parents.dadReadableName

                    momCode = parents.momCode

                    dadCode = parents.dadCode

                    eventsList.events.observe(viewLifecycleOwner) {

                        it?.let { events ->

                            events.find { e -> e.eventDbId == parents.momCode }.let { mom ->

                                femaleName.setOnClickListener {

                                    if (mom?.id == null) {

                                        Dialogs.notify(AlertDialog.Builder(requireContext()),
                                            getString(R.string.parent_event_does_not_exist))

                                    } else {
                                        findNavController()
                                            .navigate(EventDetailFragmentDirections
                                                .actionToParentEvent(mom.id ?: -1L))
                                    }
                                }

                            }

                            events.find { e -> e.eventDbId == parents.dadCode }.let { dad ->

                                maleName.setOnClickListener {

                                    if (dad?.id == null) {

                                        Dialogs.notify(AlertDialog.Builder(requireContext()),
                                            getString(R.string.parent_event_does_not_exist))

                                    } else {
                                        findNavController().navigate(EventDetailFragmentDirections
                                            .actionToParentEvent(dad.id ?: -1L))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        metaValuesViewModel.metaValues.observe(viewLifecycleOwner) {
            mMetaValuesList = it
        }

        metadataViewModel.metadata.observeOnce(viewLifecycleOwner) {
            it?.let {
                mMetaList = it
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.cross_entry_toolbar, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (::mEvent.isInitialized) {

            when (item.itemId) {

                R.id.action_print -> {

                    BluetoothUtil().print(requireContext(), arrayOf(mEvent))

                }
                R.id.action_delete -> {

                    Dialogs.onOk(AlertDialog.Builder(requireContext()),
                            getString(R.string.delete_cross_entry_title),
                            getString(R.string.cancel),
                            getString(R.string.zxing_button_ok)) {

                        eventsList.deleteById(mEvent.id ?: -1L)

                        findNavController().popBackStack()

                    }
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    //updates a single row value for the current event
    //or inserts a new metadata value row if this value has not been saved previously
    override fun onMetadataUpdated(property: String, value: Int) {

        val eid = mEvent.id?.toInt() ?: -1

        mMetaList.find { it.property == property }?.id?.toInt()?.let { metaId ->

            val values = mMetaValuesList.filter { it.eid == eid && it.metaId == metaId }
            if (mMetaValuesList.isNotEmpty() && values.isNotEmpty()) { //update the old value

                metaValuesViewModel.update(MetadataValues(
                    eid, metaId, value, values.first().id))

            } else { //insert a new row

                metaValuesViewModel.insert(
                    MetadataValues(
                    eid,
                    metaId,
                    value
                ))
            }
        }

        checkWishlist(property, value)
    }

    private fun checkWishlist(property: String, value: Int) {

        context?.let { ctx ->
            val mom = mEvent.femaleObsUnitDbId
            val dad = mEvent.maleObsUnitDbId
            val relaventWishes = mWishlist.filter { wish -> wish.momId == mom && wish.dadId == dad }
            val propertyWishes = relaventWishes.filter { wish -> wish.wishType == property }
            if (propertyWishes.any { wish -> wish.wishMax in 1..value }) {
                Dialogs.notify(AlertDialog.Builder(ctx), getString(R.string.maximum_wish_met, property))
            } else if (propertyWishes.any { wish -> wish.wishMin in 1..value }) {
                Dialogs.notify(AlertDialog.Builder(ctx), getString(R.string.minimum_wish_met, property))
            }
        }
    }
}