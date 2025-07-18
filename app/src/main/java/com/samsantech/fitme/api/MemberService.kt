package com.samsantech.fitme.api

import com.samsantech.fitme.model.AddWorkoutResponse
import com.samsantech.fitme.model.CoachNote
import com.samsantech.fitme.model.GetWorkoutResponses
import com.samsantech.fitme.model.MembershipPlan
import com.samsantech.fitme.model.ResponseSuccess
import com.samsantech.fitme.model.WeeklyWorkoutResponse
import com.samsantech.fitme.model.WorkoutInput
import com.samsantech.fitme.model.WorkoutResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface MemberService {
    @DELETE("members/del/{userid}")
    fun deleteMembersAccount(@Path("userid") userId: Int): Call<ResponseSuccess>

    @GET("members/get-membership/{userid}")
    fun getMemberShip(@Path("userid") userId: Int):
            Call<MembershipPlan>

    @GET("members/coach-notes/{userid}")
    fun getCouchNotes(@Path("userid") userId: Int): Call<CoachNote>

    @Headers("Content-Type: application/json")
    @POST("members/add-workout/{userid}")
    fun addWorkout(@Path("userid") userId: Int, @Body payload: WorkoutInput): Call<AddWorkoutResponse>

    @GET("members/get-workout/{userid}")
    fun getWorkouts(@Path("userid") userId: Int): Call<GetWorkoutResponses>

    @GET("members/get-workout-weekly/{userid}")
    fun getWorkoutWeekly(@Path("userid") userId: Int): Call<WeeklyWorkoutResponse>
}