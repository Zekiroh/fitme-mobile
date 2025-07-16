package com.samsantech.fitme.model

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val user: User? = null
)


data class ResponseSuccess (
    val success: Boolean,
    val message: String
)