package com.example.xbcad7319_vucadigital.Fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xbcad7319_vucadigital.Activites.DashboardActivity
import com.example.xbcad7319_vucadigital.Adapters.TaskAdapter
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.db.SupabaseHelper
import com.example.xbcad7319_vucadigital.models.TaskModel
import kotlinx.coroutines.launch

class TasksFragment : Fragment() {
    private lateinit var taskAdapter: TaskAdapter
    private var tasksList = mutableListOf<TaskModel>()
    private lateinit var sbHelper: SupabaseHelper
    // Sample data for now
    private val categories = listOf("Select a category", "Work", "Personal", "Urgent", "Later")
    private val assignedPersons = listOf("Select a person", "Alice", "Bob", "Charlie", "Dana")
    private val priorityLevels = listOf("Select priority level", "High", "Medium", "Low")

    override fun onResume() {
        super.onResume()
        val dashboardActivity = activity as? DashboardActivity
        dashboardActivity?.binding?.apply {
            bottomNavigation.visibility = View.VISIBLE
            plusBtn.visibility = View.VISIBLE
        }
    }

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

        sbHelper = SupabaseHelper()
        loadTasks()
    }

    private fun loadTasks() {
        try {
            lifecycleScope.launch {
                taskAdapter.updateTasks(sbHelper.getAllTasks())
            }
        }
        catch (e: Exception){
            Toast.makeText(requireContext(),"Couldn't load tasks from DB", Toast.LENGTH_SHORT).show()
        }

    }

    private fun setupSpinner(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setupDialogButtons(dialog: AlertDialog, dialogView: View, task: TaskModel) {
        dialogView.findViewById<Button>(R.id.cancelEditTask).setOnClickListener {
            dialog.dismiss() // Close the dialog
        }

        dialogView.findViewById<Button>(R.id.saveEditTask).setOnClickListener {
            saveTaskChanges(dialog, task, dialogView)
        }
    }

    private fun saveTaskChanges(dialog: AlertDialog, task: TaskModel, dialogView: View) {
        val taskNameEditText: EditText = dialogView.findViewById(R.id.taskName)
        val categorySpinner: Spinner = dialogView.findViewById(R.id.categorySpinner)
        val descriptionEditText: EditText = dialogView.findViewById(R.id.description)
        val priorityLevelSpinner: Spinner = dialogView.findViewById(R.id.priorityLevelSpinner)
        val personAssignedSpinner: Spinner = dialogView.findViewById(R.id.personAssignedSpinner)

        lifecycleScope.launch {
            try {
                // Update the task properties based on user input
                task.name = taskNameEditText.text.toString()
                task.category = getSelectedCategory(categorySpinner)
                task.description = descriptionEditText.text.toString()
                task.priorityLevel = getSelectedPriority(priorityLevelSpinner)
                task.personAssigned = getSelectedPerson(personAssignedSpinner)

                // Update the task in the database
                sbHelper.updateTask(task)

                // Update the list in the adapter
                taskAdapter.updateTasks(sbHelper.getAllTasks())

                Toast.makeText(requireContext(), "Operation Success! Updated task.", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Operation failure! Couldn't update task.", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss() // Close the dialog
        }
    }

    private fun onEditTask(task: TaskModel) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_item, null)
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setTitle("Edit Task")

        setupDialogViews(dialogView, task)

        val dialog = builder.create()

        setupDialogButtons(dialog, dialogView, task)

        dialog.show()
    }

    private fun setupDialogViews(dialogView: View, task: TaskModel) {
        // Get references to the dialog views
        val taskNameEditText: EditText = dialogView.findViewById(R.id.taskName)
        val categorySpinner: Spinner = dialogView.findViewById(R.id.categorySpinner)
        val descriptionEditText: EditText = dialogView.findViewById(R.id.description)
        val priorityLevelSpinner: Spinner = dialogView.findViewById(R.id.priorityLevelSpinner)
        val personAssignedSpinner: Spinner = dialogView.findViewById(R.id.personAssignedSpinner)

        // Populate spinners with data
        setupSpinner(categorySpinner, categories)
        setupSpinner(priorityLevelSpinner, priorityLevels)
        setupSpinner(personAssignedSpinner, assignedPersons)

        // Populate fields with the task's current values
        taskNameEditText.setText(task.name)
        categorySpinner.setSelection(categories.indexOf(task.category))
        descriptionEditText.setText(task.description)
        priorityLevelSpinner.setSelection(priorityLevels.indexOf(task.priorityLevel))
        personAssignedSpinner.setSelection(assignedPersons.indexOf(task.personAssigned))
    }

    private fun onDeleteTask(task: TaskModel) {
        // Create confirmation dialog
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Yes") { dialog, _ ->
                lifecycleScope.launch {
                    try {
                        // Delete the task from the database
                        sbHelper.deleteTask(task.id!!)
                        // Update the list in the adapter
                        taskAdapter.updateTasks(sbHelper.getAllTasks())
                        Toast.makeText(requireContext(), "Operation Success! Task deleted.", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Operation failure! Couldn't delete task.", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(requireContext(), "Operation cancelled! No task deleted.", Toast.LENGTH_SHORT).show()
            }
            .show()
    }


    private fun getSelectedCategory(spinner: Spinner): String {
        return spinner.selectedItem.toString()
    }

    private fun getSelectedPriority(spinner: Spinner): String {
        return spinner.selectedItem.toString()
    }

    private fun getSelectedPerson(spinner: Spinner): String {
        return spinner.selectedItem.toString()
    }
}