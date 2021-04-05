package org.phenoapps.intercross.fragments

import androidx.navigation.fragment.navArgs
import org.phenoapps.intercross.R
import org.phenoapps.intercross.databinding.FragmentBrapiImportBinding

//TODO
class BrapiGermplasmFragment: IntercrossBaseFragment<FragmentBrapiImportBinding>(R.layout.fragment_brapi_import) {

    val args: BrapiTrialsFragmentArgs by navArgs()

    private companion object {

        val TAG = BrapiGermplasmFragment::class.simpleName

    }

    override fun FragmentBrapiImportBinding.afterCreateView() {


    }
}