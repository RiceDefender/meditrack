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

class NewPrescriptionActivity : AppCompatActivity() {

    private lateinit var repository: MeditrackRepository
    private val vm: MeditrackViewModel by viewModels {
        GenericViewModelFactory { MeditrackViewModel(repository) }
    }

    private lateinit var profileImage: ImageView
    private lateinit var photoUri: Uri

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            profileImage.setImageURI(photoUri)
        }
    }

    // open camera function
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

        // linking to database
        repository = MeditrackRepository(
            MeditrackDatabase.getDatabase(this)
        )
        val userId = intent.getIntExtra("USER_ID", -1)


        //importing the photo
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

        //button go back
        buttonGoBack.setOnClickListener {
            startActivity(Intent(this, WelcomeActivity::class.java))
        }

        //if switch ON, hide calendar picker
        val editStartDate = findViewById<EditText>(R.id.editStartDate)
        val editEndDate = findViewById<EditText>(R.id.editEndDate)

        val switch1 = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switch1)

        fun updateDateFields(isChecked: Boolean) {
            if (isChecked) {
                editStartDate.visibility = View.GONE
                editEndDate.visibility = View.GONE
                editStartDate.isEnabled = false
                editEndDate.isEnabled = false
            } else {
                editStartDate.visibility = View.VISIBLE
                editEndDate.visibility = View.VISIBLE
                editStartDate.isEnabled = true
                editEndDate.isEnabled = true
            }
        }


        updateDateFields(switch1.isChecked)


        switch1.setOnCheckedChangeListener { _, isChecked ->
            updateDateFields(isChecked)
        }


        editStartDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    editStartDate.setText("$year/${month + 1}/$day")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        editEndDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    editEndDate.setText("$year/${month + 1}/$day")
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        // checkbox for the times of the day multiple can be chosen
        val checMorning = findViewById<CheckBox>(R.id.checkMorning)
        val checkAfternoon = findViewById<CheckBox>(R.id.checkAfternoon)
        val checkEvening = findViewById<CheckBox>(R.id.checkEvening)
        //if "add a custom time" button checked, we show the time picker
        val checkCustomTime = findViewById<CheckBox>(R.id.checkCustomTime)
        val editHour = findViewById<EditText>(R.id.editHour)

        editHour.visibility = if (checkCustomTime.isChecked) View.VISIBLE else View.GONE
        checkCustomTime.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                editHour.visibility = View.VISIBLE
            } else {
                editHour.visibility = View.GONE
            }
        }

        //spinners
        val spinner1 = findViewById<Spinner>(R.id.spinnerMedicineType)
        val options1 = listOf("Tablet/ Pill", "Syrup", "Drops", "Capsule")
        val adapter1 = ArrayAdapter(this, R.layout.spinner_item, options1)
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner1.adapter = adapter1
        spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {}
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val spinner2 = findViewById<Spinner>(R.id.spinnerUnit)
        val options2 = listOf("mg", "ml", "drops", "capsules")
        val adapter2 = ArrayAdapter(this, R.layout.spinner_item, options2)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner2.adapter = adapter2
        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {}
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // This is for the RadioButton to work normally, meaning we can only
        // choose a single one of them
        val radioEveryDay = findViewById<RadioButton>(R.id.radioEveryDay)
        val radioEveryWeek = findViewById<RadioButton>(R.id.radioEveryWeek)
        val radioEveryMonth = findViewById<RadioButton>(R.id.radioEveryMonth)
        val radioEveryYear = findViewById<RadioButton>(R.id.radioEveryYear)
        var selectFrequency = radioEveryDay// we need to get the frequency first

        radioEveryDay.setOnClickListener {
            radioEveryDay.isChecked = true
            radioEveryWeek.isChecked = false
            radioEveryMonth.isChecked = false
            radioEveryYear.isChecked = false
        }

        radioEveryWeek.setOnClickListener {
            radioEveryDay.isChecked = false
            radioEveryWeek.isChecked = true
            radioEveryMonth.isChecked = false
            radioEveryYear.isChecked = false
        }

        radioEveryMonth.setOnClickListener {
            radioEveryDay.isChecked = false
            radioEveryWeek.isChecked = false
            radioEveryMonth.isChecked = true
            radioEveryYear.isChecked = false
        }

        radioEveryYear.setOnClickListener {
            radioEveryDay.isChecked = false
            radioEveryWeek.isChecked = false
            radioEveryMonth.isChecked = false
            radioEveryYear.isChecked = true
        }

        // weekday thing
        val weekdaysLayout = findViewById<LinearLayout>(R.id.weekdaysLayout)
        radioEveryWeek.setOnClickListener {
            radioEveryDay.isChecked = false
            radioEveryWeek.isChecked = true
            radioEveryMonth.isChecked = false
            radioEveryYear.isChecked = false

            weekdaysLayout.visibility = View.VISIBLE   // SHOW weekdays
        }
        radioEveryDay.setOnClickListener {
            radioEveryDay.isChecked = true
            radioEveryWeek.isChecked = false
            radioEveryMonth.isChecked = false
            radioEveryYear.isChecked = false

            weekdaysLayout.visibility = View.GONE      // HIDE weekdays
        }
        radioEveryMonth.setOnClickListener {
            weekdaysLayout.visibility = View.GONE
        }
        radioEveryYear.setOnClickListener {
            weekdaysLayout.visibility = View.GONE
        }

        //time picker

        editHour.isFocusable = false
        editHour.isClickable = true
        editHour.setOnClickListener {
            showTimePicker(editHour)
        }

        val saveButton = findViewById<ImageButton>(R.id.confirmButton)
        saveButton.setOnClickListener {

            val name = findViewById<EditText>(R.id.editMedicineName).text.toString()
            val category = spinner1.selectedItem.toString()
            val frequency = getSelectedFrequency()
            val dosage = findViewById<EditText>(R.id.editDosage).text.toString() + " " +
                    spinner2.selectedItem.toString()
            val weekdays : List<Weekday>? = getSelectedWeekdays()
            val interval = getSelectedInterval(frequency)
            if ( frequency == Frequency.Weekly && (weekdays == null || weekdays.size != interval)) {
                // Handle invalid input
                return@setOnClickListener
            }

            val times = mutableListOf<Pair<LocalTime, TimeOfDay>>()
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
                val customTime = LocalTime.parse(editHour.text.toString())
                times.add(customTime to TimeOfDay.Custom)
            }

            lifecycleScope.launch {

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

                startActivity(Intent(this@NewPrescriptionActivity, MyPrescriptionsActivity::class.java))
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


    //function to open the camera
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
            findViewById<RadioButton>(R.id.radioEveryDay).isChecked -> Frequency.Daily
            findViewById<RadioButton>(R.id.radioEveryWeek).isChecked -> Frequency.Weekly
            findViewById<RadioButton>(R.id.radioEveryMonth).isChecked -> Frequency.Monthly
            findViewById<RadioButton>(R.id.radioEveryYear).isChecked -> Frequency.Yearly
            else -> Frequency.Daily
        }
    }

    private fun getSelectedInterval(freq: Frequency): Int? {
        return when {
            freq == Frequency.Daily -> null
            freq == Frequency.Weekly -> findViewById<EditText>(R.id.inputEveryWeek).text.toString().toIntOrNull()
            freq == Frequency.Monthly -> findViewById<EditText>(R.id.inputEveryMonth).text.toString().toIntOrNull()
            freq == Frequency.Yearly -> findViewById<EditText>(R.id.inputEveryYear).text.toString().toIntOrNull()
            else -> null
        }
    }


}
