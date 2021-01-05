package com.benlefevre.monendo.treatment

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.benlefevre.monendo.R
import com.benlefevre.monendo.databinding.TreatmentListItemBinding
import com.benlefevre.monendo.treatment.models.Treatment
import com.google.android.material.chip.Chip

class TreatmentAdapter(private val treatments: List<Treatment>) :
    RecyclerView.Adapter<TreatmentViewHolder>() {

    private lateinit var listener: View.OnClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TreatmentViewHolder {
        val binding =
            TreatmentListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder =
            TreatmentViewHolder(binding, parent.context)
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

    fun setOnClickListener(clickListener: View.OnClickListener) {
        listener = clickListener
    }
}

class TreatmentViewHolder(val binding: TreatmentListItemBinding, val context: Context) :
    RecyclerView.ViewHolder(binding.root) {

    init {
        binding.itemDeleteBtn.tag = this
    }

    fun updateUi(treatment: Treatment) {
        binding.itemName.text = treatment.name
        binding.durationTime.text = treatment.duration
        binding.chipGroup.removeAllViews()
        if (treatment.morning != "") {
            binding.chipGroup.addView(createChip(1, treatment))
        }
        if (treatment.noon != "") {
            binding.chipGroup.addView(createChip(2, treatment))
        }
        if (treatment.afternoon != "") {
            binding.chipGroup.addView(createChip(3, treatment))
        }
        if (treatment.evening != "") {
            binding.chipGroup.addView(createChip(4, treatment))
        }
    }

    private fun createChip(origin: Int, treatment: Treatment): Chip {
        val chip = LayoutInflater.from(context)
            .inflate(R.layout.single_chip, binding.chipGroup, false) as Chip
        chip.apply {
            when (origin) {
                1 -> {
                    text = treatment.morning
                    isChecked = treatment.isTakenMorning
                    setOnCheckedChangeListener { _, isChecked ->
                        treatment.isTakenMorning = isChecked
                    }
                }
                2 -> {
                    text = treatment.noon
                    isChecked = treatment.isTakenNoon
                    setOnCheckedChangeListener { _, isChecked ->
                        treatment.isTakenNoon = isChecked
                    }
                }
                3 -> {
                    text = treatment.afternoon
                    isChecked = treatment.isTakenAfternoon
                    setOnCheckedChangeListener { _, isChecked ->
                        treatment.isTakenAfternoon = isChecked
                    }
                }
                4 -> {
                    text = treatment.evening
                    isChecked = treatment.isTakenEvening
                    setOnCheckedChangeListener { _, isChecked ->
                        treatment.isTakenEvening = isChecked
                    }
                }
            }
        }
        return chip
    }
}