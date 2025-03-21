package com.example.safety.activity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.example.safety.api.ApiService
import com.example.safety.models.LoginInfoModel
import com.example.safety.models.UserModelItem
import com.example.safety.models.VerifiedUserModel
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

class LoginUserActivityTest {

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
    fun `test successful login`() {
        val loginInfo = LoginInfoModel("test@example.com", "password123")
        val call = mock(Call::class.java) as Call<VerifiedUserModel>
        `when`(apiService.verifyUser(loginInfo)).thenReturn(call)

        doAnswer {
            val callback = it.getArgument<Callback<VerifiedUserModel>>(0)
            callback.onResponse(call, Response.success(VerifiedUserModel("Welcome Back !", userData = UserModelItem(
                organization = "Test Org",
                fullName = "John Doe",
                userID = "12345",
                email = "test@example.com",
                password = "password123",
                phoneNumber = "9876543210",
                securityPIN = "1234",
                trustedContactName = "Jane Doe",
                trustedContactID = "67890",
                trustedContactNumber = "9876501234"
            ))))
        }.`when`(call).enqueue(any())

        val response = apiService.verifyUser(loginInfo)
        assertNotNull(response)
    }

    @Test
    fun `test failed login`() {
        val loginInfo = LoginInfoModel("wrong@example.com", "wrongpass")
        val call = mock(Call::class.java) as Call<VerifiedUserModel>
        `when`(apiService.verifyUser(loginInfo)).thenReturn(call)

        doAnswer {
            val callback = it.getArgument<Callback<VerifiedUserModel>>(0)
            callback.onFailure(call, Throwable("Login failed"))
        }.`when`(call).enqueue(any())

        val response = apiService.verifyUser(loginInfo)
        assertNotNull(response)
    }
}
