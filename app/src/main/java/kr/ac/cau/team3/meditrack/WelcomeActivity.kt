package kr.ac.cau.team3.meditrack

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.launch
import kr.ac.cau.team3.meditrack.data.source.local.database.MeditrackDatabase
import kr.ac.cau.team3.meditrack.data.source.local.entities.Frequency
import kr.ac.cau.team3.meditrack.data.source.local.entities.Medication
import kr.ac.cau.team3.meditrack.data.source.local.entities.MedicationScheduler
import kr.ac.cau.team3.meditrack.data.source.local.entities.IntakeStatus
import kr.ac.cau.team3.meditrack.data.source.local.entities.Weekday
import kr.ac.cau.team3.meditrack.viewmodel.GenericViewModelFactory
import kr.ac.cau.team3.meditrack.viewmodel.MeditrackViewModel
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.pm.PackageManager
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import android.util.TypedValue
import kr.ac.cau.team3.meditrack.util.NotificationScheduler
import android.provider.Settings
import android.net.Uri
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

class WelcomeActivity : AppCompatActivity() {

    private lateinit var repository: MeditrackRepository
    private val vm: MeditrackViewModel by viewModels {
        GenericViewModelFactory { MeditrackViewModel(repository) }
    }

    private var userId: Int = -1
    private var userName: String? = null
    private lateinit var scheduleContainer: LinearLayout // Container for dynamic medication blocks
    private lateinit var scheduler: NotificationScheduler
    // Define the request launcher as a class member:
    private val requestPermissionLauncher = this.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Notification permission denied. Reminders will not appear.", Toast.LENGTH_LONG).show()
        }
    }

    private val uiRefreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loadAndDisplayTodaySchedule() //for bloc colors
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)


        // Linking to database
        repository = MeditrackRepository(
            MeditrackDatabase.getDatabase(this)
        )

        // Get user data
        userId = intent.getIntExtra("USER_ID", -1)
        userName = intent.getStringExtra("USER_NAME")

        // Check if userId is valid before proceeding
        if (userId == -1) {
            Toast.makeText(this, "Error: User not logged in.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Initialize the scheduler:
        scheduler = NotificationScheduler(applicationContext)
        if (!scheduler.alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                ("package:$packageName").toUri()
            )
            // You might use registerForActivityResult or just startActivity here
            startActivity(intent)
            Toast.makeText(this, "Please allow 'Schedule exact alarms' access for reminders to work.", Toast.LENGTH_LONG).show()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // UI Initialization
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val menuIcon = findViewById<ImageView>(R.id.imageView)
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        scheduleContainer = findViewById(R.id.schedule_container)

        // New Prescription Button
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            val intent = Intent(this, NewPrescriptionActivity::class.java)
            intent.putExtra("USER_ID", userId)
            intent.putExtra("USER_NAME", userName)
            startActivity(intent)
        }

        // Drawer Menu setup
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        val headerView = navigationView.getHeaderView(0)
        val closeButton = headerView.findViewById<ImageView>(R.id.closeButton)
        closeButton.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_two -> { // New Prescription
                    val intent = Intent(this, NewPrescriptionActivity::class.java)
                    intent.putExtra("USER_ID", userId)
                    intent.putExtra("USER_NAME", userName)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.END)
                    true
                }
                R.id.nav_one -> { // My Prescriptions
                    val intent = Intent(this, MyPrescriptionsActivity::class.java)
                    intent.putExtra("USER_ID", userId)
                    intent.putExtra("USER_NAME", userName)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.END)
                    true
                }
                else -> false
            }
        }

        // Calendar setup
        setupCalendarDisplay()

        // Load and display today's medication schedule
        loadAndDisplayTodaySchedule()
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        registerReceiver(
            uiRefreshReceiver,
            android.content.IntentFilter("UPDATE_UI"),
            Context.RECEIVER_NOT_EXPORTED
        )

    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(uiRefreshReceiver)
    }


    // --- NEW HELPER FUNCTION FOR DATE CONVERSION ---
    /**
     * Converts a Timestamp to the "YYYY-MM-DD" date string required for mil_date.
     */
    private fun timestampToMilDate(timestamp: Timestamp): String {
        // Use Java Time API for modern, reliable conversion
        return timestamp.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

    /**
     * Gets today's date in the "YYYY-MM-DD" string format.
     */
    private fun getTodayDateString(): String {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

    // --- Existing setupCalendarDisplay function remains the same ---
    private fun setupCalendarDisplay() {
        // Calendar UI logic remains the same
        val days = listOf(
            findViewById<LinearLayout>(R.id.day1),
            findViewById<LinearLayout>(R.id.day2),
            findViewById<LinearLayout>(R.id.day3),
            findViewById<LinearLayout>(R.id.day4),
            findViewById<LinearLayout>(R.id.day5),
            findViewById<LinearLayout>(R.id.day6),
            findViewById<LinearLayout>(R.id.day7)
        )

        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        // Set calendar to Monday of the current week
        val delta = if (dayOfWeek == Calendar.SUNDAY) -6 else Calendar.MONDAY - dayOfWeek
        calendar.add(Calendar.DAY_OF_MONTH, delta)

        val sdfDayName = SimpleDateFormat("EEE", Locale.ENGLISH)
        val sdfDayNumber = SimpleDateFormat("d", Locale.ENGLISH)

        for (dayLayout in days) {
            val dayName = TextView(this).apply {
                text = sdfDayName.format(calendar.time)
                textSize = 12f
                gravity = Gravity.CENTER
            }

            val dayNumber = TextView(this).apply {
                text = sdfDayNumber.format(calendar.time)
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
            }

            dayLayout.orientation = LinearLayout.VERTICAL
            dayLayout.gravity = Gravity.CENTER

            if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                dayLayout.setBackgroundResource(R.drawable.circle_black)
                dayName.setTextColor(Color.WHITE)
                dayNumber.setTextColor(Color.WHITE)
            } else {
                dayLayout.setBackgroundResource(R.drawable.circle_white)
                dayName.setTextColor(Color.BLACK)
                dayNumber.setTextColor(Color.BLACK)
            }

            dayLayout.removeAllViews()
            dayLayout.addView(dayName)
            dayLayout.addView(dayNumber)
            calendar.add(Calendar.DAY_OF_MONTH, 1) // Move to the next day
        }
    }


    @SuppressLint("SetTextI18n")
    private fun loadAndDisplayTodaySchedule() {
        if (userId == -1) return

        lifecycleScope.launch {
            // Determine today's Weekday enum for filtering
            val todayCalendar = Calendar.getInstance()
            val todayDayOfWeek = when (todayCalendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> Weekday.Monday
                Calendar.TUESDAY -> Weekday.Tuesday
                Calendar.WEDNESDAY -> Weekday.Wednesday
                Calendar.THURSDAY -> Weekday.Thursday
                Calendar.FRIDAY -> Weekday.Friday
                Calendar.SATURDAY -> Weekday.Saturday
                Calendar.SUNDAY -> Weekday.Sunday
                else -> null
            }
            if (todayDayOfWeek == null) return@launch

            // Clear previous blocks
            scheduleContainer.removeAllViews()

            val medications = vm.loadMedicationsForUser(userId)
            // Clear old alarms before setting new ones for today
            medications.forEach { medication ->
                val schedules = vm.loadSchedules(medication.medication_id)
                schedules.forEach { schedule ->
                    scheduler.cancelNotification(schedule.ms_id)
                }
            }
            val todayScheduleList = mutableListOf<Pair<Medication, MedicationScheduler>>()

            for (medication in medications) {
                // filtering logic remains the same
                val isScheduledToday = when (medication.medication_frequency) {
                    Frequency.Daily -> true
                    Frequency.Weekly -> medication.medication_weekdays?.contains(todayDayOfWeek) == true
                    Frequency.Monthly -> true
                    Frequency.Yearly -> true
                    else -> false
                }

                if (isScheduledToday) {
                    val schedules = vm.loadSchedules(medication.medication_id)
                    schedules.forEach { schedule ->
                        todayScheduleList.add(Pair(medication, schedule))
                        scheduler.scheduleNotification(schedule.ms_id, schedule.ms_scheduled_time)
                    }
                }
            }

            // Sort all schedules by time
            val sortedScheduleList = todayScheduleList.sortedBy {
                // Convert LocalTime to Comparable
                it.second.ms_scheduled_time
            }

            for ((medication, schedule) in sortedScheduleList) {
                // Pass the schedule to the block creation function
                createMedicationBlock(medication, schedule)
            }

            // Display a message if no medications are scheduled for today
            if (scheduleContainer.childCount == 0) {
                val noMedicationText = TextView(this@WelcomeActivity).apply {
                    text = "No medications scheduled for today."
                    textSize = 16f
                    gravity = Gravity.CENTER
                    setPadding(0, 50.dpToPx(), 0, 50.dpToPx())
                }
                scheduleContainer.addView(noMedicationText)
            }
        }
    }

    private fun createMedicationBlock(medication: Medication, schedule: MedicationScheduler) {
        val blockLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8.dpToPx(), 0, 8.dpToPx())
            }
            orientation = LinearLayout.HORIZONTAL
            // Set padding
            setPadding(16.dpToPx(), 16.dpToPx(), 16.dpToPx(), 16.dpToPx())
        }

        // --- Persistent Status Check ---
        lifecycleScope.launch {
            val todayDate = getTodayDateString()
            // Check if this specific schedule has a log entry for today
            val isTaken = vm.isIntakeLogged(schedule.ms_id, todayDate)

            // Determine initial background color
            if (isTaken) {
                blockLayout.setBackgroundResource(R.drawable.bloc_taken_bg) // Green/Taken
            } else {
                // Check if the scheduled time has passed (Missed vs. Pending)
                val scheduledTime: LocalTime? = LocalTime.of(
                    schedule.ms_scheduled_time.hour,
                    schedule.ms_scheduled_time.minute
                )
                val isMissed = scheduledTime?.isBefore(LocalTime.now())

                if (isMissed == true) {
                    blockLayout.setBackgroundResource(R.drawable.bloc_late_bg) // Red/Missed (You need to define this drawable)
                } else {
                    blockLayout.setBackgroundResource(R.drawable.bloc_normal_bg) // Gray/Pending
                }
            }
        }

        // Left side: Time
        val timeTextView = TextView(this).apply {
            // Correctly format LocalTime to string
            val formattedTime = String.format(Locale.getDefault(), "%d:%02d", schedule.ms_scheduled_time.hour, schedule.ms_scheduled_time.minute)

            text = formattedTime
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.3f)
            gravity = Gravity.START
        }

        // Center: Medication Details
        val detailsLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f).apply {
                leftMargin = 16.dpToPx()
            }
        }
        val nameTextView = TextView(this).apply {
            text = medication.medication_name
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
        }
        val dosageTextView = TextView(this).apply {
            text = "${schedule.ms_dosage} - ${medication.medication_category}"
            textSize = 14f
            setTextColor(Color.GRAY)
        }

        detailsLayout.addView(nameTextView)
        detailsLayout.addView(dosageTextView)

        blockLayout.addView(timeTextView)
        blockLayout.addView(detailsLayout)

        // Set click listener with AlertDialog
        blockLayout.setOnClickListener {
            // Only show confirmation if not already taken
            lifecycleScope.launch {
                val isTaken = vm.isIntakeLogged(schedule.ms_id, getTodayDateString())
                if (!isTaken) {
                    showConfirmationDialog(blockLayout, schedule)
                } else {
                    Toast.makeText(this@WelcomeActivity, "Medication already logged as taken today.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        scheduleContainer.addView(blockLayout)
    }

    private fun showConfirmationDialog(blockView: View, schedule: MedicationScheduler) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Intake")
        builder.setMessage("Confirm that you have taken your medicine (${schedule.ms_dosage}) scheduled for ${String.format(Locale.getDefault(), "%d:%02d", schedule.ms_scheduled_time.hour, schedule.ms_scheduled_time.minute)}?")

        // "Yes" Button (Mark as Taken)
        builder.setPositiveButton("Yes") { dialog, _ ->
            lifecycleScope.launch {
                val takenTime = Timestamp(System.currentTimeMillis())
                val todayDate = getTodayDateString() // Get today's date string

                // Log the intake as TAKEN, including the date string
                val logId = vm.addLog(
                    scheduleId = schedule.ms_id,
                    scheduledTime = schedule.ms_scheduled_time,
                    takenTime = takenTime,
                    status = IntakeStatus.TAKEN,
                )

                if (logId > 0) {
                    // Update the block's UI to green
                    blockView.setBackgroundResource(R.drawable.bloc_taken_bg)
                    Toast.makeText(this@WelcomeActivity, "Intake logged successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@WelcomeActivity, "Failed to log intake.", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }

        // "No" Button (Keep as Pending/Missed)
        builder.setNegativeButton("No") { dialog, _ ->
            Toast.makeText(this@WelcomeActivity, "Intake pending.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        builder.show()
    }

    // --- Extension function to convert DP to Pixels ---
    private fun Int.dpToPx(): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
}