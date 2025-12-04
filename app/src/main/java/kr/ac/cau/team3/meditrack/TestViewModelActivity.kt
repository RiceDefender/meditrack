package kr.ac.cau.team3.meditrack

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.ac.cau.team3.meditrack.data.source.local.database.MeditrackDatabase
import kr.ac.cau.team3.meditrack.data.source.local.entities.*
import kr.ac.cau.team3.meditrack.viewmodel.MeditrackViewModel
import java.time.LocalTime

class TestViewModelActivity : ComponentActivity() {

    private lateinit var repository: MeditrackRepository

    private val viewModel: MeditrackViewModel by viewModels {
        makeFactory { MeditrackViewModel(repository) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = MeditrackDatabase.getDatabase(this)
        repository = MeditrackRepository(db)
        runTests()
    }

    private fun <T : ViewModel> makeFactory(create: () -> T): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            override fun <T2 : ViewModel> create(modelClass: Class<T2>): T2 {
                return create() as T2
            }
        }

    private fun runTests() {
        lifecycleScope.launch(Dispatchers.IO) {
            Log.d("TEST", "================================================")
            Log.d("TEST", "====      STARTING COMPREHENSIVE TEST       ====")
            Log.d("TEST", "================================================")

            // ---------------------------------------------------------
            // SCENARIO 1: SIGN UP (New User "Alice")
            // ---------------------------------------------------------
            Log.d("TEST", "\n[SCENARIO 1] Sign Up 'Alice'")

            val aliceName = "Alice"
            val aliceId = viewModel.createUser(aliceName, "secret_pass")
            Log.d("TEST", " -> User Created: $aliceName (ID: $aliceId)")

            // Add Data for Alice
            val medId = viewModel.addMedication(
                aliceId, "Ibuprofen", "Painkiller", Frequency.Daily, null, null
            )
            Log.d("TEST", " -> Medication Added: Ibuprofen (ID: $medId)")

            val schedId = viewModel.addSchedule(
                medId, LocalTime.of(14, 0), TimeOfDay.Afternoon, "200mg"
            )
            Log.d("TEST", " -> Schedule Added: 2:00 PM (ID: $schedId)")

            Log.d("TEST", "[SCENARIO 1] Complete. Alice has data.")


            // ---------------------------------------------------------
            // SCENARIO 2: LOGIN (Existing User "Alice")
            // ---------------------------------------------------------
            Log.d("TEST", "\n[SCENARIO 2] Simulate Login for 'Alice'")

            // A. Attempt to find user by Name (Simulating entering username in login field)
            // You might need to expose `getByName` in your ViewModel.
            // If not available in VM, we check repository directly or assume you add a function `login(name, pass)`
            // For now, let's use the Repository to simulate the VM login check:
            val loginUser = repository.getUserByName(aliceName)

            if (loginUser != null) {
                Log.d("TEST", " -> LOGIN SUCCESS: Found user '${loginUser.user_name}' with ID ${loginUser.user_id}")

                // B. Load Dashboard Data for this user
                val loadedMeds = viewModel.loadMedicationsForUser(loginUser.user_id)
                Log.d("TEST", " -> Loading Dashboard...")

                if (loadedMeds.isNotEmpty()) {
                    Log.d("TEST", " -> Found ${loadedMeds.size} medications.")
                    val firstMed = loadedMeds[0]
                    Log.d("TEST", "    * Med 1: ${firstMed.medication_name} (Category: ${firstMed.medication_category})")

                    // C. Verify Schedules for this med
                    val loadedSchedules = viewModel.loadSchedules(firstMed.medication_id)
                    Log.d("TEST", "    * Schedules found: ${loadedSchedules.size}")
                    loadedSchedules.forEach { s ->
                        Log.d("TEST", "      - Time: ${s.ms_scheduled_time}, Dosage: ${s.ms_dosage}")
                    }
                } else {
                    Log.e("TEST", " -> ERROR: No medications found for Alice!")
                }

            } else {
                Log.e("TEST", " -> LOGIN FAILED: Could not find user $aliceName")
            }


            // ---------------------------------------------------------
            // SCENARIO 3: INVALID LOGIN (User "Bob" does not exist)
            // ---------------------------------------------------------
            Log.d("TEST", "\n[SCENARIO 3] Simulate Invalid Login for 'Bob'")
            val fakeUser = repository.getUserByName("Bob")

            if (fakeUser == null) {
                Log.d("TEST", " -> SUCCESS: User 'Bob' correctly not found.")
            } else {
                Log.e("TEST", " -> FAILURE: User 'Bob' should not exist!")
            }

            Log.d("TEST", "================================================")
            Log.d("TEST", "====           TEST SUITE FINISHED          ====")
            Log.d("TEST", "================================================")
        }
    }
}