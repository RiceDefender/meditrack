package kr.ac.cau.team3.meditrack.data.source.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
@Entity(
    tableName = "medication",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["user_id"],
            childColumns = ["medication_user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medication_user_id")]
)
data class Medication(
    @PrimaryKey(autoGenerate = true) val medication_id: Int = 0,
    val medication_user_id: Int,
    val medication_name: String,
    val medication_category: String,
    val medication_frequency: Frequency, // Typeconverter Enum
    val medication_weekdays: List<Weekday>?,   // optional Typeconverter of Enum
    val medication_interval: Int?              // optional
)