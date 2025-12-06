package kr.ac.cau.team3.meditrack.data.source.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kr.ac.cau.team3.meditrack.data.source.local.entities.MedicationIntakeLog
import kr.ac.cau.team3.meditrack.data.source.local.entities.MedicationScheduler
import java.time.LocalTime

/**
 * Data Access Object for the MedicationIntakeLog table.
 */
@Dao
interface MedicationIntakeLogDao {
    /*********************            OBSERVING                **********************/
    /**
     * Observes list of MedicationIntakeLogs.
     * @return all MedicationIntakeLogs.
     */
    @Query("SELECT * FROM medication_intake_log")
    fun observeAll(): Flow<List<MedicationIntakeLog>>
    /**
     * Observes a single MedicationIntakeLog.
     * @param MedicationIntakeLogId the MedicationIntakeLog id.
     * @return the MedicationIntakeLog with MedicationIntakeLogId.
     */
    @Query("SELECT * FROM medication_intake_log WHERE mil_id = :MedicationIntakeLogId")
    fun observeById(MedicationIntakeLogId: Int): Flow<MedicationIntakeLog>


    /*********************            FINDING                **********************/
    /**
     * Select all MedicationIntakeLogs from the MedicationIntakeLogs table.
     * @return all MedicationIntakeLogs.
     */
    @Query("SELECT * FROM medication_intake_log")
    suspend fun getAll(): List<MedicationIntakeLog>
    /**
     * Select a MedicationIntakeLog by id.
     * @param MedicationIntakeLogId the MedicationIntakeLog id.
     * @return the MedicationIntakeLog with MedicationIntakeLogId.
     */
    @Query("SELECT * FROM medication_intake_log WHERE mil_id = :MedicationIntakeLogId")
    suspend fun getById(MedicationIntakeLogId: Int): MedicationIntakeLog?

    /**
     * Select a MedicationIntakeLog by medication scheduler id.
     * @param MedicationSchedulerId the MedicationScheduler id.
     * @return the MedicationIntakeLog with MedicationIntakeLogId.
     */
    @Query("SELECT * FROM medication_intake_log WHERE mil_ms_id = :msId")
    suspend fun getIntakesForMedicationScheduler(msId: Int): List<MedicationIntakeLog>?

    @Query("""
    SELECT log.* FROM medication_intake_log AS log
    INNER JOIN medication_scheduler AS sched
    ON log.mil_ms_id = sched.ms_id
    WHERE sched.ms_medication_id = :medId """
    )
    suspend fun getIntakesForMedication(medId: Int): List<MedicationIntakeLog>

    /**
     * Checks if a specific schedule time for a medication was logged as taken on a given date.
     * Searches by schedule ID (mil_ms_id) AND the specific date (mil_date).
     * @param msId The ID of the MedicationScheduler.
     * @param date The date to check (e.g., "2025-12-06").
     * @return The MedicationIntakeLog entry (or null if not found).
     */
    @Query("SELECT * FROM medication_intake_log WHERE mil_ms_id = :msId AND mil_date = :date LIMIT 1")
    suspend fun getIntakeLogByScheduleAndDate(msId: Int, date: String): MedicationIntakeLog?

    /*********************            UPDATE/INSERT                **********************/
    /**
     * Insert or update a MedicationIntakeLog in the database. If a MedicationIntakeLog already exists, replace it.
     *
     * @param MedicationIntakeLog the MedicationIntakeLog to be inserted or updated.
     */
    @Upsert
    suspend fun upsert(MedicationIntakeLog: MedicationIntakeLog) : Long
    /**
     * Insert or update MedicationIntakeLogs in the database. If a MedicationIntakeLog already exists, replace it.
     *
     * @param MedicationIntakeLogs the MedicationIntakeLogs to be inserted or updated.
     */
    @Upsert
    suspend fun upsertAll(MedicationIntakeLogs: List<MedicationIntakeLog>)


    /*********************            DELETE                **********************/
    /**
     * Delete a MedicationIntakeLog by id.
     *
     * @return the number of MedicationIntakeLogs deleted. This should always be 1.
     */
    @Query("DELETE FROM medication_intake_log WHERE mil_id = :MedicationIntakeLogId")
    suspend fun deleteById(MedicationIntakeLogId: Int): Int
    /**
     * Delete all MedicationIntakeLogs.
     */
    @Query("DELETE FROM medication_intake_log")
    suspend fun deleteAll()

}