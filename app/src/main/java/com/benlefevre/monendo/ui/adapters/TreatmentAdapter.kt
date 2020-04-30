package com.benlefevre.monendo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.benlefevre.monendo.R
import com.benlefevre.monendo.data.models.Treatment
import kotlinx.android.synthetic.main.treatment_list_item.view.*

class TreatmentAdapter(private val treatments : List<Treatment>) : RecyclerView.Adapter<TreatmentViewHolder>() {

    private lateinit var listener : View.OnClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TreatmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.treatment_list_item,parent,false)
        val viewHolder = TreatmentViewHolder(view)
        viewHolder.itemView.treatment_item_delete_btn.setOnClickListener {
            listener.onClick(it)
        }
        return viewHolder
    }


    override fun onBindViewHolder(holder: TreatmentViewHolder, position: Int) {
        holder.updateUi(treatments[position])
    }

    override fun getItemCount(): Int {
        return treatments.size
    }

    fun setOnClickListener(clickListener: View.OnClickListener){
        listener = clickListener
    }
}

class TreatmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    init {
        itemView.treatment_item_delete_btn.tag = this
    }

    fun updateUi(treatment: Treatment){
        itemView.treatment_item_name.text = treatment.name
        itemView.treatment_duration_time.text = treatment.duration
        var hours = ""
        if (treatment.morning != "")
            hours += treatment.morning
        if (treatment.noon != "")
            hours += " + ${treatment.noon}"
        if (treatment.afternoon != "")
            hours += " + ${treatment.afternoon}"
        if (treatment.evening != "")
            hours += " + ${treatment.evening}"

        itemView.treatment_item_hour_value.text = hours
    }
}