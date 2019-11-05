package org.phenoapps.intercross.fragments

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.PollenAdapter
import org.phenoapps.intercross.data.IntercrossDatabase
import org.phenoapps.intercross.data.PollenGroup
import org.phenoapps.intercross.data.PollenRepository
import org.phenoapps.intercross.databinding.FragmentPollenBinding
import org.phenoapps.intercross.util.BluetoothUtil
import org.phenoapps.intercross.util.SnackbarQueue
import org.phenoapps.intercross.viewmodels.PollenViewModel

class PollenFragment : IntercrossBaseFragment<FragmentPollenBinding>(R.layout.fragment_pollen) {

    private lateinit var mAdapter: PollenAdapter

    private lateinit var mGroup: PollenGroup

    private lateinit var mPollenViewModel: PollenViewModel

    override fun FragmentPollenBinding.afterCreateView() {

        val group = arguments?.getParcelable<PollenGroup>("pollen")

        group?.id?.let { groupId ->

            mGroup = group

            mPollenViewModel = ViewModelProviders.of(this@PollenFragment,
                    object : ViewModelProvider.NewInstanceFactory() {
                        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return PollenViewModel(PollenRepository.getInstance(
                                    IntercrossDatabase.getInstance(requireContext()).pollenDao())) as T

                        }
                    }).get(PollenViewModel::class.java)

            model = group

            barcodeImage.setImageBitmap(BarcodeEncoder().createBitmap(
                    MultiFormatWriter().encode(group.name,
                            BarcodeFormat.QR_CODE, barcodeImage.width, barcodeImage.height)))

            maleView.layoutManager = LinearLayoutManager(requireContext())

            mAdapter = PollenAdapter(requireContext())

            maleView.adapter = mAdapter

            executePendingBindings()

            startObservers()

            updateButton.setOnClickListener {
                val entry = idEntry.text.toString()
                if (entry.isNotEmpty()) {
                    mPollenViewModel.addPollen(groupId, entry)
                    idEntry.text.clear()
                }
            }

            idEntry.setOnEditorActionListener(TextView.OnEditorActionListener { _, i, _ ->
                if (i == EditorInfo.IME_ACTION_DONE) {
                    if ((idEntry.text ?: "").isNotEmpty()) {
                        mPollenViewModel.addPollen(groupId, idEntry.text.toString())
                        idEntry.text.clear()
                    }
                    return@OnEditorActionListener true
                }
                false
            })

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                    val p = mAdapter.currentList[viewHolder.adapterPosition]
                    mPollenViewModel.delete(p)
                    mSnackbar.push(SnackbarQueue.SnackJob(root, "${p.pollenId}", "Undo") {
                        mPollenViewModel.addPollen(groupId, p.pollenId)
                    })

                }
            }).attachToRecyclerView(maleView)

            setHasOptionsMenu(true)
        }


    }

    private fun FragmentPollenBinding.startObservers() {

        mPollenViewModel.pollen.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it.isNotEmpty()) {
                    mAdapter.submitList(it)
                }
            }
        })

        mSharedViewModel.lastScan.observe(viewLifecycleOwner, Observer {
            it?.let {
                if (it.isNotEmpty()) {

                    mGroup.id?.let { gid ->
                        mPollenViewModel.addPollen(gid, it)
                        mSharedViewModel.lastScan.value = ""
                    }
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.pollen_toolbar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {

            R.id.action_continuous_barcode -> {

                findNavController().navigate(R.id.barcode_scan_fragment,
                        Bundle().apply {
                            putString("mode", "single")
                        }
                )
            }
            R.id.action_print -> {
                BluetoothUtil().print(requireContext(), mGroup)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
