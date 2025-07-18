package com.samsantech.fitme.model

import com.google.gson.annotations.SerializedName

data class MembershipPlan(
    @SerializedName("id")
    val id: Int,
    @SerializedName("plan")
    val plan: String,
    @SerializedName("price")
    val price: String,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String,
    @SerializedName("payment_method")
    val paymentMethod: String,
    @SerializedName("gcash_number")
    val gcashNumber: String?,
    @SerializedName("gcash_name")
    val gcashName: String?,
    @SerializedName("card_number")
    val cardNumber: String?,
    @SerializedName("card_name")
    val cardName: String?,
    @SerializedName("card_expiry")
    val cardExpiry: String?,
    @SerializedName("card_cvv")
    val cardCvv: String?,
    @SerializedName("status")
    val status: String
)


data class CoachNote(
    @SerializedName("id")
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("coach_notes")
    val coachNotes: String,
    @SerializedName("last_workout")
    val lastWorkout: String,
    @SerializedName("current_goal")
    val currentGoal: String
)

data class Workouts(
    @SerializedName("id")
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("weight")
    val weight: String,
    @SerializedName("reps")
    val reps: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("created_at")
    val createdAt: String
)

data class WorkoutResponse(
    val message: String,
    val workout: Workouts
)


data class AddWorkoutResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("workoutId")
    val workoutId: Int
)
data class WorkoutInput(
    @SerializedName("weight")
    val weight: String,

    @SerializedName("reps")
    val reps: String,

    @SerializedName("description")
    val description: String
)

data class WeeklyWorkoutResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("workouts")
    val workouts: List<WorkoutDay>
)

data class WorkoutDay(
    @SerializedName("day")
    val day: String,

    @SerializedName("date")
    val date: String
)