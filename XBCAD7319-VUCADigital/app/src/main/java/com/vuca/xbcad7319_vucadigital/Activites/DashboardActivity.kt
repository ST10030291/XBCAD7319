package com.vuca.xbcad7319_vucadigital.Activites

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.vuca.xbcad7319_vucadigital.Fragments.CreateCustomerFragment
import com.vuca.xbcad7319_vucadigital.Fragments.CreateOpportunityFragment
import com.vuca.xbcad7319_vucadigital.Fragments.CreateProductFragment
import com.vuca.xbcad7319_vucadigital.Fragments.CreateTaskFragment
import com.vuca.xbcad7319_vucadigital.Fragments.CustomersFragment
import com.vuca.xbcad7319_vucadigital.Fragments.DashboardFragment
import com.vuca.xbcad7319_vucadigital.Fragments.OpportunitiesFragment
import com.vuca.xbcad7319_vucadigital.Fragments.ProductsFragment
import com.vuca.xbcad7319_vucadigital.Fragments.TasksFragment
import com.vuca.xbcad7319_vucadigital.R
import com.vuca.xbcad7319_vucadigital.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {
    lateinit var binding: ActivityDashboardBinding

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

    fun loadFragment(fragment: Fragment, addToBackStack: Boolean) {
        val transaction = supportFragmentManager.beginTransaction()

        transaction.setCustomAnimations(
            R.anim.fade_in,
            R.anim.fade_out,
            R.anim.fade_in,
            R.anim.fade_out
        )

        transaction.replace(binding.fragmentContainer.id, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()

        when (fragment) {
            is CustomersFragment -> {
                binding.plusBtn.visibility = View.VISIBLE
                binding.plusBtn.setOnClickListener {
                    val createCustomerFragment = CreateCustomerFragment().apply {
                        arguments = Bundle().apply {
                            putBoolean("isUpdateMode", false)
                        }
                    }
                    loadFragment(createCustomerFragment, true)
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

