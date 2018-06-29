package org.phenoapps.intercross;

import android.Manifest;

class IntercrossConstants {

    final static String[] permissions = new String[]{
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA};

    //request
    final static int PERM_REQ = 100;
    final static int LOADER_INTENT_REQ = 101;
    final static int CAMERA_INTENT_REQ = 102;
    final static int SETTINGS_INTENT_REQ = 103;
    final static int DEFAULT_CONTENT_REQ = 104;

    //extras
    final static String CSV_URI = "org.phenoapps.intercross.CSV_URI";
    final static String ID_ARRAY_EXTRA = "org.phenoapps.intercross.ID_ARRAY";
    final static String COL_ARRAY_EXTRA = "org.phenoapps.intercross.COL_ARRAY";

    final static String USER_ARRAY_EXTRA = "org.phenoapps.intercross.USER_ARRAY";
    final static String DATE_ARRAY_EXTRA = "org.phenoapps.intercross.DATE_ARRAY";
    final static String CHECKED_ARRAY_EXTRA = "org.phenoapps.intercross.CHECKED_ARRAY";
    final static String SCAN_ARRAY_EXTRA = "org.phenoapps.intercross.SCAN_ARRAY";

    final static String LIST_ID_EXTRA = "org.phenoapps.intercross.LIST_ID_EXTRA";
    final static String HEADER_LIST_EXTRA = "org.phenoapps.intercross.HEADER_LIST_EXTRA";
    final static String HEADER_DELIMETER_EXTRA = "org.phenoapps.intercross.HEADER_DELIMITER_EXTRA";

    final static String CAMERA_RETURN_ID = "org.phenoapps.intercross.CAMERA_RETURN_ID";
    final static String PAIR_COL_EXTRA = "org.phenoapps.intercross.PAIR_COL_EXTRA";
    final static String PAIR_MODE_LOADER = "org.phenoapps.intercross.PAIR_MODE_LOADER";

}
