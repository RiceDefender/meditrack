package kr.ac.cau.team3.meditrack

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File
import java.util.Calendar
import android.app.DatePickerDialog
import android.widget.ArrayAdapter
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
            } else {

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



        profileImage = findViewById(R.id.profileImage)
        val addPhotoButton = findViewById<ImageView>(R.id.addPhotoButton)

        addPhotoButton.setOnClickListener {

            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        val importPhotoButton = findViewById<ImageView>(R.id.importPhoto)

        importPhotoButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        val buttonGoBack = findViewById<ImageView>(R.id.backButton)
        buttonGoBack.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }

        val editStartDate = findViewById<EditText>(R.id.editStartDate)

        editStartDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)


            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->

                editStartDate.setText("$selectedYear/${selectedMonth + 1}/$selectedDay")
            }, year, month, day)

            datePicker.show()
        }

        val editEndDate = findViewById<EditText>(R.id.editEndDate)

        editEndDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)


            val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->

                editEndDate.setText("$selectedYear/${selectedMonth + 1}/$selectedDay")
            }, year, month, day)

            datePicker.show()
        }

        val spinner1 = findViewById<Spinner>(R.id.spinnerMedicineType)
        val options1 = listOf("Tablet/ Pill", "Syrup", "Drops", "Capsule")
        val adapter1 = ArrayAdapter(this, R.layout.spinner_item, options1)
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner1.adapter = adapter1
        spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selected = options1[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }


        val spinner2 = findViewById<Spinner>(R.id.spinnerUnit)
        val options2 = listOf("mg", "ml", "drops", "capsules")
        val adapter2 = ArrayAdapter(this, R.layout.spinner_item, options2)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner2.adapter = adapter2
        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selected = options2[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }



    }

    private fun openCamera() {
        val photoFile = File.createTempFile("profile_image", ".jpg", cacheDir)
        photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)

        takePhotoLauncher.launch(photoUri)
    }
}