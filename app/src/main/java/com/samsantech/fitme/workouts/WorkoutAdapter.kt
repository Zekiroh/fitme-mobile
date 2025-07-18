package com.samsantech.fitme.workouts

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.samsantech.fitme.R
import com.samsantech.fitme.model.WorkoutGroup
import com.samsantech.fitme.model.WorkoutItem

class WorkoutAdapter(private val workoutGroups: List<WorkoutGroup>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        var index = 0
        for (group in workoutGroups) {
            if (index == position) return TYPE_HEADER
            index++
            if (group.isExpanded) {
                if (position < index + group.items.size) return TYPE_ITEM
                index += group.items.size
            }
        }
        return TYPE_HEADER
    }

    override fun getItemCount(): Int {
        var count = 0
        for (group in workoutGroups) {
            count++ // for header
            if (group.isExpanded) {
                count += group.items.size
            }
        }
        return count
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_group_header, parent, false)
            GroupViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_workout, parent, false)
            ItemViewHolder(view)
        }
    }



    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var index = 0
        for ((groupIndex, group) in workoutGroups.withIndex()) {
            if (index == position && holder is GroupViewHolder) {
                holder.bind(group)
                holder.itemView.setOnClickListener {
                    group.isExpanded = !group.isExpanded
                    notifyDataSetChanged()
                }
                return
            }
            index++
            if (group.isExpanded) {
                for (item in group.items) {
                    if (index == position && holder is ItemViewHolder) {
                        holder.bind(item)
                        return
                    }
                    index++
                }
            }
        }
    }

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(group: WorkoutGroup) {
            itemView.findViewById<TextView>(R.id.groupTitle).text = group.description
        }
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(item: WorkoutItem) {
            itemView.findViewById<TextView>(R.id.workoutDetails).text =
                "Weight: ${item.weight}, Reps: ${item.reps}, Time: ${item.createdAt}"
        }
    }
}
