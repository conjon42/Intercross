package org.phenoapps.intercross;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

import androidx.fragment.app.Fragment;


public class IntroActivity extends AppIntro2 {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Customize view
        showStatusBar(false);

        // Add slides
        addSlide(AppIntroFragment.newInstance(getString(org.phenoapps.intercross.R.string.intro_title_1), getString(org.phenoapps.intercross.R.string.intro_body_1), org.phenoapps.intercross.R.drawable.intro_launcher, Color.parseColor("#3f51b5")));
        addSlide(AppIntroFragment.newInstance(getString(org.phenoapps.intercross.R.string.intro_title_2), getString(org.phenoapps.intercross.R.string.intro_body_2), org.phenoapps.intercross.R.drawable.intro_barcode, Color.parseColor("#285E3D")));
        addSlide(AppIntroFragment.newInstance(getString(org.phenoapps.intercross.R.string.intro_title_3), getString(org.phenoapps.intercross.R.string.intro_body_3), org.phenoapps.intercross.R.drawable.intro_zebra, Color.parseColor("#0C6291")));
        addSlide(AppIntroFragment.newInstance(getString(org.phenoapps.intercross.R.string.intro_title_4), getString(org.phenoapps.intercross.R.string.intro_body_4), org.phenoapps.intercross.R.drawable.intro_save, Color.parseColor("#343434")));

        // Hide Skip/Done button.
        showSkipButton(false);
        setProgressButtonEnabled(true);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }
}