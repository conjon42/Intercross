package org.phenoapps.intercross.fragments

import android.preference.PreferenceManager
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.android.synthetic.main.fragment_pattern.*
import org.phenoapps.intercross.R
import org.phenoapps.intercross.databinding.FragmentImportZplBinding
import org.phenoapps.intercross.util.FileUtil
import java.io.FileInputStream
import java.io.FileReader
import java.io.InputStreamReader

class ImportZPLFragment : IntercrossBaseFragment<FragmentImportZplBinding>(R.layout.fragment_import_zpl) {

    private val importZplFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->

        uri?.let {

            val text = InputStreamReader(context?.contentResolver?.openInputStream(uri))
                .readLines()
                .joinToString("\n")

            //val text = FileUtil(requireContext()).readText(requireContext(), it)

            codeTextView.text = text

            val edit = PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()

            edit.putString("ZPL_CODE", text).apply()

        }
    }

    override fun FragmentImportZplBinding.afterCreateView() {

        //import a file when button is pressed
        importButton.setOnClickListener {

            importZplFile.launch("*/*")

        }

        //set preview text to imported zpl code
        val code = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString("ZPL_CODE", "") ?: ""

        if (code.isNotBlank()) codeTextView.text = code
    }
}
