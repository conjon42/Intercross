package org.phenoapps.intercross;

import android.Manifest;

class IntercrossConstants {

    final static String[] permissions = new String[]{
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA};

    //request
    final static int PERM_REQ = 100;
    final static int CAMERA_INTENT_REQ = 102;
    final static int SETTINGS_INTENT_REQ = 103;
    final static int DEFAULT_CONTENT_REQ = 104;

    //extras
    final static String CSV_URI = "org.phenoapps.intercross.CSV_URI";

    final static String LIST_ID_EXTRA = "org.phenoapps.intercross.LIST_ID_EXTRA";

    final static String CAMERA_RETURN_ID = "org.phenoapps.intercross.CAMERA_RETURN_ID";


}
