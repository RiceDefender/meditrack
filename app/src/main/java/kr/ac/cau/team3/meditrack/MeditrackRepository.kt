package kr.ac.cau.team3.meditrack

import kr.ac.cau.team3.meditrack.data.source.local.database.MeditrackDatabase
import kr.ac.cau.team3.meditrack.data.source.local.entities.Medication

class MeditrackRepository (val db: MeditrackDatabase) {

        suspend fun addMedication(m: Medication) =
            db.MedicationDao().upsert(m)

        suspend fun getAllForUser(id: Int) =
            db.MedicationDao().getMedicationsForUser(id)
}