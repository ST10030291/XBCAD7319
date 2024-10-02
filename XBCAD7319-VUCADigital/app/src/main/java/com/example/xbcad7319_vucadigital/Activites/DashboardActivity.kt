package com.example.xbcad7319_vucadigital.Activites

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.xbcad7319_vucadigital.Fragments.CustomersFragment
import com.example.xbcad7319_vucadigital.Fragments.DashboardFragment
import com.example.xbcad7319_vucadigital.Fragments.OpportunitiesFragment
import com.example.xbcad7319_vucadigital.Fragments.TasksFragment
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // View binding setup
        binding = ActivityDashboardBinding.inflate(layoutInflater)
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
                    loadFragment(CustomersFragment())
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

    }


    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()
    }
}