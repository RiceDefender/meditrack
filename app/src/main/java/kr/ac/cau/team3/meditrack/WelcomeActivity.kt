package kr.ac.cau.team3.meditrack

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.view.View
import android.app.AlertDialog
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)




        //header stuff
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val menuIcon = findViewById<ImageView>(R.id.imageView)
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            val intent = Intent(this, NewPrescriptionActivity::class.java)
            startActivity(intent)
        }

        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        val headerView = navigationView.getHeaderView(0)
        val closeButton = headerView.findViewById<ImageView>(R.id.closeButton)
        closeButton.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
        }
        //menu button -> "new prescription" redirects to activity NewPrescription
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_two -> {
                    val intent = Intent(this, NewPrescriptionActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.END)
                    true
                }
                R.id.nav_one -> {
                    val intent = Intent(this, MyPrescriptionsActivity::class.java)
                    startActivity(intent)
                    drawerLayout.closeDrawer(GravityCompat.END)
                    true
                }
                else -> false
            }
        }

        //popup if press on a block
        //if Yes -> changes block color to green
        //if No -> keeps it gray until time passes and its too late = red
        val blocSecond = findViewById<View>(R.id.blocSecond)

        blocSecond.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Are you sure ?")
            builder.setMessage("Confirm that you have taken your medicine ?")
            builder.setNegativeButton("Yes") { dialog, _ ->
                dialog.dismiss()
            }

            builder.setPositiveButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }



        //Calendar
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

            //changing the color of the day of the week in black
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
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
    }
}
