package kr.ac.cau.team3.meditrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.text.InputType

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonSignIn = findViewById<Button>(R.id.button2)
        buttonSignIn.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }

        //button to go to signup page
        val signUp = findViewById<TextView>(R.id.textView5)
        signUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }


        //checkbox that shows the password
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
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


    }
}
