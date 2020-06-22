package org.phenoapps.intercross.data.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.phenoapps.intercross.data.WishlistRepository
import org.phenoapps.intercross.data.viewmodels.WishlistViewModel

class WishlistViewModelFactory(private val repo: WishlistRepository) : ViewModelProvider.Factory {

    @SuppressWarnings("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        return WishlistViewModel(repo) as T
    }
}