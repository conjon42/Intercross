package org.phenoapps.intercross.application;

import androidx.multidex.MultiDexApplication;

import org.phenoapps.intercross.BuildConfig;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class Intercross extends MultiDexApplication {

    public Intercross() {
        if (BuildConfig.DEBUG) {
            //StrictMode.enableDefaults();
            //un-comment to enable strict warnings in logcat
        }
    }
}
