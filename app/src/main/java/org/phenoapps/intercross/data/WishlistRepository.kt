package org.phenoapps.intercross.data

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.phenoapps.intercross.data.dao.WishlistDao
import org.phenoapps.intercross.data.models.Wishlist

class WishlistRepository private constructor(
        private val wishlistDao: WishlistDao): BaseRepository<Wishlist>(wishlistDao) {

    suspend fun createWishlist( fid: String,  mid: String,
                                fname: String,  mname: String,
                                wishType: String,  wishCurrent: Int,
                                wishMin: Int,  wishMax: Int) {
        withContext(IO) {
            wishlistDao.insert(Wishlist(fid, mid, fname, mname, wishType, wishCurrent, wishMin, wishMax))
        }
    }

    suspend fun createWishlist(vararg wishlist: Wishlist) {

        withContext(IO) {

            wishlistDao.insert(*wishlist)
        }
    }

    suspend fun deleteAll() {
        withContext(IO) {
            wishlistDao.deleteAll()
        }
    }

    fun getCrossblock() = wishlistDao.getCrossBlock()

    fun getAll() = wishlistDao.getAll()

    companion object {
        @Volatile private var instance: WishlistRepository? = null

        fun getInstance(wishlistDao: WishlistDao) =
                instance ?: synchronized(this) {
                    instance ?: WishlistRepository(wishlistDao)
                        .also { instance = it }
                }
    }
}