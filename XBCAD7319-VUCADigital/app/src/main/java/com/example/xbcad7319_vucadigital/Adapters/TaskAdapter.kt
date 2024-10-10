package com.example.xbcad7319_vucadigital.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.models.TaskModel

class TaskAdapter(
    private var tasks: List<TaskModel>,
    private val onEditClick: (TaskModel) -> Unit,
    private val onDeleteClick: (TaskModel) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.task_name)
        val priorityButton : Button = itemView.findViewById(R.id.task_priority)
        val status : TextView = itemView.findViewById(R.id.task_status)
        val customerName : TextView = itemView.findViewById(R.id.customer_name)
        val personAssigned : TextView = itemView.findViewById(R.id.task_person_assigned)
        val startDate : TextView = itemView.findViewById(R.id.task_start_date)

        val editButton: Button = itemView.findViewById(R.id.task_edit_button)
        val deleteButton: Button = itemView.findViewById(R.id.task_delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.nameTextView.text = task.name
        holder.priorityButton.text = task.priorityLevel
        holder.status.text = "Doing" // For now
        holder.customerName.text = "Eben (For now)" // For now
        holder.personAssigned.text = task.personAssigned
        holder.startDate.text = task.startDate

        holder.editButton.setOnClickListener { onEditClick(task) }
        holder.deleteButton.setOnClickListener { onDeleteClick(task) }
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<TaskModel>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}
