package com.benlefevre.monendo.treatment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.benlefevre.monendo.databinding.TreatmentListItemBinding
import com.benlefevre.monendo.treatment.models.Treatment

class TreatmentAdapter(private val treatments : List<Treatment>) : RecyclerView.Adapter<TreatmentViewHolder>() {

    private lateinit var listener : View.OnClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TreatmentViewHolder {
        val binding = TreatmentListItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        val viewHolder =
            TreatmentViewHolder(binding)
        viewHolder.binding.itemDeleteBtn.setOnClickListener {
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

class TreatmentViewHolder(val binding: TreatmentListItemBinding) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.itemDeleteBtn.tag = this
    }

    fun updateUi(treatment: Treatment){
        binding.itemName.text = treatment.name
        binding.durationTime.text = treatment.duration
        var hours = ""
        if (treatment.morning != "")
            hours += treatment.morning
        if (treatment.noon != "")
            hours += " + ${treatment.noon}"
        if (treatment.afternoon != "")
            hours += " + ${treatment.afternoon}"
        if (treatment.evening != "")
            hours += " + ${treatment.evening}"

        binding.itemHourValue.text = hours
    }
}