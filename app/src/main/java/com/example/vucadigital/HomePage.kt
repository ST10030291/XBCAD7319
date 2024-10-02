package com.example.vucadigital

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.vucadigital.databinding.ActivityHomePageBinding

class HomePage : AppCompatActivity() {

    private lateinit var binding: ActivityHomePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View binding setup
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()

        // Set padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set default fragment
        loadFragment(DashboardFragment())

        // Bottom navigation item selection
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    loadFragment(DashboardFragment())
                    true
                }
                R.id.navigation_account -> {
                    loadFragment(AccountFragment())
                    true
                }
                R.id.navigation_tasks -> {
                    loadFragment(TasksFragment())
                    true
                }
                R.id.navigation_opportunities -> {
                    loadFragment(OpportunitiesFragment())
                    true
                }
                else -> false
            }
        }

        // Floating Action Button click listener
        binding.fabPopupTray.setOnClickListener {
            showPopupMenu()
        }
    }

    private fun showPopupMenu() {
        val popup = Dialog(this)
        popup.setContentView(R.layout.custom_popup_layout)
        popup.setCancelable(true)
        popup.show()

        // Adjusting popup position
        val window = popup.window
        val layoutParams = window?.attributes
        layoutParams?.gravity = Gravity.BOTTOM
        layoutParams?.x = 0
        layoutParams?.y = 200
        window?.attributes = layoutParams
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }
}
