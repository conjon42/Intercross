package org.phenoapps.intercross;

import android.Manifest;
import android.app.Application;
import android.os.Environment;

public class Constants extends Application {
    public static final String TAG = "Intercross";

    public final static String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CAMERA
    };

    public final static int PERM_REQ = 100;

    public static final String BRAPI_PATH_V1 = "/brapi/v1";
    public static final String BRAPI_PATH_V2 = "/brapi/v2";

}