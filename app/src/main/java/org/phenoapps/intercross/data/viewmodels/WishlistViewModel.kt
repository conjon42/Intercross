package org.phenoapps.intercross.data.viewmodels

import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.phenoapps.intercross.data.WishlistRepository
import org.phenoapps.intercross.data.dao.WishlistDao
import org.phenoapps.intercross.data.models.Wishlist

class WishlistViewModel internal constructor(private val repo: WishlistRepository)
    : BaseViewModel<Wishlist>(repo) {

    private val viewModelJob = Job()

    //coroutines do not complete until all launched children complete
    private val viewModelScope = CoroutineScope(Main + viewModelJob)

    val wishlist: LiveData<List<Wishlist>> = repo.getAll()

    val crossblock: LiveData<List<WishlistDao.CrossBlock>> = repo.getCrossblock()

    fun drop() {

        viewModelScope.launch {

            repo.drop()
        }
    }
}