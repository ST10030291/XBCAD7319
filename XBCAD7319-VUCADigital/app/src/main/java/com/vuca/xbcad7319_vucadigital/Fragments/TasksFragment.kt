package com.vuca.xbcad7319_vucadigital.Fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SearchView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vuca.xbcad7319_vucadigital.Activites.DashboardActivity
import com.vuca.xbcad7319_vucadigital.Adapters.CustomSpinnerAdapter
import com.vuca.xbcad7319_vucadigital.Adapters.TaskAdapter
import com.vuca.xbcad7319_vucadigital.R
import com.vuca.xbcad7319_vucadigital.db.SupabaseHelper
import com.vuca.xbcad7319_vucadigital.models.CustomerModel
import com.vuca.xbcad7319_vucadigital.models.TaskModel
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class TasksFragment : Fragment() {
    private lateinit var taskAdapter: TaskAdapter
    private var tasksList = mutableListOf<TaskModel>()
    private lateinit var sbHelper: SupabaseHelper
    private lateinit var customers: List<CustomerModel>
    private lateinit var filteredTasks: List<TaskModel>
    private lateinit var tasks: List<TaskModel>

    private lateinit var allFilterButton: Button
    private lateinit var toDoFilterButton: Button
    private lateinit var doingFilterButton: Button
    private lateinit var doneFilterButton: Button

    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        taskAdapter = TaskAdapter(tasksList, ::onEditTask, ::onDeleteTask)
        recyclerView.adapter = taskAdapter

        searchView = view.findViewById(R.id.searchView)
        allFilterButton = view.findViewById(R.id.AllFilter)
        toDoFilterButton = view.findViewById(R.id.ToDoFilter)
        doingFilterButton= view.findViewById(R.id.DoingFilter)
        doneFilterButton = view.findViewById(R.id.DoneFilter)

        sbHelper = SupabaseHelper()

        Handler(Looper.getMainLooper()).postDelayed({
            val shimmerLayout = view.findViewById<ShimmerFrameLayout>(R.id.shimmerTasks)
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            // Get Tasks from db and load them onto the adapter
            loadTasks()

            setUpSearchView()

            // Set up the click listeners for each filter button
            setFilterButtonClickListener(allFilterButton, null)
            setFilterButtonClickListener(toDoFilterButton, "To Do")
            setFilterButtonClickListener(doingFilterButton, "Doing")
            setFilterButtonClickListener(doneFilterButton, "Done")
        },2000)
    }

    private fun selectButton(selectedButton: Button) {
        allFilterButton.isSelected = selectedButton == allFilterButton
        toDoFilterButton.isSelected = selectedButton == toDoFilterButton
        doingFilterButton.isSelected = selectedButton == doingFilterButton
        doneFilterButton.isSelected = selectedButton == doneFilterButton
    }

    // StackOverflow post
    // Titled: How can I filter an ArrayList in Kotlin so I only have elements which match my condition?
    // Posted by: Nithinlal
    // Available at: https://stackoverflow.com/questions/44098709/how-can-i-filter-an-arraylist-in-kotlin-so-i-only-have-elements-which-match-my-c
    private fun setFilterButtonClickListener(button: Button, filterStatus: String?) {
        button.setOnClickListener {
            filteredTasks = if (filterStatus == null) {
                tasks
            } else {
                tasks.filter { it.status == filterStatus }
            }
            selectButton(button)
            taskAdapter.updateTasks(filteredTasks)
        }
    }

    // Blog post
    // Title: SearchView in Android with Kotlin
    // Posted by: chaitanyamunje
    // Posted on:  28 July 2022
    // Available at: https://www.geeksforgeeks.org/searchview-in-android-with-kotlin/
    private fun setUpSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchTasksByName(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchTasksByName(it) }
                return true
            }
        })
    }

    // StackOverflow post
    // Titled: How can I filter an ArrayList in Kotlin so I only have elements which match my condition?
    // Posted by: Nithinlal
    // Available at: https://stackoverflow.com/questions/44098709/how-can-i-filter-an-arraylist-in-kotlin-so-i-only-have-elements-which-match-my-c
    private fun searchTasksByName(query: String) {
        val queryLower = query.lowercase()

        filteredTasks = tasks.filter { task ->
            task.name.lowercase().contains(queryLower)
        }

        taskAdapter.updateTasks(filteredTasks)

        // Show a toast message if filteredTasks is empty
        if (filteredTasks.isEmpty()) {
            Toast.makeText(context, "Task \"$query\" not found!", Toast.LENGTH_SHORT).show()
        }
    }

    // StackOverflow post
    // Title: How to use LifecycleScope to execute coroutine
    // Posted by: Arpit Shukla
    // Available at: https://stackoverflow.com/questions/70058423/how-to-use-lifecyclescope-to-execute-coroutine
    private fun loadTasks() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                tasks = sbHelper.getAllTasks()
                withContext(Dispatchers.Main) {
                    taskAdapter.updateTasks(tasks)
                }
            } catch (e: Exception) {
                Log.e("TaskLoadError", "Error loading tasks", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Couldn't load tasks from DB", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // StackOverflow post
    // Title: Android Kotlin Get Value of Selected Spinner Item
    // Posted by: Subhrajyoti Sen
    // Available at: https://stackoverflow.com/questions/65556362/android-kotlin-get-value-of-selected-spinner-item
    private fun retrieveCustomerIDFromSpinner(spinner: Spinner): String? {
        try {
            val selectedPosition = spinner.selectedItemPosition
            if (selectedPosition != Spinner.INVALID_POSITION) {
                // Adjust index if necessary based on your customer list
                val selectedCustomer = customers[selectedPosition - 1]
                return selectedCustomer.id
            }
        } catch (e: IndexOutOfBoundsException) {
            Toast.makeText(requireContext(), "Operation failed! Couldn't retrieve customer ID.", Toast.LENGTH_SHORT).show()
        }
        return null
    }

    // Blog post
    // Title: DatePicker in Kotlin
    // Posted by: Praveenruhil
    // Available at: https://www.geeksforgeeks.org/datepicker-in-kotlin/
    private fun setupDatePickers(dialogView: View, task: TaskModel) {
        val startDateEditText: EditText = dialogView.findViewById(R.id.startDate)
        val endDateEditText: EditText = dialogView.findViewById(R.id.endDate)

        // Set initial values
        startDateEditText.setText(task.startDate)
        endDateEditText.setText(task.endDate)

        // Show date picker when start date is clicked
        startDateEditText.setOnClickListener {
            showDatePickerDialog(startDateEditText, task.startDate)
        }

        // Show date picker when end date is clicked
        endDateEditText.setOnClickListener {
            showDatePickerDialog(endDateEditText, task.endDate)
        }
    }

    // StackOverflow post
    // Title: findViewById in Fragment
    // Posted by: LeffelMania
    // Available at: https://stackoverflow.com/questions/6495898/findviewbyid-in-fragment
    private fun setupDialogButtons(dialog: AlertDialog, dialogView: View, task: TaskModel) {
        dialogView.findViewById<Button>(R.id.cancelEditTask).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<Button>(R.id.saveEditTask).setOnClickListener {
            saveTaskChanges(dialog, task, dialogView)
        }
    }

    // StackOverflow post
    // Title: findViewById in Fragment
    // Posted by: LeffelMania
    // Available at: https://stackoverflow.com/questions/6495898/findviewbyid-in-fragment
    private fun setupDialogViews(dialogView: View, task: TaskModel) {
        // Get references to the dialog views
        val taskNameEditText: EditText = dialogView.findViewById(R.id.taskName)
        val categorySpinner: Spinner = dialogView.findViewById(R.id.categorySpinner)
        val descriptionEditText: EditText = dialogView.findViewById(R.id.description)
        val priorityLevelSpinner: Spinner = dialogView.findViewById(R.id.priorityLevelSpinner)
        val personAssignedSpinner: Spinner = dialogView.findViewById(R.id.personAssignedSpinner)
        val customerSpinner: Spinner = dialogView.findViewById(R.id.customerSpinner)
        val statusSpinner: Spinner = dialogView.findViewById(R.id.statusSpinner)
        val taskStartDateEditText: EditText = dialogView.findViewById(R.id.startDate)
        val taskEndDateEditText: EditText = dialogView.findViewById(R.id.endDate)


        // StackOverflow post
        // Title: How to use LifecycleScope to execute coroutine
        // Posted by: Arpit Shukla
        // Available at: https://stackoverflow.com/questions/70058423/how-to-use-lifecyclescope-to-execute-coroutine

        // Populate customer spinner
        lifecycleScope.launch {
            try {
                customers = sbHelper.getAllCustomers() // Assuming you have access to sbHelper
                val customerNames = listOf("Select a customer name") + customers.map { it.CustomerName }
                customerSpinner.adapter = CustomSpinnerAdapter(requireContext(), customerNames)

                // Set selection if customerID is provided
                task.customerID?.let {
                    val customerIndex = customers.indexOfFirst { customer -> customer.id == it }
                    if (customerIndex != -1) {
                        // Set selection, add 1 to account for the "Select a customer name" entry
                        customerSpinner.setSelection(customerIndex + 1)
                    }
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Operation failure! Couldn't load customers.", Toast.LENGTH_SHORT).show()
            }
        }

        // Sample data for now
        val categories = listOf("Select a category", "Follow up", "Installation", "Billing", "Support")
        val assignedPersons = listOf("Select a person", "Samantha", "Ethan", "Olivia", "Daniel", "Jonathan", "Carlos")
        val priorityLevels = listOf("Select a priority level", "High", "Medium", "Low")
        val statuses = listOf("Select a task status", "To Do", "Doing", "Done")

        // Set adapters
        categorySpinner.adapter = CustomSpinnerAdapter(requireContext(), categories)
        personAssignedSpinner.adapter = CustomSpinnerAdapter(requireContext(), assignedPersons)
        priorityLevelSpinner.adapter = CustomSpinnerAdapter(requireContext(), priorityLevels)
        statusSpinner.adapter = CustomSpinnerAdapter(requireContext(), statuses)

        // Populate fields with the task's current values
        taskNameEditText.setText(task.name)
        categorySpinner.setSelection(categories.indexOf(task.category))
        descriptionEditText.setText(task.description)
        priorityLevelSpinner.setSelection(priorityLevels.indexOf(task.priorityLevel))
        personAssignedSpinner.setSelection(assignedPersons.indexOf(task.personAssigned))
        statusSpinner.setSelection(statuses.indexOf(task.status))
        taskStartDateEditText.setText(task.startDate)
        taskEndDateEditText.setText(task.endDate)
    }

    // StackOverflow post
    // Titled: How to use DatePickerDialog in Kotlin?
    // Posted by: Derek
    // Available at: https://stackoverflow.com/questions/45842167/how-to-use-datepickerdialog-in-kotlin
    private fun showDatePickerDialog(editText: EditText, initialDate: String) {
        val calendar = Calendar.getInstance()

        if (initialDate.isNotEmpty()) {
            val dateParts = initialDate.split("-")
            calendar.set(dateParts[0].toInt(), dateParts[1].toInt() - 1, dateParts[2].toInt())
        }

        // Create the DatePickerDialog with custom theme
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val formattedDate = String.format("%04d/%02d/%02d", year, month + 1, dayOfMonth)
                editText.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Show the DatePickerDialog
        datePickerDialog.show()
    }

    private fun saveTaskChanges(dialog: AlertDialog, task: TaskModel, dialogView: View) {
        // StackOverflow post
        // Title: findViewById in Fragment
        // Posted by: LeffelMania
        // Available at: https://stackoverflow.com/questions/6495898/findviewbyid-in-fragment
        val taskNameEditText: EditText = dialogView.findViewById(R.id.taskName)
        val startDateEditText: EditText = dialogView.findViewById(R.id.startDate)
        val endDateEditText: EditText = dialogView.findViewById(R.id.endDate)
        val categorySpinner: Spinner = dialogView.findViewById(R.id.categorySpinner)
        val descriptionEditText: EditText = dialogView.findViewById(R.id.description)
        val priorityLevelSpinner: Spinner = dialogView.findViewById(R.id.priorityLevelSpinner)
        val personAssignedSpinner: Spinner = dialogView.findViewById(R.id.personAssignedSpinner)
        val statusSpinner: Spinner = dialogView.findViewById(R.id.statusSpinner)
        val customerSpinner: Spinner = dialogView.findViewById(R.id.customerSpinner)

        // StackOverflow post
        // Title: How to use LifecycleScope to execute coroutine
        // Posted by: Arpit Shukla
        // Available at: https://stackoverflow.com/questions/70058423/how-to-use-lifecyclescope-to-execute-coroutine
        lifecycleScope.launch {
            val updatedTask = task.copy(
                name = taskNameEditText.text.toString(),
                category = getSelectedSpinnerProperty(categorySpinner),
                description = descriptionEditText.text.toString(),
                priorityLevel = getSelectedSpinnerProperty(priorityLevelSpinner),
                personAssigned = getSelectedSpinnerProperty(personAssignedSpinner),
                status = getSelectedSpinnerProperty(statusSpinner),
                startDate = startDateEditText.text.toString(),
                endDate = endDateEditText.text.toString(),
                customerID = retrieveCustomerIDFromSpinner(customerSpinner)
            )

            try {
                // Update the task in the database
                sbHelper.updateTask(updatedTask)

                // Update the task in the adapter
                taskAdapter.updateTask(updatedTask)

                Toast.makeText(requireContext(), "Operation Success! Updated task.", Toast.LENGTH_SHORT).show()
                dialog.dismiss() // Dismiss only after success
            } catch (e: Exception) {
                Log.e("EditTask", "Error updating task", e) // Log error details
                Toast.makeText(requireContext(), "Operation failure! Couldn't update task.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onEditTask(task: TaskModel) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_item, null)
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Edit Task")

        setupDialogViews(dialogView, task)

        val dialog = builder.create()

        setupDatePickers(dialogView, task)

        setupDialogButtons(dialog, dialogView, task)

        dialog.show()
    }

    private fun onDeleteTask(task: TaskModel) {
        // StackOverflow post
        // Title: findViewById in Fragment
        // Posted by: LeffelMania
        // Available at: https://stackoverflow.com/questions/6495898/findviewbyid-in-fragment
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_task, null)
        val deleteDialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        val taskNameTextView: TextView = dialogView.findViewById(R.id.dialog_task_name)
        val messageTextView: TextView = dialogView.findViewById(R.id.dialog_message)
        val cancelButton: Button = dialogView.findViewById(R.id.button_cancel)
        val deleteButton: Button = dialogView.findViewById(R.id.button_delete)

        // Set the task name in the dialog
        taskNameTextView.text = task.name

        cancelButton.setOnClickListener {
            deleteDialog.dismiss()
            Toast.makeText(requireContext(), "Operation cancelled! No task deleted.", Toast.LENGTH_SHORT).show()
        }

        // StackOverflow post
        // Title: How to use LifecycleScope to execute coroutine
        // Posted by: Arpit Shukla
        // Available at: https://stackoverflow.com/questions/70058423/how-to-use-lifecyclescope-to-execute-coroutine
        deleteButton.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // Delete the task from the database
                    sbHelper.deleteTask(task.id!!)
                    withContext(Dispatchers.Main) {
                        taskAdapter.removeTask(task)
                        Toast.makeText(requireContext(), "Operation Success! Task deleted.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("TaskDeleteError", "Error deleting task: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Operation failure! Couldn't delete task.", Toast.LENGTH_SHORT).show()
                    }
                }
                deleteDialog.dismiss()
            }
        }
        deleteDialog.show()
    }

    // StackOverflow post
    // Titled: How to get Spinner value?
    // Posted by: dodo
    // Available at: https://stackoverflow.com/questions/1947933/how-to-get-spinner-value
    private fun getSelectedSpinnerProperty(spinner: Spinner): String {
        return spinner.selectedItem.toString()
    }

    override fun onResume() {
        super.onResume()
        val dashboardActivity = activity as? DashboardActivity
        dashboardActivity?.binding?.apply {
            bottomNavigation.visibility = View.VISIBLE
            plusBtn.visibility = View.VISIBLE
        }
    }
}