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

    //search preference
    val searchPrefKey by key(R.string.key_pref_search)

    //region root preference screen keys
    val root by key(R.string.root_preferences)
    val profileRoot by key(R.string.root_profile)

    val behaviorRoot by key(R.string.root_behavior)
    val behaviorKeySet = setOf(behaviorRoot)

    val printingRoot by key(R.string.root_printing)
    val databaseRoot by key(R.string.root_database)
    val aboutRoot by key(R.string.root_about)
    //endregion

    //region profile preference keys
    //endregion
    val profileKeySet = setOf(profileRoot)

    //region naming preference keys
    //endregion

    //region workflow preference keys
    //endregion

    //region printing preference keys
    val printSetupKey by key(R.string.key_pref_print_setup)
    val printZplImportKey by key(R.string.key_pref_print_zpl_import)
    val argPrintZplCode by key(R.string.arg_print_zpl_code)
    //endregion
    val printKeySet = setOf(printingRoot, printSetupKey, printZplImportKey)

    //region database preference keys
    //endregion
    val dbKeySet = setOf(databaseRoot)

    //region about preference keys
    val aboutKey by key(R.string.key_pref_about)
    val aboutKeySet = setOf(aboutRoot, aboutKey)
    //endregion

    val prefsRootKeys = setOf(profileRoot, behaviorRoot, databaseRoot, printingRoot, aboutRoot)
}
