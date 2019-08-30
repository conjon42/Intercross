package org.phenoapps.intercross.fragments

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.adapters.EventsAdapter
import org.phenoapps.intercross.data.*
import org.phenoapps.intercross.databinding.FragmentEventsBinding
import org.phenoapps.intercross.util.SnackbarQueue
import org.phenoapps.intercross.viewmodels.*
import kotlin.reflect.KClass

//base fragment class that loads all db viewmodels
abstract class IntercrossBaseFragment<T : ViewDataBinding>(private val layoutId: Int) : Fragment() {

    lateinit var mEventsListViewModel: EventsListViewModel
    lateinit var mSettingsViewModel: SettingsViewModel
    lateinit var mWishlistViewModel: WishlistViewModel
    lateinit var mParentsViewModel: ParentsViewModel

    lateinit var mSharedViewModel: CrossSharedViewModel

    lateinit var mSnackbar: SnackbarQueue

    lateinit var mBinding: T

    var mSettings = Settings()

    var mOrder: Int = 0
    var mAllowBlank: Boolean = false
    var mCollectData = true

    //lateinit var mAdapter: ListAdapter<*,*>

    abstract fun T.afterCreateView()

    /*infix fun <VH : RecyclerView.ViewHolder> RecyclerView.lists(adapter: ListAdapter<*,VH>) {

        //mAdapter = adapter
        this.layoutManager = LinearLayoutManager(requireContext())
        this.adapter = adapter
    }*/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mBinding = DataBindingUtil.inflate<T>(inflater, layoutId, container, false)
        return with(mBinding) {
            afterCreateView()
            root
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        val db = IntercrossDatabase.getInstance(requireContext())

        activity?.run {
            with(ViewModelProviders.of(this)) {
                mSharedViewModel = this.get(CrossSharedViewModel::class.java)
            }
        }

        mParentsViewModel = ViewModelProviders.of(this,
                object : ViewModelProvider.NewInstanceFactory() {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return ParentsViewModel(ParentsRepository.getInstance(
                                db.parentsDao())) as T

                    }
                }).get(ParentsViewModel::class.java)

        mSettingsViewModel = ViewModelProviders.of(this,
                object : ViewModelProvider.NewInstanceFactory() {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return SettingsViewModel(SettingsRepository.getInstance(
                                db.settingsDao())) as T

                    }
                }).get(SettingsViewModel::class.java)

        mWishlistViewModel = ViewModelProviders.of(this,
                object : ViewModelProvider.NewInstanceFactory() {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return WishlistViewModel(WishlistRepository.getInstance(
                                db.wishlistDao())) as T

                    }
                }).get(WishlistViewModel::class.java)

        mEventsListViewModel = ViewModelProviders.of(this,
                object : ViewModelProvider.NewInstanceFactory() {

                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return EventsListViewModel(EventsRepository.getInstance(
                                db.eventsDao())) as T

                    }
                }
        ).get(EventsListViewModel::class.java)

        mSnackbar = SnackbarQueue()

    }
}