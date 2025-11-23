package kr.ac.cau.team3.meditrack.data.source.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.LocalTime

@Entity(
    tableName = "medication_scheduler",
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["medication_id"],
            childColumns = ["ms_medication_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ms_medication_id")]
)
data class MedicationScheduler(
    @PrimaryKey(autoGenerate = true) val ms_id: Int = 0,
    val ms_medication_id: Int,
    val ms_time_of_day: TimeOfDay, // Typeconverter
    val ms_scheduled_time: LocalTime, // Recurring alarm
    val ms_dosage: String
)

