package com.vuca.xbcad7319_vucadigital.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vuca.xbcad7319_vucadigital.Activites.DashboardActivity
import com.vuca.xbcad7319_vucadigital.Adapters.NotificationHistoryAdapter
import com.vuca.xbcad7319_vucadigital.R
import com.vuca.xbcad7319_vucadigital.db.SupabaseHelper
import com.vuca.xbcad7319_vucadigital.models.NotificationHistoryModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationHistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var notificationHistoryAdapter: NotificationHistoryAdapter
    private var notificationList: MutableList<NotificationHistoryModel> = mutableListOf()

    private lateinit var filteredNotificationList: MutableList<NotificationHistoryModel>
    private var tempNotifications : MutableList<NotificationHistoryModel> = mutableListOf()
    private var sbHelper: SupabaseHelper = SupabaseHelper()

    private lateinit var visibleFilterBtn: Button
    private lateinit var hiddenFilterBtn: Button
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for the fragment
        val binding = inflater.inflate(R.layout.fragment_notification_history, container, false)
        recyclerView = binding.findViewById(R.id.notificationHistoryRecyclerView)
        searchView = binding.findViewById(R.id.notificationHistorySearchView)
        visibleFilterBtn = binding.findViewById(R.id.visibleFilterBtn)
        hiddenFilterBtn = binding.findViewById(R.id.hiddenFilterBtn)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        notificationHistoryAdapter = NotificationHistoryAdapter(
            notifications = notificationList,
            onHide = { position -> changeNotificationState(position, false) },
            onShow = { position -> changeNotificationState(position, true) },
            onDelete = { position -> deleteNotification(position) }
        )
        recyclerView.adapter = notificationHistoryAdapter

        // Load notifications from Supabase
        loadNotifications()

        setUpSearchView()

        selectButton(visibleFilterBtn)

        // Set up the click listeners for each filter button
        setFilterButtonClickListener(visibleFilterBtn, true)
        setFilterButtonClickListener(hiddenFilterBtn, false)

        return binding
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
                filteredNotificationList = if(filterVisibility == true){
                    tempNotifications.filter { it.visible == true }.toMutableList()
                } else{
                    tempNotifications.filter { it.visible == false }.toMutableList()
                }

                selectButton(button)
                notificationHistoryAdapter.updateData(filteredNotificationList)
            }
        }
        catch (e : Exception){
            Toast.makeText(context, "Empty History! No notifications available to filter.", Toast.LENGTH_SHORT).show()
        }
    }

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
        }.toMutableList()

        notificationHistoryAdapter.updateData(filteredNotificationList)

        // Show a toast message if filteredTasks is empty
        if (filteredNotificationList.isEmpty()) {
            Toast.makeText(context, "Task \"$query\" not found!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadNotifications() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                tempNotifications = sbHelper.getAllNotificationHistory().toMutableList()
                //notificationList = notifications.toMutableList()
                withContext(Dispatchers.Main) {
                    // Update RecyclerView on the main thread
                    notificationHistoryAdapter.updateData(tempNotifications)
                }
            } catch (e: Exception) {
                Log.e("NotificationLoadError", "Error loading notifications", e)
                withContext(Dispatchers.Main) {
                    // Show an error message if loading fails
                    Toast.makeText(requireContext(), "Couldn't load notifications from DB", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Hide a notification (set visible = false in database)
    private fun changeNotificationState(position: Int, visibilityState: Boolean) {
        val notification = tempNotifications[position]
        // Update visibility locally
        tempNotifications[position] = notification.copy(visible = visibilityState)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                sbHelper.updateNotificationHistory(notification.id!!, visibilityState)
                withContext(Dispatchers.Main) {
                    // Refresh the item
                    notificationHistoryAdapter.notifyItemChanged(position)
                    Toast.makeText(requireContext(), "Notification visibility changed.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("HideNotificationError", "Error hiding notification", e)

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Failed to hide notification", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Delete a notification (actually delete from database)
    private fun deleteNotification(position: Int) {
        val notification = tempNotifications[position]
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                sbHelper.deleteNotificationHistory(notification.id!!)
                withContext(Dispatchers.Main) {
                    notificationList.removeAt(position)
                    notificationHistoryAdapter.notifyItemRemoved(position)
                    Toast.makeText(requireContext(), "Notification deleted successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("NotificationDeleteError", "Error deleting notification", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Failed to delete notification", Toast.LENGTH_SHORT).show()
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