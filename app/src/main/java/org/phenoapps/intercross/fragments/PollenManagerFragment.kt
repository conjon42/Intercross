package org.phenoapps.intercross.fragments

import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import org.phenoapps.intercross.R
import org.phenoapps.intercross.adapters.ParentsAdapter
import org.phenoapps.intercross.data.EventsRepository
import org.phenoapps.intercross.data.ParentsRepository
import org.phenoapps.intercross.data.PollenGroupRepository
import org.phenoapps.intercross.data.models.Event
import org.phenoapps.intercross.data.models.Parent
import org.phenoapps.intercross.data.models.PollenGroup
import org.phenoapps.intercross.data.viewmodels.EventListViewModel
import org.phenoapps.intercross.data.viewmodels.ParentsListViewModel
import org.phenoapps.intercross.data.viewmodels.PollenGroupListViewModel
import org.phenoapps.intercross.data.viewmodels.factory.EventsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.ParentsListViewModelFactory
import org.phenoapps.intercross.data.viewmodels.factory.PollenGroupListViewModelFactory
import org.phenoapps.intercross.databinding.FragmentPollenManagerBinding
import java.util.*

class PollenManagerFragment : IntercrossBaseFragment<FragmentPollenManagerBinding>(R.layout.fragment_pollen_manager) {

    private val args: PollenManagerFragmentArgs by navArgs()

    private lateinit var mAdapter: ParentsAdapter

    private var mEvents: List<Event> = ArrayList<Event>()

    private var mMales: List<Parent> = ArrayList<Parent>()

    private var mPolycrosses: List<PollenGroup> = ArrayList<PollenGroup>()

    private val eventList: EventListViewModel by viewModels {

        EventsListViewModelFactory(
                EventsRepository.getInstance(db.eventsDao()))
    }

    private val parentList: ParentsListViewModel by viewModels {

        ParentsListViewModelFactory(
                ParentsRepository.getInstance(db.parentsDao())
        )
    }

    private val groupList: PollenGroupListViewModel by viewModels {

        PollenGroupListViewModelFactory(
                PollenGroupRepository.getInstance(db.pollenGroupDao())
        )
    }

    override fun FragmentPollenManagerBinding.afterCreateView() {

        /***
         * When the safe args = 0 we are creating females, otherwise we are creating males/groups
         */
        //an error is shown when a barcode already exists in the database
        val error = getString(R.string.ErrorCodeExists)

        if (args.mode == 1) {

            mAdapter = ParentsAdapter(parentList, groupList)

            parentList.males.observe(viewLifecycleOwner, Observer {

                it?.let { males ->

                    mMales = males

                    groupList.groups.observeForever { groups ->

                        groups?.let {

                            /**
                             * Transform polycrosses to simple parent object before submitting to parent adapter.
                             */
                            mAdapter.submitList(groups
                                    .distinctBy { g -> g.codeId }+males.distinctBy { m -> m.codeId })
                        }
                    }
                }
            })

            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            recyclerView.adapter = mAdapter

        } else {

            //TODO replace with data binding

            textView.visibility = View.GONE

            recyclerView.visibility = View.GONE
        }

        /**
         * Error check, ensure that the entered code does not exist in the events table.
         * TODO also check parents codes
         */
        eventList.events.observe(viewLifecycleOwner, Observer {

            it?.let {

                mEvents = it

                codeEditText.addTextChangedListener {

                    var flag = true

                    for (e: Event in mEvents) {

                        if (codeEditText.text.toString() == e.eventDbId) {

                            if (codeTextHolder.error == null) codeTextHolder.error = error

                            flag = false

                            break

                        }
                    }

                    if (flag) {

                        codeTextHolder.error = null

                    }
                }
            }
        })

        codeEditText.setText(UUID.randomUUID().toString())

        newButton.setOnClickListener {

            when (args.mode) {
                /**
                 * Insert a single female into the database based on the data entry.
                 */
                0 -> {
                    //TODO add text sanitization
                    parentList.insert(
                            Parent(codeEditText.text.toString(),
                                    0,
                                    nameEditText.text.toString()))

                    mBinding.root.findNavController().navigate(
                            PollenManagerFragmentDirections
                                    .actionReturnToParentsFragment(0))
                }
                /**
                 * Either enter a group if a list is selected, or a single male with the
                 * entered data
                 */
                1 -> {
                    val addedMales = ArrayList<PollenGroup>()

                    for (p: Parent in mMales) {

                        if (p.selected) {

                            p.id?.let { id ->

                                addedMales.add(buildGroup(id))
                            }
                        }
                    }

                    for (poly: PollenGroup in mPolycrosses) {

                        if (poly.selected) {

                            poly.id?.let {id ->

                                addedMales.add(buildGroup(id))
                            }
                        }
                    }

                    /***
                     * check if list has been created, otherwise insert a single male
                     */
                    if (addedMales.isEmpty()) {

                        //Todo make a build function to replace this
                        parentList.insert(
                                Parent(codeEditText.text.toString(),
                                        1,
                                        nameEditText.text.toString()))

                    } else {

                        groupList.insert(*addedMales.toTypedArray())

                    }

                    mBinding.root.findNavController().navigate(
                            PollenManagerFragmentDirections
                                    .actionReturnToParentsFragment(1))
                }
            }

        }
    }

    /**
     * This function initializes and returns a PollenGroup object with the elements of the UI.
     */
    private fun FragmentPollenManagerBinding.buildGroup(id: Long) =
            PollenGroup(codeEditText.text.toString(),
                    nameEditText.text.toString(), id)


}
