package org.phenoapps.intercross.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import org.phenoapps.intercross.R
import org.phenoapps.intercross.data.*
import org.phenoapps.intercross.databinding.FragmentPatternBinding
import org.phenoapps.intercross.viewmodels.*
import java.util.*

class PatternFragment: Fragment() {

    private lateinit var mBinding: FragmentPatternBinding

    private lateinit var mSettingsViewModel: SettingsViewModel

    private lateinit var mSettings: Settings

    private var mLastUsed: String = "0"

    private var mLastUUID: String = UUID.randomUUID().toString()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        mBinding = FragmentPatternBinding
                .inflate(inflater, container, false)

        mSettingsViewModel = ViewModelProviders.of(this,
                object : ViewModelProvider.NewInstanceFactory() {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return SettingsViewModel(SettingsRepository.getInstance(
                                IntercrossDatabase.getInstance(requireContext()).settingsDao())) as T

                    }
                }).get(SettingsViewModel::class.java)


        mSettingsViewModel.settings.observe(viewLifecycleOwner, Observer {
            it?.let {
                with(mBinding) {
                    when {
                        it.isUUID -> {
                            mBinding.fragmentPatternInput.visibility = View.INVISIBLE
                            mBinding.codeTextView.text = mLastUUID

                            noneButton.isChecked = false
                            uuidButton.isChecked = true
                            patternButton.isChecked = false
                        }
                        it.isPattern -> {
                            mBinding.fragmentPatternInput.visibility = View.VISIBLE

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
                            mBinding.fragmentPatternInput.visibility = View.INVISIBLE
                            mBinding.codeTextView.text = ""
                            uuidButton.isChecked = false
                            patternButton.isChecked = false
                            noneButton.isChecked = true
                        }
                    }

                    mSettings = it
                }
            }
        })

        mBinding.saveButton.setOnClickListener {
            mSettingsViewModel.addSetting(buildSettings())
        }

        mBinding.radioGroup2.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.uuidButton -> {
                    mBinding.fragmentPatternInput.visibility = View.INVISIBLE
                    mBinding.codeTextView.text = mLastUUID

                }
                R.id.patternButton -> {
                    mBinding.fragmentPatternInput.visibility = View.VISIBLE
                    with(buildPatternViewModel()) {
                        mBinding.codeTextView.text = "${prefix.value}${number.value.toString().padStart(pad.value ?: 0, '0')}${suffix.value}"
                    }
                }
                R.id.noneButton -> {
                    mBinding.fragmentPatternInput.visibility = View.INVISIBLE
                    mBinding.codeTextView.text = ""
                }
            }
        }

        mBinding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId) {
                R.id.autoRadioButton -> {
                    mBinding.numberEditText.isEnabled = false
                    mLastUsed = mBinding.numberEditText.text.toString()
                    mBinding.numberEditText.setText("0")
                }
                else -> {
                    mBinding.numberEditText.isEnabled = true
                    mBinding.numberEditText.setText(mLastUsed)
                }
            }

            updateCodeTextView()
        }

        arrayOf(mBinding.prefixEditText, mBinding.numberEditText, mBinding.suffixEditText, mBinding.padEditText).forEach {
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

        return mBinding.root
    }

    private fun buildSettings() = (if (::mSettings.isInitialized) mSettings else Settings()).apply {
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
}