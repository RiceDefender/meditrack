package kr.ac.cau.team3.meditrack.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kr.ac.cau.team3.meditrack.util.NotificationHelper

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val msId = intent.getIntExtra("MS_ID", -1)
        val scheduledHour = intent.getIntExtra("SCHEDULED_TIME_HOUR", -1)
        val scheduledMinute = intent.getIntExtra("SCHEDULED_TIME_MINUTE", -1)

        if (msId != -1) {
            Log.d("AlarmReceiver", "Received alarm for MS_ID: $msId")

            // Display the notification
            val notificationHelper = NotificationHelper(context)
            notificationHelper.sendMedicationNotification(
                msId,
                scheduledHour,
                scheduledMinute
            )

            // Schedule the next alarm immediately (for recurrence)
            // You will implement this later, but for now, we rely on the main activity
            // to reschedule daily, or you can add recurrence here.

            // Status Enforcement Logic (TBD)
            // You would run a check here (using coroutines) to update the status in the DB
            // if the user hasn't pressed the "Taken" button.
        }
    }
}