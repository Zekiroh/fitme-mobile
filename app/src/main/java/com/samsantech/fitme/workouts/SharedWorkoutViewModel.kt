package com.samsantech.fitme.workouts

import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class CustomExercise(
    val name: String,
    val restTimeSeconds: Int = 60, // Default 60 seconds rest
    val sets: Int = 3,
    val reps: Int = 10
) : Parcelable

class SharedWorkoutViewModel : ViewModel() {
    val selectedGroupNames = MutableLiveData<MutableList<String>>(mutableListOf())
    val selectedExercises = MutableLiveData<MutableList<CustomExercise>>(mutableListOf())
}