package org.phenoapps.intercross.data.fts

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import org.phenoapps.intercross.data.fts.dao.CrossesDao
import org.phenoapps.intercross.data.fts.dao.CrossesFtsDao
import org.phenoapps.intercross.data.fts.tables.Crosses
import org.phenoapps.intercross.data.fts.tables.CrossesFts

@androidx.room.Database(entities = [Crosses::class, CrossesFts::class], version = 1,
    exportSchema = true)
abstract class CrossesDatabase : RoomDatabase() {

    abstract fun crossesDao(): CrossesDao
    abstract fun crossesFtsDao(): CrossesFtsDao

    companion object {

        fun getInstance(ctx: Context): CrossesDatabase {

            return buildDatabase(ctx)

        }

        fun getInMemoryInstance(ctx: Context): CrossesDatabase {

            return buildInMemoryDatabase(ctx)

        }

        private fun buildInMemoryDatabase(ctx: Context): CrossesDatabase {
            return Room.inMemoryDatabaseBuilder(ctx, CrossesDatabase::class.java)
                .build()
        }

        private fun buildDatabase(ctx: Context): CrossesDatabase {
            return Room.databaseBuilder(ctx, CrossesDatabase::class.java, "crosses_fts.db")
                .build()
        }
    }
}