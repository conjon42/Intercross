package org.phenoapps.intercross.data

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class WishlistRepository private constructor(
        private val wishlistDao: WishlistDao
) {
    suspend fun createWishlist( maleDbId: String,  femaleDbId: String,
                                maleName: String,  femaleName: String,
                                wishType: String,  wishMin: Int,  wishMax: Int) {
        withContext(IO) {
            wishlistDao.insert(Wishlist(maleDbId, femaleDbId, maleName, femaleName, wishType, wishMin, wishMax))
        }
    }

    suspend fun deleteAll() {
        withContext(IO) {
            wishlistDao.deleteAll()
        }
    }

    suspend fun update(vararg p: Wishlist?) {
        withContext(IO) {
            wishlistDao.update(*p)
        }
    }

    suspend fun delete(vararg p: Wishlist?) {
        withContext(IO) {
            wishlistDao.delete(*p)
        }
    }

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