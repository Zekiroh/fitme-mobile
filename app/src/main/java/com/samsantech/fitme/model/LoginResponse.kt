package com.samsantech.fitme.model

data class LoginResponse(
    val message: String,
    val user: User
)

data class User(
    val id: Int,
    val full_name: String,
    val username: String,
    val email: String,
    val role: String
)
