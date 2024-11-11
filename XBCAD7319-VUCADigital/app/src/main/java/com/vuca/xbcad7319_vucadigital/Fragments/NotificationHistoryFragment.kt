package com.vuca.xbcad7319_vucadigital.Fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.vuca.xbcad7319_vucadigital.Activites.DashboardActivity
import com.vuca.xbcad7319_vucadigital.Adapters.NotificationHistoryAdapter
import com.vuca.xbcad7319_vucadigital.R
import com.vuca.xbcad7319_vucadigital.databinding.FragmentNotificationHistoryBinding
import com.vuca.xbcad7319_vucadigital.db.SupabaseHelper
import com.vuca.xbcad7319_vucadigital.models.NotificationHistoryModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationHistoryFragment : Fragment() {
    private lateinit var binding: FragmentNotificationHistoryBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var notificationHistoryAdapter: NotificationHistoryAdapter
    private var notificationList: MutableList<NotificationHistoryModel> = mutableListOf()
    private lateinit var lastButtonClicked: Button

    private lateinit var filteredNotificationList: List<NotificationHistoryModel>
    private lateinit var tempNotifications : List<NotificationHistoryModel>
    private var sbHelper: SupabaseHelper = SupabaseHelper()

    private lateinit var visibleFilterBtn: Button
    private lateinit var hiddenFilterBtn: Button
    private lateinit var searchView: SearchView

    private lateinit var shimmerFrameLayout: ShimmerFrameLayout
    private lateinit var notFoundLayout: LinearLayout
    private lateinit var notFoundImg: ImageView
    private lateinit var notFoundMessage: TextView
    private lateinit var notFoundMessage2: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the ViewBinding object and return the root view
        binding = FragmentNotificationHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notFoundLayout = binding.notFoundLayout
        notFoundImg = binding.notFoundImg
        notFoundMessage = binding.notFoundMessage
        notFoundMessage2 = binding.notFoundMessage2

        recyclerView = binding.notificationHistoryRecyclerView
        searchView = binding.notificationHistorySearchView
        visibleFilterBtn = binding.visibleFilterBtn
        hiddenFilterBtn = binding.hiddenFilterBtn

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        notificationHistoryAdapter = NotificationHistoryAdapter(notificationList, ::changeNotificationState, ::deleteNotification)
        recyclerView.adapter = notificationHistoryAdapter

        Handler(Looper.getMainLooper()).postDelayed({
            shimmerFrameLayout = binding.shimmerTasks
            shimmerFrameLayout.stopShimmer()

            notFoundLayout.visibility = View.GONE
            shimmerFrameLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            setUpSearchView()

            // Set up the click listeners for each filter button
            setFilterButtonClickListener(visibleFilterBtn, true)
            setFilterButtonClickListener(hiddenFilterBtn, false)

            // Load notifications from Supabase
            loadNotifications()
        },2000)
    }

    private fun selectButton(selectedButton: Button) {
        visibleFilterBtn.isSelected = selectedButton == visibleFilterBtn
        hiddenFilterBtn.isSelected = selectedButton == hiddenFilterBtn
    }

    // StackOverflow post
    // Titled: How can I filter an ArrayList in Kotlin so I only have elements which match my condition?
    // Posted by: Nithinlal
    // Available at: https://stackoverflow.com/questions/44098709/how-can-i-filter-an-arraylist-in-kotlin-so-i-only-have-elements-which-match-my-c
    private fun setFilterButtonClickListener(button: Button, filterVisibility: Boolean?) {
        try {
            button.setOnClickListener {
                filteredNotificationList = when (filterVisibility) {
                    null -> {
                        tempNotifications
                    }
                    true -> {
                        tempNotifications.filter { it.visible == true }
                    }
                    false -> {
                        tempNotifications.filter { it.visible == false }
                    }
                }

                selectButton(button)
                notificationHistoryAdapter.updateNotifications(filteredNotificationList)
                lastButtonClicked = button

                notFoundLayout.visibility = View.GONE
                shimmerFrameLayout.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

                if(filteredNotificationList.isEmpty()){
                    notFoundMessage2.text = getString(R.string.try_choosing_another_filter)
                    notFoundLayout.visibility = View.VISIBLE
                    shimmerFrameLayout.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                }
            }
        }
        catch (e : Exception){
            Toast.makeText(context, "Empty History! No notifications available to filter.", Toast.LENGTH_SHORT).show()
        }
    }

    // Blog post
    // Title: SearchView in Android with Kotlin
    // Posted by: chaitanyamunje
    // Posted on:  28 July 2022
    // Available at: https://www.geeksforgeeks.org/searchview-in-android-with-kotlin/
    private fun setUpSearchView() {
        try{
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    query?.let { searchNotificationHistoryByCustomerName(it) }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let { searchNotificationHistoryByCustomerName(it) }
                    return true
                }
            })
        }
        catch (e : Exception){
            Toast.makeText(context, "Empty History! No notifications available to filter.", Toast.LENGTH_SHORT).show()
        }
    }

    // StackOverflow post
    // Titled: How can I filter an ArrayList in Kotlin so I only have elements which match my condition?
    // Posted by: Nithinlal
    // Available at: https://stackoverflow.com/questions/44098709/how-can-i-filter-an-arraylist-in-kotlin-so-i-only-have-elements-which-match-my-c
    private fun searchNotificationHistoryByCustomerName(query: String) {
        val queryLower = query.lowercase()

        filteredNotificationList = tempNotifications.filter { notification ->
            notification.customerName.lowercase().contains(queryLower)
        }

        notificationHistoryAdapter.updateNotifications(filteredNotificationList)

        notFoundLayout.visibility = View.GONE
        shimmerFrameLayout.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE

        // Show a toast message if filteredTasks is empty
        if (filteredNotificationList.isEmpty()) {
            notFoundMessage2.text = getString(R.string.try_entering_a_different_search_term)
            notFoundLayout.visibility = View.VISIBLE
            shimmerFrameLayout.visibility = View.GONE
            recyclerView.visibility = View.GONE
        }
    }

    // StackOverflow post
    // Title: How to use LifecycleScope to execute coroutine
    // Posted by: Arpit Shukla
    // Available at: https://stackoverflow.com/questions/70058423/how-to-use-lifecyclescope-to-execute-coroutine
    private fun loadNotifications() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                tempNotifications = sbHelper.getAllNotificationHistory()
                withContext(Dispatchers.Main) {
                    // Update RecyclerView on the main thread
                    notificationHistoryAdapter.updateNotifications(tempNotifications)

                    // After loading the data, manually trigger the default filter (visible filter in this case)
                    visibleFilterBtn.performClick() // Simulate button click to trigger filter action
                }
            } catch (e: Exception) {
                Log.e("NotificationLoadError", "Error loading notifications", e)
                withContext(Dispatchers.Main) {
                    // Show an error message if loading fails
                    Toast.makeText(requireContext(), "Operation failure! Couldn't load notifications from DB.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // StackOverflow post
    // Titled: How to update RecyclerView Adapter Data
    // Uploaded by: Suragch
    // Available at: https://stackoverflow.com/questions/31367599/how-to-update-recyclerview-adapter-data
    private fun changeNotificationState(notification: NotificationHistoryModel, visibilityState: Boolean) {
        lifecycleScope.launch {
            try {
                // Update the notification in the database
                sbHelper.updateNotificationHistory(notification.id!!, visibilityState)
                notificationHistoryAdapter.updateNotification(notification)
                lastButtonClicked.performClick()
                Toast.makeText(requireContext(), "Operation Success! Notification visibility has been changed.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("HideNotificationError", "Error hiding notification", e)
                Toast.makeText(requireContext(), "Operation failure! Failed to hide notification", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // StackOverflow post
    // Title: How to use LifecycleScope to execute coroutine
    // Posted by: Arpit Shukla
    // Available at: https://stackoverflow.com/questions/70058423/how-to-use-lifecyclescope-to-execute-coroutine
    private fun deleteNotification(notification: NotificationHistoryModel) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Delete the notification from the database
                sbHelper.deleteNotificationHistory(notification.id!!)
                withContext(Dispatchers.Main) {
                    // Delete the notification in the adapter
                    tempNotifications = tempNotifications.filter { it.id != notification.id }
                    notificationHistoryAdapter.removeNotification(notification)
                    Toast.makeText(requireContext(), "Operation Success! Notification deleted.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Operation failure! Could not delete notification.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val dashboardActivity = activity as? DashboardActivity
        dashboardActivity?.binding?.apply {
            bottomNavigation.visibility = View.VISIBLE
            plusBtn.visibility = View.GONE
        }
    }
}