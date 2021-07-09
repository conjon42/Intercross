package org.phenoapps.intercross.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.phenoapps.intercross.data.dao.EventsDao
import org.phenoapps.intercross.data.dao.ParentsDao
import org.phenoapps.intercross.data.dao.PollenGroupDao
import org.phenoapps.intercross.data.dao.SettingsDao
import org.phenoapps.intercross.data.dao.WishlistDao
import org.phenoapps.intercross.data.migrations.MigrationV2MetaData
import org.phenoapps.intercross.data.models.*

//added database migration from version 1 -> version 2 6/28/2021 see MigrationV2MetaData for documentation.
@Database(entities = [Event::class, Parent::class,
    Wishlist::class, Settings::class, PollenGroup::class],
        views = [WishlistView::class], version = 2)
@TypeConverters(Converters::class)
abstract class IntercrossDatabase : RoomDatabase() {

    abstract fun eventsDao(): EventsDao
    abstract fun parentsDao(): ParentsDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun settingsDao(): SettingsDao
    abstract fun pollenGroupDao(): PollenGroupDao

    companion object {

        const val DATABASE_NAME = "intercross.db"

        //singleton pattern
        @Volatile private var instance: IntercrossDatabase? = null

        fun getInstance(ctx: Context): IntercrossDatabase {

            return instance ?: synchronized(this) {

                instance ?: buildDatabase(ctx).also { instance = it }
            }
        }

        private fun buildDatabase(ctx: Context): IntercrossDatabase {

            return Room.databaseBuilder(ctx, IntercrossDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigrationOnDowngrade() //allows flexible dev tests for going back db versions
                .addMigrations(MigrationV2MetaData()) //v1 -> v2 migration added JSON based metadata
                .setJournalMode(JournalMode.TRUNCATE) //truncate mode makes it easier to export/import database w/o having to manage WAL files.
                .build()
        }
    }
}