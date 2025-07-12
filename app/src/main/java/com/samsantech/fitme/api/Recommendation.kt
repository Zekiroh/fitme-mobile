package com.samsantech.fitme.api

import com.samsantech.fitme.model.Workout
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface Recommendation {
    @GET("api/recommendations")
    fun getRecommendations(
        @Query("goal") goal: String,
        @Query("level") level: String
    ): Call<List<Workout>>
}