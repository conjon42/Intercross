package org.phenoapps.intercross.util

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.preference.PreferenceManager
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.Meta
import org.phenoapps.intercross.data.models.MetadataValues
import org.phenoapps.intercross.data.models.Parent
import org.phenoapps.intercross.data.models.Settings
import org.phenoapps.intercross.data.models.WishlistView
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.MetaValuesViewModel
import org.phenoapps.intercross.data.viewmodels.ParentsListViewModel
import org.phenoapps.intercross.data.viewmodels.SettingsViewModel
import org.phenoapps.intercross.fragments.preferences.GeneralKeys
import java.util.UUID

class CrossUtil(val context: Context) {

    private val mPref by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    private val mKeyUtil by lazy {
        KeyUtil(context)
    }

    fun submitCrossEvent(activity: FragmentActivity?,
                         female: String,
                         male: String,
                         crossName: String,
                         settings: Settings,
                         settingsModel: SettingsViewModel,
                         eventsModel: EventListViewModel,
                         parents: List<Parent>,
                         parentModel: ParentsListViewModel,
                         wishlistProgress: List<WishlistView>,
                         metaList: List<Meta>,
                         metaValueModel: MetaValuesViewModel): Long {

        var name = crossName

        if (settings.isPattern) {

            //if this submit is from the barcode fragment the cross name is empty

            if (name.isBlank()) {

                name = settings.pattern

            }

            //in the case the user inputs a name while in pattern mode, use the name and don't update the pattern
            if (name == settings.pattern) {

                settingsModel.insert(settings.apply {
                    number += 1
                })

            }
        }

        if (settings.isUUID && name.isBlank()) {

            name = UUID.randomUUID().toString()

        }

        val isCommutative = mPref.getBoolean(mKeyUtil.workCommutativeKey, false)

        val experiment = mPref.getString(mKeyUtil.profExpKey, "")

        val firstName = mPref.getString(GeneralKeys.FIRST_NAME,"")
        val lastName = mPref.getString(GeneralKeys.LAST_NAME,"")
        val person = if (!firstName.isNullOrEmpty() || !lastName.isNullOrEmpty()) {
            "$firstName $lastName"
        } else {
            ""
        }
//            val person = mPref.getString(mKeyUtil.profPersonKey, "")

        val date = DateUtil().getTime()

        val e = Event(name,
                female,
                male,
                "",
                date,
                person,
      experiment ?: "?")

        /** Insert mom/dad cross ids only if they don't exist in the DB already **/
        if (!parents.any { p -> p.codeId == e.femaleObsUnitDbId }) {

            parentModel.insert(Parent(e.femaleObsUnitDbId, 0))

        }

        if (!parents.any { p -> p.codeId == e.maleObsUnitDbId }) {

            parentModel.insert(Parent(e.maleObsUnitDbId, 1))

        }

        val eid = eventsModel.insert(e)

        FileUtil(context).ringNotification(true)

        //TODO FirebaseCrashlytics.getInstance().log("Cross created: $name $date")

        //issue 40 was to disable toast messages when crosses are created
        //val wasCreated = context.getString(R.string.was_created)
        //if (Looper.myLooper() == null) Looper.prepare()
        //SnackbarQueue().push(SnackbarQueue.SnackJob(root, "$name $wasCreated"))

        //insert default metadata values
        metaList.forEach {
            metaValueModel.insert(
                MetadataValues(eid.toInt(), it.id?.toInt() ?: -1, it.defaultValue)
            )
        }

        activity?.runOnUiThread {
            if (isCommutative) checkCommutativeWishlist(female, male, wishlistProgress)
            else checkWishlist(female, male, wishlistProgress)
        }

        return eid
    }

    private fun checkCommutativeWishlist(f: String, m: String, wishlist: List<WishlistView>) {

        val dadId = if (m == "blank") "-1" else m

        wishlist.filter { (it.momId == f && it.dadId == dadId)
                || (it.momId == dadId && it.dadId == f) }.forEach { item ->

            if (item.wishProgress + 1 >= item.wishMin && item.wishMin != 0) {

                FileUtil(context).ringNotification(true)

                if (item.wishProgress + 1 >= item.wishMax && item.wishMax != 0) {

                    Dialogs.notify(AlertDialog.Builder(context), context.getString(R.string.wish_max_complete))

                } else {

                    Dialogs.notify(AlertDialog.Builder(context), context.getString(R.string.wish_min_complete))

                }
            }
        }
    }

    private fun checkWishlist(f: String, m: String, wishlist: List<WishlistView>) {

        val dadId = if (m == "blank") "-1" else m

        wishlist.find { it.momId == f && it.dadId == dadId }?.let { item ->

            if (item.wishProgress + 1 >= item.wishMin && item.wishMin != 0) {

                FileUtil(context).ringNotification(true)

                if (item.wishProgress + 1 >= item.wishMax && item.wishMax != 0) {

                    Dialogs.notify(AlertDialog.Builder(context), context.getString(R.string.wish_max_complete))

                } else {

                    Dialogs.notify(AlertDialog.Builder(context), context.getString(R.string.wish_min_complete))

                }
            }
        }
    }

    /**
     * Function implemented for issue_34, checks a new workflow preference whether or not to open the cross after creation
     */
    fun checkPrefToOpenCrossEvent(controller: NavController, direction: NavDirections) {

        val openCross = mPref.getBoolean(mKeyUtil.workOpenCrossKey, false)

        if (openCross) {
            controller.navigate(
                direction)
        }
    }
}
