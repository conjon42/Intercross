package org.phenoapps.intercross.fragments

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import org.phenoapps.intercross.data.*
import org.phenoapps.intercross.util.SnackbarQueue
import org.phenoapps.intercross.viewmodels.*

//base fragment class that loads all db viewmodels
open class IntercrossBaseFragment : Fragment() {

    lateinit var mEventsListViewModel: EventsListViewModel
    lateinit var mSettingsViewModel: SettingsViewModel
    lateinit var mWishlistViewModel: WishlistViewModel
    lateinit var mParentsViewModel: ParentsViewModel

    lateinit var mSharedViewModel: CrossSharedViewModel

    lateinit var mSnackbar: SnackbarQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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