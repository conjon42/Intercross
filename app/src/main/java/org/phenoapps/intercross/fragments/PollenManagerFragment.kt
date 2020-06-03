package org.phenoapps.intercross.fragments

import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.PollenGroupAdapter
import org.phenoapps.intercross.databinding.FragmentPollenManagerBinding
import org.phenoapps.intercross.util.SnackbarQueue
import java.util.*

class PollenManagerFragment : IntercrossBaseFragment<FragmentPollenManagerBinding>(R.layout.fragment_pollen_manager) {

    private lateinit var mAdapter: PollenGroupAdapter

    override fun FragmentPollenManagerBinding.afterCreateView() {

        //activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        mAdapter = PollenGroupAdapter(requireContext())

        mPollenManagerViewModel.groups.observe(viewLifecycleOwner, Observer {
            it?.let {
                mAdapter.submitList(it)
            }
        })

        pollenView.layoutManager = LinearLayoutManager(requireContext())
        pollenView.adapter = mAdapter

        editText2.setText(UUID.randomUUID().toString())

        newButton.setOnClickListener {
            if (editText.text.toString().isNotEmpty()) {
                mPollenManagerViewModel.addPollenSet(editText.text.toString(), editText2.text.toString())
                editText.text.clear()
            }
        }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val p = mAdapter.currentList[viewHolder.adapterPosition]
                mPollenManagerViewModel.delete(p)
                mSnackbar.push(SnackbarQueue.SnackJob(root, p.name, "Undo") {
                    mPollenManagerViewModel.addPollenSet(p.name, editText2.text.toString())
                })

            }
        }).attachToRecyclerView(pollenView)

    }
}
