package org.phenoapps.intercross.fragments

import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.fragment_pattern.*
import org.phenoapps.intercross.R
import org.phenoapps.intercross.databinding.FragmentImportZplBinding
import org.phenoapps.intercross.util.FileUtil
import org.phenoapps.intercross.util.KeyUtil

class ImportZPLFragment : IntercrossBaseFragment<FragmentImportZplBinding>(R.layout.fragment_import_zpl) {

    private val mPref by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    private val mKeyUtil by lazy {
        KeyUtil(context)
    }

    private val importZplFile by lazy {

        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

            uri?.let {

                val text = FileUtil(requireContext()).readText(requireContext(), it)

                codeTextView.text = text

                mPref.edit().putString("ZPL_CODE", text.toString()).apply()

            }
        }
    }

    override fun FragmentImportZplBinding.afterCreateView() {

        //import a file when button is pressed
        importButton.setOnClickListener {

            importZplFile.launch("*/*")

        }

        //set preview text to imported zpl code
        val code = mPref.getString(mKeyUtil.argPrintZplCode, "") ?: ""

        if (code.isNotBlank()) codeTextView.text = code
    }
}
