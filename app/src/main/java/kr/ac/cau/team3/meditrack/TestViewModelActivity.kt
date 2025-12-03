package kr.ac.cau.team3.meditrack

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.ac.cau.team3.meditrack.data.source.local.database.MeditrackDatabase
import kr.ac.cau.team3.meditrack.data.source.local.entities.*
import kr.ac.cau.team3.meditrack.MeditrackViewModel
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
        lifecycleScope.launch {

            Log.d("TEST", "---- RUNNING UNIFIED VIEWMODEL TEST ----")

            // 1) USER
            val userId = viewModel.createUser("Bob", "pass123")
            Log.d("TEST", "User created = $userId")

            // 2) MEDICATION
            val medId = viewModel.addMedication(
                userId,
                "Vitamin C",
                "Supplement",
                Frequency.Daily,
                null,
                null,
            )
            Log.d("TEST", "Medication created = $medId")

            // 3) SCHEDULER
            val scheduleId = viewModel.addSchedule(
                medId,
                time = LocalTime.of(8, 0),
                timeOfDay = TimeOfDay.Morning,
                dosage = "1 pill"
            )
            Log.d("TEST", "Schedule created = $scheduleId")

            // 4) LOG
            val logId = viewModel.addLog(
                scheduleId = scheduleId,
                scheduledTime = LocalTime.of(8, 0),
                takenTime = null,
                status = IntakeStatus.MISSED
            )
            Log.d("TEST", "Log created = $logId")

            // 5) LOAD BACK
            Log.d("TEST", "Medications = ${viewModel.loadMedicationsForUser(userId)}")
            Log.d("TEST", "Schedules = ${viewModel.loadSchedules(medId)}")
            Log.d("TEST", "Logs = ${viewModel.loadLogsForMed(medId)}")

            Log.d("TEST", "--- TEST COMPLETED! ---")
        }
    }
}