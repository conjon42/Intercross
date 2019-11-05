package org.phenoapps.intercross.fragments

import android.app.Activity
import android.content.Intent
import android.preference.PreferenceManager
import kotlinx.android.synthetic.main.fragment_pattern.*
import org.phenoapps.intercross.R
import org.phenoapps.intercross.databinding.FragmentImportZplBinding
import org.phenoapps.intercross.util.FileUtil

class ImportZPLFragment : IntercrossBaseFragment<FragmentImportZplBinding>(R.layout.fragment_import_zpl) {

    override fun FragmentImportZplBinding.afterCreateView() {
        //import a file when button is pressed
        importButton.setOnClickListener {
            startActivityForResult(
                    Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = "*/*"
                    }, "Choose file to import."), 100)
        }

        //set preview text to imported zpl code
        val code = PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString("ZPL_CODE", "") ?: ""

        if (code.isNotBlank()) codeTextView.text = code
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {

        super.onActivityResult(requestCode, resultCode, intent)

        intent?.let {
            if (resultCode == Activity.RESULT_OK) {
                when (requestCode) {
                    100 -> {

                        val text = FileUtil(requireContext()).readText(intent.data)
                        codeTextView.text = text

                        val edit = PreferenceManager.getDefaultSharedPreferences(requireContext()).edit()
                        edit.putString("ZPL_CODE", text.toString())
                        edit.apply()
                    }
                }
            }
        }
    }
}
