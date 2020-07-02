package org.phenoapps.intercross.util

import android.content.Context
import android.preference.PreferenceManager
import androidx.appcompat.app.AlertDialog
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.Parent
import org.phenoapps.intercross.data.models.Settings
import org.phenoapps.intercross.data.models.WishlistView
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.ParentsListViewModel
import org.phenoapps.intercross.data.viewmodels.SettingsViewModel
import java.util.*

class CrossUtil(val context: Context) {

    fun submitCrossEvent(female: String,
                                 male: String,
                                 crossName: String,
                                 settings: Settings,
                                 settingsModel: SettingsViewModel,
                                 eventsModel: EventListViewModel,
                                 parents: List<Parent>,
                                 parentModel: ParentsListViewModel,
                                 wishlistProgress: List<WishlistView>) {

        val cross = when {

            crossName.isNotBlank() -> crossName

            settings.isPattern -> {
                val n = settings.number
                settings.number += 1
                settingsModel.update(settings)
                "${settings.prefix}${n.toString().padStart(settings.pad, '0')}${settings.suffix}"
            }

            settings.isUUID -> {
                UUID.randomUUID().toString()
            }

            else -> crossName
        }

        checkWishlist(female, male, wishlistProgress)

        val experiment = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("org.phenoapps.intercross.EXPERIMENT", "")

        val person = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("org.phenoapps.intercross.PERSON", "")

        val e = Event(cross,
                female,
                male,
                "",
                DateUtil().getTime(), person ?: "?", experiment ?: "?")


        /** Insert mom/dad cross ids only if they don't exist in the DB already **/
        if (!parents.any { p -> p.codeId == e.femaleObsUnitDbId }) {

            parentModel.insert(Parent(e.femaleObsUnitDbId, 0))

        }

        if (!parents.any { p -> p.codeId == e.maleObsUnitDbId }) {

            parentModel.insert(Parent(e.maleObsUnitDbId, 1))

        }

        eventsModel.insert(e)

    }

    private fun checkWishlist(f: String, m: String, wishlist: List<WishlistView>) {

        wishlist.find { it.momId == f && it.dadId == m }?.let { item ->

            if (item.wishProgress >= item.wishMin && item.wishMin != 0) {

                FileUtil(context).ringNotification(true)

                if (item.wishProgress >= item.wishMax && item.wishMax != 0) {

                    Dialogs.notify(AlertDialog.Builder(context), context.getString(R.string.wish_max_complete))

                } else {

                    Dialogs.notify(AlertDialog.Builder(context), context.getString(R.string.wish_min_complete))

                }
            }
        }
    }
}
