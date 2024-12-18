package org.phenoapps.intercross.fragments.preferences

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import org.phenoapps.intercross.R
import org.phenoapps.intercross.util.KeyUtil

class ProfileFragment : BasePreferenceFragment(R.xml.profile_preferences) {

    private val mPrefs by lazy {
        context?.let { PreferenceManager.getDefaultSharedPreferences(it) }
    }

    private val mKeyUtil by lazy {
        KeyUtil(context)
    }

    private var profilePerson: Preference? = null
    private var profileReset: Preference? = null

    private var personDialog: AlertDialog? = null

    override fun onResume() {
        super.onResume()
        setToolbar(getString(R.string.prefs_profile_title))
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        mPrefs?.edit()?.putLong(mKeyUtil.lastTimeAskedKey, System.nanoTime())?.apply()

        profilePerson = findPreference(mKeyUtil.profilePersonKey)
        profileReset = findPreference(mKeyUtil.profileResetKey)

        updatePersonSummary()

        setPreferenceClickListeners()

        val arguments = arguments

        if (arguments != null) {
            val updatePerson = arguments.getBoolean(mKeyUtil.personUpdateKey, false)

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
    }

    private fun showPersonDialog() {
        val inflater = this.layoutInflater
        val layout: View = inflater.inflate(R.layout.dialog_person, null)
        val firstName = layout.findViewById<EditText>(R.id.firstName)
        val lastName = layout.findViewById<EditText>(R.id.lastName)

        firstName.setText(mPrefs?.getString(mKeyUtil.personFirstNameKey, ""))
        lastName.setText(mPrefs?.getString(mKeyUtil.personLastNameKey, ""))

        firstName.setSelectAllOnFocus(true)
        lastName.setSelectAllOnFocus(true)

        val builder = AlertDialog.Builder(context)
            .setTitle(R.string.profile_person_title)
            .setCancelable(true)
            .setView(layout)
            .setNegativeButton(getString(R.string.dialog_cancel)) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(getString(R.string.dialog_save)) { _, _ ->
                val e = mPrefs?.edit()
                e?.putString(mKeyUtil.personFirstNameKey, firstName.text.toString())
                e?.putString(mKeyUtil.personLastNameKey, lastName.text.toString())

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
                ed.putString(mKeyUtil.personFirstNameKey, "")
                ed.putString(mKeyUtil.personLastNameKey, "")
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

        val firstNameLength = mPrefs?.getString(mKeyUtil.personFirstNameKey, "")?.length ?: 0
        val lastNameLength = mPrefs?.getString(mKeyUtil.personLastNameKey, "")?.length ?: 0

        if ((firstNameLength > 0) or (lastNameLength > 0)) {
            tagName += mPrefs?.getString(mKeyUtil.personFirstNameKey, "") + " " + mPrefs?.getString(mKeyUtil.personLastNameKey, "")
        }
        return tagName
    }
}