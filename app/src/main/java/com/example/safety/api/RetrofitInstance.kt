package com.example.safety.api

import android.util.Log
import com.example.safety.common.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * RetrofitInstance
 *
 * A singleton object that provides a configured Retrofit client for making API calls.
 */
object RetrofitInstance {

    /**
     * OkHttpClient instance with customized timeout settings.
     * Ensures smooth communication with the server by preventing long waits.
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
            val requestBody = request.body

            Log.d("${Constants.TAG} Retrofit", "Request URL: ${request.url}")
            Log.d("${Constants.TAG} Retrofit", "Request Headers: ${request.headers}")

            // Try to log request body if it's not null
            requestBody?.let {
                val buffer = okio.Buffer()
                it.writeTo(buffer)
                Log.d("${Constants.TAG} Retrofit", "Request Body: ${buffer.readUtf8()}")
            }

            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS) // Maximum time to establish a connection
        .readTimeout(30, TimeUnit.SECONDS)   // Maximum time to receive data from the server
        .writeTimeout(30, TimeUnit.SECONDS)  // Maximum time to send data to the server
        .build()

    /**
     * Initializes Retrofit with the base URL and required configurations.
     *
     * @return An instance of ApiService to interact with the API.
     */
    fun initialize(): ApiService {
        return Retrofit.Builder()
            .baseUrl("https://rest-api-safety-qyd4.onrender.com/") // API base URL
            .addConverterFactory(GsonConverterFactory.create()) // Converts JSON responses into Kotlin objects
            .client(okHttpClient) // Attaching OkHttpClient for custom network configurations
            .build()
            .create(ApiService::class.java) // Creates an implementation of the API service interface
    }
}
