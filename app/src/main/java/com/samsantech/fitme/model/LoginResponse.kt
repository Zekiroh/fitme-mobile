package com.samsantech.fitme.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val message: String,
    val user: User
)

data class User(
    val id: Int,
    @SerializedName("full_name")
    val fullName: String,
    val username: String,
    val email: String,
    val role: String,
    val gender: String,
    val frequency: String?,
    val weight: Int,
    val height: Int,
    @SerializedName("fitness_plan")
    val fitnessPlan: String?
)