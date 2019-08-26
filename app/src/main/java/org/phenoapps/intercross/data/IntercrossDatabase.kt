package org.phenoapps.intercross.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Events::class, Parents::class, Wishlist::class, Settings::class, Pollen::class, PollenGroup::class], version = 6)
abstract class IntercrossDatabase : RoomDatabase() {
    abstract fun eventsDao(): EventsDao
    abstract fun parentsDao(): ParentsDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun settingsDao(): SettingsDao
    abstract fun pollenDao(): PollenDao
    abstract fun pollenGroupDao(): PollenGroupDao

    companion object {
        //singleton pattern
        @Volatile private var instance: IntercrossDatabase? = null

        fun getInstance(ctx: Context): IntercrossDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(ctx).also { instance = it }
            }
        }

        private fun buildDatabase(ctx: Context): IntercrossDatabase {
            return Room.databaseBuilder(ctx, IntercrossDatabase::class.java, "INTERCROSS")
                    .build()
        }
    }
}