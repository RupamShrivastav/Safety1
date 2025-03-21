package com.example.safety.api

import com.example.safety.models.*
import retrofit2.Call
import retrofit2.http.*

/**
 * ApiService
 * Defines Retrofit API endpoints for user management and authentication.
 */
interface ApiService {

    /**
     * Retrieves users belonging to a specific organization.
     * @param orgName The name of the organization.
     * @return A Call object containing a list of users.
     */
    @GET("/organization/{org_name}/users")
    fun getUsersByOrg(@Path("org_name") orgName: String): Call<UsersListModel>

    /**
     * Handles the forgot password request.
     * @param forgotPasswordModel Request body containing forgot password details.
     * @return A Call object containing an API response.
     */
    @POST("/forgotPassword")
    fun forgotPassword(@Body forgotPasswordModel: ForgotPasswordRequest): Call<APIResponseModel>

    /**
     * Registers a new user.
     * @param user User details.
     * @return A Call object containing registration response.
     */
    @POST("/user")
    fun registerNewUser(@Body user: UserModelItem): Call<NewRegistrationResponse>

    /**
     * Verifies user login credentials.
     * @param loginInfoModel Login credentials (email and password).
     * @return A Call object containing user verification data.
     */
    @POST("/users/verifyUser")
    fun verifyUser(@Body loginInfoModel: LoginInfoModel): Call<VerifiedUserModel>

    /**
     * Updates a user's trusted contact details.
     * @param updateTrustedContactRequest New trusted contact information.
     * @return A Call object containing an API response.
     */
    @PUT("/updateTrustedContactNumber")
    fun updateTrustedContact(
        @Body updateTrustedContactRequest: UpdateTrustedContactRequest
    ): Call<APIResponseModel>

    /**
     * Updates a users Full Name.
     * @param updateNameModel  updateNameRequest: UpdateNameRequest.
     * @return A Call object containing an API response.
     */
    @PUT("/updateUserFullName")
    fun updateUserFullName(
        @Body updateNameModel: UpdateNameRequest
    ): Call<APIResponseModel>

    /**
     * Updates a users Phone Number.
     * @param updatePhoneNumberRequest updatePhoneNumberRequest: UpdatePhoneNumberRequest
     * @return A Call object containing an API response.
     */
    @PUT("/updateUserPhoneNo")
    fun updateUserPhoneNo(
        @Body updatePhoneNumberRequest: UpdatePhoneNumberRequest
    ): Call<APIResponseModel>

    /**
     * Updates a users Password.
     * @param updatePasswordRequest updatePasswordRequest : UpdatePasswordRequest
     * @return A Call object containing an API response.
     */
    @PUT("/updateUserPassword")
    fun updateUserPassword(
        @Body updatePasswordRequest: UpdatePasswordRequest
    ): Call<APIResponseModel>

    /**
     * Updates a users Security PIN.
     * @param updateSecurityPinRequest updateSecurityPinRequest : UpdateSecurityPinRequest
     * @return A Call object containing an API response.
     */
    @PUT("/updateUserSecurityPin")
    fun updateSecurityPin(
        @Body updateSecurityPinRequest: UpdateSecurityPinRequest
    ): Call<APIResponseModel>

}