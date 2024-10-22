package com.example.xbcad7319_vucadigital.Adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.xbcad7319_vucadigital.R
import com.example.xbcad7319_vucadigital.models.OpportunityModel
import com.example.xbcad7319_vucadigital.models.TaskModel

class OpportunityAdapter (private var opportunities: MutableList<OpportunityModel> = mutableListOf(),
                          //private val onEditClick: (OpportunityModel) -> Unit,
                          private val onDeleteClick: (OpportunityModel) -> Unit
    ) : RecyclerView.Adapter<OpportunityAdapter.OpportunityViewHolder>() {

        class OpportunityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val opportunityName: TextView = itemView.findViewById(R.id.opportunityNameText)
            val valueText : TextView = itemView.findViewById(R.id.valueText)
            val stage : TextView = itemView.findViewById(R.id.labelStage)
            val status : TextView = itemView.findViewById(R.id.displayStatus)
            val customerName : TextView = itemView.findViewById(R.id.displayName)
            val priority : TextView = itemView.findViewById(R.id.displayPriority)
            val creationDate : TextView = itemView.findViewById(R.id.displayCreationDate)
            val moreImageView: ImageView = itemView.findViewById(R.id.more_image_view)
            //val editButton: Button = itemView.findViewById(R.id.task_edit_button)
           // val deleteButton: Button = itemView.findViewById(R.id.task_delete_button)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OpportunityViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.opportunity_item, parent, false)
            return OpportunityViewHolder(view)
        }

        override fun onBindViewHolder(holder: OpportunityViewHolder, position: Int) {
            val opportunity = opportunities[position]
            holder.opportunityName.text = opportunity.OpportunityName
            holder.valueText.text = "R" + opportunity.TotalValue.toString()
            holder.stage.text = opportunity.Stage // For now
            holder.customerName.text = opportunity.CustomerName // For now
            holder.status.text = opportunity.Status
            holder.creationDate.text = opportunity.CreationDate
            holder.priority.text = opportunity.Priority

            holder.moreImageView.setOnClickListener {
                showPopupMenu(holder.moreImageView, opportunity)
            }
            /*holder.moreImageView.setOnClickListener {
                showPopupMenu(holder.moreImageView, opportunity)
            }*/
        }
   /* private fun showPopupMenu(view: View, opportunity: OpportunityModel) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.menu_items, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit_item -> {
                    onEditClick(opportunity) // Call the edit click listener
                    true
                }
                R.id.delete_item -> {
                    onDeleteClick(opportunity) // Call the delete click listener
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }*/

        override fun getItemCount(): Int = opportunities.size

    private fun showPopupMenu(view: View, opportunity: OpportunityModel) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.menu_items, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit_item -> {
                    // Call the edit click listener
                   // Log.d("INF355", "Edit button called")
                    //onEditClick(task)
                    true
                }
                R.id.delete_item -> {
                    // Call the delete click listener
                    onDeleteClick(opportunity)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
    fun removeOpportunity(opportunity: OpportunityModel) {
        val position = opportunities.indexOfFirst { it.id == opportunity.id }
        if (position != -1) {
            // Remove the opportunity at the found position
            opportunities.removeAt(position)  // Change this line to use 'opportunities' list
            // Notify that an item was removed
            notifyItemRemoved(position)
        }
    }

       /* private fun showPopupMenu(view: View, opportunity: OpportunityModel) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.menu_items, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.edit_item -> {
                        onEditClick(opportunity) // Call the edit click listener
                        true
                    }
                    R.id.delete_item -> {
                        onDeleteClick(opportunity) // Call the delete click listener
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }*/

    fun updateOpportunity(newOpportuniy: List<OpportunityModel>) {
        Log.d("OpportunityAdapter", "Updating opportunities: ${newOpportuniy.size} items")
        opportunities = newOpportuniy.toMutableList()
        notifyDataSetChanged()
    }
    }