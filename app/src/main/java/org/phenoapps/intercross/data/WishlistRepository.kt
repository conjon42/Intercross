package org.phenoapps.intercross.data

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.phenoapps.intercross.data.dao.WishlistDao
import org.phenoapps.intercross.data.models.Wishlist

class WishlistRepository private constructor(
        private val wishlistDao: WishlistDao): BaseRepository<Wishlist>(wishlistDao) {

    suspend fun createWishlist( fid: String,  mid: String,
                                fname: String,  mname: String,
                                wishType: String,
                                wishMin: Int,  wishMax: Int) {
        withContext(IO) {

            wishlistDao.insert(Wishlist(fid, mid, fname, mname, wishType, wishMin, wishMax))

        }
    }

    suspend fun drop() {

        withContext(IO) {

            wishlistDao.drop()

        }
    }

    fun getAll() = wishlistDao.getAll()

    fun getAllCounts() = wishlistDao.getAllCounts()

    fun getAllCommutativeWishCounts() = wishlistDao.getAllCommutativeWishCounts()

    fun getCrossblock() = wishlistDao.getCrossblock()

    fun getCommutativeCrossblock() = wishlistDao.getCommutativeCrossBlock()

    fun getMaleHeaders() = wishlistDao.getMaleHeaders()

    fun getFemaleHeaders() = wishlistDao.getFemaleHeaders()

    companion object {
        @Volatile private var instance: WishlistRepository? = null

        fun getInstance(wishlistDao: WishlistDao) =
                instance ?: synchronized(this) {
                    instance ?: WishlistRepository(wishlistDao)
                        .also { instance = it }
                }
    }
}