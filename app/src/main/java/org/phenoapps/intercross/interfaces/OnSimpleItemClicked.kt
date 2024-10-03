package org.phenoapps.intercross.interfaces

interface OnSimpleItemClicked {
    fun onItemClicked(pair: Pair<String, String>)
    fun onItemLongClicked(pair: Pair<String, String>) = Unit
}