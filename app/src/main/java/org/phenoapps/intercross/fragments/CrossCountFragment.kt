package org.phenoapps.intercross.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import org.phenoapps.intercross.MainActivity
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.CrossCountAdapter
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.WishlistRepository
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.WishlistViewModel
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.WishlistViewModelFactory
import org.phenoapps.intercross.databinding.FragmentCrossCountBinding
import org.phenoapps.intercross.util.Dialogs

/**
 * Summary Fragment is a recycler list of currenty crosses.
 * Users can navigate to and from cross block and wishlist fragments.
 */
class CrossCountFragment : IntercrossBaseFragment<FragmentCrossCountBinding>(R.layout.fragment_cross_count) {

    private val eventsModel: EventListViewModel by viewModels {
        EventsListViewModelFactory(EventsRepository.getInstance(db.eventsDao()))
    }

    private val wishModel: WishlistViewModel by viewModels {
        WishlistViewModelFactory(WishlistRepository.getInstance(db.wishlistDao()))
    }

    private var mWishlistEmpty = true

    /**
     * Polymorphism setup to allow adapter to work with two different types of objects.
     * Wishlists and Summary data are the same but they have to be rendered differently.
     */
    open class ListEntry(open var m: String, open var f: String, open var count: String, open var events: List<Event>)

    data class WishlistData(override var m: String, override var f: String, override var count: String, override var events: List<Event>):
            ListEntry(m, f, count, events)

    data class CrossData(override var m: String, override var f: String, override var count: String, override var events: List<Event>):
            ListEntry(m, f, count, events)


    override fun FragmentCrossCountBinding.afterCreateView() {

        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit().putString("last_visited_summary", "summary").apply()

        recyclerView.adapter = CrossCountAdapter(this@CrossCountFragment, eventsModel, requireContext())

        recyclerView.layoutManager = LinearLayoutManager(context)

        eventsModel.parents.observe(viewLifecycleOwner, {

            it?.let { crosses ->

                val crossData = ArrayList<CrossData>()

                eventsModel.events.observe(viewLifecycleOwner, { data ->

                    data?.let { events ->

                        crosses.forEach { parentrow ->

                            crossData.add(
                                    CrossData(
                                            parentrow.dad,
                                            parentrow.mom,
                                            parentrow.count.toString(),
                                            events.filter { e -> e.maleObsUnitDbId == parentrow.dad
                                                    && e.femaleObsUnitDbId == parentrow.mom })
                            )

                        }

                        (recyclerView.adapter as CrossCountAdapter).submitList(crossData as List<ListEntry>?)

                        deleteButton.setOnClickListener {

                            Dialogs.onOk(AlertDialog.Builder(requireContext()),
                                    getString(R.string.delete_cross_entry_title),
                                    getString(R.string.cancel),
                                    getString(R.string.zxing_button_ok)) {

                                eventsModel.deleteAll()

                                findNavController().popBackStack()

                            }
                        }
                    }
                })
            }
        })

        /**
         * Keep track if wishlist repo is empty to disable options items menu
         */
        wishModel.wishlist.observe(viewLifecycleOwner, {

            it?.let {

                mWishlistEmpty = it.isEmpty()
            }
        })

        bottomNavBar.selectedItemId = R.id.action_nav_cross_count

        setupBottomNavBar()

    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.summary_toolbar, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {

            R.id.action_nav_crossblock -> {

                if (!mWishlistEmpty) {

                    Navigation.findNavController(mBinding.root)
                            .navigate(CrossCountFragmentDirections.actionToCrossblock())
                } else {

                    Dialogs.notify(AlertDialog.Builder(requireContext()), getString(R.string.wishlist_is_empty))

                }
            }

            R.id.action_nav_wishlist -> {

                if (!mWishlistEmpty) {

                    Navigation.findNavController(mBinding.root)
                            .navigate(CrossCountFragmentDirections.actionToWishlist())
                } else {

                    Dialogs.notify(AlertDialog.Builder(requireContext()), getString(R.string.wishlist_is_empty))

                }
            }

            R.id.action_nav_summary -> {

                Navigation.findNavController(mBinding.root)
                    .navigate(CrossCountFragmentDirections.actionToSummary())
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun FragmentCrossCountBinding.setupBottomNavBar() {

        bottomNavBar.setOnNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.action_nav_settings -> {

                    findNavController().navigate(R.id.global_action_to_settings_fragment)
                }
                R.id.action_nav_parents -> {

                    findNavController().navigate(CrossCountFragmentDirections.globalActionToParents())

                }
                R.id.action_nav_export -> {

                    (activity as MainActivity).showImportOrExportDialog()

                }
                R.id.action_nav_home -> {

                    findNavController().navigate(CrossCountFragmentDirections.globalActionToEvents())

                }
            }

            true
        }
    }
}