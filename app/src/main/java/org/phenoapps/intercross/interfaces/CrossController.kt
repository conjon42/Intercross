package org.phenoapps.intercross.interfaces

import org.phenoapps.intercross.fragments.CrossTrackerFragment

interface CrossController {
    fun onCrossClicked(male: String, female: String)
    fun onPersonChipClicked(persons: List<CrossTrackerFragment.PersonCount>, crossCount: Int)
    fun onDateChipClicked(dates: List<CrossTrackerFragment.DateCount>)
}