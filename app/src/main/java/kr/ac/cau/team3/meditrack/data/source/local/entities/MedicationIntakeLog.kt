package kr.ac.cau.team3.meditrack.data.source.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.sql.Timestamp
import java.time.LocalTime

@Entity(
    tableName = "medication_intake_log",
    foreignKeys = [
        ForeignKey(
            entity = MedicationScheduler::class,
            parentColumns = ["ms_id"],
            childColumns = ["mil_ms_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("mil_ms_id")]
)

data class MedicationIntakeLog(
    @PrimaryKey(autoGenerate = true) val mil_id: Int = 0,
    val mil_ms_id: Int,
    val mil_scheduled_time: LocalTime, // Timestamp
    val mil_taken_time: Timestamp?, // Nullable Timestamp
    val mil_status: IntakeStatus // Typeconverter
)
