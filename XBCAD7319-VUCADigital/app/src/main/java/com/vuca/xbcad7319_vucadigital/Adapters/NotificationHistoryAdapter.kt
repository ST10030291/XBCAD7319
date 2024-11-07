package com.vuca.xbcad7319_vucadigital.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.vuca.xbcad7319_vucadigital.R
import com.vuca.xbcad7319_vucadigital.models.NotificationHistoryModel


class NotificationHistoryAdapter(
    private val notifications: MutableList<NotificationHistoryModel>,
    private val onHide: (Int) -> Unit,
    private val onShow: (Int) -> Unit,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<NotificationHistoryAdapter.NotificationHistoryViewHolder>() {

    class NotificationHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val customerName: TextView = itemView.findViewById(R.id.textCustomerName)
        val message: TextView = itemView.findViewById(R.id.textMessage)
        val dateTime: TextView = itemView.findViewById(R.id.textDateTime)
        val imageHide: ImageView = itemView.findViewById(R.id.imageHide)
        val imageDelete: ImageView = itemView.findViewById(R.id.imageDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationHistoryViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_notification_history, parent, false)
        return NotificationHistoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NotificationHistoryViewHolder, position: Int) {
        val notifications = notifications[position]
        holder.customerName.text = notifications.customerName
        holder.message.text = notifications.message
        holder.dateTime.text = notifications.dateTime

        if(notifications.visible == true){
           updateVisibilityIcon(holder.imageHide, true)
        }
        else{
            updateVisibilityIcon(holder.imageHide, false)
        }

        holder.imageHide.setOnClickListener {
            if(notifications.visible == true){
                onHide(position)
                updateVisibilityIcon(holder.imageHide, true)
            }
            else{
                onShow(position)
                updateVisibilityIcon(holder.imageHide, false)
            }
        }
        holder.imageDelete.setOnClickListener { onDelete(position) }
    }

    override fun getItemCount(): Int = notifications.size

//    // Update a single notification in the adapter
//    fun updateNotification(updatedNotification: NotificationHistoryModel) {
//        val index = notifications.indexOfFirst { it.id == updatedNotification.id }
//        if (index != -1) {
//            notifications[index] = updatedNotification
//            // Notify the adapter that the item at 'index' has changed
//            notifyItemChanged(index)
//        }
//    }
//
//    // Update the entire list of notifications
//    fun updateNotifications(newNotifications: List<NotificationHistoryModel>) {
//        val oldSize = notifications.size
//        notifications.clear()
//        notifications.addAll(newNotifications)
//        // Notify that old items were removed
//        notifyItemRangeRemoved(0, oldSize)
//        // Notify that new items were added
//        notifyItemRangeInserted(0, newNotifications.size)
//    }
//
//    // Remove a notification from the adapter
//    fun removeNotification(notification: NotificationHistoryModel) {
//        val position = notifications.indexOfFirst { it.id == notification.id }
//        if (position != -1) {
//            // Remove the notification at the found position
//            notifications.removeAt(position)
//            // Notify that an item was removed
//            notifyItemRemoved(position)
//        }
//    }

    fun updateData(newNotifications: List<NotificationHistoryModel>) {
        val diffCallback = NotificationHistoryDiffCallback(notifications, newNotifications)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        notifications.clear()
        notifications.addAll(newNotifications)
        diffResult.dispatchUpdatesTo(this)
    }

    private fun updateVisibilityIcon(imageView: ImageView, isVisible: Boolean) {
        if (isVisible) {
            imageView.setImageResource(R.drawable.visibility_off_icon)
        } else {
            imageView.setImageResource(R.drawable.visibility_on_icon)
        }
    }
}

class NotificationHistoryDiffCallback(
   private val oldList: List<NotificationHistoryModel>,
    private val newList: List<NotificationHistoryModel>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}

