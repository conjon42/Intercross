package org.phenoapps.intercross.util

import android.content.Context
import android.preference.PreferenceManager
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.Parent
import org.phenoapps.intercross.data.models.Settings
import org.phenoapps.intercross.data.models.Wishlist
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.ParentsListViewModel
import org.phenoapps.intercross.data.viewmodels.SettingsViewModel
import org.phenoapps.intercross.data.viewmodels.WishlistViewModel
import java.util.*

class CrossUtil(val context: Context) {

    fun submitCrossEvent(female: String,
                                 male: String,
                                 crossName: String,
                                 settings: Settings,
                                 settingsModel: SettingsViewModel,
                                 eventsModel: EventListViewModel,
                                 parents: List<Parent>,
                                 parentModel: ParentsListViewModel) {

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

        //todo reimplement wishlist checks
        //checkWishlist(female, male, cross)

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

    //Todo: Update this to check a wishlist view
    private fun checkWishlist(f: String, m: String, x: String, wishlist: List<Wishlist>, wishlistModel: WishlistViewModel) {

        wishlist.find { it.femaleDbId == f && it.maleDbId == m}?.let { item ->

            //TODO: Add Alert Dialog when min or max is achieved.

            val current = item.wishCurrent + 1

            //wishlist item has been found, item should be updated and visualized
            wishlistModel.update(item.apply {

                wishCurrent = current
            })

            if (current >= item.wishMin && item.wishMin != 0) {

                FileUtil(context).ringNotification(true)

                //mSnackbar.push(SnackbarQueue.SnackJob(mBinding.root, "Wishlist complete for $f and $m : $current/${item.wishMin}"))

            }// else mSnackbar.push(SnackbarQueue.SnackJob(mBinding.root, "New Cross Event! $x added."))
        }
    }

}
