package org.phenoapps.intercross

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment

import com.github.paolorotolo.appintro.AppIntro2
import com.github.paolorotolo.appintro.AppIntroFragment


class IntroActivity : AppIntro2() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Customize view
        showStatusBar(false)

        // Add slides
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_title_1), getString(org.phenoapps.intercross.R.string.intro_body_1), R.drawable.intercross_large, Color.parseColor("#A84937")))
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_title_2), getString(org.phenoapps.intercross.R.string.intro_body_2), R.drawable.intro_folder, Color.parseColor("#285E3D")))
        addSlide(AppIntroFragment.newInstance(getString(R.string.intro_title_3), getString(R.string.intro_body_3), R.drawable.intro_list, Color.parseColor("#0C6291")))

        // Hide Skip/Done button.
        showSkipButton(false)
        isProgressButtonEnabled = true
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        finish()
    }

    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)
    }
}