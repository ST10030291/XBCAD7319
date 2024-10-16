package com.example.xbcad7319_vucadigital.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.xbcad7319_vucadigital.Adapters.TaskAdapter
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.db.SupabaseHelper
import com.example.xbcad7319_vucadigital.models.TaskModel
import kotlinx.coroutines.launch

class TasksFragment : Fragment() {
    private lateinit var taskAdapter: TaskAdapter
    private var tasksList = mutableListOf<TaskModel>()
    private lateinit var sbHelper: SupabaseHelper

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
        lifecycleScope.launch {
            taskAdapter.updateTasks(sbHelper.getAllTasks())
        }
    }

    private fun onEditTask(task: TaskModel) {
        // Handle editing a task
        // by opening a dialog or an another fragment
        // Update the task in the list and notify the adapter as well
    }

    private fun onDeleteTask(task: TaskModel) {
        tasksList.remove(task)
        taskAdapter.updateTasks(tasksList)
    }
}