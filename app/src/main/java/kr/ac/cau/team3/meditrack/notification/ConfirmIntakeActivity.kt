package kr.ac.cau.team3.meditrack

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kr.ac.cau.team3.meditrack.data.source.local.database.MeditrackDatabase
import kr.ac.cau.team3.meditrack.viewmodel.MeditrackViewModel
import java.sql.Timestamp
import java.time.LocalTime

class ConfirmIntakeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val msId = intent.getIntExtra("MS_ID", -1)
        val hour = intent.getIntExtra("HOUR", 0)
        val minute = intent.getIntExtra("MINUTE", 0)

        if (msId == -1) {
            finish()
            return
        }

        // Show popup
        AlertDialog.Builder(this)
            .setTitle("Confirm Intake")
            .setMessage("Confirm that you have taken your medicine scheduled at $hour:${String.format("%02d", minute)} ?")
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    val db = MeditrackDatabase.getDatabase(this@ConfirmIntakeActivity)
                    val repo = kr.ac.cau.team3.meditrack.MeditrackRepository(db)

                    // Log intake
                    repo.logIntakeTaken(
                        msId,
                        LocalTime.of(hour, minute)
                    )

                    // Cancel notification
                    val nm = getSystemService(NotificationManager::class.java)
                    nm.cancel(msId)

                    Toast.makeText(this@ConfirmIntakeActivity, "Intake logged!", Toast.LENGTH_LONG).show()
                    // Notify UI to refresh
                    val updateIntent = Intent("UPDATE_UI")
                    sendBroadcast(updateIntent)


                    finish()
                }
            }
            .setNegativeButton("No") { _, _ ->
                Toast.makeText(this, "Intake canceled.", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setCancelable(false)
            .show()
    }
}
