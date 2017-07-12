package edu.ksu.wheatgenetics.verify;

import android.Manifest;

class VerifyConstants {

    final static String[] permissions = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
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
    final static String CSV_URI = "edu.ksu.wheatgenetics.verify.CSV_URI";
    final static String ID_ARRAY_EXTRA = "edu.ksu.wheatgenetics.verify.ID_ARRAY";
    final static String COL_ARRAY_EXTRA = "edu.ksu.wheatgenetics.verify.COL_ARRAY";
    final static String CAMERA_RETURN_ID = "edu.ksu.wheatgenetics.verify.CAMERA_RETURN_ID";

}
