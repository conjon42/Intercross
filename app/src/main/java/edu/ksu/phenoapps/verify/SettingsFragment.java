package edu.ksu.phenoapps.verify;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(edu.ksu.phenoapps.verify.R.xml.preferences);
    }
}
