package kr.ac.cau.team3.meditrack.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import kr.ac.cau.team3.meditrack.broadcast.AlarmReceiver
import java.util.Calendar
import java.time.LocalTime

class NotificationScheduler(private val context: Context) {

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedules a notification to fire at a specific time today.
     * @param msId The MedicationScheduler ID (used as a unique request code).
     * @param scheduledTime The LocalTime object for the schedule.
     */
    fun scheduleNotification(msId: Int, scheduledTime: LocalTime) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Check if the app is allowed to schedule exact alarms
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.e("Scheduler", "Permission to schedule exact alarms denied. Cannot set alarm for MS_ID $msId.")
                // You may want to notify the user that they need to grant this permission
                return
            }
        }

        // Calculate the trigger time in milliseconds
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, scheduledTime.hour)
            set(Calendar.MINUTE, scheduledTime.minute)
            set(Calendar.SECOND, 0)
        }

        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Create the Intent and PendingIntent
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            // Pass the schedule ID so the receiver knows which medication it is
            putExtra("MS_ID", msId)
            putExtra("SCHEDULED_TIME_HOUR", scheduledTime.hour)
            putExtra("SCHEDULED_TIME_MINUTE", scheduledTime.minute)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            msId, // Use msId as unique request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set the repeating alarm
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        Log.d("Scheduler", "Alarm set for MS_ID $msId at ${scheduledTime}")
    }

    // Function to cancel an alarm (e.g., if a medication is deleted)
    fun cancelNotification(msId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            msId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            Log.d("Scheduler", "Alarm canceled for MS_ID $msId")
        }
    }
}