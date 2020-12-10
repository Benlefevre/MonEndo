package com.benlefevre.monendo.doctor.repository

import com.benlefevre.monendo.doctor.api.AdresseService

class AdresseRepository(private val adresseService: AdresseService) {

    suspend fun getAdresses(map: Map<String, String>) = adresseService.getAdresses(map)
}