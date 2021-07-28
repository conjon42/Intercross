package org.phenoapps.intercross.fragments.preferences

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import org.phenoapps.intercross.R

class PrintingFragment : ToolbarPreferenceFragment(R.xml.printing_preferences, R.string.root_printing) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(findPreference<Preference>("org.phenoapps.intercross.ZPL_IMPORT")) {
            this?.let {
                setOnPreferenceClickListener {
                    findNavController().navigate(PrintingFragmentDirections.actionToImportZplFragment())
                    true
                }
            }
        }

        val printSetup = findPreference<Preference>("org.phenoapps.intercross.PRINTER_SETUP")
        printSetup?.setOnPreferenceClickListener {
            val intent = activity?.packageManager
                ?.getLaunchIntentForPackage("com.zebra.printersetup")
            when (intent) {
                null -> {
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(
                        "https://play.google.com/store/apps/details?id=com.zebra.printersetup")
                    startActivity(i)
                }
                else -> {
                    startActivity(intent)
                }
            }
            true
        }
    }
}
