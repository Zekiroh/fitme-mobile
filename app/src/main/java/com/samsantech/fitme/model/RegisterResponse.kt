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


data class ApiErrorResponse(
    val success: Boolean,
    val message: String,
    val error: SqlError?
)

data class SqlError(
    val code: String?,
    val errno: Int?,
    val sqlState: String?,
    val sqlMessage: String?,
    val sql: String?
)
