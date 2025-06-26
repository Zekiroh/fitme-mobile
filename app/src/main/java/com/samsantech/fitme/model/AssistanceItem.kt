package com.samsantech.fitme.model

data class AssistanceItem(
    val id: Int,
    val category: String,
    val message: String,
    val status: String,
    val created_at: String,
    val replies: List<AssistanceReply>?
)

data class AssistanceReply(
    val message: String,
    val sender: String,
    val sent_at: String
)