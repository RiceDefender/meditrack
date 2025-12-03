package kr.ac.cau.team3.meditrack.data.source.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kr.ac.cau.team3.meditrack.data.source.local.entities.Medication

/**
 * Data Access Object for the medication table.
 */
@Dao
interface MedicationDao {
    /*********************            OBSERVE               **********************/
    /**
     * Observes list of medications.
     *
     * @return all medications.
     */
    @Query("SELECT * FROM medication")
    fun observeAll(): Flow<List<Medication>>
    /**
     * Observes a single medication.
     *
     * @param medicationId the medication id.
     * @return the medication with medicationId.
     */
    @Query("SELECT * FROM medication WHERE medication_id = :medicationId")
    fun observeById(medicationId: Int): Flow<Medication>


    /*********************            SELECT                **********************/
    /**
     * Select all medications from the medications table.
     *
     * @return all medications.
     */
    @Query("SELECT * FROM medication")
    suspend fun getAll(): List<Medication>
    /**
     * Select a medication by id.
     *
     * @param medicationId the medication id.
     * @return the medication with medicationId.
     */
    @Query("SELECT * FROM medication WHERE medication_id = :medicationId")
    suspend fun getById(medicationId: Int): Medication?
    /**
     * Select a medication by medication name.
     *
     * @param medicationName the medication name.
     * @return the medication with medicationId.
     */
    @Query("SELECT * FROM medication WHERE medication_name = :medicationName")
    suspend fun getByName(medicationName: String): Medication?
    /**
     * Select medications by user.
     *
     * @param userId the medication name.
     * @return the medication with medicationId.
     */
    @Query("SELECT * FROM medication WHERE medication_user_id = :userId")
    suspend fun getMedicationsForUser(userId: Int): List<Medication>

    /*********************            UPDATE/INSERT                **********************/
    /**
     * Insert or update a medication in the database. If a medication already exists, replace it.
     *
     * @param medication the medication to be inserted or updated.
     */
    @Upsert
    suspend fun upsert(medication: Medication) : Long
    /**
     * Insert or update medications in the database. If a medication already exists, replace it.
     *
     * @param medications the medications to be inserted or updated.
     */
    @Upsert
    suspend fun upsertAll(medications: List<Medication>)


    /*********************            DELETE                **********************/
    /**
     * Delete a medication by id.
     *
     * @return the number of medications deleted. This should always be 1.
     */
    @Query("DELETE FROM medication WHERE medication_id = :medicationId")
    suspend fun deleteById(medicationId: Int): Int
    /**
     * Delete all medications.
     */
    @Query("DELETE FROM medication")
    suspend fun deleteAll()

    /************************* LAST ADDED ID  *****************************/
    @Query("SELECT last_insert_rowid()")
    suspend fun getLastId(): Int
}