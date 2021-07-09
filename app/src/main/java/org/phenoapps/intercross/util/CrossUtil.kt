package org.phenoapps.intercross.util

import android.content.Context
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.preference.PreferenceManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSyntaxException
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

    fun submitCrossEvent(root: View,
                         female: String,
                         male: String,
                         crossName: String,
                         settings: Settings,
                         settingsModel: SettingsViewModel,
                         eventsModel: EventListViewModel,
                         parents: List<Parent>,
                         parentModel: ParentsListViewModel,
                         wishlistProgress: List<WishlistView>): Long {

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

        val experiment = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("org.phenoapps.intercross.EXPERIMENT", "")

        val person = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("org.phenoapps.intercross.PERSON", "")

        val date = DateUtil().getTime()

        val eventsList = eventsModel.events.value

        //use the default metadata values to populate the new cross event
        val metadata = if (eventsList?.isNotEmpty() == true) {

            eventsList.first().metadata.toDefaultJson()

        } else Event.metadataDefault

        val e = Event(name,
                female,
                male,
                "",
                date,
         person ?: "?",
      experiment ?: "?",
                metadata = metadata)

        /** Insert mom/dad cross ids only if they don't exist in the DB already **/
        if (!parents.any { p -> p.codeId == e.femaleObsUnitDbId }) {

            parentModel.insert(Parent(e.femaleObsUnitDbId, 0))

        }

        if (!parents.any { p -> p.codeId == e.maleObsUnitDbId }) {

            parentModel.insert(Parent(e.maleObsUnitDbId, 1))

        }

        val eid = eventsModel.insert(e)

        FileUtil(context).ringNotification(true)

        FirebaseCrashlytics.getInstance().log("Cross created: $name $date")

        //issue 40 was to disable toast messages when crosses are created
        //val wasCreated = context.getString(R.string.was_created)
        //if (Looper.myLooper() == null) Looper.prepare()
        //SnackbarQueue().push(SnackbarQueue.SnackJob(root, "$name $wasCreated"))

        checkWishlist(female, male, wishlistProgress)

        return eid
    }

    //takes a json string and resets its value with its default value
    private fun String.toDefaultJson(): String = try {

        val element = JsonParser.parseString(this)

        if (element.isJsonObject) {

            val json = element.asJsonObject

            json.entrySet().forEach {
                with(json.getAsJsonArray(it.key)) {
                    this[0] = this[1].asJsonPrimitive
                }
            }

            json.toString()

        } else Event.metadataDefault

    } catch (e: JsonSyntaxException) {

        e.printStackTrace()

        Event.metadataDefault
    }


    private fun checkWishlist(f: String, m: String, wishlist: List<WishlistView>) {

        wishlist.find { it.momId == f && it.dadId == m }?.let { item ->

            if (item.wishProgress + 1 >= item.wishMin && item.wishMin != 0) {

                FileUtil(context).ringNotification(true)

                if (item.wishProgress >= item.wishMax && item.wishMax != 0) {

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

        val pref = PreferenceManager.getDefaultSharedPreferences(context)

        val openCross = pref?.getBoolean("org.phenoapps.intercross.OPEN_CROSS_IMMEDIATELY", false) ?: false

        if (openCross) {
            controller.navigate(
                direction)
        }
    }
}
