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
    val namingRoot by key(R.string.root_naming)
    val workflowRoot by key(R.string.root_workflow)
    val printingRoot by key(R.string.root_printing)
    val databaseRoot by key(R.string.root_database)
    val aboutRoot by key(R.string.root_about)
    //endregion

    //region profile preference keys
    val profPersonKey by key(R.string.key_pref_profile_person)
    val profExpKey by key(R.string.key_pref_profile_experiment)
    val argProfAskPerson by key(R.string.arg_profile_ask_person)
    //endregion
    val profileKeySet = setOf(profileRoot, profPersonKey, profExpKey, argProfAskPerson)

    //region naming preference keys
    val nameBlankMaleKey by key(R.string.key_pref_naming_blank_male)
    val nameCrossOrderKey by key(R.string.key_pref_naming_cross_order)
    val nameCreatePatternKey by key(R.string.key_pref_naming_create_pattern)
    //endregion
    val nameKeySet = setOf(namingRoot, nameBlankMaleKey, nameCrossOrderKey, nameCreatePatternKey)

    //region workflow preference keys
    val workCollectKey by key(R.string.key_pref_work_collect)
    val workMetaKey by key(R.string.key_pref_work_meta)
    val workMetaDefaultsKey by key(R.string.key_pref_work_meta_defaults)
    val workAudioKey by key(R.string.key_pref_work_audio)
    val workOpenCrossKey by key(R.string.key_pref_work_open_cross)
    val workCommutativeKey by key(R.string.key_pref_work_commutative)
    //endregion
    val workKeySet = setOf(workflowRoot, workCollectKey, workMetaKey, workMetaDefaultsKey,
        workAudioKey, workOpenCrossKey, workCommutativeKey)

    //region printing preference keys
    val printSetupKey by key(R.string.key_pref_print_setup)
    val printZplImportKey by key(R.string.key_pref_print_zpl_import)
    val argPrintZplCode by key(R.string.arg_print_zpl_code)
    //endregion
    val printKeySet = setOf(printingRoot, printSetupKey, printZplImportKey)

    //region database preference keys
    val dbImportKey by key(R.string.key_pref_db_import)
    val dbExportKey by key(R.string.key_pref_db_export)
    //endregion
    val dbKeySet = setOf(databaseRoot, dbImportKey, dbExportKey)

    //region about preference keys
    val aboutKey by key(R.string.key_pref_about)
    val aboutKeySet = setOf(aboutRoot, aboutKey)
    //endregion
}