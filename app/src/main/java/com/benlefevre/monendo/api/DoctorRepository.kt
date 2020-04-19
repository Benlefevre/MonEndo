package com.benlefevre.monendo.api

class DoctorRepository(private val cpamService: CpamService) {

    suspend fun getDoctors(map: Map<String, String>) = cpamService.getDoctors(map)
}