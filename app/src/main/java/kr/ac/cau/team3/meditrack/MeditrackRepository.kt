package kr.ac.cau.team3.meditrack

import kr.ac.cau.team3.meditrack.data.source.local.database.MeditrackDatabase
import kr.ac.cau.team3.meditrack.data.source.local.entities.Medication
import kr.ac.cau.team3.meditrack.data.source.local.entities.User
import kr.ac.cau.team3.meditrack.data.source.local.entities.MedicationScheduler
import kr.ac.cau.team3.meditrack.data.source.local.entities.MedicationIntakeLog

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


    // SCHEDULERS
    suspend fun insertScheduler(sched: MedicationScheduler): Int {
        val newIdLong = db.medicationSchedulerDao().upsert(sched)
        return newIdLong.toInt()
    }

    suspend fun getSchedulersForMed(medId: Int) =
        db.medicationSchedulerDao().getSchedules(medId)


    // LOGS
    suspend fun logIntake(log: MedicationIntakeLog): Int {
        val newIdLong = db.medicationIntakeLogDao().upsert(log)
        return newIdLong.toInt()
    }

    suspend fun getLogsForMed(medId: Int) =
        db.medicationIntakeLogDao().getIntakesForMedication(medId)
}