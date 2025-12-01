package kr.ac.cau.team3.meditrack

import kr.ac.cau.team3.meditrack.data.source.local.database.MeditrackDatabase
import kr.ac.cau.team3.meditrack.data.source.local.entities.Medication
import kr.ac.cau.team3.meditrack.data.source.local.entities.User
import kr.ac.cau.team3.meditrack.data.source.local.entities.MedicationScheduler
import kr.ac.cau.team3.meditrack.data.source.local.entities.MedicationIntakeLog

class MeditrackRepository(
    private val db: MeditrackDatabase
) {

    // --- Users ---
    suspend fun insertUser(user: User) = db.userDao().upsert(user)
    suspend fun getUserByName(name: String) = db.userDao().getByName(name)

    // --- Medications ---
    suspend fun insertMedication(medication: Medication) = db.medicationDao().upsert(medication)
    suspend fun getMedicationsForUser(userId: Int) =
        db.medicationDao().getMedicationsForUser(userId)

    // --- Schedulers ---
    suspend fun insertScheduler(scheduler: MedicationScheduler) =
        db.medicationSchedulerDao().upsert(scheduler)

    suspend fun getSchedulersForMed(medId: Int) =
        db.medicationSchedulerDao().getSchedules(medId)

    // --- Intake Log ---
    suspend fun logIntake(log: MedicationIntakeLog) =
        db.medicationIntakeLogDao().upsert(log)

    suspend fun getLogsForMed(medId: Int) =
        db.medicationIntakeLogDao().getIntakesForMedication(medId)
}
