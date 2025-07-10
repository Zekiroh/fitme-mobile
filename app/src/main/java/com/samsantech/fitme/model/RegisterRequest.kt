package com.samsantech.fitme.model

data class RegisterRequest(
    val full_name: String,
    val username: String,
    val email: String,
    val password: String,
    val plan: String? = null,
    val price: Int? = null,
    val payment_method: String,
    val gcash_number: String? = null,
    val gcash_name: String? = null,
    val card_number: String? = null,
    val card_name: String? = null,
    val card_expiry: String? = null,
    val card_cvv: String? = null
)