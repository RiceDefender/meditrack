package kr.ac.cau.team3.meditrack

import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.ac.cau.team3.meditrack.databinding.ActivityMainBinding
import kr.ac.cau.team3.meditrack.data.source.local.database.MeditrackDatabase
import kr.ac.cau.team3.meditrack.data.source.local.entities.Frequency
import kr.ac.cau.team3.meditrack.data.source.local.entities.Medication
import kr.ac.cau.team3.meditrack.data.source.local.entities.User
import kr.ac.cau.team3.meditrack.data.source.local.entities.Weekday

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val db by lazy { MeditrackDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }

        // 2. Run the Test immediately on startup
        runDatabaseSmokeTest()
    }

    private fun runDatabaseSmokeTest() {
        lifecycleScope.launch(Dispatchers.IO) {
            Log.d("DB_TEST", "--- Starting Database Smoke Test ---")

            try {
                // STEP 1: Create & Insert User
                val testUser = User(
                    user_name = "TestUser_${System.currentTimeMillis()}", // Unique name
                    user_passwordhash = "secret_hash"
                )
                db.userDao().upsert(testUser)

                // Fetch back to get the auto-generated ID
                val savedUser = db.userDao().getByName(testUser.user_name)

                if (savedUser == null) {
                    Log.e("DB_TEST", "Failed to save user!")
                    return@launch
                }
                Log.d("DB_TEST", "User Saved: ID=${savedUser.user_id}, Name=${savedUser.user_name}")


                // STEP 2: Create & Insert Medication linked to User
                val testMed = Medication(
                    medication_user_id = savedUser.user_id,
                    medication_name = "Ibuprofen",
                    medication_category = "Painkiller",
                    medication_frequency = Frequency.Daily,
                    medication_weekdays = listOf(Weekday.Monday, Weekday.Wednesday),
                    medication_interval = null
                )
                db.medicationDao().upsert(testMed)
                Log.d("DB_TEST", "Medication inserted.")


                // STEP 3: Verify Data
                val userMeds = db.medicationDao().getMedicationsForUser(savedUser.user_id)
                Log.d("DB_TEST", "--- Fetching Results ---")
                Log.d("DB_TEST", "Found ${userMeds.size} medications for user ${savedUser.user_id}")

                userMeds.forEach { med ->
                    Log.d("DB_TEST", "READ: ${med.medication_name} (Freq: ${med.medication_frequency})")
                    Log.d("DB_TEST", "DAYS: ${med.medication_weekdays}")
                }

            } catch (e: Exception) {
                Log.e("DB_TEST", "CRASH DURING TEST", e)
            }

            Log.d("DB_TEST", "--- End of Test ---")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}