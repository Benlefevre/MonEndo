package com.benlefevre.monendo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.benlefevre.monendo.R
import com.benlefevre.monendo.data.models.UserActivities
import kotlinx.android.synthetic.main.activity_list_item.view.*

class UserActivityAdapter(val activities : List<UserActivities>) : RecyclerView.Adapter<UserActivityViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserActivityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_list_item,parent,false)
        return UserActivityViewHolder(view)
    }


    override fun onBindViewHolder(holder: UserActivityViewHolder, position: Int) {
        holder.updateUi(activities[position])
    }

    override fun getItemCount(): Int {
        return activities.size
    }
}

class UserActivityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun updateUi(userActivities: UserActivities){
        itemView.activity_item_name.text = userActivities.name
        itemView.activity_duration_time.text = userActivities.duration.toString()
        itemView.activity_item_intensity_value.text = userActivities.intensity.toString()
    }
}