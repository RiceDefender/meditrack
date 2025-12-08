package kr.ac.cau.team3.meditrack

import kr.ac.cau.team3.meditrack.data.source.local.database.MeditrackDatabase
import kr.ac.cau.team3.meditrack.data.source.local.entities.IntakeStatus
import kr.ac.cau.team3.meditrack.data.source.local.entities.Medication
import kr.ac.cau.team3.meditrack.data.source.local.entities.User
import kr.ac.cau.team3.meditrack.data.source.local.entities.MedicationScheduler
import kr.ac.cau.team3.meditrack.data.source.local.entities.MedicationIntakeLog
import kr.ac.cau.team3.meditrack.viewmodel.timestampToMilDate
import java.sql.Timestamp
import java.time.LocalTime

class MeditrackRepository(
    private val db: MeditrackDatabase
) {

    // USERS
    suspend fun createUser(user : User): Int {
        val newIdLong = db.userDao().upsert(user)
        val newIdInt = newIdLong.toInt()

        return newIdInt
    }

    suspend fun getUserByName(name: String) = db.userDao().getByName(name)

    // MEDICATIONS
    suspend fun insertMedication(med: Medication): Int {
        val newIdLong = db.medicationDao().upsert(med)
        return newIdLong.toInt()
    }

    suspend fun getMedicationsForUser(userId: Int) =
        db.medicationDao().getMedicationsForUser(userId)

    suspend fun deleteMedicationById(medicationId: Int) =
        db.medicationDao().deleteById(medicationId)

    // SCHEDULERS
    suspend fun insertScheduler(sched: MedicationScheduler): Int {
        val newIdLong = db.medicationSchedulerDao().upsert(sched)
        return newIdLong.toInt()
    }

    suspend fun getSchedulersForMed(medId: Int) =
        db.medicationSchedulerDao().getSchedules(medId)

    suspend fun getSchedulerById(msId: Int) =
        db.medicationSchedulerDao().getById(msId)

    // LOGS
    suspend fun logIntake(log: MedicationIntakeLog): Int {
        val newIdLong = db.medicationIntakeLogDao().upsert(log)
        return newIdLong.toInt()
    }

    suspend fun getLogsForMed(medId: Int) =
        db.medicationIntakeLogDao().getIntakesForMedication(medId)

    suspend fun logIntakeTaken(msId: Int, scheduledTime: LocalTime): Long {
        val logEntry = MedicationIntakeLog(
            mil_ms_id = msId,
            mil_scheduled_time = scheduledTime,
            mil_taken_time = Timestamp(System.currentTimeMillis()),
            mil_status = IntakeStatus.TAKEN,
            mil_date = timestampToMilDate(Timestamp(System.currentTimeMillis())) // *** Saving the date ***
        )
        return db.medicationIntakeLogDao().upsert(logEntry)
    }

    suspend fun isIntakeLogged(msId: Int, date: String): Boolean {
        return db.medicationIntakeLogDao().getIntakeLogByScheduleAndDate(msId, date) != null
    }

}