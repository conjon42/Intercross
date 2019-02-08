package org.phenoapps.intercross

import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleObserver

class CrossPatternActivity : AppCompatActivity(), LifecycleObserver {

    private lateinit var mPatternText: TextView
    private lateinit var mPrefixEditText: EditText
    private lateinit var mNumberEditText: EditText
    private lateinit var mSuffixEditText: EditText
    private lateinit var mPadEditText: EditText
    private lateinit var mRadioGroup: RadioGroup

    private val mSaveButton: Button by lazy {
        findViewById<Button>(R.id.saveButton)
    }

    override fun onStart() {

        super.onStart()

        supportActionBar?.let {
            it.title = ""
            it.themedContext
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }

        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        mPatternText = findViewById(R.id.codeTextView)
        mPrefixEditText = findViewById(R.id.prefixEditText)
        mSuffixEditText = findViewById(R.id.suffixEditText)
        mNumberEditText = findViewById(R.id.numberEditText)
        mPadEditText = findViewById(R.id.padEditText)

        mRadioGroup = findViewById(R.id.radioGroup)

        mRadioGroup.check(R.id.startFromRadioButton)

        mPrefixEditText.setText(pref.getString("LABEL_PATTERN_PREFIX", ""))
        mSuffixEditText.setText(pref.getString("LABEL_PATTERN_SUFFIX", ""))
        val initialNum = pref.getInt("LABEL_PATTERN_MID", -1)
        if (initialNum == -1) mNumberEditText.setText("")
        else mNumberEditText.setText(initialNum.toString())
        val initialPad = pref.getInt("LABEL_PATTERN_PAD", -1)
        if (initialPad == -1) mPadEditText.setText("")
        else mPadEditText.setText(initialPad.toString())

        mRadioGroup.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.autoRadioButton -> {
                    mNumberEditText.setText("0001")
                    mNumberEditText.isEnabled = false
                }
                R.id.startFromRadioButton -> {
                    //mNumberEditText.setText("")
                    mNumberEditText.isEnabled = true
                }
            }
        }

        val watcher: TextWatcher = object : TextWatcher {

            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val pad = mPadEditText.text.toString()
                var padValue = 0
                if (!pad.isEmpty()) padValue = pad.toInt()
                mPatternText.text =
                        "${mPrefixEditText.text}${mNumberEditText.text.padStart(padValue, '0')}${mSuffixEditText.text}"
            }
        }

        var pad = mPadEditText.text.toString()
        var padValue = 0
        if (!pad.isEmpty()) padValue = pad.toInt()
        mPatternText.text = "${mPrefixEditText.text}${mNumberEditText.text.padStart(padValue, '0')}${mSuffixEditText.text}"
        mPrefixEditText.addTextChangedListener(watcher)
        mNumberEditText.addTextChangedListener(watcher)
        mSuffixEditText.addTextChangedListener(watcher)
        mPadEditText.addTextChangedListener(watcher)

        mSaveButton.setOnClickListener {
            //val i = Intent()

            val newPad = mPadEditText.text.toString()
            var midNum = mNumberEditText.text.toString()
            if (midNum.isBlank()) midNum = "0001"
            pad = if (newPad.isBlank()) "0"
            else newPad

            val edit = PreferenceManager.getDefaultSharedPreferences(this).edit()
            edit.putString("LABEL_PATTERN_PREFIX", mPrefixEditText.text.toString())
            edit.putString("LABEL_PATTERN_SUFFIX", mSuffixEditText.text.toString())
            edit.putInt("LABEL_PATTERN_MID", midNum.toInt())
            edit.putBoolean("LABEL_PATTERN_AUTO", mRadioGroup.checkedRadioButtonId == R.id.autoRadioButton)
            edit.putInt("LABEL_PATTERN_PAD", pad.toInt())

            edit.putBoolean("LABEL_PATTERN_CREATED", true)
            edit.apply()

            finish()
        }
    }

    override fun onBackPressed() {

        val builder = AlertDialog.Builder(this).apply {

            setTitle("Would you like to save your changes?")

            setNegativeButton("No") { _, _ ->
                finish()
            }

            setPositiveButton("Yes") { _, _ ->

                var pad = mPadEditText.text.toString()
                var midNum = mNumberEditText.text.toString()
                if (midNum.isBlank()) midNum = "0001"
                if (pad.isBlank()) pad = "0"

                val edit = PreferenceManager.getDefaultSharedPreferences(this@CrossPatternActivity).edit()
                edit.putString("LABEL_PATTERN_PREFIX", mPrefixEditText.text.toString())
                edit.putString("LABEL_PATTERN_SUFFIX", mSuffixEditText.text.toString())
                edit.putInt("LABEL_PATTERN_MID", midNum.toInt())
                edit.putBoolean("LABEL_PATTERN_AUTO", mRadioGroup.checkedRadioButtonId == R.id.autoRadioButton)
                edit.putInt("LABEL_PATTERN_PAD", pad.toInt())

                edit.putBoolean("LABEL_PATTERN_CREATED", true)
                edit.apply()

                finish()
            }
        }

        builder.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_auto_generate)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        onBackPressed()

        return true
    }
}
