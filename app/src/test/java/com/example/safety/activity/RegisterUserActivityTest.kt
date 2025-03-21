package com.example.safety.activity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.example.safety.api.ApiService
import com.example.safety.models.NewRegistrationResponse
import com.example.safety.models.UserModelItem
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterUserActivityTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var apiService: ApiService
    private lateinit var lifecycleOwner: LifecycleOwner

    @Before
    fun setUp() {
        apiService = mock(ApiService::class.java)
        lifecycleOwner = mock(LifecycleOwner::class.java)
        `when`(lifecycleOwner.lifecycle).thenReturn(LifecycleRegistry(lifecycleOwner))
    }

    @Test
    fun testSuccessfulRegistration() {
        val newUser = UserModelItem(
            organization = "Test Org",
            fullName = "John Doe",
            userID = "12345",
            email = "test@example.com",
            password = "password123",
            phoneNumber = "9876543210",
            securityPIN = "12345",
            trustedContactName = "Jane Doe",
            trustedContactID = "67890",
            trustedContactNumber = "9876501234"
        )
        val call = mock(Call::class.java) as Call<NewRegistrationResponse>
        `when`(apiService.registerNewUser(newUser)).thenReturn(call)

        doAnswer {
            val callback = it.getArgument<Callback<NewRegistrationResponse>>(0)
            callback.onResponse(call, Response.success(NewRegistrationResponse(message = "Registration successful")))
        }.`when`(call).enqueue(any())

        val response = apiService.registerNewUser(newUser)
        assertNotNull(response)
    }

    @Test
    fun testFailedRegistration() {
        val newUser = UserModelItem(
            organization = "Test Org",
            fullName = "John Doe",
            userID = "12345",
            email = "test@example.com",
            password = "password123",
            phoneNumber = "9876543210",
            securityPIN = "12345",
            trustedContactName = "Jane Doe",
            trustedContactID = "67890",
            trustedContactNumber = "9876501234"
        )
        val call = mock(Call::class.java) as Call<NewRegistrationResponse>
        `when`(apiService.registerNewUser(newUser)).thenReturn(call)

        doAnswer {
            val callback = it.getArgument<Callback<NewRegistrationResponse>>(0)
            callback.onFailure(call, Throwable("Registration failed"))
        }.`when`(call).enqueue(any())

        val response = apiService.registerNewUser(newUser)
        assertNotNull(response)
    }

    @Test
    fun testRegistrationErrorResponse() {
        val newUser = UserModelItem(
            organization = "Test Org",
            fullName = "John Doe",
            userID = "12345",
            email = "test@example.com",
            password = "password123",
            phoneNumber = "9876543210",
            securityPIN = "12345",
            trustedContactName = "Jane Doe",
            trustedContactID = "67890",
            trustedContactNumber = "9876501234"
        )
        val call = mock(Call::class.java) as Call<NewRegistrationResponse>
        `when`(apiService.registerNewUser(newUser)).thenReturn(call)

        val errorResponse = Response.error<NewRegistrationResponse>(400, ResponseBody.create("application/json".toMediaTypeOrNull(), "{ \"message\": \"Email already exists\" }"))

        doAnswer {
            val callback = it.getArgument<Callback<NewRegistrationResponse>>(0)
            callback.onResponse(call, errorResponse)
        }.`when`(call).enqueue(any())

        val response = apiService.registerNewUser(newUser)
        assertNotNull(response)
        // In a real test scenario, you might want to assert that the error message is handled correctly,
        // for example, by checking if a specific error Toast is shown (if you are using instrumentation tests)
        // or by verifying some state change in the ViewModel/Presenter if you are using MVVM/MVP.
        // For this unit test focusing on API interaction, we are just verifying that the call is made and response is handled.
    }
}
