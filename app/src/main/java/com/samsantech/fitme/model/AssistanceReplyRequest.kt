package com.samsantech.fitme.model

data class AssistanceReplyRequest(
    val request_id: Int,
    val message: String,
    val sender: String
)