package com.samsantech.fitme.workouts

import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.samsantech.fitme.model.CustomWorkoutItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class CustomExercise(
    val name: String,
    val restTimeSeconds: Int = 60, // Default 60 seconds rest
    val sets: Int = 3,
    val reps: Int = 10
) : Parcelable

@Parcelize
data class WorkoutCompletion(
    val isCompleted: Boolean = false,
    val completionTime: Long = 0,
    val workoutDetails: String = "",
    val duration: Int = 0,
    val exercises: List<CustomExercise> = emptyList()
) : Parcelable

@Parcelize
data class CompletedWorkout(
    val id: String = "",
    val completionTime: Long = 0,
    val workoutDetails: String = "",
    val duration: Int = 0,
    val exercises: List<CustomExercise> = emptyList()
) : Parcelable

class SharedWorkoutViewModel : ViewModel() {
    val selectedGroupNames = MutableLiveData<MutableList<String>>(mutableListOf())
    val selectedExercises = MutableLiveData<MutableList<CustomExercise>>(mutableListOf())
    val workoutCompletion = MutableLiveData<WorkoutCompletion>(WorkoutCompletion())
    val completedWorkouts = MutableLiveData<MutableList<CompletedWorkout>>(mutableListOf())
    val apiCustomWorkouts = MutableLiveData<MutableList<CustomWorkoutItem>>(mutableListOf())
    
    fun markWorkoutAsCompleted(completion: WorkoutCompletion) {
        workoutCompletion.value = completion
        
        // Add to completed workouts history
        val completedWorkout = CompletedWorkout(
            id = System.currentTimeMillis().toString(),
            completionTime = completion.completionTime,
            workoutDetails = completion.workoutDetails,
            duration = completion.duration,
            exercises = completion.exercises
        )
        
        val currentHistory = completedWorkouts.value ?: mutableListOf()
        currentHistory.add(completedWorkout)
        completedWorkouts.value = currentHistory
        
        // Clear selected exercises after completion
        selectedExercises.value = mutableListOf()
    }
    
    fun clearWorkoutCompletion() {
        workoutCompletion.value = WorkoutCompletion()
    }
    
    fun addExercise(exercise: CustomExercise) {
        val current = selectedExercises.value ?: mutableListOf()
        current.add(exercise)
        selectedExercises.value = current
    }
    
    fun removeExercise(exercise: CustomExercise) {
        val current = selectedExercises.value ?: mutableListOf()
        current.removeAll { it.name == exercise.name }
        selectedExercises.value = current
    }
    
    fun clearExercises() {
        selectedExercises.value = mutableListOf()
    }
    
    fun getCompletedWorkouts(): List<CompletedWorkout> {
        return completedWorkouts.value ?: emptyList()
    }
    
    fun clearWorkoutHistory() {
        completedWorkouts.value = mutableListOf()
    }
    
    fun addApiCustomWorkout(workout: CustomWorkoutItem) {
        val current = apiCustomWorkouts.value ?: mutableListOf()
        current.add(workout)
        apiCustomWorkouts.value = current
    }
    
    fun getApiCustomWorkouts(): List<CustomWorkoutItem> {
        return apiCustomWorkouts.value ?: emptyList()
    }
    
    fun clearApiCustomWorkouts() {
        apiCustomWorkouts.value = mutableListOf()
    }
}