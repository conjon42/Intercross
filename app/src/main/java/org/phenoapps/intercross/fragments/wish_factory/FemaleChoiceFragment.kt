package org.phenoapps.intercross.fragments.wish_factory

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.SimpleListAdapter
import org.phenoapps.intercross.data.ParentsRepository
import org.phenoapps.intercross.data.viewmodels.ParentsListViewModel
import org.phenoapps.intercross.data.viewmodels.factory.ParentsListViewModelFactory
import org.phenoapps.intercross.databinding.FragmentWfChooseFemaleBinding
import org.phenoapps.intercross.fragments.IntercrossBaseFragment
import org.phenoapps.intercross.interfaces.OnSimpleItemClicked
import org.phenoapps.intercross.util.observeOnce

/**
 * @author Chaney 8/16/2021
 * Simple fragment for the user to choose a female name or "blank"
 * This is the first fragment in the wish factory workflow graph
 * This fragment is initiated by clicking on the plus icon in the parents fragment.
 * When onNext is activated (next button is clicked) one option must be chosen.
 * If nothing is chosen, None is assumed and a notification should be displayed to the user.
 */
class FemaleChoiceFragment : IntercrossBaseFragment<FragmentWfChooseFemaleBinding>(R.layout.fragment_wf_choose_female),
    OnSimpleItemClicked {

    private val parentList: ParentsListViewModel by viewModels {
        ParentsListViewModelFactory(ParentsRepository.getInstance(db.parentsDao()))
    }

    companion object {
        val EMPTY_PARENT = "-1" to "blank"
    }

    override fun FragmentWfChooseFemaleBinding.afterCreateView() {

        mBinding.wfFemaleNextBt.setOnClickListener {
            findNavController().navigate(FemaleChoiceFragmentDirections
                .actionFromFemalesToMalesFragment())
        }

        mBinding.wfFemaleRv.adapter = SimpleListAdapter(this@FemaleChoiceFragment)

        parentList.females.observeOnce(viewLifecycleOwner) { moms ->

            (mBinding.wfFemaleRv.adapter as? SimpleListAdapter)?.submitList(
                listOf(EMPTY_PARENT) + moms
                .filter { it.name.isNotBlank() }
                .map { it.codeId to it.name })
        }
    }

    override fun onItemClicked(pair: Pair<String, String>) {

        mBinding.wfFemaleNextBt.setOnClickListener {
            findNavController().navigate(FemaleChoiceFragmentDirections
                .actionFromFemalesToMalesFragment(pair.first, pair.second))
        }
    }
}