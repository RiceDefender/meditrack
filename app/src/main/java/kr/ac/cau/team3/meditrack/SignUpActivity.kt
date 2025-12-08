package kr.ac.cau.team3.meditrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.text.InputType
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.ac.cau.team3.meditrack.data.source.local.database.MeditrackDatabase
import kr.ac.cau.team3.meditrack.viewmodel.GenericViewModelFactory
import kr.ac.cau.team3.meditrack.viewmodel.MeditrackViewModel

class SignUpActivity : AppCompatActivity() {

    private lateinit var repository: MeditrackRepository
    private val vm: MeditrackViewModel by viewModels {
        GenericViewModelFactory { MeditrackViewModel(repository) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // linking to database
        repository = MeditrackRepository(
            MeditrackDatabase.getDatabase(this)
        )

        val buttonGoBack = findViewById<ImageView>(R.id.backButton)
        buttonGoBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        val usernameField = findViewById<EditText>(R.id.editTextUser)
        val buttonSignUp = findViewById<Button>(R.id.button2)
        //checkbox that shows the password
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val confirmPasswordEditText = findViewById<EditText>(R.id.editConfirmPassword)
        val checkBox = findViewById<CheckBox>(R.id.checkBoxShowPassword)

        buttonSignUp.setOnClickListener {
            val username = usernameField.text.toString()
            val pwd = passwordEditText.text.toString()
            val confirmPwd = confirmPasswordEditText.text.toString()

            if (pwd != confirmPwd) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val id = vm.createUser(username, pwd)

                Toast.makeText(
                    this@SignUpActivity,
                    "Account created! You can now login",
                    Toast.LENGTH_SHORT
                ).show()

                startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
            }
        }

        //this is to keep the font
        val pwdTypeface = passwordEditText.typeface
        val confirmPwdTypeface = confirmPasswordEditText.typeface

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

                confirmPasswordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

                confirmPasswordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }

            //this is to keep the font
            passwordEditText.typeface = pwdTypeface
            confirmPasswordEditText.typeface = confirmPwdTypeface

            passwordEditText.setSelection(passwordEditText.text.length)
            confirmPasswordEditText.setSelection(confirmPasswordEditText.text.length)
        }



    }
}
