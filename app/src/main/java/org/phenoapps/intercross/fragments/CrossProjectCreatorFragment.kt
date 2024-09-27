package org.phenoapps.intercross.fragments

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import org.phenoapps.intercross.R
import org.phenoapps.intercross.brapi.service.BrAPIServiceV2
import org.phenoapps.intercross.databinding.FragmentCrossProjectCreatorBinding

class CrossProjectCreatorFragment:
    IntercrossBaseFragment<FragmentCrossProjectCreatorBinding>(R.layout.fragment_cross_project_creator),
    CoroutineScope by MainScope() {

    companion object {
        val TAG = CrossProjectCreatorFragment::class.simpleName
    }

    var mName: String = ""

    private val mService: BrAPIServiceV2 by lazy {

        BrAPIServiceV2(this.context)

    }

    /**
     * Logs each time a failure api callback occurs
     */
    private fun handleFailure(fail: Int): Void? {

        Log.d(TAG, "BrAPI cross project post callback failed. $fail")

        return null //default fail callback return type for brapi

    }

    /**
     * this will call the crossesApi to create a new project with name
     */
    private fun postCrossingProject(name: String) {

        //uses an async call to the server
        //ui starts a indeterminate progress bar that ends when the async call returns
        mService.postCrossingProject(name, { projects ->

            activity?.runOnUiThread {

                disableProgress()

                mBinding.dialogCrossProjectSummaryText.text = projects.first().crossingProjectName

                findNavController().popBackStack()
            }

            null

        }) { fail ->

            activity?.runOnUiThread {

                disableProgress()

                Toast.makeText(this@CrossProjectCreatorFragment.context,
                    getString(R.string.fragment_project_creator_post_failed), Toast.LENGTH_SHORT).show()

            }

            handleFailure(fail)

        }
    }

    //the initial step that asks the user for row/column size of their field
    //row*column plots will be generated based on a chosen pattern
    private fun setupUi() {

//        val submitButton = findViewById<Button>(R.id.dialog_planned_crosses_submit_button)
//
//        //when the OK button is pressed...
//        submitButton.setOnClickListener {
//
//            val projectNameEditText = findViewById<EditText>(R.id.dialog_cross_project_name_edit_text)
//
//            mName = projectNameEditText.text.toString()
//
//        }
    }

    fun disableProgress() {

        mBinding.dialogCrossProjectProgress.visibility = View.INVISIBLE

    }

    fun enableProgress() {

        mBinding.dialogCrossProjectProgress.visibility = View.VISIBLE


    }

    override fun FragmentCrossProjectCreatorBinding.afterCreateView() {

        this@CrossProjectCreatorFragment.activity.let { act ->

            setupUi()

            mBinding.dialogCrossProjectSubmitButton.setOnClickListener {

                enableProgress()

                val name = mBinding.dialogCrossProjectNameEditText.text.toString()

                if (name.isNotEmpty()) {

                    postCrossingProject(name)

                } else {

                    disableProgress()

                    Toast.makeText(this@CrossProjectCreatorFragment.context,
                        getString(R.string.fragment_project_creator_empty_name), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}