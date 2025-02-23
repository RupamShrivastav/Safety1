package com.example.safety.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun initialize(): ApiService {
        return Retrofit.Builder()
            .baseUrl("https://rest-api-safety-qyd4.onrender.com/")
//            .baseUrl("https://rest-api-safety.onrender.com/")
//            .baseUrl("https://127.0.0.1:5000/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)
    }
}

