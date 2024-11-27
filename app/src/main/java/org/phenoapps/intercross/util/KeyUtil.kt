package org.phenoapps.intercross.util

import android.content.Context
import org.phenoapps.intercross.R
import kotlin.properties.ReadOnlyProperty


/**
 * Utility class for easily accessing preference keys.
 * Converts keys.xml into string fields to be accessed within a context.
 */
class KeyUtil(private val ctx: Context?) {

    //explicitly state phenolib utils
    val brapiKeys by lazy {
        org.phenoapps.utils.KeyUtil(ctx)
    }

    private fun key(id: Int): ReadOnlyProperty<Any?, String> =
        ReadOnlyProperty { _, _ -> ctx?.getString(id)!! }

    //non preference keys
    //boolean value to determine if brapi has been imported previously
    val brapiHasBeenImported by key(R.string.key_brapi_has_been_imported)
}
