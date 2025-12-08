package kr.ac.cau.team3.meditrack.broadcast

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.ac.cau.team3.meditrack.data.source.local.database.MeditrackDatabase
import kr.ac.cau.team3.meditrack.MeditrackRepository
import kr.ac.cau.team3.meditrack.viewmodel.MeditrackViewModel
import java.sql.Timestamp
import java.time.LocalTime

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "ACTION_TAKEN") {
            val msId = intent.getIntExtra("MS_ID", -1)

            if (msId != -1) {
                // Since this is a BroadcastReceiver, we must use a CoroutineScope manually
                // to execute database operations asynchronously.
                val scope = CoroutineScope(Dispatchers.IO)

                scope.launch {
                    val db = MeditrackDatabase.getDatabase(context)
                    val repository = MeditrackRepository(db)

                    // Fetch the scheduled time for the given msId
                    // This requires a new DAO/Repo function to get the schedule by ID
                    val scheduler = repository.getSchedulerById(msId)

                    if (scheduler != null) {
                        // Log the intake. The repository handles the TAKEN vs. LATE logic.
                        val logId = repository.logIntakeTaken(
                            scheduler.ms_id,
                            scheduler.ms_scheduled_time
                        )

                        // Cancel the notification after intake is logged
                        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.cancel(msId)

                        // Send broadcast to update UI
                        val updateIntent = Intent("UPDATE_UI")
                        context.sendBroadcast(updateIntent)


                        // Show confirmation on the main thread
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, "Intake logged! Thank you.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}