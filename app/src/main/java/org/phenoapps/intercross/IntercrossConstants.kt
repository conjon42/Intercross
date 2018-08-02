package org.phenoapps.intercross

import android.Manifest

internal object IntercrossConstants {

    val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA)

    //request
    val PERM_REQ = 100
    val CAMERA_INTENT_REQ = 102
    val SETTINGS_INTENT_REQ = 103
    val DEFAULT_CONTENT_REQ = 104

    //extras
    val CSV_URI = "org.phenoapps.intercross.CSV_URI"

    val LIST_ID_EXTRA = "org.phenoapps.intercross.LIST_ID_EXTRA"

    val CAMERA_RETURN_ID = "org.phenoapps.intercross.CAMERA_RETURN_ID"


}
