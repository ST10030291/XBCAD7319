package com.example.xbcad7319_vucadigital.Adapters

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.db.SupabaseHelper
import com.example.xbcad7319_vucadigital.models.TaskModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskAdapter(
    private var tasks: MutableList<TaskModel> = mutableListOf(),
    private val onEditClick: (TaskModel) -> Unit,
    private val onDeleteClick: (TaskModel) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    // Define colors for different priority levels with sentence case
    private val priorityColors = mapOf(
        "High" to "#E8715C",    // Red
        "Medium" to "#FFD700",  // Yellow
        "Low" to "#D3D3D3"      // Dark Gray
    )

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.task_name)
        val priorityButton: Button = itemView.findViewById(R.id.task_priority)
        val status: Button = itemView.findViewById(R.id.task_status)
        val customerName: TextView = itemView.findViewById(R.id.customer_name)
        val personAssigned: TextView = itemView.findViewById(R.id.task_person_assigned)
        val startDate: TextView = itemView.findViewById(R.id.task_start_date)
        val moreImageView: ImageView = itemView.findViewById(R.id.more_image_view)
        val cardView: CardView = itemView.findViewById(R.id.card_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        // Set item background color based on position
        val colorIndex = position % 3  // Alternating background colors
        val backgroundColor = when (colorIndex) {
            0 -> Color.parseColor("#2D2D2D")
            1 -> Color.parseColor("#FFFFFF")
            2 -> Color.parseColor("#E8715C")
            else -> Color.parseColor("#FFFFFF")  // Default fallback
        }

        holder.cardView.setCardBackgroundColor(backgroundColor)

        // Set text color based on background color
        val textColor = if (backgroundColor == Color.WHITE) {
            // Use black text on white background
            Color.BLACK
        } else {
            // Use white text on any other background
            Color.WHITE
        }

        // Set text colors
        holder.nameTextView.setTextColor(textColor)
        holder.priorityButton.setTextColor(textColor)
        holder.customerName.setTextColor(textColor)
        holder.personAssigned.setTextColor(textColor)
        holder.startDate.setTextColor(textColor)

        Log.d("TEST44", "Old date: ${task.startDate}")
        val formattedDate = task.startDate.replace("-", "/")
        holder.startDate.text = formattedDate
        Log.d("TEST44", "New date:$formattedDate")

        holder.nameTextView.text = task.name
        holder.priorityButton.text = task.priorityLevel

        // Adjust the priority button text color based on the background color
        if (backgroundColor == Color.parseColor("#E8715C")) { // Red background
            holder.priorityButton.setBackgroundColor(Color.WHITE)
            // Set text color to black
            holder.priorityButton.setTextColor(Color.RED)
            holder.moreImageView.setColorFilter(Color.WHITE)
        } else {
            // Set button background color based on priority level
            val priorityColor = priorityColors[task.priorityLevel] ?: "#FFFFFF" // Default to white if not found
            holder.priorityButton.setBackgroundColor(Color.parseColor(priorityColor))
            // Use the determined text color
            holder.priorityButton.setTextColor(textColor)
        }

        if(backgroundColor == Color.parseColor("#E8715C") || backgroundColor == Color.parseColor("#2D2D2D")){
            holder.moreImageView.setColorFilter(Color.WHITE)
        }

        if(backgroundColor == Color.parseColor("#FFFFFF")){
            holder.status.setBackgroundColor(Color.parseColor("#2D2D2D"))
            holder.status.setTextColor(Color.WHITE)
        }

        holder.status.text = task.status
        getCustomerName(task.customerID!!) { customerName ->
            holder.customerName.text = customerName ?: "Unassigned customer"
        }
        holder.personAssigned.text = task.personAssigned
        holder.startDate.text = task.startDate

        // Set click listener for the more options icon
        holder.moreImageView.setOnClickListener {
            showPopupMenu(holder.moreImageView, task)
        }
    }

    override fun getItemCount(): Int = tasks.size

    private fun showPopupMenu(view: View, task: TaskModel) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.menu_items, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit_item -> {
                    // Call the edit click listener
                    Log.d("INF355", "Edit button called")
                    onEditClick(task)
                    true
                }
                R.id.delete_item -> {
                    // Call the delete click listener
                    onDeleteClick(task)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun getCustomerName(id: String, onResult: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val sbHelper = SupabaseHelper()
            val customer = sbHelper.getCustomerNameUsingID(id)

            // Switch to the Main thread to handle the result
            withContext(Dispatchers.Main) {
                onResult(customer.CustomerName)
            }
        }
    }

    fun updateTask(updatedTask: TaskModel) {
        val index = tasks.indexOfFirst { it.id == updatedTask.id }
        if (index != -1) {
            tasks[index] = updatedTask
            notifyItemChanged(index) // Notify the adapter of the item change
        }
    }

    fun updateTasks(newTasks: List<TaskModel>) {
        val oldSize = tasks.size
        tasks.clear()
        tasks.addAll(newTasks)
        // Notify that all old items were removed
        notifyItemRangeRemoved(0, oldSize)
        // Notify that new items were added
        notifyItemRangeInserted(0, newTasks.size)
    }

    fun removeTask(task: TaskModel) {
        val position = tasks.indexOfFirst { it.id == task.id }
        if (position != -1) {
            // Remove the task at the found position
            tasks.removeAt(position)
            // Notify that an item was removed
            notifyItemRemoved(position)
        }
    }
}

