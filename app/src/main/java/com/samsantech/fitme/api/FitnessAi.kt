package com.samsantech.fitme.api

import com.samsantech.fitme.model.WorkoutPlanRequest
import com.samsantech.fitme.model.WorkoutPlanResponse
import com.samsantech.fitme.model.CustomWorkoutRequest
import com.samsantech.fitme.model.CustomWorkoutResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface FitnessAi {
    @POST("fitness-ai")
    fun getAiWorkoutFitness(@Body param: WorkoutPlanRequest): Call<WorkoutPlanResponse>
    
    @POST("fitness-ai/custom-workout/{userId}")
    fun saveCustomWorkout(@Path("userId") userId: Int, @Body request: CustomWorkoutRequest): Call<CustomWorkoutResponse>
    
    @GET("fitness-ai/get-workout-custom/{userId}")
    fun getCustomWorkouts(@Path("userId") userId: Int): Call<CustomWorkoutResponse>
    
    @PUT("fitness-ai/update-custom-workout/{workoutId}")
    fun markWorkoutAsDone(@Path("workoutId") workoutId: Int): Call<CustomWorkoutResponse>
}