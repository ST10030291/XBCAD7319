package com.vuca.xbcad7319_vucadigital.Adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.vuca.xbcad7319_vucadigital.R
import com.vuca.xbcad7319_vucadigital.db.SupabaseHelper
import com.vuca.xbcad7319_vucadigital.models.TaskModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// YouTube video
// Titled: Notes App - CRUD SQLite Database in Android Studio using Kotlin | Create Read Update Delete Data
// Upload by: Android Knowledge
// Available at: https://www.youtube.com/watch?v=BVAslimaGSk
class TaskAdapter(
    private var tasks: MutableList<TaskModel> = mutableListOf(),
    private val onEditClick: (TaskModel) -> Unit,
    private val onDeleteClick: (TaskModel) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    // Blog post
    // Titled: Kotlin Map : mapOf()
    // Posted by: manishkhetan
    // Available at: https://www.geeksforgeeks.org/kotlin-map-mapof/
    // Map of priority levels to their corresponding colors
    private val priorityColors = mapOf(
        "High" to "#E8715C",    // Red
        "Medium" to "#FFD700",  // Yellow
        "Low" to "#D3D3D3"      // Dark Gray
    )

    // ViewHolder class to hold references to the views in each item
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

    // Called when the ViewHolder is created
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    // Called to bind data to the ViewHolder
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        // Determine the background color based on the position
        val backgroundColor = when (position % 3) {
            0 -> Color.parseColor("#2D2D2D") // Dark Gray
            1 -> Color.WHITE // White
            2 -> Color.parseColor("#E8715C") // Red
            else -> Color.WHITE // Default to White
        }
        holder.cardView.setCardBackgroundColor(backgroundColor)

        // Determine text color based on the background color
        val textColor = if (backgroundColor == Color.WHITE) Color.BLACK else Color.WHITE
        setTextColor(holder, textColor)

        // Format and set the task start date
        holder.startDate.text = task.startDate.replace("-", "/")
        holder.nameTextView.text = task.name
        holder.priorityButton.text = task.priorityLevel
        holder.status.text = task.status

        // Set the priority button appearance
        setPriorityButton(holder, task, backgroundColor, textColor)
        // Set the status button appearance
        setStatusButton(holder, backgroundColor)

        // Set assigned person and customer name
        holder.personAssigned.text = task.personAssigned
        getCustomerName(task.customerID!!) { customerName ->
            holder.customerName.text = customerName ?: "Unassigned customer"
        }

        // Set click listener for the more options icon
        holder.moreImageView.setOnClickListener {
            showPopupMenu(holder.moreImageView, task)
        }

        // Set the color of the moreImageView based on the background color
        setMoreImageViewColor(holder.moreImageView, backgroundColor)
    }

    // Returns the total number of items in the adapter
    override fun getItemCount(): Int = tasks.size

    // Sets the text color for the relevant views
    private fun setTextColor(holder: TaskViewHolder, textColor: Int) {
        holder.nameTextView.setTextColor(textColor)
        holder.priorityButton.setTextColor(textColor)
        holder.customerName.setTextColor(textColor)
        holder.personAssigned.setTextColor(textColor)
        holder.startDate.setTextColor(textColor)
    }

    // Configures the priority button appearance
    private fun setPriorityButton(holder: TaskViewHolder, task: TaskModel, backgroundColor: Int, textColor: Int) {
        // Red background
        if (backgroundColor == Color.parseColor("#E8715C")) {
            holder.priorityButton.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            //holder.priorityButton.setBackgroundColor(Color.WHITE)
            holder.priorityButton.setTextColor(Color.RED)
        } else {
            // Set background color based on priority level
            val newColor = Color.parseColor(priorityColors[task.priorityLevel] ?: "#FFFFFF")
            holder.priorityButton.backgroundTintList = ColorStateList.valueOf(newColor)
            //holder.priorityButton.setBackgroundColor(Color.parseColor(priorityColors[task.priorityLevel] ?: "#FFFFFF"))
            holder.priorityButton.setTextColor(textColor)
        }
    }

    // Configures the status button appearance
    private fun setStatusButton(holder: TaskViewHolder, backgroundColor: Int) {
        // Determine the tint color based on the cardview background color
        val tintColor = if (backgroundColor == Color.parseColor("#2D2D2D") ||
            backgroundColor == Color.parseColor("#E8715C")) {
            Color.WHITE
        } else {
            // Set background to dark grey
            Color.parseColor("#2D2D2D")
        }

        // Set the background tint
        holder.status.backgroundTintList = ColorStateList.valueOf(tintColor)

        // Set the text color based on the background color
        holder.status.setTextColor(if (backgroundColor == Color.parseColor("#2D2D2D") ||
            backgroundColor == Color.parseColor("#E8715C")) Color.BLACK else Color.WHITE)
    }

    // Sets the color of the moreImageView based on the background color
    private fun setMoreImageViewColor(imageView: ImageView, backgroundColor: Int) {
        if (backgroundColor == Color.parseColor("#E8715C") || backgroundColor == Color.parseColor("#2D2D2D")) {
            // Set to white for red or grey backgrounds
            imageView.setColorFilter(Color.WHITE)
        } else {
            // Reset filter for other backgrounds
            imageView.clearColorFilter()
        }
    }

    // Shows the popup menu for editing or deleting a task
    private fun showPopupMenu(view: View, task: TaskModel) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.menu_items, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit_item -> {
                    // Call edit listener
                    onEditClick(task)
                    true
                }
                R.id.delete_item -> {
                    // Call delete listener
                    onDeleteClick(task)
                    true
                }
                else -> false
            }
        }
        // Show the popup menu
        popupMenu.show()
    }

    // StackOverflow post
    // Title: How to use LifecycleScope to execute coroutine
    // Posted by: Arpit Shukla
    // Available at: https://stackoverflow.com/questions/70058423/how-to-use-lifecyclescope-to-execute-coroutine
    // Fetches the customer name using the provided ID
    private fun getCustomerName(id: String, onResult: (String?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val sbHelper = SupabaseHelper()
            val customer = sbHelper.getCustomerNameUsingID(id)
            withContext(Dispatchers.Main) {
                // Return the customer name on the main thread
                onResult(customer.CustomerName)
            }
        }
    }

    // Updates an existing task in the adapter
    fun updateTask(updatedTask: TaskModel) {
        val index = tasks.indexOfFirst { it.id == updatedTask.id }
        if (index != -1) {
            tasks[index] = updatedTask
            // Notify the adapter of the item change
            notifyItemChanged(index)
        }
    }

    // Updates the entire list of tasks in the adapter
    fun updateTasks(newTasks: List<TaskModel>) {
        val oldSize = tasks.size
        tasks.clear()
        tasks.addAll(newTasks)
        // Notify that old items were removed
        notifyItemRangeRemoved(0, oldSize)
        // Notify that new items were added
        notifyItemRangeInserted(0, newTasks.size)
    }

    // Removes a task from the adapter
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




