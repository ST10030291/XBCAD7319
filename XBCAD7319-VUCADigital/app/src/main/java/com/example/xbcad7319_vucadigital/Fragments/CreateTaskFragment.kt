package com.example.xbcad7319_vucadigital.Fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.xbcad7319_vucadigital.Adapters.CustomSpinnerAdapter
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.models.TaskModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateTaskFragment : Fragment() {

    private lateinit var categorySpinner: Spinner
    private lateinit var personAssignedSpinner: Spinner
    private lateinit var priorityLevelSpinner: Spinner
    private lateinit var nameEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var startDateEditText: EditText
    private lateinit var endDateEditText: EditText
    private lateinit var createButton: Button
    private lateinit var backButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout for current fragment
        val view = inflater.inflate(R.layout.fragment_create_task, container, false)

        // Initialize UI
        categorySpinner = view.findViewById(R.id.categorySpinner)
        personAssignedSpinner = view.findViewById(R.id.personAssignedSpinner)
        priorityLevelSpinner = view.findViewById(R.id.priorityLevelSpinner)
        nameEditText = view.findViewById(R.id.taskName)
        descriptionEditText = view.findViewById(R.id.description)
        startDateEditText = view.findViewById(R.id.startDate)
        endDateEditText = view.findViewById(R.id.endDate)
        createButton = view.findViewById(R.id.createTaskButton)
        backButton = view.findViewById(R.id.back_btn)

        // Populate spinners
        setupSpinners()

        // date picker dialog for start date click listener setup
        startDateEditText.setOnClickListener {
            showDatePickerDialog { selectedDate ->
                startDateEditText.setText(selectedDate)
            }
        }

        // date picker dialog for end date click listener setup
        endDateEditText.setOnClickListener {
            showDatePickerDialog { selectedDate ->
                endDateEditText.setText(selectedDate)
            }
        }

        // Create button click listener setup
        createButton.setOnClickListener {
            createTask()
        }

        // Back button click listener setup
        backButton.setOnClickListener{
//            (context as AppCompatActivity).supportFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, TasksFragment()).commit()
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    private fun setupSpinners() {
        // Sample data for now
        val categories = listOf("Select a category", "Work", "Personal", "Urgent", "Later")
        val assignedPersons = listOf("Select a person", "Alice", "Bob", "Charlie", "Dana")
        val priorityLevels = listOf("Select priority level", "High", "Medium", "Low")

        // Create ArrayAdapter for each spinner with custom layout
        categorySpinner.adapter = CustomSpinnerAdapter(requireContext(), categories)
        personAssignedSpinner.adapter = CustomSpinnerAdapter(requireContext(), assignedPersons)
        priorityLevelSpinner.adapter = CustomSpinnerAdapter(requireContext(), priorityLevels)
    }

    private fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format selected date as "YYYY/MM/DD"
                val selectedDate = String.format("%04d/%02d/%02d", selectedYear, selectedMonth + 1, selectedDay)

                onDateSelected(selectedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun isValidDateRange(startDate: String, endDate: String): Boolean {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        return try {
            val start = dateFormat.parse(startDate)
            val end = dateFormat.parse(endDate)
            start != null && end != null && !end.before(start)
        } catch (e: ParseException) {
            // If there's a parsing error, consider it invalid
            false
        }
    }

    private fun createTask() {
        // Retrieve values from the inputs
        val name = nameEditText.text.toString()
        val category = categorySpinner.selectedItem.toString()
        val description = descriptionEditText.text.toString().trim()
        val personAssigned = personAssignedSpinner.selectedItem.toString()
        val startDate = startDateEditText.text.toString()
        val endDate = endDateEditText.text.toString()
        val priorityLevel = priorityLevelSpinner.selectedItem.toString()

        // Validate inputs
        if (!validateInputs(name, category, personAssigned, priorityLevel, startDate, endDate, description)) return

        // Create a new task to save to supabase later
        val task = TaskModel(
            name = name,
            category = category,
            description = description,
            personAssigned = personAssigned,
            startDate = startDate,
            endDate = endDate,
            priorityLevel = priorityLevel
        )

        Log.d(task.name, "${task.name} saved successfully!")

        Toast.makeText(requireContext(), "Task created successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun validateInputs(
        name: String,
        category: String,
        personAssigned: String,
        priorityLevel: String,
        startDate: String,
        endDate: String,
        description: String
    ): Boolean {
        return when {
            name.isEmpty() -> {
                showToast("Empty Task Name! Please enter a task name.")
                false
            }
            name.length < 4 -> {
                showToast("Name too short! Task name must be at least 4 characters.")
                false
            }
            category == "Select a category" -> {
                showToast("Category not selected! Please select a category.")
                false
            }
            personAssigned == "Select a person" -> {
                showToast("Person assigned not selected! Please select a person assigned.")
                false
            }
            priorityLevel == "Select priority level" -> {
                showToast("Priority level not selected! Please select a priority level.")
                false
            }
            !isValidDateRange(startDate, endDate) -> {
                showToast("Issue with dates! End date must be after start date.")
                false
            }
            description.isEmpty() -> {
                showToast("Empty description! Please enter a Description.")
                false
            }
            description.length < 6 -> {
                showToast("Description too short! Please enter a longer description.")
                false
            }
            // If we're here, everything was okay
            else -> true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    // Remove listeners to prevent memory leaks
    override fun onDestroy() {
        super.onDestroy()
        startDateEditText.setOnClickListener(null)
        endDateEditText.setOnClickListener(null)
        createButton.setOnClickListener(null)
        backButton.setOnClickListener(null)
    }
}