package com.samsantech.fitme.model

import com.google.gson.annotations.SerializedName

data class RegUpdateParam(
    val gender: String,
    @SerializedName("fitness_plan")
    val fitnessPlan: String,
    val frequency: String,
    val weight: Int,
    val height: String
)