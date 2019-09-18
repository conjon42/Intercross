package org.phenoapps.intercross.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.view.*
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import org.phenoapps.intercross.MainActivity.Companion.REQ_FILE_IMPORT
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.ParentsAdapter
import org.phenoapps.intercross.data.EventName
import org.phenoapps.intercross.data.Events
import org.phenoapps.intercross.data.Parents
import org.phenoapps.intercross.databinding.FragmentParentsBinding
import org.phenoapps.intercross.util.BluetoothUtil
import org.phenoapps.intercross.util.DateUtil
import org.phenoapps.intercross.util.FileUtil

class ParentsFragment: IntercrossBaseFragment<FragmentParentsBinding>(R.layout.fragment_parents) {

    private lateinit var mMales: List<Parents>
    private lateinit var mFemales: List<Parents>
    private lateinit var mAdapter: ParentsAdapter

    override fun onActivityResult(requestCode: Int, resultCode: Int, i: Intent?) {
        super.onActivityResult(requestCode, resultCode, i)
        if (requestCode == REQ_FILE_IMPORT && resultCode == RESULT_OK) {
            i?.data?.let {
                val lines = FileUtil(requireContext()).parseUri(it)
                if (lines.isNotEmpty()) {
                    val headerLine = lines[0]
                    val headers = headerLine.split(",")
                    val numCols = headers.size
                    if (numCols == 4) { //lines = id,name,type,order
                        (lines - lines[0]).forEach {
                            val row = it.split(",")
                            if (row.size == numCols) {
                                mParentsViewModel.addParents(
                                        row[0], row[1], row[2], row[3]
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun FragmentParentsBinding.afterCreateView() {

        mAdapter = ParentsAdapter()

        recyclerView.adapter = mAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        importButton.setOnClickListener {
            mParentsViewModel.delete(*(mMales + mFemales).toTypedArray())

            val uri = Uri.parse(Environment.getExternalStorageDirectory().path
                    + "/Intercross/Import/Parents/")
            startActivityForResult(Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT)
                    .apply {
                        setDataAndType(uri, "*/*")
                    }, "Choose parents to import"), REQ_FILE_IMPORT)
        }

        tabLayout2.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    when(it.text) {
                        "Female" -> {
                            mAdapter.submitList(mFemales)
                        }
                        "Male" -> {
                            mAdapter.submitList(mMales)
                        }
                    }
                }
            }

        })


        button3.setOnClickListener {

            val experiment = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .getString("org.phenoapps.intercross.EXPERIMENT", "")

            val person = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .getString("org.phenoapps.intercross.PERSON", "")

            val events = ArrayList<Events>()
            (mMales + mFemales).forEach {
                //add person TODO
                if (it.isSelected) events.add(
                        Events(null, it.parentDbId, EventName.POLLINATION.itemType,
                                "none", "none", null, DateUtil().getTime(), person, experiment))
            }
            if (events.isNotEmpty()) {
                BluetoothUtil().templatePrint(requireContext(), events.toTypedArray())
            }

        }

        mParentsViewModel.parents.observe(viewLifecycleOwner, Observer {
            it?.let {

                //get uniques
                val males = it.filter { p -> p.parentType == "male" }.toSet()
                val females = it.filter { p -> p.parentType == "female" }.toSet()

                mMales = males.toList()
                mFemales = females.toList()

                when (tabLayout2.selectedTabPosition) {
                    0 -> {
                        mAdapter.submitList(mFemales)
                    }
                    else -> mAdapter.submitList(mMales)
                }
            }
        })

    }
}