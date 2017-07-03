package edu.ksu.wheatgenetics.verify;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by chaneylc on 6/27/2017.
 */

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
