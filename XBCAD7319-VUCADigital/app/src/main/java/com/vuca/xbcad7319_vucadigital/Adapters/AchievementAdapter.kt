package com.vuca.xbcad7319_vucadigital.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.vuca.xbcad7319_vucadigital.R
import com.vuca.xbcad7319_vucadigital.models.AchievementModel
import com.mackhartley.roundedprogressbar.RoundedProgressBar
import com.squareup.picasso.Picasso

class AchievementAdapter(context: Context, achievements: List<AchievementModel>) :
    ArrayAdapter<AchievementModel>(context, 0, achievements) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.achievement_grid_item, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val achievement = getItem(position)

        achievement?.let {
            viewHolder.nameTextView.text = achievement.Name
            viewHolder.descriptionTextView.text = achievement.Description

            Picasso.get()
                .load(achievement.ImageUrl)
                .into(viewHolder.imageView);

            val progressPercentage = if (achievement.Target > 0) {
                (achievement.Current.toDouble() / achievement.Target) * 100
            } else {
                0.0
            }

            viewHolder.progressBar.setProgressPercentage(progressPercentage.coerceAtMost(100.0), true)

            if (it.Status == "completed") {
                viewHolder.progressBar.visibility = View.GONE
                viewHolder.isCompletedTextView.visibility = View.VISIBLE
            } else {
                viewHolder.progressBar.visibility = View.VISIBLE
                viewHolder.isCompletedTextView.visibility = View.GONE
            }
        }

        return view
    }

    inner class ViewHolder(view: View) {
        val nameTextView: TextView = view.findViewById(R.id.Title)
        val descriptionTextView: TextView = view.findViewById(R.id.Description)
        val imageView: ImageView = view.findViewById(R.id.Image)
        val progressBar: RoundedProgressBar = view.findViewById(R.id.ProgressBar)
        val isCompletedTextView: TextView = view.findViewById(R.id.isCompleted)
    }

}
