package com.benlefevre.monendo.doctor.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Doctor(
    val id: String, val name: String, val civilite: String?, val address: String,
    val spec: String, val convention: String?, val phone: String?, val actes: String?,
    val typesActes: String?, val coordonnees: List<Double>, val dist : Double = 0.0 ,var nbComment: Int = 0, var rating: Double = 0.0
) : Parcelable