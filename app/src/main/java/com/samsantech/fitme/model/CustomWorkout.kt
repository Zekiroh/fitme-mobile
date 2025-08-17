package com.samsantech.fitme.model

data class CustomWorkoutRequest(
    val rest: String,
    val weight: String,
    val reps: String,
    val description: String
)

data class CustomWorkoutResponse(
    val message: String,
    val workouts: List<CustomWorkoutItem>? = null,
    val workoutId: Int? = null
)

data class CustomWorkoutItem(
    val id: Int,
    val user_id: Int,
    val rest: String,
    val weight: String,
    val reps: String,
    val description: String,
    val is_done: Boolean
)
