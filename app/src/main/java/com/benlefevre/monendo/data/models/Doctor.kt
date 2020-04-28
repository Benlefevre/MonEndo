package com.benlefevre.monendo.data.models

data class Doctor(
    val id: String, val name: String, val civilite: String?, val address: String,
    val spec: String, val convention: String?, val phone: String?, val actes: String?,
    val typesActes: String?, val coordonnees: List<Double>, var nbComment: Int = 0, var rating: Int = 0
)