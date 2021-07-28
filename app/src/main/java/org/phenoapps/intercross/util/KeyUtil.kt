package org.phenoapps.intercross.util

import android.content.Context
import org.phenoapps.intercross.R
import kotlin.properties.ReadOnlyProperty

/**
 * Utility class for easily accessing preference keys.
 * Converts keys.xml into string fields to be accessed within a context.
 */
class KeyUtil(private val ctx: Context?) {

    private fun key(id: Int): ReadOnlyProperty<Any?, String> =
        ReadOnlyProperty { _, _ -> ctx?.getString(id)!! }

    //region root preference screen keys
    val root by key(R.string.root_preferences)
    val profileRoot by key(R.string.root_profile)
    val namingRoot by key(R.string.root_naming)
    val workflowRoot by key(R.string.root_workflow)
    val printingRoot by key(R.string.root_printing)
    val databaseRoot by key(R.string.root_database)
    val brapiRoot by key(R.string.root_brapi)
    val aboutRoot by key(R.string.root_about)
    //endregion

    //region profile preference keys
    val profPersonKey by key(R.string.key_pref_profile_person)
    val profExpKey by key(R.string.key_pref_profile_experiment)
    val argProfAskPerson by key(R.string.arg_profile_ask_person)
    //endregion

    //region naming preference keys
    val nameBlankMaleKey by key(R.string.key_pref_naming_blank_male)
    val nameCrossOrderKey by key(R.string.key_pref_naming_cross_order)
    val nameCreatePatternKey by key(R.string.key_pref_naming_create_pattern)
    //endregion

    //region workflow preference keys
    val workCollectKey by key(R.string.key_pref_work_collect)
    val workMetaKey by key(R.string.key_pref_work_meta)
    val workMetaDefaultsKey by key(R.string.key_pref_work_meta_defaults)
    val workAudioKey by key(R.string.key_pref_work_audio)
    val workOpenCrossKey by key(R.string.key_pref_work_open_cross)
    val workCommutativeKey by key(R.string.key_pref_work_commutative)
    //endregion

    //region printing preference keys
    val printSetupKey by key(R.string.key_pref_print_setup)
    val printZplImportKey by key(R.string.key_pref_print_zpl_import)
    val argPrintZplCode by key(R.string.arg_print_zpl_code)
    //endregion

    //region database preference keys
    val dbImportKey by key(R.string.key_pref_db_import)
    val dbExportKey by key(R.string.key_pref_db_export)
    //endregion

    //region brapi preference keys
    val brapiUrlKey by key(R.string.key_pref_brapi_url)
    //endregion

    //region about preference keys
    val aboutKey by key(R.string.key_pref_about)
    //endregion
}