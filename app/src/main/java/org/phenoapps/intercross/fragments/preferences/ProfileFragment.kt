package org.phenoapps.intercross.fragments.preferences

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import org.phenoapps.intercross.R

class ProfileFragment : BasePreferenceFragment(R.xml.profile_preferences) {

    private val mPrefs by lazy {
        context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
    }

    private var profilePerson: Preference? = null
    private var profileReset: Preference? = null
    private var requirePersonPref: Preference? = null

    private var personDialog: AlertDialog? = null

    override fun onResume() {
        super.onResume()
        setToolbar(getString(R.string.prefs_profile_title))
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        mPrefs?.edit()?.putLong(GeneralKeys.LAST_TIME_ASKED, System.nanoTime())?.apply()

        profilePerson = findPreference("pref_profile_person")
        profileReset = findPreference("pref_profile_reset")
        requirePersonPref = findPreference<CheckBoxPreference>(GeneralKeys.REQUIRE_USER_TO_COLLECT)

        updatePersonSummary()

        setPreferenceClickListeners()

        setupPersonTimeIntervalPreference(null)

        val arguments = arguments

        if (arguments != null) {
            val updatePerson = arguments.getBoolean(GeneralKeys.PERSON_UPDATE, false)

            if (updatePerson) {
                showPersonDialog()
            }
        }
    }

    private fun setPreferenceClickListeners() {
        profilePerson?.setOnPreferenceClickListener {
            showPersonDialog()
            true
        }

        profileReset?.setOnPreferenceClickListener {
            showClearSettingsDialog()
            true
        }

        requirePersonPref?.setOnPreferenceChangeListener { _, newValue ->
            Log.d("TAG", "setPreferenceClickListeners: $newValue")
            setupPersonTimeIntervalPreference(newValue as Boolean)
            true
        }
    }

    private fun showPersonDialog() {
        val inflater = this.layoutInflater
        val layout: View = inflater.inflate(R.layout.dialog_person, null)
        val firstName = layout.findViewById<EditText>(R.id.firstName)
        val lastName = layout.findViewById<EditText>(R.id.lastName)

        firstName.setText(mPrefs?.getString(GeneralKeys.FIRST_NAME, ""))
        lastName.setText(mPrefs?.getString(GeneralKeys.LAST_NAME, ""))

        firstName.setSelectAllOnFocus(true)
        lastName.setSelectAllOnFocus(true)

        val builder = AlertDialog.Builder(context)
            .setTitle(R.string.profile_person_title)
            .setCancelable(true)
            .setView(layout)
            .setNegativeButton(getString(R.string.dialog_cancel)) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(getString(R.string.dialog_save)) { _, _ ->
                val e = mPrefs?.edit()
                e?.putString(GeneralKeys.FIRST_NAME, firstName.text.toString())
                e?.putString(GeneralKeys.LAST_NAME, lastName.text.toString())

                e?.apply()
                updatePersonSummary()
            }

        personDialog = builder.create()
        personDialog?.show()

        val langParams = personDialog?.window?.attributes
        langParams?.width = LinearLayout.LayoutParams.MATCH_PARENT
        personDialog?.window?.attributes = langParams
    }

    private fun showClearSettingsDialog() {
        val builder = AlertDialog.Builder(context)
            .setTitle(getString(R.string.profile_reset))
            .setMessage(getString(R.string.dialog_confirm))
            .setNegativeButton(getString(R.string.dialog_no)) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(getString(R.string.dialog_yes)) { dialog, _ ->
                dialog.dismiss()
                val ed = mPrefs!!.edit()
                ed.putString(GeneralKeys.FIRST_NAME, "")
                ed.putString(GeneralKeys.LAST_NAME, "")
                ed.apply()
                updatePersonSummary()
            }

        builder.create().show()
    }

    private fun updatePersonSummary() {
        profilePerson?.setSummary(personSummary())
    }

    private fun personSummary(): String {
        var tagName = ""

        val firstNameLength = mPrefs?.getString(GeneralKeys.FIRST_NAME, "")?.length ?: 0
        val lastNameLength = mPrefs?.getString(GeneralKeys.LAST_NAME, "")?.length ?: 0

        if ((firstNameLength > 0) or (lastNameLength > 0)) {
            tagName += mPrefs?.getString(GeneralKeys.FIRST_NAME, "") + " " + mPrefs?.getString(GeneralKeys.LAST_NAME, "")
        }
        return tagName
    }

    private fun setupPersonTimeIntervalPreference(explicitUpdate: Boolean?) {
        var updateFlag = explicitUpdate

        //set visibility of update choices only if enabled
        if (explicitUpdate == null) {
            updateFlag = mPrefs?.getBoolean(GeneralKeys.REQUIRE_USER_TO_COLLECT, false)
        }

        val updateInterval = findPreference<Preference>(GeneralKeys.REQUIRE_USER_INTERVAL)

        if (updateInterval != null) {
            updateInterval.isVisible = updateFlag ?: false
        }
    }
}