package edu.ksu.wheatgenetics.survey.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Experiment::class, Sample::class], version = 3)
abstract class SurveyDatabase : RoomDatabase() {
    abstract fun experimentDao(): ExperimentDao
    abstract fun sampleDao(): SampleDao

    companion object {
        //singleton pattern
        @Volatile private var instance: SurveyDatabase? = null

        fun getInstance(ctx: Context): SurveyDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(ctx).also { instance = it }
            }
        }

        private fun buildDatabase(ctx: Context): SurveyDatabase {
            return Room.databaseBuilder(ctx, SurveyDatabase::class.java, "SURVEY")
                    .build()
        }
    }
}