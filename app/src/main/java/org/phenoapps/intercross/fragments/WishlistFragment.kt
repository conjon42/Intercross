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
import com.google.android.material.tabs.TabLayout
import org.phenoapps.intercross.MainActivity
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.WishlistAdapter
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.WishlistRepository
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.WishlistViewModel
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.WishlistViewModelFactory
import org.phenoapps.intercross.databinding.CrossBlockManagerBinding
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

        bottomNavBar.selectedItemId = R.id.action_nav_cross_count

        setupBottomNavBar()

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

                                findNavController().navigate(WishlistFragmentDirections.globalActionToCrossCount())
                            }
                        }
                    }
                })
            }
        })

        summaryTabLayout.getTabAt(1)?.select()

        setupTabLayout()
    }

    //a quick wrapper function for tab selection
    private fun tabSelected(onSelect: (TabLayout.Tab?) -> Unit) = object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            onSelect(tab)
        }
        override fun onTabUnselected(tab: TabLayout.Tab?) {}
        override fun onTabReselected(tab: TabLayout.Tab?) {}
    }

    private fun FragmentWishlistBinding.setupTabLayout() {

        summaryTabLayout.addOnTabSelectedListener(tabSelected { tab ->

            when (tab?.text) {
                getString(R.string.summary) -> {

                    if (mEvents.isNotEmpty()) {

                        Navigation.findNavController(mBinding.root)
                            .navigate(WishlistFragmentDirections.actionToSummary())
                    } else {

                        Dialogs.notify(AlertDialog.Builder(requireContext()), getString(R.string.crosses_empty))
                        summaryTabLayout.getTabAt(1)?.select()

                    }
                }

                getString(R.string.cross_count) -> {

                    if (mEvents.isNotEmpty()) {

                        Navigation.findNavController(mBinding.root)
                            .navigate(WishlistFragmentDirections.actionToCrossCount())
                    } else {

                        Dialogs.notify(AlertDialog.Builder(requireContext()), getString(R.string.crosses_empty))
                        summaryTabLayout.getTabAt(1)?.select()

                    }
                }
                getString(R.string.crossblock) -> {

                    if (!wishlistEmpty) {

                        Navigation.findNavController(mBinding.root)
                            .navigate(WishlistFragmentDirections.actionToCrossblock())
                    } else {

                        Dialogs.notify(AlertDialog.Builder(requireContext()), getString(R.string.wishlist_is_empty))
                        summaryTabLayout.getTabAt(1)?.select()

                    }
                }
            }
        })
    }

    private fun FragmentWishlistBinding.setupBottomNavBar() {

        bottomNavBar.setOnNavigationItemSelectedListener { item ->

            when (item.itemId) {

                R.id.action_nav_home -> {

                    findNavController().navigate(WishlistFragmentDirections.globalActionToEvents())
                }
                R.id.action_nav_settings -> {

                    findNavController().navigate(WishlistFragmentDirections.globalActionToSettingsFragment())
                }
                R.id.action_nav_parents -> {

                    findNavController().navigate(WishlistFragmentDirections.globalActionToParents())

                }
                R.id.action_nav_export -> {

                    (activity as MainActivity).showImportOrExportDialog {

                        findNavController().navigate(R.id.wishlist_fragment)
                    }

                }
                R.id.action_nav_cross_count -> {

                    findNavController().navigate(WishlistFragmentDirections.globalActionToCrossCount())

                }
            }

            true
        }
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

            R.id.action_import -> {

                (activity as MainActivity).launchImport()

                findNavController().navigate(R.id.wishlist_fragment)
            }
        }

        return super.onOptionsItemSelected(item)
    }
}