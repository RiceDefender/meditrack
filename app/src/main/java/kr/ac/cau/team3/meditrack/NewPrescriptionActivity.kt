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

class NewPrescriptionActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var photoUri: Uri

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

        //if "add a custom time" button checked, we show the time picker
        val checkCustomTime = findViewById<CheckBox>(R.id.checkCustomTime)
        val editHour2 = findViewById<EditText>(R.id.editHour)


        editHour2.visibility = if (checkCustomTime.isChecked) View.VISIBLE else View.GONE


        checkCustomTime.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                editHour2.visibility = View.VISIBLE
            } else {
                editHour2.visibility = View.GONE
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

        //making the radiobutton radiobuttoning
        val radioEveryDay = findViewById<RadioButton>(R.id.radioEveryDay)
        val radioEveryWeek = findViewById<RadioButton>(R.id.radioEveryWeek)
        val radioEveryMonth = findViewById<RadioButton>(R.id.radioEveryMonth)
        val radioEveryYear = findViewById<RadioButton>(R.id.radioEveryYear)

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


        val editHour = findViewById<EditText>(R.id.editHour)
        editHour.isFocusable = false
        editHour.isClickable = true
        editHour.setOnClickListener {
            showTimePicker(editHour)
        }
    }

    //time picker
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
}
