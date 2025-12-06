package kr.ac.cau.team3.meditrack.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kr.ac.cau.team3.meditrack.MeditrackRepository
import kr.ac.cau.team3.meditrack.data.source.local.entities.Frequency
import kr.ac.cau.team3.meditrack.data.source.local.entities.IntakeStatus
import kr.ac.cau.team3.meditrack.data.source.local.entities.Medication
import kr.ac.cau.team3.meditrack.data.source.local.entities.MedicationIntakeLog
import kr.ac.cau.team3.meditrack.data.source.local.entities.MedicationScheduler
import kr.ac.cau.team3.meditrack.data.source.local.entities.TimeOfDay
import kr.ac.cau.team3.meditrack.data.source.local.entities.User
import kr.ac.cau.team3.meditrack.data.source.local.entities.Weekday
import java.sql.Timestamp
import java.time.LocalTime

class MeditrackViewModel(
    private val repo: MeditrackRepository
) : ViewModel() {
    // -------------------------------------------------------------
    // USERS
    // -------------------------------------------------------------
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    suspend fun createUser(name: String, password: String): Int {
        // Check if user exists first to avoid crash
        val existingUser = repo.getUserByName(name)
        if (existingUser != null) {
            return -1 // Or throw exception
        }

        val user = User(
            user_name = name,
            user_passwordhash = password.hashCode()
                .toString()/*hashed password is stored in database*/
        )
        val id = repo.createUser(user)
        _users.value = listOfNotNull(repo.getUserByName(name))
        return id
    }

    suspend fun loginUserByName(name: String, password: String): User? {
        val user = repo.getUserByName(name)
        if( user?.user_passwordhash == password.hashCode().toString()){
            return user
        }
        return null
    }


    // -------------------------------------------------------------
    // MEDICATIONS
    // -------------------------------------------------------------
    private val _medications = MutableStateFlow<List<Medication>>(emptyList())
    val medications: StateFlow<List<Medication>> = _medications

    suspend fun addMedication(
        medication_user_id: Int,
        medication_name: String,
        medication_category: String,
        medication_frequency: Frequency,
        medication_weekdays: List<Weekday>?,
        medication_interval: Int?
    ): Int {
        val med = Medication(
            medication_user_id = medication_user_id,
            medication_name = medication_name,
            medication_category = medication_category,
            medication_frequency = medication_frequency,
            medication_weekdays = medication_weekdays,
            medication_interval = medication_interval
        )
        val medId = repo.insertMedication(med).toInt()

        _medications.value = repo.getMedicationsForUser(medication_user_id)
        return medId
    }

    suspend fun loadMedicationsForUser(userId: Int): List<Medication> {
        val meds = repo.getMedicationsForUser(userId)
        _medications.value = meds
        return meds
    }
    suspend fun deleteMedicationById(medicationId: Int): Int {
        return repo.deleteMedicationById(medicationId)
    }

    // -------------------------------------------------------------
    // SCHEDULERS
    // -------------------------------------------------------------
    private val _schedules = MutableStateFlow<List<MedicationScheduler>>(emptyList())
    val schedules: StateFlow<List<MedicationScheduler>> = _schedules

    suspend fun addSchedule(
        medId: Int,
        time: LocalTime,
        timeOfDay: TimeOfDay,
        dosage: String
    ): Int {

        val scheduler = MedicationScheduler(
            ms_medication_id = medId,
            ms_scheduled_time = time,
            ms_time_of_day = timeOfDay,
            ms_dosage = dosage
        )

        val id = repo.insertScheduler(scheduler)
        return id
    }

    suspend fun loadSchedules(medId: Int): List<MedicationScheduler> {
        val list = repo.getSchedulersForMed(medId)
        _schedules.value = list
        return list
    }


    // -------------------------------------------------------------
    // INTAKE LOG
    // -------------------------------------------------------------
    suspend fun addLog(
        scheduleId: Int,
        scheduledTime: LocalTime,
        takenTime: Timestamp?,
        status: IntakeStatus
    ): Int {

        val log = MedicationIntakeLog(
            mil_ms_id = scheduleId,
            mil_scheduled_time = scheduledTime,
            mil_taken_time = takenTime,
            mil_status = status
        )

        return repo.logIntake(log)
    }

    suspend fun loadLogsForMed(medId: Int): List<MedicationIntakeLog> {
        return repo.getLogsForMed(medId)
    }
}