package com.benlefevre.monendo.doctor.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.RecyclerView
import com.benlefevre.monendo.R
import com.benlefevre.monendo.databinding.DoctorListItemBinding
import com.benlefevre.monendo.doctor.defineNbStars
import com.benlefevre.monendo.doctor.models.Doctor

class DoctorAdapter(
    private val doctors: List<Doctor>,
    private val listener: DoctorListAdapterListener
) : RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder>(){

    interface DoctorListAdapterListener {
        fun onDoctorSelected(doctor: Doctor)
    }

    private lateinit var context: Context
    var index = -1

    class DoctorViewHolder(val binding: DoctorListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        context = parent.context
        val binding = DoctorListItemBinding.inflate(LayoutInflater.from(context),parent,false)
        return DoctorViewHolder(binding)
    }

    override fun getItemCount(): Int = doctors.size

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = doctors[position]
        with(holder) {
            binding.root.setOnClickListener{
                index = position
                listener.onDoctorSelected(it.tag as Doctor)
                notifyDataSetChanged()
            }
            binding.root.tag = doctor
            binding.itemName.text = doctor.name
            binding.itemSpec.text = doctor.spec
            binding.itemAddress.text = doctor.address
            binding.itemPhone.text = doctor.phone ?: context.getString(R.string.no_phone)
            binding.itemCommentary.text =
                if (doctor.nbComment != 0) context.getString(
                    R.string.doctor_nb_comment,
                    doctor.nbComment
                ) else context.getString(R.string.no_doctor_comment)
            defineNbStars(doctor.rating, binding.doctorStars)
            if (index == position){
                binding.root.setBackgroundColor(getColor(context,R.color.itemSelected))
            }else{
                binding.root.setBackgroundColor(getColor(context,R.color.colorOnPrimary))
            }
        }
    }
}