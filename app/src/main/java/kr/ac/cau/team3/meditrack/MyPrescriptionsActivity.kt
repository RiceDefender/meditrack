package kr.ac.cau.team3.meditrack

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.ac.cau.team3.meditrack.data.source.local.database.MeditrackDatabase
import kr.ac.cau.team3.meditrack.data.source.local.entities.Frequency
import kr.ac.cau.team3.meditrack.data.source.local.entities.Medication
import kr.ac.cau.team3.meditrack.data.source.local.entities.MedicationScheduler
import kr.ac.cau.team3.meditrack.viewmodel.GenericViewModelFactory
import kr.ac.cau.team3.meditrack.viewmodel.MeditrackViewModel
import java.util.Locale
import java.time.LocalTime // Assuming this is used for MedicationScheduler

class MyPrescriptionsActivity : AppCompatActivity() {

    private lateinit var repository: MeditrackRepository
    private val vm: MeditrackViewModel by viewModels {
        GenericViewModelFactory { MeditrackViewModel(repository) }
    }

    private var userId: Int = -1
    private var userName: String? = null
    // Changed to lateinit var as it's now essential for dynamic loading
    private lateinit var medicationListContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_myprescriptions)

        // linking to database
        repository = MeditrackRepository(
            MeditrackDatabase.getDatabase(this)
        )
        // Ensure userId and userName are fetched
        userId = intent.getIntExtra("USER_ID", -1)
        userName = intent.getStringExtra("USER_NAME")

        // Find the new dynamic container
        medicationListContainer = findViewById(R.id.medication_list_container)

        val buttonGoBack = findViewById<ImageView>(R.id.backButton)
        val buttonAddNew = findViewById<Button>(R.id.button2)

        // Go Back Button
        buttonGoBack.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.putExtra("USER_ID", userId)
            intent.putExtra("USER_NAME", userName)
            startActivity(intent)
            finish()
        }

        // Add New Prescription Button
        buttonAddNew.setOnClickListener {
            startNewPrescriptionActivity(medicationId = null)
        }

        // Load and display medications
        loadAndDisplayMedications()

        // *** CRITICAL CHANGE: Static listeners removed ***
        // The original code's static listeners (edit1, deleteFirst, etc.)
        // are now handled dynamically within createMedicationBlock.
    }

    /**
     * Fetches all medications for the current user and builds the dynamic UI.
     */
    private fun loadAndDisplayMedications() {
        if (userId == -1) return

        lifecycleScope.launch {
            try {
                val medications = vm.loadMedicationsForUser(userId)

                // Clear the container before adding new views
                medicationListContainer.removeAllViews()

                if (medications.isEmpty()) {
                    displayNoMedicationsMessage()
                } else {
                    medications.forEach { medication ->
                        // Load schedules for detailed view
                        val schedules = vm.loadSchedules(medication.medication_id)
                        createMedicationBlock(medication, schedules)
                    }
                }
            } catch (e: Exception) {
                Log.e("MyPrescriptionsActivity", "Error loading medications: ${e.message}", e)
                Toast.makeText(this@MyPrescriptionsActivity, "Failed to load prescriptions.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayNoMedicationsMessage() {
        val noMedicationText = TextView(this).apply {
            text = "You haven't added any prescriptions yet."
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 50.dpToPx(), 0, 50.dpToPx())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        medicationListContainer.addView(noMedicationText)
    }

    /**
     * Creates a single dynamic medication block by inflating item_prescription.xml.
     */
    private fun createMedicationBlock(medication: Medication, schedules: List<MedicationScheduler>) {
        try {
            // 1. Inflate the item_prescription.xml template
            val blockView = layoutInflater.inflate(R.layout.item_prescription, medicationListContainer, false)

            // 2. Find and populate views
            // CRITICAL: These IDs must match item_prescription.xml
            val nameMedicine: TextView = blockView.findViewById(R.id.nameMedicine)
            val detailsFrequency: TextView = blockView.findViewById(R.id.details_frequency)
            val editButton: ImageView = blockView.findViewById(R.id.editMedicineButton)
            val deleteButton: ImageView = blockView.findViewById(R.id.deleteMedicineButton)
            val detailContentLayout: LinearLayout = blockView.findViewById(R.id.detailContentLayout)

            // 3. Set data
            nameMedicine.text = medication.medication_name
            detailsFrequency.text = formatMedicationDetails(medication)

            // 4. Set listeners
            editButton.setOnClickListener {
                startNewPrescriptionActivity(medication.medication_id)
            }
            deleteButton.setOnClickListener {
                showDeleteConfirmationDialog(medication)
            }

            // 5. Populate dynamic schedule times
            populateScheduleDetails(detailContentLayout, schedules)

            // 6. Add the completed view to the main container
            medicationListContainer.addView(blockView)

        } catch (e: Exception) {
            Log.e("MyPrescriptionsActivity", "FATAL: Layout inflation failed. Check item_prescription.xml IDs and existence.", e)
            Toast.makeText(this, "Layout creation failed. See logcat.", Toast.LENGTH_LONG).show()
        }
    }

    // --- Helper function to display frequency details ---
    private fun formatMedicationDetails(medication: Medication): String {
        return when (medication.medication_frequency) {
            Frequency.Daily -> "Daily: ${medication.medication_category}"
            Frequency.Weekly -> {
                val weekdays = medication.medication_weekdays?.joinToString(", ") { it.toString().substring(0, 3) } ?: "N/A"
                "Weekly: $weekdays"
            }
            Frequency.Monthly -> {
                val intervalText = medication.medication_interval?.let { "Every $it month(s)" } ?: "Monthly"
                "$intervalText: ${medication.medication_category}"
            }
            Frequency.Yearly -> {
                val intervalText = medication.medication_interval?.let { "Every $it year(s)" } ?: "Yearly"
                "$intervalText: ${medication.medication_category}"
            }
        }
    }

    // --- Helper function to add schedule details (checkmarks/times) ---
    private fun populateScheduleDetails(layout: LinearLayout, schedules: List<MedicationScheduler>) {
        val sortedSchedules = schedules.sortedBy {
            // Handle optional LocalTime property gracefully if needed
            it.ms_scheduled_time // Assuming ms_scheduled_time is LocalTime
        }

        sortedSchedules.forEach { schedule ->
            val checkmark = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    25.dpToPx(),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = 10.dpToPx()
                }
                setImageResource(R.drawable.check_logo)
                setPadding(5.dpToPx())
            }

            val timeText = TextView(this).apply {
                val time: LocalTime = schedule.ms_scheduled_time
                val formattedTime = String.format(Locale.getDefault(), "%d:%02d", time.hour, time.minute)

                text = "${formattedTime} (${schedule.ms_dosage})"
                textSize = 11f
                setTextColor(Color.BLACK)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = 6.dpToPx()
                    marginEnd = 12.dpToPx()
                }
            }
            layout.addView(checkmark)
            layout.addView(timeText)
        }

        if (schedules.isEmpty()) {
            val emptyText = TextView(this).apply {
                text = "No intake times set."
                textSize = 11f
                setTextColor(Color.DKGRAY)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = 10.dpToPx()
                }
            }
            layout.addView(emptyText)
        }
    }


    private fun startNewPrescriptionActivity(medicationId: Int?) {
        val intent = Intent(this, NewPrescriptionActivity::class.java)
        intent.putExtra("USER_ID", userId)
        intent.putExtra("USER_NAME", userName)

        if (medicationId != null) {
            intent.putExtra("MEDICATION_ID", medicationId)
        }
        startActivity(intent)
    }

    private fun showDeleteConfirmationDialog(medication: Medication) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Prescription")
        builder.setMessage("Are you sure you want to delete ${medication.medication_name}?")

        // Changed button from Negative to Positive for Yes
        builder.setPositiveButton("Yes") { dialog, _ ->
            lifecycleScope.launch {
                try {
                    val deletedCount = vm.deleteMedicationById(medication.medication_id)
                    if (deletedCount > 0) {
                        Toast.makeText(this@MyPrescriptionsActivity, "${medication.medication_name} deleted.", Toast.LENGTH_SHORT).show()
                        loadAndDisplayMedications()
                    } else {
                        Toast.makeText(this@MyPrescriptionsActivity, "Failed to delete ${medication.medication_name}.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("MyPrescriptionsActivity", "Delete failed (DB error): ${e.message}", e)
                    Toast.makeText(this@MyPrescriptionsActivity, "Error deleting: Check ViewModel/Repository.", Toast.LENGTH_LONG).show()
                }
            }
            dialog.dismiss()
        }

        // Changed button from Positive to Negative for No
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    // **Helper function to convert DP to Pixels**
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }
}