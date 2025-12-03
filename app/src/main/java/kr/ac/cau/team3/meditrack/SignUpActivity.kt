package kr.ac.cau.team3.meditrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.text.InputType
class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val buttonGoBack = findViewById<ImageView>(R.id.backButton)
        buttonGoBack.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }


        //checkbox that shows the password
        val passwordEditText = findViewById<EditText>(R.id.editTextPassword)
        val confirmPasswordEditText = findViewById<EditText>(R.id.editConfirmPassword)
        val checkBox = findViewById<CheckBox>(R.id.checkBoxShowPassword)

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
