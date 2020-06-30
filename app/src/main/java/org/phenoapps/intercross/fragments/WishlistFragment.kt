package org.phenoapps.intercross.fragments

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.SummaryAdapter
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.WishlistRepository
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
//TODO Trevor when a wishlist is deleted and re-imported, should the current cross table be checked for progress?
//Update wishlist table
class WishlistFragment : IntercrossBaseFragment<FragmentSummaryBinding>(R.layout.fragment_summary) {

    private val eventsModel: EventListViewModel by viewModels {
        EventsListViewModelFactory(EventsRepository.getInstance(db.eventsDao()))
    }

    private val wishModel: WishlistViewModel by viewModels {
        WishlistViewModelFactory(WishlistRepository.getInstance(db.wishlistDao()))
    }

    private var wishlistEmpty = true

    private var eventsEmpty = true

    override fun FragmentSummaryBinding.afterCreateView() {

        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit().putString("last_visited_summary", "wishlist").apply()

        recyclerView.adapter = SummaryAdapter(requireContext())

        recyclerView.layoutManager = LinearLayoutManager(context)

        eventsModel.events.observe(viewLifecycleOwner, Observer {

            it?.let {

                eventsEmpty = it.isEmpty()
            }
        })

        wishModel.wishlist.observe(viewLifecycleOwner, Observer {

            it?.let { crosses ->

                wishlistEmpty = crosses.isEmpty()

                (recyclerView.adapter as SummaryAdapter)
                        .submitList(crosses.map { res ->
                            SummaryFragment.WishlistData(res.maleName, res.femaleName,
                                    res.wishCurrent.toString() + "/" + res.wishMin + "/" + res.wishMax.toString(), ArrayList())
                        })

                recyclerView.adapter?.notifyDataSetChanged()
            }
        })
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        inflater.inflate(R.menu.wishlist_toolbar, menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {

            R.id.action_nav_crossblock -> {

                if (!wishlistEmpty)
                    Navigation.findNavController(mBinding.root)
                            .navigate(WishlistFragmentDirections.actionToCrossblock())
                else Dialogs.notify(AlertDialog.Builder(requireContext()), getString(R.string.wishlist_is_empty))

            }

            R.id.action_nav_summary -> {

                if (!eventsEmpty)
                    Navigation.findNavController(mBinding.root)
                            .navigate(WishlistFragmentDirections.actionToSummary())
                else Dialogs.notify(AlertDialog.Builder(requireContext()), getString(R.string.summary_empty))

            }
        }

        return super.onOptionsItemSelected(item)
    }
}