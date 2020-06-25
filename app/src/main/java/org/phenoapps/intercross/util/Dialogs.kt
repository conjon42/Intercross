package org.phenoapps.intercross.util

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.fragments.SummaryFragmentDirections

class Dialogs {

    companion object {

        fun notify(builder: AlertDialog.Builder, title: String) {

            builder.apply {

                setPositiveButton("OK") { _, _ ->

                }
            }

            builder.setTitle(title)
            builder.show()
        }

        fun list(builder: AlertDialog.Builder, title: String, root: View, events: List<Event>) {

            var choiceIndex = 0

            builder.setTitle(title)
                    .setSingleChoiceItems(events
                    .map { event -> event.readableName }
                    .toTypedArray(), 0) { view, index -> choiceIndex = index }
                    .setPositiveButton(R.string.go) { view, index ->

                        Navigation.findNavController(root)
                                .navigate(SummaryFragmentDirections.actionToEventDetail(
                                        events[choiceIndex].id ?: -1L
                                ))
                    }
                    .setNegativeButton(R.string.cancel) { _, _ -> }
                    .create()
                    .show()
        }
    }
}