package org.phenoapps.intercross.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.SummaryAdapter
import org.phenoapps.intercross.data.WishlistRepository
import org.phenoapps.intercross.data.viewmodels.WishlistViewModel
import org.phenoapps.intercross.data.viewmodels.factory.WishlistViewModelFactory
import org.phenoapps.intercross.databinding.FragmentSummaryBinding

/**
 * Summary Fragment is a recycler list of currenty crosses.
 * Users can navigate to and from cross block and wishlist fragments.
 */
class WishlistFragment : IntercrossBaseFragment<FragmentSummaryBinding>(R.layout.fragment_summary) {


    override fun FragmentSummaryBinding.afterCreateView() {

        recyclerView.adapter = SummaryAdapter(requireContext())

        recyclerView.layoutManager = LinearLayoutManager(context)

        val viewModel: WishlistViewModel by viewModels {
            WishlistViewModelFactory(WishlistRepository.getInstance(db.wishlistDao()))
        }

        viewModel.wishlist.observe(viewLifecycleOwner, Observer {

            it?.let { crosses ->

                (recyclerView.adapter as SummaryAdapter)
                        .submitList(crosses.map { res ->
                            SummaryFragment.WishlistData(res.maleName, res.femaleName,
                                    res.wishCurrent.toString() + "/" + res.wishMax.toString(), ArrayList())
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

                Navigation.findNavController(mBinding.root)
                        .navigate(WishlistFragmentDirections.actionToCrossblock())
            }

            R.id.action_nav_summary -> {

                Navigation.findNavController(mBinding.root)
                        .navigate(WishlistFragmentDirections.actionToSummary())
            }
        }

        return super.onOptionsItemSelected(item)
    }
}