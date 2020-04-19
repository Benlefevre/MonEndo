package com.benlefevre.monendo.data.models

data class Doctor(
    val id: String, val name: String, val civilite: String?, val address: String?,
    val spec: String?, val convention: String?, val phone: String?, val actes: String?,
    val typesActes: String?, val coordonnees: List<Double>?, val nbComment: Int = 0, val rating: Int = 0
)