package kr.ac.cau.team3.meditrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.text.InputType
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.ac.cau.team3.meditrack.data.source.local.database.MeditrackDatabase
import kr.ac.cau.team3.meditrack.viewmodel.GenericViewModelFactory
import kr.ac.cau.team3.meditrack.viewmodel.MeditrackViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var repository: MeditrackRepository
    private val vm: MeditrackViewModel by viewModels {
        GenericViewModelFactory { MeditrackViewModel(repository) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // linking to database
        repository = MeditrackRepository(
            MeditrackDatabase.getDatabase(this)
        )

        val usernameField = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val buttonSignIn = findViewById<Button>(R.id.button2)

        buttonSignIn.setOnClickListener {
            val name = usernameField.text.toString()
            val password = passwordEditText.text.toString()

            lifecycleScope.launch {
                val user = vm.loginUserByName(name, password)

                if (user != null) {
                    // Send userId to next activity
                    val intent = Intent(this@MainActivity, WelcomeActivity::class.java)
                    intent.putExtra("USER_ID", user.user_id)
                    intent.putExtra("USER_Name", user.user_name)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Invalid username or password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


        //checkbox that shows the password
        val checkBox = findViewById<CheckBox>(R.id.checkBoxShowPassword)
        val originalTypeface = passwordEditText.typeface

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                passwordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }

            passwordEditText.typeface = originalTypeface

            passwordEditText.setSelection(passwordEditText.text.length)
        }

        //button to go to signup page
        val signUp = findViewById<TextView>(R.id.textView5)
        signUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}
