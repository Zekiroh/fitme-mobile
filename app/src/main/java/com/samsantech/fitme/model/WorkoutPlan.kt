package com.samsantech.fitme.model

import java.io.Serializable

data class WorkoutPlanResponse (
    val success: Boolean,
    val message: String,
    val data: WorkoutPlan
)
data class WorkoutPlan(
    val plan_name: String,
    val goal: String,
    val level: String,
    val duration_weeks: Int,
    val days_per_week: Int,
    val training_split: List<TrainingDay>,
    val nutrition: Nutrition
)

data class TrainingDay(
    val day: String,
    val muscle_group: String,
    val exercises: List<Exercise>,
    val done: Boolean
)

data class Exercise(
    val name: String,
    val sets: Int,
    val reps: List<Int>,
    val rest_time_seconds: Int,
    val equipment: String
) : Serializable

data class Nutrition(
    val caloric_surplus: String,
    val protein: String,
    val carbohydrates: String,
    val fats: String,
    val meal_frequency: String
)
data class WorkoutPlanRequest(
    val goal: String?,
    val level: String?
)