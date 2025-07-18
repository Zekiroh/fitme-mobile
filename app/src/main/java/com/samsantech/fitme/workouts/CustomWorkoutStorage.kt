package com.samsantech.fitme.workouts

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.edit

object CustomWorkoutStorage {
    private const val PREF_NAME = "custom_workout_prefs"
    private const val KEY_CUSTOM_EXERCISES = "custom_exercises"

    @SuppressLint("MutatingSharedPrefs")
    fun addExercise(context: Context, exercise: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_CUSTOM_EXERCISES, mutableSetOf()) ?: mutableSetOf()
        set.add(exercise)
        prefs.edit { putStringSet(KEY_CUSTOM_EXERCISES, set) }
    }

    fun getExercises(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_CUSTOM_EXERCISES, emptySet())?.toList() ?: emptyList()
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit {
                remove(KEY_CUSTOM_EXERCISES)
            }
    }
}
