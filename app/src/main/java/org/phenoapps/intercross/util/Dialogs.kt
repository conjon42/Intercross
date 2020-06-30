package org.phenoapps.intercross.util

import android.view.View
import androidx.appcompat.app.AlertDialog
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.models.Event

class Dialogs {

    companion object {

        /***
         * Generic dialog to run a function if the OK/Neutral button are pressed.
         * If the ok button is pressed the boolean parameter to the function is set to true, false otherwise.
         */
        fun booleanOption(builder: AlertDialog.Builder, title: String,
                          positiveText: String, negativeText: String,
                          neutralText: String, function: (Boolean) -> Unit) {

            builder.apply {

                setTitle(title)

                setPositiveButton(positiveText) { _, _ ->

                    function(true)

                }

                setNeutralButton(neutralText) { _, _ ->

                    function(false)

                }

                setNegativeButton(negativeText) { _, _ ->

                }

                show()
            }
        }

        /**
         * Simple alert dialog to notify the user of a message.
         */
        fun notify(builder: AlertDialog.Builder, title: String) {

            builder.apply {

                setPositiveButton("OK") { _, _ ->

                }
            }

            builder.setTitle(title)
            builder.show()
        }

        /**
         * Alert dialog wrapper that displays a list of clickable Event models.
         */
        fun list(builder: AlertDialog.Builder, title: String, root: View, events: List<Event>, function: (Long) -> Unit) {

            var choiceIndex = 0

            builder.setTitle(title)
                    .setSingleChoiceItems(events
                    .map { event -> event.readableName }
                    .toTypedArray(), 0) { view, index -> choiceIndex = index }
                    .setPositiveButton(R.string.go) { view, index ->

                        function(events[choiceIndex].id ?: -1L)

                    }
                    .setNegativeButton(R.string.cancel) { _, _ -> }
                    .create()
                    .show()
        }
    }
}