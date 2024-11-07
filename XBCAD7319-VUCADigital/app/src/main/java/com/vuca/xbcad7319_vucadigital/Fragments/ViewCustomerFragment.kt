package com.vuca.xbcad7319_vucadigital.Fragments

import android.Manifest
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vuca.xbcad7319_vucadigital.Activites.DashboardActivity
import com.vuca.xbcad7319_vucadigital.Adapters.CustomerProductsAdapter
import com.vuca.xbcad7319_vucadigital.R
import com.vuca.xbcad7319_vucadigital.db.SupabaseHelper
import com.vuca.xbcad7319_vucadigital.models.CustomerModel
import com.vuca.xbcad7319_vucadigital.models.CustomerProductModel
import com.vuca.xbcad7319_vucadigital.models.NotificationHistoryModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class ViewCustomerFragment : Fragment() {
    private lateinit var customer: CustomerModel
    private lateinit var sbHelper: SupabaseHelper
    private var customerProducts = mutableListOf<CustomerProductModel>()
    private lateinit var customerProductAdapter: CustomerProductsAdapter

    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var notificationTitle: String
    private lateinit var notificationType: String

    //Ensures the Navbar is visible and the plus btn is gone
    override fun onResume() {
        super.onResume()
        val dashboardActivity = activity as? DashboardActivity
        dashboardActivity?.binding?.apply {
            plusBtn.visibility = View.GONE
            bottomNavigation.visibility = View.VISIBLE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sbHelper = SupabaseHelper()

        val recyclerView: RecyclerView = view.findViewById(R.id.customer_products_recycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        customerProductAdapter = CustomerProductsAdapter(customerProducts)
        recyclerView.adapter = customerProductAdapter

        retrieveArguments()
        displayCustomerData(view)
        setupClickListeners(view)

        loadCustomerProducts()

        // Reset the notification state at the start of the day
        checkAndResetNotificationState()

        // Initialize the permission launcher
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                createNotificationChannel()
                checkContracts()
            } else {
                Toast.makeText(requireContext(), "Notification permission denied", Toast.LENGTH_SHORT).show()
                promptUserToEnableNotifications()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_view_customer, container, false)
    }

    private fun retrieveArguments() {
        arguments?.let {
            customer = it.getSerializable("customer") as CustomerModel
        }
    }

    //Displays the customers information and initialises action buttons
    private fun displayCustomerData(view: View) {
        view.findViewById<TextView>(R.id.customer_name).text = customer.CustomerName
        view.findViewById<TextView>(R.id.customer_email).text = customer.CustomerEmail
        view.findViewById<TextView>(R.id.customer_type).text = customer.CustomerType
        view.findViewById<TextView>(R.id.account_number).text = customer.AccountNumber
        view.findViewById<TextView>(R.id.billing_account_number).text = customer.BillingAccountNumber
    }

    private fun setupClickListeners(view: View) {
        view.findViewById<ImageView>(R.id.customer_call_click).setOnClickListener {
            makePhoneCall(customer.TelephoneNumber)
        }

        view.findViewById<ImageView>(R.id.customerDeleteBtn).setOnClickListener {
            lifecycleScope.launch { deleteCustomer() }
        }

        view.findViewById<ImageView>(R.id.customerUpdateBtn).setOnClickListener {
            navigateToUpdateCustomer()
        }

        view.findViewById<ImageView>(R.id.back_btn).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        view.findViewById<AppCompatButton>(R.id.NewCustomerProductBtn).setOnClickListener{
            navigateToCreateCustomerProductFragment()
        }
    }

    // StackOverflow post
    // Titled: How to make a phone call using intent in Android?
    // Posted by: UMAR-MOBITSOLUTIONS
    // Available at: https://stackoverflow.com/questions/4275678/how-to-make-a-phone-call-using-intent-in-android
    private fun makePhoneCall(phoneNumber: String) {
        if (phoneNumber.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            startActivity(intent)
        } else {
            Toast.makeText(context, "Phone number is not available", Toast.LENGTH_SHORT).show()
        }
    }

    //Function to delete the customer from the database and return back to the customer Fragment
    private suspend fun deleteCustomer() {
        try {
            customer.id?.let {
                sbHelper.deleteCustomer(it)
                showToast("Deletion successfully")
                popBackStack()
            }
        } catch (e: Exception) {
            showToast("Something went wrong! Deleting process cancelled.")
        }
    }

    //Directs the user to the Create Customer Fragment but with update UI
    private fun navigateToUpdateCustomer() {
        val updateCustomerFragment = CreateCustomerFragment().apply {
            arguments = Bundle().apply {
                putBoolean("isUpdateMode", true)
                putSerializable("customer", customer)
            }
        }

        (activity as? DashboardActivity)?.apply {
            binding.bottomNavigation.visibility = View.GONE
            binding.plusBtn.visibility = View.GONE
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, updateCustomerFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToCreateCustomerProductFragment() {

        (activity as? DashboardActivity)?.apply {
            binding.bottomNavigation.visibility = View.GONE
            binding.plusBtn.visibility = View.GONE
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, AddCustomerProductFragment())
            .addToBackStack(null)
            .commit()
    }

    //Displays the Customers products by fetching the products from the database
    private fun loadCustomerProducts() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                customer.CustomerName.let {
                    customerProducts = sbHelper.getCustomerProducts(it).toMutableList()
                }

                withContext(Dispatchers.Main) {
                    customerProductAdapter.updateCustomerProducts(customerProducts)
                    checkNotificationsPermissionsBeforeTrigger()
                }
            } catch (e: Exception) {
                Log.e("CustomerProductsLoadError", "Error loading customer products", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Couldn't load customer products from DB", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun popBackStack() {
        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun checkContractEnd(contractEnd: String?): String? {
        if (contractEnd.isNullOrEmpty()) {
            // Handle the case where the contractEnd is null or empty
            return null
        }

        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val contractEndDate = sdf.parse(contractEnd) ?: return null
            val currentDate = Date()

            // Check if the ContractEnd is today
            if (isSameDay(contractEndDate, currentDate)) {
                val message = "${customer.CustomerName}'s contract ends today! Call them to attempt to renew their contract."
                val split = message.split("!", limit = 2)
                notificationTitle = split[0]
                notificationType = "ENDING_TODAY"
                return message
            }

            // Check if the ContractEnd is in the next 7 days
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.DAY_OF_YEAR, 7)
            if (contractEndDate.before(calendar.time) && contractEndDate.after(currentDate)) {
                val message = "${customer.CustomerName}'s contract ends soon! On ${convertDateFormat(contractEnd)}. Consider calling them to renew their contract."
                val split = message.split("!", limit = 2)
                notificationTitle = split[0]
                notificationType = "ENDING_SOON"
                return message
            }

            // Check if the ContractEnd has passed
            if (contractEndDate.before(currentDate)) {
                val message = "${customer.CustomerName}'s contract has ended! Contract expired on ${convertDateFormat(contractEnd)}. Call them to attempt to renew their contract."
                val split = message.split("!", limit = 2)
                notificationTitle = split[0]
                notificationType = "ENDED"
                return message
            }

            null // Return null if none of the conditions are met
        } catch (e: Exception) {
            // Log the exception (you can use Log.e() to log error details)
            Log.e("ContractEndCheck", "Error parsing date: ${e.message}")
            null // Return null on error
        }
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun checkContracts() {
        try {
            customerProducts.forEach { product ->
                checkContractEnd(product.ContractEnd)?.let { message ->
//                    val uniqueId = "notification_${System.currentTimeMillis()}"
                    val customerName = customer.CustomerName
                    showNotificationIfNotShown(requireContext(), customerName, message, generateNotificationID(customerName,notificationType), notificationTitle)
                }
            }
        } catch (e: Exception) {
            Log.e("ContractCheck", "Error checking contracts: ${e.message}")
        }
    }

    // Blog post
    // Title: Create and manage notification channels
    // Posted by: Google for Developers
    // Available at: https://developer.android.com/develop/ui/views/notifications/channels#:~:text=To%20create%20a%20notification%20channel%2C%20follow%20these%20steps%3A,the%20notification%20channel%20by%20passing%20it%20to%20createNotificationChannel%28%29.
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "contract_notifications"
            val channelName = "Contract Notifications"
            val channelDescription = "Notifications for contract reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 1000) // Vibration pattern
            }

            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    // Blog post
    // Title: Create and manage notification channels
    // Posted by: Google for Developers
    // Available at: https://developer.android.com/develop/ui/views/notifications/channels#:~:text=To%20create%20a%20notification%20channel%2C%20follow%20these%20steps%3A,the%20notification%20channel%20by%20passing%20it%20to%20createNotificationChannel%28%29.
    private fun showNotification(context: Context, customerName: String, message: String, notificationId: String, notificationTitle: String) {
        if (message.isEmpty()) {
            Log.w("Notification", "No message to display in the notification")
            return
        }

        val channelId = "contract_notifications"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Build the notification
        try {
            val notificationBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(notificationTitle)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 500, 1000))

            // Show the notification
            notificationManager.notify(notificationId.hashCode(), notificationBuilder.build())

            saveNotificationToDB(customerName,message)
        } catch (e: Exception) {
            Log.e("Notification", "Error showing notification: ${e.message}")
        }
    }


    private fun areNotificationsEnabled(): Boolean {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel("contract_notifications")
            channel != null && channel.importance != NotificationManager.IMPORTANCE_NONE
        } else {
            // For devices below Oreo, all notifications are enabled by default
            true
        }
    }

    private fun promptUserToEnableNotifications() {
        if (!areNotificationsEnabled()) {
            AlertDialog.Builder(requireContext())
                .setTitle("Notifications Disabled")
                .setMessage("Please enable notifications for this app in your settings.")
                .setPositiveButton("Settings") { _, _ ->
                    // Open the notification settings for the app
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        // For API 26 and above
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName)
                        }
                        startActivity(intent)
                    } else {
                        // For earlier API levels (up to API 25)
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${requireContext().packageName}")
                        }
                        startActivity(intent)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun obtainPermissions(onPermissionGranted: () -> Unit) {
        // If below API level Tiramisu, no need to request permission
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onPermissionGranted()
            return
        }

        // Check if the notification permission is granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            onPermissionGranted() // Call the callback if granted
        } else {
            // Request permission
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun checkNotificationsPermissionsBeforeTrigger() {
        obtainPermissions {
            // This will be called when permission is granted
            checkContracts()
        }
    }

    private fun saveNotificationToDB(customerName: String, message: String) {
        val notification = NotificationHistoryModel(
            customerName = customerName,
            message = message,
            dateTime = getCurrentDate()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                sbHelper.addNotificationHistory(notification)
                Log.e("INSERT454", "Notification history saved to DB successfully!")
            } catch (e: Exception) {
                Log.e("INSERT987", "${e.message}")
            }
        }
    }

    private fun showNotificationIfNotShown(context: Context, customerName: String, message: String, notificationId: String, notificationTitle: String) {
        // Do not show if already shown today
        if (hasNotificationBeenShown(notificationId)) return

        // Show the notification
        showNotification(context, customerName, message, notificationId, notificationTitle)

        // Save the notification ID to prevent it from being shown again
        saveNotificationId(notificationId)
    }

    private fun saveNotificationId(notificationId: String) {
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val notificationIds = sharedPreferences.getStringSet("notification_ids", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        Log.d("Notification", "Saving ID: $notificationId")
        notificationIds.add(notificationId)

        with(sharedPreferences.edit()) {
            putStringSet("notification_ids", notificationIds)
            apply()
        }
    }

    private fun hasNotificationBeenShown(notificationId: String): Boolean {
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val notificationIds = sharedPreferences.getStringSet("notification_ids", mutableSetOf()) ?: mutableSetOf()

        Log.d("Notification", "Checking ID: $notificationId, Already Shown: ${notificationIds.contains(notificationId)}")
        return notificationIds.contains(notificationId)
    }


    private fun checkAndResetNotificationState() {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastResetDate = getLastResetDate()

        if (lastResetDate != currentDate) {
            // Clear the stored notification IDs
            resetNotificationIds()
            // Update the last reset date
            saveLastResetDate(currentDate)
        }
    }

    private fun resetNotificationIds() {
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            // Clear the stored notification IDs
            putStringSet("notification_ids", mutableSetOf())
            apply()
        }
    }

    private fun saveLastResetDate(date: String) {
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("last_reset_date", date)
            apply()
        }
    }

    private fun getLastResetDate(): String? {
        val sharedPreferences = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("last_reset_date", null)
    }

    private fun generateNotificationID(customerName: String, notificationType: String): String{
        return "notification_${customerName}_${notificationType}"
    }

    private fun convertDateFormat(inputDate: String): String {
        // Define the input and output date formats
        val inputFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormatter = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.ENGLISH)

        return try {
            // Parse the input date string to a Date object
            val date = inputFormatter.parse(inputDate)

            // Format the Date object to the desired output format
            date?.let {
                outputFormatter.format(it)
            } ?: "Invalid date"
        } catch (e: Exception) {
            // Handle invalid input date format
            "Invalid date format"
        }
    }

    private fun getCurrentDate(): String{
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        val currentDateTime = dateFormat.format(Date())
        return currentDateTime

    }
}
