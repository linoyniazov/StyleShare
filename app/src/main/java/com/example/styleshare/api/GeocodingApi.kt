package com.example.styleshare.api

import retrofit2.http.GET
import retrofit2.http.Query

data class GeocodingResponse(
    val lat: Double,
    val lon: Double,
    val name: String,
    val country: String,
    val state: String?
)

interface GeocodingApi {
    @GET("geo/1.0/direct")
    suspend fun getCoordinates(
        @Query("q") location: String,
        @Query("limit") limit: Int = 1,
        @Query("appid") apiKey: String
    ): List<GeocodingResponse>
}