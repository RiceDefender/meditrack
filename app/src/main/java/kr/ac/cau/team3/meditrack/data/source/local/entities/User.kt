package kr.ac.cau.team3.meditrack.data.source.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user",
    indices = [Index(value = ["user_name"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true) val user_id: Int = 0,
    var user_name: String,
    var user_passwordhash: String,
)