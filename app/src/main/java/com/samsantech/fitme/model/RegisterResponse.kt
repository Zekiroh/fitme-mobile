package com.samsantech.fitme.model

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val user: User? = null
)