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
            defineNbStars(doctor.rating, ratingStar)
        }
    }

    override fun onClick(v: View?) {
        listener.onDoctorSelected(v?.tag as Doctor)
    }
}