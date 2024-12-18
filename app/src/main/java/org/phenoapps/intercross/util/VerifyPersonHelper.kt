package org.phenoapps.intercross.util

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.util.Log
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ActivityContext
import org.phenoapps.intercross.R
import org.phenoapps.intercross.activities.MainActivity
import org.phenoapps.intercross.fragments.EventsFragmentDirections
import javax.inject.Inject

class VerifyPersonHelper @Inject constructor(@ActivityContext private val context: Context) {

    @Inject
    lateinit var preferences: SharedPreferences

    private val mPrefs by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    private val mKetUtil by lazy {
        KeyUtil(context)
    }

    /**
     * Simple function that checks if the dialog asked last time was opened >24hrs ago.
     * If the condition is met, it asks the user to reenter the collector id.
     *
     * If the conditions were not met, we simply execute action to be performed after the checks (actionAfterDialog)
     */
    fun checkLastOpened(actionAfterDialog: (() -> Unit)?) {
        val lastOpen: Long = mPrefs.getLong(mKetUtil.lastTimeAskedKey, 0L)
        val alreadyAsked: Boolean = mPrefs.getBoolean(mKetUtil.askedSinceOpenedKey, false)
        val systemTime = System.nanoTime()

        //number of hours to wait before asking for user, pref found in profile
        val interval = when (mPrefs.getString(mKetUtil.requireUserIntervalKey, "1")) {
            "1" -> 0
            "2" -> 12
            else -> 24
        }

       val nanosToWait = 1e9.toLong() * 3600 * interval

        if (!alreadyAsked) { // skip if already asked
            if ((interval == 0) // ask on every open
                || (lastOpen != 0L && (systemTime - lastOpen > nanosToWait))) { // ask after interval and interval has elapsed
                val verify: Boolean = mPrefs.getBoolean(mKetUtil.requireUserToCollect, true)
                if (verify) {
                    val firstName: String = mPrefs.getString(mKetUtil.personFirstNameKey, "") ?: ""
                    val lastName: String = mPrefs.getString(mKetUtil.personLastNameKey, "") ?: ""
                    if (firstName.isNotEmpty() || lastName.isNotEmpty()) {
                        //person presumably has been set
                        if (actionAfterDialog != null) {
                            showAskCollectorDialog(
                                context.getString(R.string.collect_dialog_verify_collector) + " " + firstName + " " + lastName + "?",
                                context.getString(R.string.collect_dialog_verify_yes_button),
                                context.getString(R.string.collect_dialog_neutral_button),
                                context.getString(R.string.collect_dialog_verify_no_button),
                                actionAfterDialog
                            )
                        }
                    } else {
                        //person presumably hasn't been set
                        if (actionAfterDialog != null) {
                            showAskCollectorDialog(
                                context.getString(R.string.collect_dialog_new_collector),
                                context.getString(R.string.collect_dialog_verify_no_button),
                                context.getString(R.string.collect_dialog_neutral_button),
                                context.getString(R.string.collect_dialog_verify_yes_button),
                                actionAfterDialog
                            )
                        }
                    }
                }
                // if any kind of prompt was shown to user
                // update the LAST_TIME_ASKED and ASKED_SINCE_OPENED
                mPrefs.edit().putLong(mKetUtil.lastTimeAskedKey, System.nanoTime()).apply()
                mPrefs.edit().putBoolean(mKetUtil.askedSinceOpenedKey, true).apply()
            } else if (actionAfterDialog != null) {
                actionAfterDialog()
            }
        } else if (actionAfterDialog != null) {
            actionAfterDialog()
        }

    }

    // once dialog is shown actionAfterDialog
    // is executed only if positive button pressed
    private fun showAskCollectorDialog(
        message: String,
        positive: String,
        neutral: String,
        negative: String,
        actionAfterDialog: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(message) //yes button
            .setPositiveButton(positive) { dialog: DialogInterface, _:
                // yes, don't ask again button
                Int -> dialog.dismiss()
                actionAfterDialog()
            }
            .setNeutralButton(neutral) { dialog: DialogInterface, _: Int ->
                // modify settings (navigates to profile preferences)
                dialog.dismiss()
                (context as? MainActivity)?.findNavController(R.id.nav_fragment)
                    ?.navigate(EventsFragmentDirections.actionFromEventsToPreferences(PERSONUPDATE = false, MODIFYPROFILESETTINGS = true))
            }
            .setNegativeButton(negative) { dialog: DialogInterface, _: Int ->
                // no (navigates to the person preference)
                dialog.dismiss()
                (context as? MainActivity)?.findNavController(R.id.nav_fragment)
                    ?.navigate(EventsFragmentDirections.actionFromEventsToPreferences(PERSONUPDATE = true, MODIFYPROFILESETTINGS = false))
            }
            .show()
    }

    fun updateAskedSinceOpened() {
        mPrefs.edit().putBoolean(mKetUtil.askedSinceOpenedKey, false).apply()
    }
}

