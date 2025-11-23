package kr.ac.cau.team3.meditrack.data.source.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kr.ac.cau.team3.meditrack.data.source.local.entities.MedicationScheduler

/**
 * Data Access Object for the MedicationScheduler table.
 */
@Dao
interface MedicationSchedulerDao {
    /*********************            OBSERVING                **********************/
    /**
     * Observes list of MedicationSchedulers.
     * @return all MedicationSchedulers.
     */
    @Query("SELECT * FROM medication_scheduler")
    fun observeAll(): Flow<List<MedicationScheduler>>
    /**
     * Observes a single MedicationScheduler.
     * @param MedicationSchedulerId the MedicationScheduler id.
     * @return the MedicationScheduler with MedicationSchedulerId.
     */
    @Query("SELECT * FROM medication_scheduler WHERE ms_id = :MedicationSchedulerId")
    fun observeById(MedicationSchedulerId: Int): Flow<MedicationScheduler>


    /*********************            FINDING                **********************/
    /**
     * Select all MedicationSchedulers from the MedicationSchedulers table.
     * @return all MedicationSchedulers.
     */
    @Query("SELECT * FROM medication_scheduler")
    suspend fun getAll(): List<MedicationScheduler>
    /**
     * Select a MedicationScheduler by id.
     * @param MedicationSchedulerId the MedicationScheduler id.
     * @return the MedicationScheduler with MedicationSchedulerId.
     */
    @Query("SELECT * FROM medication_scheduler WHERE ms_id = :MedicationSchedulerId")
    suspend fun getById(MedicationSchedulerId: Int): MedicationScheduler?

    @Query("SELECT * FROM medication_scheduler WHERE ms_medication_id = :medId")
    suspend fun getSchedules(medId: Int): List<MedicationScheduler>

    /*********************            UPDATE/INSERT                **********************/
    /**
     * Insert or update a MedicationScheduler in the database. If a MedicationScheduler already exists, replace it.
     *
     * @param MedicationScheduler the MedicationScheduler to be inserted or updated.
     */
    @Upsert
    suspend fun upsert(MedicationScheduler: MedicationScheduler)
    /**
     * Insert or update MedicationSchedulers in the database. If a MedicationScheduler already exists, replace it.
     *
     * @param MedicationSchedulers the MedicationSchedulers to be inserted or updated.
     */
    @Upsert
    suspend fun upsertAll(MedicationSchedulers: List<MedicationScheduler>)


    /*********************            DELETE                **********************/
    /**
     * Delete a MedicationScheduler by id.
     *
     * @return the number of MedicationSchedulers deleted. This should always be 1.
     */
    @Query("DELETE FROM medication_scheduler WHERE ms_id = :MedicationSchedulerId")
    suspend fun deleteById(MedicationSchedulerId: Int): Int
    /**
     * Delete all MedicationSchedulers.
     */
    @Query("DELETE FROM medication_scheduler")
    suspend fun deleteAll()
}