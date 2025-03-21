package com.example.safety.api

import android.util.Log
import android.widget.Toast
import com.example.safety.common.Constants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

/**
 * Custom interceptor to retry failed requests due to timeouts with exponential backoff.
 */
class RetryInterceptor(private val maxRetries: Int = 5,
                       private val baseDelayMillis: Long = 500) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var response: Response? = null
        var exception: IOException? = null

        while (attempt < maxRetries) {
            try {
                response?.close()
                response = chain.proceed(chain.request())

                if (response.isSuccessful) {
                    return response
                } else {
                    Log.e("${Constants.TAG} Retrofit", "Response failed (attempt ${attempt + 1}): HTTP ${response.code}")

                }
            } catch (e: IOException) {
                exception = e

                if (e is SocketTimeoutException) {
                    Log.e("${Constants.TAG} Retrofit", "Timeout occurred (attempt ${attempt + 1}): ${e.message}")
                } else {
                    Log.e("${Constants.TAG} Retrofit", "Network error (attempt ${attempt + 1}): ${e.message}")
                }
            }

            attempt++
            Thread.sleep(baseDelayMillis * attempt) // ⏳ Exponential backoff (500ms, 1000ms, 1500ms, ...)
        }

        return response!!
    }
}

/**
 * RetrofitInstance - Configured Retrofit client with timeout handling and retry logic.
 */
object RetrofitInstance {
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(RetryInterceptor(5)) // Add retry logic with exponential backoff
        .addInterceptor { chain ->
            val request = chain.request()
            val requestBody = request.body

            Log.d("${Constants.TAG} Retrofit", "Request URL: ${request.url}")
            Log.d("${Constants.TAG} Retrofit", "Request Headers: ${request.headers}")

            requestBody?.let {
                val buffer = okio.Buffer()
                it.writeTo(buffer)
                Log.d("${Constants.TAG} Retrofit", "Request Body: ${buffer.readUtf8()}")
            }

            chain.proceed(request)
        }
        .connectTimeout(10, TimeUnit.SECONDS) // Reduce timeout for faster retries
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    /**
     * Initializes Retrofit with retry logic.
     */
    fun initialize(): ApiService {
        return Retrofit.Builder()
            .baseUrl("https://rest-api-safety-qyd4.onrender.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)
    }
}
