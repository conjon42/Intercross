package org.phenoapps.intercross.fragments

import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.PreferenceManager
import org.phenoapps.intercross.R
import org.phenoapps.intercross.activities.MainActivity
import org.phenoapps.intercross.databinding.FragmentImportZplBinding
import org.phenoapps.intercross.util.KeyUtil
import java.io.InputStreamReader

class ImportZPLFragment : IntercrossBaseFragment<FragmentImportZplBinding>(R.layout.fragment_import_zpl) {

    private val mPref by lazy {
        PreferenceManager.getDefaultSharedPreferences(requireContext())
    }

    private val mKeyUtil by lazy {
        KeyUtil(context)
    }

    private val importZplFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

        uri?.let {

            val text = InputStreamReader(context?.contentResolver?.openInputStream(uri))
                .readLines()
                .joinToString("\n")

            mBinding.codeTextView.text = text

            mPref.edit().putString(mKeyUtil.zplCodeKey, text).apply()

        }
    }

    override fun FragmentImportZplBinding.afterCreateView() {

        (activity as MainActivity).setBackButtonToolbar()
        (activity as MainActivity).supportActionBar?.apply{
            title = null
            show()
        }

        //import a file when button is pressed
        importButton.setOnClickListener {

            importZplFile.launch("*/*")

        }

        //set preview text to imported zpl code
        val code = mPref.getString(mKeyUtil.zplCodeKey, "") ?: ""

        if (code.isNotBlank()) codeTextView.text = code
    }
}
