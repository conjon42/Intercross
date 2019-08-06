package org.phenoapps.intercross.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.phenoapps.intercross.data.*

class WishlistViewModel internal constructor(
        private val repo: WishlistRepository
) : ViewModel() {

    private val viewModelJob = Job()

    //coroutines do not complete until all launched children complete
    private val viewModelScope = CoroutineScope(Main + viewModelJob)

    val wishlist: LiveData<List<Wishlist>> = repo.getAll()

    fun addWishlist(fid: String, mid: String, fname: String, mname: String, type: String, min: Int, max: Int) {

        viewModelScope.launch {
            repo.createWishlist(fid, mid, fname, mname, type, min, max)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            repo.deleteAll()
        }
    }

    fun delete(vararg p: Wishlist) {
        viewModelScope.launch {
            repo.delete(*p)
        }
    }

    fun update(vararg p: Wishlist) {
        viewModelScope.launch {
            repo.update(*p)
        }
    }
}