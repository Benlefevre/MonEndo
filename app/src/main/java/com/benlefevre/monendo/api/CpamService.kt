package com.benlefevre.monendo.api

import retrofit2.http.GET
import retrofit2.http.QueryMap

interface CpamService {

    @GET("api/records/1.0/search/")
    suspend fun getDoctors(@QueryMap map: Map<String, String>) : ResultApi
}