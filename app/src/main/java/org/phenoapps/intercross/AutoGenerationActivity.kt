package org.phenoapps.intercross

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.*
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleObserver
import java.util.*

class AutoGenerationActivity : AppCompatActivity(), LifecycleObserver {

    private lateinit var mPatternText: TextView
    private lateinit var mPrefixEditText: EditText
    private lateinit var mNumberEditText: EditText
    private lateinit var mSuffixEditText: EditText
    private lateinit var mPadEditText: EditText
    private lateinit var mRadioGroup: RadioGroup

    private val mSaveButton: Button by lazy {
        findViewById<Button>(R.id.saveButton)
    }

    private val mDbHelper: IdEntryDbHelper = IdEntryDbHelper(this)

    override fun onStart() {

        super.onStart()

        supportActionBar?.let {
            it.title = ""
            it.themedContext
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        }

        val pref = PreferenceManager.getDefaultSharedPreferences(this)

        mPatternText = findViewById<TextView>(R.id.codeTextView)
        mPrefixEditText = findViewById<EditText>(R.id.prefixEditText)
        mSuffixEditText = findViewById<EditText>(R.id.suffixEditText)
        mNumberEditText = findViewById<EditText>(R.id.numberEditText)
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

        mRadioGroup.setOnCheckedChangeListener { radioGroup, i ->
            when (i) {
                R.id.autoRadioButton -> {
                    mNumberEditText.setText("0001")
                    mNumberEditText.isEnabled = false
                }
                R.id.startFromRadioButton -> {
                    //mNumberEditText.setText("")
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
        mPatternText.setText("${mPrefixEditText.text}${mNumberEditText.text.padStart(padValue, '0')}${mSuffixEditText.text}")
        mPrefixEditText.addTextChangedListener(watcher)
        mNumberEditText.addTextChangedListener(watcher)
        mSuffixEditText.addTextChangedListener(watcher)
        mPadEditText.addTextChangedListener(watcher)

        mSaveButton.setOnClickListener {
            //val i = Intent()

            var newPad = mPadEditText.text.toString()
            var midNum = mNumberEditText.text.toString()
            if (midNum.isBlank()) midNum = "0001"
            if (newPad.isBlank()) pad = "0"

            /*i.putExtra(IntercrossConstants.PATTERN, LabelPattern(mPrefixEditText.text.toString(),
                    mSuffixEditText.text.toString(),
                    midNum.toInt(),
                    mRadioGroup.checkedRadioButtonId == R.id.autoRadioButton,
                    pad.toInt()))

            this@AutoGenerationActivity.setResult(Activity.RESULT_OK, i)*/

            val edit = PreferenceManager.getDefaultSharedPreferences(this).edit()
            edit.putString("LABEL_PATTERN_PREFIX", mPrefixEditText.text.toString())
            edit.putString("LABEL_PATTERN_SUFFIX", mSuffixEditText.text.toString())
            edit.putInt("LABEL_PATTERN_MID", midNum.toInt())
            edit.putBoolean("LABEL_PATTERN_AUTO", mRadioGroup.checkedRadioButtonId == R.id.autoRadioButton)
            edit.putInt("LABEL_PATTERN_PAD", newPad.toInt())
            edit.apply()

            finish()
        }
    }

    data class LabelPattern(val prefix: String, val suffix: String, val number: Int,
                            val auto: Boolean, val pad: Int) : Parcelable {
        constructor(parcel: Parcel) : this(
                parcel.readString(),
                parcel.readString(),
                parcel.readInt(),
                parcel.readByte() != 0.toByte(),
                parcel.readInt()) {
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(prefix)
            parcel.writeString(suffix)
            parcel.writeInt(number)
            parcel.writeByte(if (auto) 1 else 0)
            parcel.writeInt(pad)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<LabelPattern> {
            override fun createFromParcel(parcel: Parcel): LabelPattern {
                return LabelPattern(parcel)
            }

            override fun newArray(size: Int): Array<LabelPattern?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_auto_generate)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        finish()

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {

        super.onActivityResult(requestCode, resultCode, intent)

        if (resultCode == Activity.RESULT_OK) {

            if (intent != null) {
                when (requestCode) {
                    IntercrossConstants.MANAGE_HEADERS_REQ -> {
                        mDbHelper.updateColumns(intent.extras?.getStringArrayList(IntercrossConstants.HEADERS) ?: ArrayList())
                    }
                    IntercrossConstants.USER_INPUT_HEADERS_REQ -> {
                        mDbHelper.updateValues(intent.extras?.getInt(IntercrossConstants.COL_ID_KEY).toString(),
                                intent.extras.getStringArrayList(IntercrossConstants.USER_INPUT_VALUES)
                        )
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        //do nothing
    }
}
