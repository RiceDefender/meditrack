package kr.ac.cau.team3.meditrack.data.source.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kr.ac.cau.team3.meditrack.data.source.local.entities.MedicationIntakeLog

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


    /*********************            UPDATE/INSERT                **********************/
    /**
     * Insert or update a MedicationIntakeLog in the database. If a MedicationIntakeLog already exists, replace it.
     *
     * @param MedicationIntakeLog the MedicationIntakeLog to be inserted or updated.
     */
    @Upsert
    suspend fun upsert(MedicationIntakeLog: MedicationIntakeLog)
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