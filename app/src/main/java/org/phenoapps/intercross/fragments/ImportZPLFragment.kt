package org.phenoapps.intercross.fragments

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.preference.PreferenceManager
import android.provider.DocumentsContract
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.fragment_pattern.*
import org.phenoapps.intercross.BuildConfig
import org.phenoapps.intercross.R
import org.phenoapps.intercross.databinding.FragmentImportZplBinding
import org.phenoapps.intercross.util.FileUtil
import java.io.File

class ImportZPLFragment : IntercrossBaseFragment<FragmentImportZplBinding>(R.layout.fragment_import_zpl) {

    override fun FragmentImportZplBinding.afterCreateView() {

        val ctx = requireContext()

        //import a file when button is pressed
        importButton.setOnClickListener {

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type="*/*"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    putExtra(DocumentsContract.EXTRA_INITIAL_URI,
                            FileProvider.getUriForFile(ctx,
                                    "${BuildConfig.APPLICATION_ID}.fileprovider",
                                    File(File(ctx.externalCacheDir, "ZPL"), "zpl_example.zpl")))
                }

            }

            val importZplString = getString(R.string.import_a_zpl_file)
            startActivityForResult(Intent.createChooser(intent, importZplString), 100)

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

                        val text = FileUtil(requireContext()).readText(requireContext(), intent.data)
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
