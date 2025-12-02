package kr.ac.cau.team3.meditrack

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MyPrescriptionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_myprescriptions)

        val buttonGoBack = findViewById<ImageView>(R.id.backButton)
        buttonGoBack.setOnClickListener {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }

        //add a new prescription button
        val button = findViewById<Button>(R.id.button2)
        button.setOnClickListener {
            val intent = Intent(this, NewPrescriptionActivity::class.java)
            startActivity(intent)
        }


        //buttons for the editing, it's the same layout as
        //NewPrescriptionActivity but with everything already pre-answered
        val edit1 = findViewById<ImageView>(R.id.editMedicine1)
        edit1.setOnClickListener {
            val intent = Intent(this, NewPrescriptionActivity::class.java)
            startActivity(intent)
        }

        val edit2 = findViewById<ImageView>(R.id.editMedicine2)
        edit2.setOnClickListener {
            val intent = Intent(this, NewPrescriptionActivity::class.java)
            startActivity(intent)
        }


        //delete alertdialog buttons
        val deleteFirst = findViewById<ImageView>(R.id.deleteMedicine1)

        deleteFirst.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Are you sure ?")
            builder.setMessage("Do you want to delete this Prescription?")
            builder.setNegativeButton("Yes") { dialog, _ ->
                dialog.dismiss()
            }

            builder.setPositiveButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }

        val deleteSecond = findViewById<ImageView>(R.id.deleteMedicine2)

        deleteSecond.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Are you sure ?")
            builder.setMessage("Do you want to delete this Prescription?")
            builder.setNegativeButton("Yes") { dialog, _ ->
                dialog.dismiss()
            }

            builder.setPositiveButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }
    }
}
