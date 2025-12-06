package kr.ac.cau.team3.meditrack

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.util.Calendar
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.View
import android.widget.AdapterView
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.ac.cau.team3.meditrack.data.source.local.database.MeditrackDatabase
import kr.ac.cau.team3.meditrack.data.source.local.entities.Frequency
import kr.ac.cau.team3.meditrack.data.source.local.entities.TimeOfDay
import kr.ac.cau.team3.meditrack.data.source.local.entities.Weekday
import kr.ac.cau.team3.meditrack.viewmodel.GenericViewModelFactory
import kr.ac.cau.team3.meditrack.viewmodel.MeditrackViewModel
import java.time.LocalTime
import kotlin.getValue
import android.util.Log // Added for logging

class NewPrescriptionActivity : AppCompatActivity() {

    private lateinit var repository: MeditrackRepository
    private val vm: MeditrackViewModel by viewModels {
        GenericViewModelFactory { MeditrackViewModel(repository) }
    }

    private lateinit var profileImage: ImageView
    private lateinit var photoUri: Uri

    private var userId: Int = -1
    private var userName: String? = null

    private lateinit var radioEveryDay: RadioButton
    private lateinit var radioEveryWeek: RadioButton
    private lateinit var radioEveryMonth: RadioButton
    private lateinit var radioEveryYear: RadioButton
    private lateinit var weekdaysLayout: LinearLayout

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            profileImage.setImageURI(photoUri)
        }
    }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                openCamera()
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                profileImage.setImageURI(it)
            }
        }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_prescription)

        repository = MeditrackRepository(
            MeditrackDatabase.getDatabase(this)
        )
        userId = intent.getIntExtra("USER_ID", -1)
        userName = intent.getStringExtra("USER_NAME")

        if (userId == -1) {
            Toast.makeText(this, "Error: User not logged in.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // --- UI Initialization ---
        profileImage = findViewById(R.id.profileImage)
        val addPhotoButton = findViewById<ImageView>(R.id.addPhotoButton)
        val importPhotoButton = findViewById<ImageView>(R.id.importPhoto)
        val buttonGoBack = findViewById<ImageView>(R.id.backButton)

        addPhotoButton.setOnClickListener {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        importPhotoButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        buttonGoBack.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.putExtra("USER_ID", userId)
            intent.putExtra("USER_NAME", userName)
            startActivity(intent)
            finish()
        }

        // --- Date Range Switch ---
        val editStartDate = findViewById<EditText>(R.id.editStartDate)
        val editEndDate = findViewById<EditText>(R.id.editEndDate)
        val switch1 = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switch1)

        fun updateDateFields(isChecked: Boolean) {
            val visibility = if (isChecked) View.GONE else View.VISIBLE
            editStartDate.visibility = visibility
            editEndDate.visibility = visibility
            editStartDate.isEnabled = !isChecked
            editEndDate.isEnabled = !isChecked
        }

        updateDateFields(switch1.isChecked)

        switch1.setOnCheckedChangeListener { _, isChecked ->
            updateDateFields(isChecked)
        }

        editStartDate.setOnClickListener { showDatePicker(it as EditText) }
        editEndDate.setOnClickListener { showDatePicker(it as EditText) }

        // --- Time Checkboxes and Custom Time ---
        val checkCustomTime = findViewById<CheckBox>(R.id.checkCustomTime)
        val editHour = findViewById<EditText>(R.id.editHour)

        editHour.visibility = if (checkCustomTime.isChecked) View.VISIBLE else View.GONE
        checkCustomTime.setOnCheckedChangeListener { _, isChecked ->
            editHour.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        editHour.isFocusable = false
        editHour.isClickable = true
        editHour.setOnClickListener { showTimePicker(editHour) }

        // --- Spinners ---
        val spinner1 = findViewById<Spinner>(R.id.spinnerMedicineType)
        val options1 = listOf("Tablet/ Pill", "Syrup", "Drops", "Capsule")
        val adapter1 = ArrayAdapter(this, R.layout.spinner_item, options1)
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner1.adapter = adapter1

        val spinner2 = findViewById<Spinner>(R.id.spinnerUnit)
        val options2 = listOf("mg", "ml", "drops", "capsules")
        val adapter2 = ArrayAdapter(this, R.layout.spinner_item, options2)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner2.adapter = adapter2

        // --- Frequency Radio Buttons ---
        radioEveryDay = findViewById(R.id.radioEveryDay)
        radioEveryWeek = findViewById(R.id.radioEveryWeek)
        radioEveryMonth = findViewById(R.id.radioEveryMonth)
        radioEveryYear = findViewById(R.id.radioEveryYear)
        weekdaysLayout = findViewById(R.id.weekdaysLayout)

        // Refactored Radio Button Click Logic
        val radioGroupListener = View.OnClickListener { view ->
            radioEveryDay.isChecked = (view.id == R.id.radioEveryDay)
            radioEveryWeek.isChecked = (view.id == R.id.radioEveryWeek)
            radioEveryMonth.isChecked = (view.id == R.id.radioEveryMonth)
            radioEveryYear.isChecked = (view.id == R.id.radioEveryYear)

            weekdaysLayout.visibility = if (view.id == R.id.radioEveryWeek) View.VISIBLE else View.GONE
            // If you have hidden EditTexts for interval (e.g., inputEveryMonth),
            // you would toggle their visibility here based on the selected frequency.
        }

        radioEveryDay.setOnClickListener(radioGroupListener)
        radioEveryWeek.setOnClickListener(radioGroupListener)
        radioEveryMonth.setOnClickListener(radioGroupListener)
        radioEveryYear.setOnClickListener(radioGroupListener)

        // --- Save Button ---
        val saveButton = findViewById<ImageButton>(R.id.confirmButton)
        saveButton.setOnClickListener {
            saveMedication()
        }
    }

    private fun saveMedication() {
        val name = findViewById<EditText>(R.id.editMedicineName).text.toString()
        val category = findViewById<Spinner>(R.id.spinnerMedicineType).selectedItem.toString()
        val dosageUnit = findViewById<Spinner>(R.id.spinnerUnit).selectedItem.toString()
        val dosage = findViewById<EditText>(R.id.editDosage).text.toString() + " " + dosageUnit

        val frequency = getSelectedFrequency()
        val weekdays: List<Weekday>? = if (frequency == Frequency.Weekly) getSelectedWeekdays() else null
        val interval = getSelectedInterval(frequency)

        // Basic validation
        if (name.isBlank() || findViewById<EditText>(R.id.editDosage).text.isBlank()) {
            Toast.makeText(this, "Please fill in medication name and dosage.", Toast.LENGTH_SHORT).show()
            return
        }
        if (frequency == Frequency.Weekly && weekdays.isNullOrEmpty()) {
            Toast.makeText(this, "Please select at least one day for weekly frequency.", Toast.LENGTH_SHORT).show()
            return
        }

        val times = getSelectedTimes()
        if (times.isEmpty()) {
            Toast.makeText(this, "Please select at least one intake time.", Toast.LENGTH_SHORT).show()
            return
        }


        lifecycleScope.launch {
            try {
                // Insert medication
                val medId = vm.addMedication(
                    medication_user_id = userId,
                    medication_name = name,
                    medication_category = category,
                    medication_frequency = frequency,
                    medication_weekdays = weekdays,
                    medication_interval = interval
                )

                times.forEach { (time, timeOfDay) ->
                    vm.addSchedule(
                        medId = medId,
                        time = time,
                        timeOfDay = timeOfDay,
                        dosage = dosage
                    )
                }

                Toast.makeText(this@NewPrescriptionActivity, "Prescription saved!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@NewPrescriptionActivity, MyPrescriptionsActivity::class.java)
                intent.putExtra("USER_ID", userId)
                intent.putExtra("USER_NAME", userName)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Log.e("NewPrescription", "Error saving medication: ${e.message}", e)
                Toast.makeText(this@NewPrescriptionActivity, "Error saving prescription.", Toast.LENGTH_LONG).show()
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun showTimePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                editText.setText(String.format("%02d:%02d", selectedHour, selectedMinute))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePicker.show()
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                editText.setText("$year/${month + 1}/$day")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun openCamera() {
        val photoFile = File.createTempFile("profile_image", ".jpg", cacheDir)
        photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
        takePhotoLauncher.launch(photoUri)
    }

    private fun getSelectedWeekdays(): List<Weekday>? {
        val days = mutableListOf<Weekday>()

        if (findViewById<CheckBox>(R.id.checkMon).isChecked)    days.add(Weekday.Monday)
        if (findViewById<CheckBox>(R.id.checkTue).isChecked)   days.add(Weekday.Tuesday)
        if (findViewById<CheckBox>(R.id.checkWed).isChecked) days.add(Weekday.Wednesday)
        if (findViewById<CheckBox>(R.id.checkThu).isChecked)  days.add(Weekday.Thursday)
        if (findViewById<CheckBox>(R.id.checkFri).isChecked)    days.add(Weekday.Friday)
        if (findViewById<CheckBox>(R.id.checkSat).isChecked)  days.add(Weekday.Saturday)
        if (findViewById<CheckBox>(R.id.checkSun).isChecked)    days.add(Weekday.Sunday)

        return if (days.isEmpty()) null else days
    }

    private fun getSelectedFrequency(): Frequency {
        return when {
            radioEveryDay.isChecked -> Frequency.Daily
            radioEveryWeek.isChecked -> Frequency.Weekly
            radioEveryMonth.isChecked -> Frequency.Monthly
            radioEveryYear.isChecked -> Frequency.Yearly
            else -> Frequency.Daily // Default
        }
    }

    private fun getSelectedInterval(freq: Frequency): Int? {
        // This relies on you having EditTexts named inputEveryWeek, inputEveryMonth, inputEveryYear
        // in your layout, linked to the corresponding frequency selection.
        return when (freq) {
            Frequency.Weekly -> findViewById<EditText>(R.id.inputEveryWeek)?.text.toString().toIntOrNull()
            Frequency.Monthly -> findViewById<EditText>(R.id.inputEveryMonth)?.text.toString().toIntOrNull()
            Frequency.Yearly -> findViewById<EditText>(R.id.inputEveryYear)?.text.toString().toIntOrNull()
            else -> null
        }
    }

    private fun getSelectedTimes(): List<Pair<LocalTime, TimeOfDay>> {
        val times = mutableListOf<Pair<LocalTime, TimeOfDay>>()
        val checMorning = findViewById<CheckBox>(R.id.checkMorning)
        val checkAfternoon = findViewById<CheckBox>(R.id.checkAfternoon)
        val checkEvening = findViewById<CheckBox>(R.id.checkEvening)
        val checkCustomTime = findViewById<CheckBox>(R.id.checkCustomTime)
        val editHour = findViewById<EditText>(R.id.editHour)

        if (checMorning.isChecked) {
            times.add(LocalTime.of(8, 0) to TimeOfDay.Morning)
        }
        if (checkAfternoon.isChecked) {
            times.add(LocalTime.of(13, 0) to TimeOfDay.Afternoon)
        }
        if (checkEvening.isChecked) {
            times.add(LocalTime.of(19, 0) to TimeOfDay.Evening)
        }
        if (checkCustomTime.isChecked) {
            val customTimeString = editHour.text.toString()
            try {
                if (customTimeString.matches(Regex("\\d{2}:\\d{2}"))) {
                    val customTime = LocalTime.parse(customTimeString)
                    times.add(customTime to TimeOfDay.Custom)
                }
            } catch (e: Exception) {
                // Ignore if time is malformed, but a Toast might be better for the user
                Log.w("NewPrescription", "Invalid custom time format: $customTimeString")
            }
        }
        return times
    }
}