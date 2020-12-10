package com.benlefevre.monendo.doctor.api

import retrofit2.http.GET
import retrofit2.http.QueryMap

interface AdresseService {

    @GET("search/")
    suspend fun getAdresses(@QueryMap map: Map<String, String>): Adresse
}