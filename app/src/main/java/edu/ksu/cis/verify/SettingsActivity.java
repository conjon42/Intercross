package edu.ksu.cis.verify;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.util.List;

import edu.ksu.cis.mobilevisbarcodechecker.R;

/**
 * Created by Chaney on 4/13/2017.
 */

public class SettingsActivity extends PreferenceActivity {

    public static String FILTER_LIST = "edu.ksu.wheatgenetics.verify.FILTER_LIST";
    public static String COLOR_LIST = "edu.ksu.wheatgenetics.verify.COLOR_LIST";
    public static String ORDER_LIST = "edu.ksu.wheatgenetics.verify.ORDER_LIST";

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

            // Make sure default values are applied.  In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            //PreferenceManager.setDefaultValues(getActivity(),
             //       R.xml.preferences, false);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences_sound);
        }
    }

    public static class PrefsMatchFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Make sure default values are applied.  In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            //PreferenceManager.setDefaultValues(getActivity(),
            //       R.xml.preferences, false);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences_match);
        }
    }

    @Override
    protected boolean isValidFragment(String fragName) {
        return PrefsSoundFragment.class.getName().equals(fragName)
                || PrefsMatchFragment.class.getName().equals(fragName);
    }
}