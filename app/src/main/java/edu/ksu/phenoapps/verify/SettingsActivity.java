package edu.ksu.phenoapps.verify;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatActivity {

    public static String SCAN_MODE_LIST = "edu.ksu.phenoapps.verify.SCAN_MODE";
    public static String AUDIO_ENABLED = "edu.ksu.phenoapps.verify.AUDIO_ENABLED";
    public static String TUTORIAL_MODE = "edu.ksu.phenoapps.verify.TUTORIAL_MODE";
    public static String USER_NAME = "edu.ksu.phenoapps.verify.USER_NAME";
    public static String LIST_KEY_NAME = "edu.ksu.phenoapps.verify.LIST_KEY_NAME";
    public static String PAIR_NAME = "edu.ksu.phenoapps.verify.PAIR_NAME";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle(null);
            getSupportActionBar().getThemedContext();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_OK);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}