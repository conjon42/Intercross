package org.phenoapps.intercross.util

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
        fun notify(builder: AlertDialog.Builder, title: String, message: String = "") {

            builder.apply {

                setPositiveButton("OK") { _, _ ->

                }
            }

            builder.setMessage(message)

            builder.setTitle(title)
            builder.show()
        }

        /**
         * Alert dialog wrapper that displays a list of clickable Event models.
         */
        //TODO: create adapter variant to show ubiquitous event view s.a in the main fragmnet
        //TODO: Maybe add search function to filter codes
        fun list(builder: AlertDialog.Builder, title: String, empty: String,
                 male: String, female: String, events: List<Event>,
                 function: (Long) -> Unit,
                 makeCrossFunction: (String, String) -> Unit) {

            builder.setTitle(if (events.isEmpty()) empty else title)
                .setNeutralButton(R.string.make_cross_option) { dialog, which ->

                    makeCrossFunction(male, female)

                }
                .setItems(events.map { event -> event.eventDbId }.toTypedArray()) { dialog, index ->

                    function(events[index].id ?: -1L)

                    dialog.dismiss()

                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .create()
                .show()

        }

        fun onOk(builder: AlertDialog.Builder, title: String, cancel: String, ok: String, message: String? = "", function: () -> Unit) {

            builder.apply {

                setNegativeButton(cancel) { _, _ ->

                }

                setPositiveButton(ok) { _, _ ->

                    function()

                }

                setMessage(message)

                setTitle(title)

                show()
            }
        }
    }
}