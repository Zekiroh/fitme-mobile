package com.samsantech.fitme.model

data class AssistanceRequest(
    val user_id: Int,
    val category: String,
    val message: String
)