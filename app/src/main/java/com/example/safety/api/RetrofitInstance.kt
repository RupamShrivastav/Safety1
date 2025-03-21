package com.example.safety.api

import android.util.Log
import com.example.safety.common.Constants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

/**
 * Custom interceptor to retry failed requests due to timeouts with exponential backoff.
 */
class RetryInterceptor(private val maxRetries: Int = 3, private val baseDelayMillis: Long = 1000) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        var response: Response
        var attempt = 0
        var exception: IOException? = null

        while (attempt < maxRetries) {
            try {
                response = chain.proceed(request) // Proceed with the request
                if (response.isSuccessful) {
                    return response // Return immediately if successful
                }
                // Log non-successful response (but don't throw an exception).
                Log.w("${Constants.TAG} Retrofit", "Request failed (attempt ${attempt + 1}/${maxRetries}): HTTP ${response.code} - ${request.url}") // Log URL
                response.close() // Close the unsuccessful response

            } catch (e: IOException) {
                exception = e // Store the exception to throw later if needed

                // Log more detailed error info
                if (e is SocketTimeoutException) {
                    Log.w("${Constants.TAG} Retrofit", "Timeout (attempt ${attempt + 1}/${maxRetries}): ${e.message} - ${request.url}") // URL is useful for debugging
                } else {
                    Log.w("${Constants.TAG} Retrofit", "Network error (attempt ${attempt + 1}/${maxRetries}): ${e.message} - ${request.url}")
                }

                // Check if we should retry based on exception type and attempts remaining
                if (!shouldRetry(e) || attempt >= maxRetries -1) { // Check if we should retry, and reduce magic numbers.
                    throw e // rethrow if we should not retry
                }

            }

            attempt++
            try {
                Thread.sleep(baseDelayMillis * attempt * attempt)  // Exponential backoff.  attempt*attempt for faster growth
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt() // Restore interrupted status
                throw IOException("Request interrupted during retry delay", e) // Wrap in IOException
            }

        }

        throw exception ?: IOException("Unknown network error after multiple retries") // Throw stored exception, or a generic one

    }

    // helper function to determine if the exception is retryable
    private fun shouldRetry(e: IOException): Boolean {
        return e is SocketTimeoutException // only retrying TimeOut exception
    }
}

/**
 * RetrofitInstance - Configured Retrofit client with timeout handling and retry logic.
 */
object RetrofitInstance {

    private const val BASE_URL = "https://rest-api-safety-qyd4.onrender.com/"  // Base URL as a constant

    // Create a logging interceptor (for debugging).  Use Level.BODY for detailed logging.
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY  // Log request/response bodies.  Change to Level.NONE for production.
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(RetryInterceptor()) // Add retry logic (default retries and delay)
        .addNetworkInterceptor(loggingInterceptor)  // Add logging interceptor.  Use addNetworkInterceptor for complete request/response logging.
        .connectTimeout(30, TimeUnit.SECONDS) // Connection timeout
        .readTimeout(30, TimeUnit.SECONDS) // Read timeout
        .writeTimeout(30, TimeUnit.SECONDS) // Write timeout
        .build()

    // Initializes and returns the ApiService instance.
    val apiService: ApiService by lazy { // Use lazy initialization
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Use Gson for JSON conversion
            .client(okHttpClient) // Set the custom OkHttpClient
            .build()
            .create(ApiService::class.java) // Create the ApiService
    }


    fun initialize(): ApiService = apiService
}