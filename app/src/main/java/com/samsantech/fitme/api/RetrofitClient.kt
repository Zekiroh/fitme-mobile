package com.samsantech.fitme.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.samsantech.fitme.api.AuthService
import com.samsantech.fitme.api.AssistanceService
import com.samsantech.fitme.api.Recommendation
import android.util.Log
import retrofit2.create

object RetrofitClient {
//    private const val LOCAL_URL = "http://10.0.2.2:5000/"
    private const val LOCAL_URL = "https://iavnscdgtc.a.pinggy.link/api/"
    private const val PROD_URL = "https://fitmegym.com/api/"

    // Change this value to switch environment
    private const val USE_LOCAL = true

    private val BASE_URL = if (USE_LOCAL) LOCAL_URL else PROD_URL

    init {
        Log.d("FitMeBaseURL", "Base URL: $BASE_URL")
    }

    val auth: AuthService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthService::class.java)
    }

    val members: MemberService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MemberService::class.java)
    }
    val assistance: AssistanceService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AssistanceService::class.java)
    }

    val payments: Payments by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Payments::class.java)
    }

    val users: Users by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Users::class.java)
    }

    // Recommendation service
    val recommendation: Recommendation by lazy {
        Retrofit.Builder()
            .baseUrl("https://fitmegym.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Recommendation::class.java)
    }
}