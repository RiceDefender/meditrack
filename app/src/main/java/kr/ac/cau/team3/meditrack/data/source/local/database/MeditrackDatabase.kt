package kr.ac.cau.team3.meditrack.data.source.local.database

import android.content.Context
import kr.ac.cau.team3.meditrack.data.source.local.database.converters.Converters
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kr.ac.cau.team3.meditrack.data.source.local.dao.MedicationDao
import kr.ac.cau.team3.meditrack.data.source.local.dao.MedicationIntakeLogDao
import kr.ac.cau.team3.meditrack.data.source.local.dao.MedicationSchedulerDao
import kr.ac.cau.team3.meditrack.data.source.local.dao.UserDao
import kr.ac.cau.team3.meditrack.data.source.local.entities.User
import kr.ac.cau.team3.meditrack.data.source.local.entities.Medication
import kr.ac.cau.team3.meditrack.data.source.local.entities.MedicationIntakeLog
import kr.ac.cau.team3.meditrack.data.source.local.entities.MedicationScheduler

/**
 * The Room Database that contains the tables.
 */
@Database(entities = [User::class,Medication::class,MedicationIntakeLog::class,MedicationScheduler::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class) // Use your custom class here
abstract class MeditrackDatabase : RoomDatabase() {

    abstract fun UserDao(): UserDao
    abstract fun MedicationDao(): MedicationDao
    abstract fun MedicationSchedulerDao(): MedicationSchedulerDao
    abstract fun MedicationIntakeLogDao(): MedicationIntakeLogDao

    companion object {
        @Volatile
        private var INSTANCE: MeditrackDatabase? = null

        fun getDatabase(context: Context): MeditrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MeditrackDatabase::class.java,
                    "medication_database"
                )
                    .fallbackToDestructiveMigration() // optional, for quick dev
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

