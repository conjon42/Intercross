package edu.ksu.wheatgenetics.verify;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import java.util.List;

/**
 * Created by Chaney on 4/13/2017.
 */

public class SettingsActivity extends PreferenceActivity {

    public static String SCAN_MODE_LIST = "edu.ksu.wheatgenetics.verify.SCAN_MODE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (hasHeaders()) {

        }
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    public static class PrefsSoundFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_sound);
        }
    }

    public static class PrefsMatchFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences_scan_mode);
        }
    }

    @Override
    protected boolean isValidFragment(String fragName) {
        return PrefsSoundFragment.class.getName().equals(fragName)
                || PrefsMatchFragment.class.getName().equals(fragName);
    }
}