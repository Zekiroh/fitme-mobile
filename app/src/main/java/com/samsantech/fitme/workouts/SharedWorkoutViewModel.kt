package com.samsantech.fitme.workouts

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedWorkoutViewModel : ViewModel() {
    val selectedGroupNames = MutableLiveData<MutableList<String>>(mutableListOf())
}