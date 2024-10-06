package com.example.xbcad7319_vucadigital.Activites

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.xbcad7319_vucadigital.Fragments.CreateCustomerFragment
import com.example.xbcad7319_vucadigital.Fragments.CreateOpportunityFragment
import com.example.xbcad7319_vucadigital.Fragments.CreateProductFragment
import com.example.xbcad7319_vucadigital.Fragments.CreateTaskFragment
import com.example.xbcad7319_vucadigital.Fragments.CustomersFragment
import com.example.xbcad7319_vucadigital.Fragments.DashboardFragment
import com.example.xbcad7319_vucadigital.Fragments.OpportunitiesFragment
import com.example.xbcad7319_vucadigital.Fragments.ProductsFragment
import com.example.xbcad7319_vucadigital.Fragments.TasksFragment
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.databinding.ActivityDashboardBinding
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadFragment(DashboardFragment(), false)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    loadFragment(DashboardFragment(), true)
                    true
                }
                R.id.navigation_account -> {
                    loadFragment(CustomersFragment(), true)
                    true
                }
                R.id.navigation_tasks -> {
                    loadFragment(TasksFragment(), true)
                    true
                }
                R.id.navigation_opportunities -> {
                    loadFragment(OpportunitiesFragment(), true)
                    true
                }
                else -> false
            }
        }

    }

    private fun loadFragment(fragment: Fragment, addToBackStack: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()

        when (fragment) {
            is CustomersFragment -> {
                binding.plusBtn.visibility = View.VISIBLE
                binding.plusBtn.setOnClickListener {
                    loadFragment(CreateCustomerFragment(), true)
                    binding.bottomNavigation.visibility = View.GONE
                }
            }
            is TasksFragment -> {
                binding.plusBtn.visibility = View.VISIBLE
                binding.plusBtn.setOnClickListener {
                    loadFragment(CreateTaskFragment(), true)
                    binding.bottomNavigation.visibility = View.GONE
                }
            }
            is OpportunitiesFragment -> {
                binding.plusBtn.visibility = View.VISIBLE
                binding.plusBtn.setOnClickListener {
                    loadFragment(CreateOpportunityFragment(), true)
                    binding.bottomNavigation.visibility = View.GONE
                }
            }
            is ProductsFragment -> {
                binding.plusBtn.visibility = View.VISIBLE
                binding.plusBtn.setOnClickListener {
                    loadFragment(CreateProductFragment(), true)
                    binding.bottomNavigation.visibility = View.GONE
                }
            }
            else -> {
                binding.plusBtn.visibility = View.GONE
                binding.plusBtn.setOnClickListener(null)
            }
        }
    }


}

