package com.samsantech.fitme.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.samsantech.fitme.api.AuthService
import com.samsantech.fitme.api.AssistanceService
import android.util.Log // Added for debugging

object RetrofitClient {
    private const val LOCAL_URL = "http://10.0.2.2:5000/"
    private const val PROD_URL = "https://fitmegym.com/api/"

    // Change this value to switch environment
    private const val USE_LOCAL = false

    private val BASE_URL = if (USE_LOCAL) LOCAL_URL else PROD_URL

    // Debug log to confirm which URL is used
    init {
        Log.d("FitMeBaseURL", "Base URL: $BASE_URL")
    }

    // 🔥 Renamed to `auth` for clarity
    val auth: AuthService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthService::class.java)
    }

    val assistance: AssistanceService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AssistanceService::class.java)
    }
}
