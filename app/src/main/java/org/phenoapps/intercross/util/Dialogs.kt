package org.phenoapps.intercross.util

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import org.phenoapps.intercross.R

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
    }
}