package com.benlefevre.monendo.doctor

import com.benlefevre.monendo.doctor.api.ResultApi
import com.benlefevre.monendo.doctor.models.Doctor

fun createDoctorsFromCpamApi(response : ResultApi) : List<Doctor>{
    val doctors = mutableListOf<Doctor>()
    response.records.forEach {
        doctors.add(
            Doctor(
                it.recordid,
                it.fields.nom,
                it.fields.civilite,
                it.fields.adresse,
                it.fields.libelle_profession,
                it.fields.convention,
                it.fields.telephone,
                it.fields.actes,
                it.fields.types_actes,
                it.fields.coordonnees,
                it.fields.dist
            )
        )
    }
    return doctors
}