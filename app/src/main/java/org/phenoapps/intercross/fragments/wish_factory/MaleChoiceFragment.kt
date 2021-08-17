package org.phenoapps.intercross.fragments.wish_factory

import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.SimpleListAdapter
import org.phenoapps.intercross.data.ParentsRepository
import org.phenoapps.intercross.data.viewmodels.ParentsListViewModel
import org.phenoapps.intercross.data.viewmodels.factory.ParentsListViewModelFactory
import org.phenoapps.intercross.databinding.FragmentWfChooseMaleBinding
import org.phenoapps.intercross.fragments.IntercrossBaseFragment
import org.phenoapps.intercross.fragments.wish_factory.FemaleChoiceFragment.Companion.EMPTY_PARENT
import org.phenoapps.intercross.interfaces.OnSimpleItemClicked
import org.phenoapps.intercross.util.SnackbarQueue

/**
 * @author Chaney 8/16/2021
 * Simple fragment for the user to choose a male name or "None"
 * This is the second fragment in the wish factory workflow graph
 * Inputs: id: Long, name: String defaults are -1L and "None" which means a self cross
 * Outputs: femaleId, femaleName, maleId: Long, maleName: String
 */
class MaleChoiceFragment : IntercrossBaseFragment<FragmentWfChooseMaleBinding>(R.layout.fragment_wf_choose_male),
    OnSimpleItemClicked {

    private val femaleName by lazy {
        arguments?.getString("name") ?: "blank"
    }

    private val femaleId by lazy {
        arguments?.getString("id") ?: "-1"
    }

    private val parentList: ParentsListViewModel by viewModels {
        ParentsListViewModelFactory(ParentsRepository.getInstance(db.parentsDao()))
    }

    override fun FragmentWfChooseMaleBinding.afterCreateView() {

        mBinding.wfMaleSummaryTv.text = getString(R.string.frag_wf_male_choice_summary, femaleName)

        mBinding.wfMaleRv.adapter = SimpleListAdapter(this@MaleChoiceFragment)

        parentList.males.observe(viewLifecycleOwner) { dads ->

            val dadPairs = dads.filter { it.name.isNotBlank() }
                .map { it.codeId to it.name }

            //only add empty parent if it was not chosen for the female
            (mBinding.wfMaleRv.adapter as SimpleListAdapter).submitList(
                if (femaleId == "-1") dadPairs
                else listOf(EMPTY_PARENT) + dadPairs)
        }
    }

    private fun navigateToTypeChoiceFragment(pair: Pair<String, String>) {
        findNavController().navigate(MaleChoiceFragmentDirections
            .actionFromMalesToTypesFragment(femaleId, femaleName,
                pair.first, pair.second))
    }

    override fun onItemClicked(pair: Pair<String, String>) {

        mBinding.wfMaleNextBt.setOnClickListener {
            navigateToTypeChoiceFragment(pair)
        }
    }
}