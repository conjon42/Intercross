package org.phenoapps.intercross

import android.Manifest

internal object IntercrossConstants {

    val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA)

    //request
    val PERM_REQ = 100
    val CAMERA_INTENT_REQ = 102
    val SETTINGS_INTENT_REQ = 103
    val DEFAULT_CONTENT_REQ = 104
    val MANAGE_HEADERS_REQ = 300
    val USER_INPUT_HEADERS_REQ = 301

    //extras
    val CSV_URI = "org.phenoapps.intercross.CSV_URI"

    val LIST_ID_EXTRA = "org.phenoapps.intercross.LIST_ID_EXTRA"

    val COL_ID_KEY = "org.phenoapps.intercross.COL_ID_KEY"

    val TIMESTAMP = "org.phenoapps.intercross.TIMESTAMP"

    val CROSS_ID = "org.phenoapps.intercross.CROSS_ID"

    val CAMERA_RETURN_ID = "org.phenoapps.intercross.CAMERA_RETURN_ID"

    val HEADERS = "org.phenoapps.intercross.HEADERS"

    val USER_INPUT_VALUES = "org.phenoapps.intercross.USER_INPUT_VALUES"


}
