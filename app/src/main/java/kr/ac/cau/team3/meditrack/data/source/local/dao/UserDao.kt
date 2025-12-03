package kr.ac.cau.team3.meditrack.data.source.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import kr.ac.cau.team3.meditrack.data.source.local.entities.User

/**
 * Data Access Object for the user table.
 */
@Dao
interface UserDao {
    /*********************            OBSERVING                **********************/
    /**
     * Observes list of users.
     * @return all users.
     */
    @Query("SELECT * FROM user")
    fun observeAll(): Flow<List<User>>
    /**
     * Observes a single user.
     * @param userId the user id.
     * @return the user with userId.
     */
    @Query("SELECT * FROM user WHERE user_id = :userId")
    fun observeById(userId: Int): Flow<User>


    /*********************            FINDING                **********************/
    /**
     * Select all users from the users table.
     * @return all users.
     */
    @Query("SELECT * FROM user")
    suspend fun getAll(): List<User>
    /**
     * Select a user by id.
     * @param userId the user id.
     * @return the user with userId.
     */
    @Query("SELECT * FROM user WHERE user_id = :userId")
    suspend fun getById(userId: Int): User?
    /**
     * Select a user by user name.
     * @param userName the user name.
     * @return the user with userId.
     */
    @Query("SELECT * FROM user WHERE user_name = :userName")
    suspend fun getByName(userName: String): User?


    /*********************            UPDATE/INSERT                **********************/
    /**
     * Insert or update a user in the database. If a user already exists, replace it.
     *
     * @param user the user to be inserted or updated.
     */
    @Upsert
    suspend fun upsert(user: User) : Long
    /**
     * Insert or update users in the database. If a user already exists, replace it.
     *
     * @param users the users to be inserted or updated.
     */
    @Upsert
    suspend fun upsertAll(users: List<User>)


    /*********************            DELETE                **********************/
    /**
     * Delete a user by id.
     *
     * @return the number of users deleted. This should always be 1.
     */
    @Query("DELETE FROM user WHERE user_id = :userId")
    suspend fun deleteById(userId: Int): Int
    /**
     * Delete all users.
     */
    @Query("DELETE FROM user")
    suspend fun deleteAll()
}