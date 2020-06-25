package org.phenoapps.intercross.fragments

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.SummaryAdapter
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.WishlistRepository
import org.phenoapps.intercross.data.dao.EventsDao
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.WishlistViewModel
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.WishlistViewModelFactory
import org.phenoapps.intercross.databinding.FragmentSummaryBinding
import org.phenoapps.intercross.util.Dialogs

/**
 * Summary Fragment is a recycler list of currenty crosses.
 * Users can navigate to and from cross block and wishlist fragments.
 */
class SummaryFragment : IntercrossBaseFragment<FragmentSummaryBinding>(R.layout.fragment_summary) {

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


    override fun FragmentSummaryBinding.afterCreateView() {

        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit().putString("last_visited_summary", "summary").apply()

        recyclerView.adapter = SummaryAdapter(requireContext())

        recyclerView.layoutManager = LinearLayoutManager(context)

        eventsModel.parents.observe(viewLifecycleOwner, Observer {

            it?.let { crosses ->

                val crossData = ArrayList<CrossData>()

                eventsModel.events.observe(viewLifecycleOwner, Observer {

                    it?.let { events ->

                        crosses.forEach {

                            crossData.add(
                                    CrossData(it.dad, it.mom, it.count.toString(),
                                    events.filter { e -> e.maleObsUnitDbId == it.dad && e.femaleObsUnitDbId == it.mom })
                            )

                        }

                        (recyclerView.adapter as SummaryAdapter).submitList(crossData as List<ListEntry>?)

                        recyclerView.adapter?.notifyDataSetChanged()

                    }
                })
            }
        })

        /**
         * Keep track if wishlist repo is empty to disable options items menu
         */
        wishModel.wishlist.observe(viewLifecycleOwner, Observer {

            it?.let {

                mWishlistEmpty = it.isEmpty()
            }
        })
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

        if (!mWishlistEmpty) {

            when(item.itemId) {

                R.id.action_nav_crossblock -> {

                    Navigation.findNavController(mBinding.root)
                            .navigate(SummaryFragmentDirections.actionToCrossblock())
                }

                R.id.action_nav_wishlist -> {

                    Navigation.findNavController(mBinding.root)
                            .navigate(SummaryFragmentDirections.actionToWishlist())
                }
            }

        } else Dialogs.notify(AlertDialog.Builder(requireContext()), "Wishlist is empty.")

        return super.onOptionsItemSelected(item)
    }
}