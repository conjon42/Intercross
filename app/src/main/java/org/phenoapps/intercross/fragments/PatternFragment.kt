package org.phenoapps.intercross.fragments

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.SettingsRepository
import org.phenoapps.intercross.data.models.Settings
import org.phenoapps.intercross.data.viewmodels.SettingsViewModel
import org.phenoapps.intercross.data.viewmodels.factory.SettingsViewModelFactory
import org.phenoapps.intercross.databinding.FragmentPatternBinding
import java.util.*

class PatternFragment: IntercrossBaseFragment<FragmentPatternBinding>(R.layout.fragment_pattern) {

    data class Pattern(val uuid: Boolean = false,
                       val auto: Boolean = false,
                       val suffix: String = "",
                       val prefix: String = "",
                       val number: Int = 0,
                       val pad: Int = 3) {

        val pattern = prefix + number.toString().padStart(pad, '0') + suffix

    }

    private val settingsModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(SettingsRepository.getInstance(db.settingsDao()))
    }

    override fun onPause() {
        super.onPause()

        settingsModel.insert(buildSettings())
    }

    private var mLastUsed: String = "0"

    private var mLastUUID: String = UUID.randomUUID().toString()

    override fun FragmentPatternBinding.afterCreateView() {

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        settingsModel.settings.observeForever {

            it?.let {

                with (it) {

                    settings = this

                    when {

                        isUUID -> {

                            codeTextView.text = mLastUUID

                        }
                        isPattern -> {

                            prefixEditText.setText(prefix)
                            suffixEditText.setText(suffix)
                            numberEditText.setText(number.toString())
                            padEditText.setText(pad.toString())

                            when {
                                startFrom -> {
                                    startFromRadioButton.isChecked = true
                                    autoRadioButton.isChecked = false
                                }
                                isAutoIncrement -> {
                                    startFromRadioButton.isChecked = false
                                    autoRadioButton.isChecked = true
                                }
                            }
                        }
                        else -> {
                            codeTextView.text = ""
                        }
                    }
                }
            }
        }

        radioGroup2.setOnCheckedChangeListener { _, checkedId ->

            closeKeyboard()

            when (checkedId) {
                R.id.uuidButton -> {
                    fragmentPatternInput.visibility = View.GONE
                    codeTextView.text = mLastUUID

                }
                R.id.patternButton -> {
                    fragmentPatternInput.visibility = View.VISIBLE
                    with(buildPattern()) {
                        codeTextView.text = pattern
                    }
                }
                R.id.noneButton -> {
                    fragmentPatternInput.visibility = View.GONE
                    codeTextView.text = ""
                }
            }
        }

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId) {
                R.id.autoRadioButton -> {
                    numberEditText.isEnabled = false
                    mLastUsed = numberEditText.text.toString()
                    numberEditText.setText("0")
                }
                else -> {
                    numberEditText.isEnabled = true
                    numberEditText.setText(mLastUsed)
                }
            }

            updateCodeTextView()
        }

        arrayOf(prefixEditText, numberEditText, suffixEditText, padEditText).forEach {
            it.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable?) {
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    updateCodeTextView()
                }
            })
        }
    }

    private fun buildSettings() = Settings().apply {

        id = 0
        var n = mBinding.numberEditText.text.toString()
        var p = mBinding.padEditText.text.toString()
        if(n.isEmpty()) n = "0"
        if(p.isEmpty()) p = "0"
        isAutoIncrement = mBinding.autoRadioButton.isChecked
        isPattern = mBinding.patternButton.isChecked
        isUUID = mBinding.uuidButton.isChecked
        number = n.toInt()
        pad = p.toInt()
        prefix = mBinding.prefixEditText.text.toString()
        suffix = mBinding.suffixEditText.text.toString()
        startFrom = mBinding.startFromRadioButton.isChecked
    }

    fun updateCodeTextView() {

        with (buildPattern()) {

            mBinding.codeTextView.text = pattern
        }
    }

    private fun buildPattern(): Pattern {

        var num = mBinding.numberEditText.text.toString().trim()

        if (num.isEmpty()) num = "0"

        var pad = mBinding.padEditText.text.toString().trim()

        if (pad.isEmpty()) pad = "0"

        return Pattern(mBinding.uuidButton.isChecked,
                mBinding.autoRadioButton.isChecked,
                mBinding.suffixEditText.text.toString(),
                mBinding.prefixEditText.text.toString(),
                num.toInt(), pad.toInt()
        )
    }

    fun onBackButtonPressed() {

        settingsModel.insert(buildSettings())

        findNavController().popBackStack()
    }
}