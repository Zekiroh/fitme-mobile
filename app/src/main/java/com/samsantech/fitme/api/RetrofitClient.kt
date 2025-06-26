package com.samsantech.fitme.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.samsantech.fitme.api.AuthService
import com.samsantech.fitme.api.AssistanceService

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:5000/" // use 10.0.2.2 for Android emulator

    val instance: AuthService by lazy {
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
