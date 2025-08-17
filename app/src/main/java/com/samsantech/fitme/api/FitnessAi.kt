package com.samsantech.fitme.api

import com.samsantech.fitme.model.WorkoutPlanRequest
import com.samsantech.fitme.model.WorkoutPlanResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface FitnessAi {
    @POST("fitness-ai")
    fun getAiWorkoutFitness(@Body param: WorkoutPlanRequest): Call<WorkoutPlanResponse>
}