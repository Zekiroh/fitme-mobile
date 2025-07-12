package com.samsantech.fitme.model

data class Workout(
    val id: Int,
    val name: String,
    val category: String,
    val muscle_group: String,
    val duration: Int
)