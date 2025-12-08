package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.myapplication.R
import java.io.File


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
    }

    private fun openCamera() {
        val photoFile = File.createTempFile("profile_image", ".jpg", cacheDir)
        photoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)

        takePhotoLauncher.launch(photoUri)
    }
}
