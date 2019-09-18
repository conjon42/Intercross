package org.phenoapps.intercross.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.Settings
import org.phenoapps.intercross.databinding.FragmentPatternBinding
import org.phenoapps.intercross.util.SnackbarQueue
import org.phenoapps.intercross.viewmodels.PatternViewModel
import java.util.*

class PatternFragment: IntercrossBaseFragment<FragmentPatternBinding>(R.layout.fragment_pattern) {

    private var mLastUsed: String = "0"

    private var mLastUUID: String = UUID.randomUUID().toString()

    private var mSettingsHaveChanged = false

    private var mPrevCheckedId = -1

    override fun FragmentPatternBinding.afterCreateView() {

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        mSettingsViewModel.settings.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                noneButton.isChecked = true
            } else {
                when {
                    it.isUUID -> {
                        fragmentPatternInput.visibility = View.INVISIBLE
                        codeTextView.text = mLastUUID

                        noneButton.isChecked = false
                        uuidButton.isChecked = true
                        patternButton.isChecked = false
                    }
                    it.isPattern -> {
                        fragmentPatternInput.visibility = View.VISIBLE

                        noneButton.isChecked = false
                        uuidButton.isChecked = false
                        patternButton.isChecked = true
                        prefixEditText.setText(it.prefix)
                        suffixEditText.setText(it.suffix)
                        numberEditText.setText(it.number.toString())
                        padEditText.setText(it.pad.toString())

                        when {
                            it.startFrom -> {
                                startFromRadioButton.isChecked = true
                                autoRadioButton.isChecked = false
                            }
                            it.isAutoIncrement -> {
                                startFromRadioButton.isChecked = false
                                autoRadioButton.isChecked = true
                            }
                        }
                    }
                    else -> {
                        fragmentPatternInput.visibility = View.INVISIBLE
                        codeTextView.text = ""
                        uuidButton.isChecked = false
                        patternButton.isChecked = false
                        noneButton.isChecked = true
                    }
                }

                mSettings = it
            }
        })

        saveButton.setOnClickListener {

            mSettingsViewModel.addSetting(buildSettings())

        }

        radioGroup2.setOnCheckedChangeListener { group, checkedId ->

            closeKeyboard()

            if (mPrevCheckedId == -1) {
                mPrevCheckedId = checkedId
            } else if (mPrevCheckedId != checkedId) {
                mSettingsHaveChanged = true
            }

            when (checkedId) {
                R.id.uuidButton -> {
                    fragmentPatternInput.visibility = View.GONE
                    codeTextView.text = mLastUUID

                }
                R.id.patternButton -> {
                    fragmentPatternInput.visibility = View.VISIBLE
                    with(buildPatternViewModel()) {
                        codeTextView.text = "${prefix.value}${number.value.toString().padStart(pad.value ?: 0, '0')}${suffix.value}"
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

    private fun buildSettings() = mSettings.apply {
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
        with (buildPatternViewModel()) {
            mBinding.codeTextView.text = "${prefix.value}${number.value.toString().padStart(pad.value ?: 0, '0')}${suffix.value}"
        }
    }

    fun buildPatternViewModel(): PatternViewModel {

        var num = mBinding.numberEditText.text.toString().trim()
        if (num.isEmpty()) num = "0"

        var pad = mBinding.padEditText.text.toString().trim()
        if (pad.isEmpty()) pad = "0"

        return PatternViewModel(mBinding.uuidButton.isChecked,
                mBinding.autoRadioButton.isChecked,
                mBinding.suffixEditText.text.toString(),
                mBinding.prefixEditText.text.toString(),
                num.toInt(), pad.toInt()
        )
    }

    fun onBackButtonPressed() {

        if (mSettingsHaveChanged) {
            askUserToSave()
        } else findNavController().popBackStack()


    }

    private fun askUserToSave() {

        val builder = AlertDialog.Builder(requireContext()).apply {

            setNegativeButton("Cancel") { _, _ ->
                findNavController().popBackStack()
            }

            setPositiveButton("Save") { _, _ ->
                mSettingsViewModel.addSetting(buildSettings())
                findNavController().popBackStack()
            }
        }

        builder.setTitle("Would you like to save your settings?")
        builder.show()
    }
}