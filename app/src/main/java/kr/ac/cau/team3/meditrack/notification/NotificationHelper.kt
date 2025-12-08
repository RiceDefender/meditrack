package kr.ac.cau.team3.meditrack.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kr.ac.cau.team3.meditrack.ConfirmIntakeActivity
import kr.ac.cau.team3.meditrack.R
import kr.ac.cau.team3.meditrack.broadcast.NotificationActionReceiver // To be created next

class NotificationHelper(private val context: Context) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val CHANNEL_ID = "meditrack_reminders"
    private val CHANNEL_NAME = "Medication Reminders"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Channel for daily medication reminders"
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun sendMedicationNotification(msId: Int, hour: Int, minute: Int) {

        //Create the PendingIntent for the "Taken" action button
        val takenIntent = Intent(context, ConfirmIntakeActivity::class.java).apply {
            putExtra("MS_ID", msId)
            putExtra("HOUR", hour)
            putExtra("MINUTE", minute)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val takenPendingIntent = PendingIntent.getActivity(
            context,
            msId,
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        //Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("💊 Medication Reminder")
            .setContentText("It's time for your medication scheduled at $hour:${String.format("%02d", minute)}.")
            .setSmallIcon(R.drawable.new_prescription_logo) // Use a relevant drawable icon
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

            // Add the "Taken" action button
            .addAction(
                R.drawable.confirm_button, // Use a relevant check icon
                "Taken",
                takenPendingIntent
            )
            .build()

        // Display the notification
        notificationManager.notify(msId, notification) // Use msId as unique notification ID
    }
}