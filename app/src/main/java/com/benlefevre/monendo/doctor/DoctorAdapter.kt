package com.benlefevre.monendo.doctor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.benlefevre.monendo.R
import com.benlefevre.monendo.doctor.models.Doctor
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.doctor_list_item.view.*
import kotlinx.android.synthetic.main.rating_stars.view.*

class DoctorAdapter(
    private val doctors: List<Doctor>,
    private val listener: DoctorListAdapterListener
) : RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder>(),
    View.OnClickListener {

    private lateinit var context: Context

    interface DoctorListAdapterListener {
        fun onDoctorSelected(doctor: Doctor)
    }

    class DoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val doctorItem: ConstraintLayout = itemView.doctor_item_root
        val doctorName: MaterialTextView = itemView.doctor_item_name
        val doctorSpec: MaterialTextView = itemView.doctor_item_spec
        val doctorAddress: MaterialTextView = itemView.doctor_item_address
        val doctorComment: MaterialTextView = itemView.doctor_item_commentary
        val doctorPhone: MaterialTextView = itemView.doctor_item_phone
        val ratingStar: View = itemView.doctor_stars
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.doctor_list_item, parent, false)
        return DoctorViewHolder(view)
    }

    override fun getItemCount(): Int = doctors.size

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = doctors[position]
        with(holder) {
            doctorItem.setOnClickListener(this@DoctorAdapter)
            doctorItem.tag = doctor
            doctorName.text = doctor.name
            doctorSpec.text = doctor.spec
            doctorAddress.text = doctor.address
            doctorPhone.text = doctor.phone ?: context.getString(R.string.no_phone)
            doctorComment.text =
                if (doctor.nbComment != 0) context.getString(
                    R.string.doctor_nb_comment,
                    doctor.nbComment
                ) else context.getString(R.string.no_doctor_comment)
            defineNbStars(doctor.rating, holder)
        }
    }

    private fun defineNbStars(doctorRating: Double, holder: DoctorViewHolder) {
        when (doctorRating) {
            in 0.1..0.5 -> {
                holder.ratingStar.star1.setImageResource(R.drawable.half_star)
                holder.ratingStar.star2.setImageResource(R.drawable.empty_star)
                holder.ratingStar.star3.setImageResource(R.drawable.empty_star)
                holder.ratingStar.star4.setImageResource(R.drawable.empty_star)
                holder.ratingStar.star5.setImageResource(R.drawable.empty_star)
            }
            in 0.6..1.0 -> {
                holder.ratingStar.star1.setImageResource(R.drawable.full_star)
                holder.ratingStar.star2.setImageResource(R.drawable.empty_star)
                holder.ratingStar.star3.setImageResource(R.drawable.empty_star)
                holder.ratingStar.star4.setImageResource(R.drawable.empty_star)
                holder.ratingStar.star5.setImageResource(R.drawable.empty_star)
            }
            in 1.1..1.5 -> {
                holder.ratingStar.star1.setImageResource(R.drawable.full_star)
                holder.ratingStar.star2.setImageResource(R.drawable.half_star)
                holder.ratingStar.star3.setImageResource(R.drawable.empty_star)
                holder.ratingStar.star4.setImageResource(R.drawable.empty_star)
                holder.ratingStar.star5.setImageResource(R.drawable.empty_star)
            }
            in 1.6..2.0 -> {
                holder.ratingStar.star1.setImageResource(R.drawable.full_star)
                holder.ratingStar.star2.setImageResource(R.drawable.full_star)
                holder.ratingStar.star3.setImageResource(R.drawable.empty_star)
                holder.ratingStar.star4.setImageResource(R.drawable.empty_star)
                holder.ratingStar.star5.setImageResource(R.drawable.empty_star)
            }
            in 2.1..2.5 -> {
                holder.ratingStar.star1.setImageResource(R.drawable.full_star)
                holder.ratingStar.star2.setImageResource(R.drawable.full_star)
                holder.ratingStar.star3.setImageResource(R.drawable.half_star)
                holder.ratingStar.star4.setImageResource(R.drawable.empty_star)
                holder.ratingStar.star5.setImageResource(R.drawable.empty_star)
            }
            in 2.6..3.0 -> {
                holder.ratingStar.star1.setImageResource(R.drawable.full_star)
                holder.ratingStar.star2.setImageResource(R.drawable.full_star)
                holder.ratingStar.star3.setImageResource(R.drawable.full_star)
                holder.ratingStar.star4.setImageResource(R.drawable.empty_star)
                holder.ratingStar.star5.setImageResource(R.drawable.empty_star)
            }
            in 3.1..3.5 -> {
                holder.ratingStar.star1.setImageResource(R.drawable.full_star)
                holder.ratingStar.star2.setImageResource(R.drawable.full_star)
                holder.ratingStar.star3.setImageResource(R.drawable.full_star)
                holder.ratingStar.star4.setImageResource(R.drawable.half_star)
                holder.ratingStar.star5.setImageResource(R.drawable.empty_star)
            }
            in 3.6..4.0 -> {
                holder.ratingStar.star1.setImageResource(R.drawable.full_star)
                holder.ratingStar.star2.setImageResource(R.drawable.full_star)
                holder.ratingStar.star3.setImageResource(R.drawable.full_star)
                holder.ratingStar.star4.setImageResource(R.drawable.full_star)
                holder.ratingStar.star5.setImageResource(R.drawable.empty_star)
            }
            in 4.1..4.5 -> {
                holder.ratingStar.star1.setImageResource(R.drawable.full_star)
                holder.ratingStar.star2.setImageResource(R.drawable.full_star)
                holder.ratingStar.star3.setImageResource(R.drawable.full_star)
                holder.ratingStar.star4.setImageResource(R.drawable.full_star)
                holder.ratingStar.star5.setImageResource(R.drawable.half_star)
            }
            in 4.6..5.0 -> {
                holder.ratingStar.star1.setImageResource(R.drawable.full_star)
                holder.ratingStar.star2.setImageResource(R.drawable.full_star)
                holder.ratingStar.star3.setImageResource(R.drawable.full_star)
                holder.ratingStar.star4.setImageResource(R.drawable.full_star)
                holder.ratingStar.star5.setImageResource(R.drawable.full_star)
            }
            else -> {
                holder.ratingStar.star1.setImageResource(R.drawable.empty_star)
                holder.ratingStar.star2.setImageResource(R.drawable.empty_star)
                holder.ratingStar.star3.setImageResource(R.drawable.empty_star)
                holder.ratingStar.star4.setImageResource(R.drawable.empty_star)
                holder.ratingStar.star5.setImageResource(R.drawable.empty_star)
            }
        }
    }

    override fun onClick(v: View?) {
        listener.onDoctorSelected(v?.tag as Doctor)
    }
}