package com.benlefevre.monendo.doctor.repository

import com.benlefevre.monendo.doctor.api.CpamService

class DoctorRepository(private val cpamService: CpamService) {

    suspend fun getDoctors(map: Map<String, String>) = cpamService.getDoctors(map)
}