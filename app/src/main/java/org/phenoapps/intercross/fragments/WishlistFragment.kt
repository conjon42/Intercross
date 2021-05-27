package org.phenoapps.intercross.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.WishlistAdapter
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.WishlistRepository
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.WishlistViewModel
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.WishlistViewModelFactory
import org.phenoapps.intercross.databinding.FragmentWishlistBinding
import org.phenoapps.intercross.util.Dialogs

/**
 * Summary Fragment is a recycler list of currenty crosses.
 * Users can navigate to and from cross block and wishlist fragments.
 */
class WishlistFragment : IntercrossBaseFragment<FragmentWishlistBinding>(R.layout.fragment_wishlist) {

    private val eventsModel: EventListViewModel by viewModels {
        EventsListViewModelFactory(EventsRepository.getInstance(db.eventsDao()))
    }

    private val wishModel: WishlistViewModel by viewModels {
        WishlistViewModelFactory(WishlistRepository.getInstance(db.wishlistDao()))
    }

    private var wishlistEmpty = true

    private var mEvents: List<Event> = ArrayList()

    override fun FragmentWishlistBinding.afterCreateView() {

        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .edit().putString("last_visited_summary", "wishlist").apply()

        recyclerView.adapter = WishlistAdapter(requireContext())

        recyclerView.layoutManager = LinearLayoutManager(context)

        eventsModel.events.observe(viewLifecycleOwner, {

            it?.let {

                mEvents = it

                wishModel.wishes.observe(viewLifecycleOwner, {

                    it?.let { block ->

                        val crosses = block.filter { wish -> wish.wishType == "cross" }

                        wishlistEmpty = crosses.isEmpty()

                        (recyclerView.adapter as WishlistAdapter)
                                .submitList(crosses.map { res ->
                                    CrossCountFragment.WishlistData(res.dadName, res.momName,
                                            res.wishProgress.toString() + "/" + res.wishMin + "/" + res.wishMax.toString(), mEvents.filter {
                                        event -> event.femaleObsUnitDbId == res.momId && event.maleObsUnitDbId == res.dadId
                                    })
                                })

                        deleteButton.setOnClickListener {

                            Dialogs.onOk(AlertDialog.Builder(requireContext()),
                                    getString(R.string.delete_all_wishlist_title),
                                    getString(R.string.cancel),
                                    getString(R.string.zxing_button_ok)) {

                                wishModel.deleteAll()

                                findNavController().popBackStack()
                            }
                        }
                    }
                })
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

                if (!wishlistEmpty) {

                    Navigation.findNavController(mBinding.root)
                            .navigate(WishlistFragmentDirections.actionToCrossblock())

                }
                else {

                    Dialogs.notify(AlertDialog.Builder(requireContext()), getString(R.string.wishlist_is_empty))

                }
            }

            R.id.action_nav_summary -> {

                if (mEvents.isNotEmpty()) {

                    Navigation.findNavController(mBinding.root)
                            .navigate(WishlistFragmentDirections.actionToCrossCount())
                }
                else {

                    Dialogs.notify(AlertDialog.Builder(requireContext()), getString(R.string.summary_empty))

                }
            }
        }

        return super.onOptionsItemSelected(item)
    }
}