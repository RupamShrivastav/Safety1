package com.example.safety.api

import com.example.safety.models.*
import retrofit2.Call
import retrofit2.http.*

/**
 * ApiService
 *
 * This interface defines Retrofit API endpoints for network operations
 * related to user management and authentication in the Safety App.
 */
interface ApiService {

    /**
     * Retrieves a list of all users from the server.
     *
     * @return UsersListModel containing all registered users.
     */
    @GET("/allUsers")
    fun getAllUsers(): UsersListModel

    /**
     * Retrieves users belonging to a specific organization.
     *
     * @param org_name The name of the organization.
     * @return A Call object containing a list of users in the specified organization.
     */
    @GET("/organization/{org_name}/users")
    fun getUsersByOrg(@Path("org_name") org_name: String): Call<UsersListModel>


    @POST("/forgotPassword")
    fun forgotPassword(@Body forgotPasswordModel: ForgotPasswordRequest) : Call<APIResponseModel>

    /**
     * Registers a new user.
     *
     * @param user A UserModelItem containing the user's details.
     * @return A Call object containing an APIResponseModel with the registration status.
     */
    @POST("/user")
    fun registerNewUser(@Body user: UserModelItem): Call<NewRegistrationResponse>

    /**
     * Verifies user login credentials.
     *
     * @param loginInfoModel A LoginInfoModel containing email and password.
     * @return A Call object containing a VerifiedUserModel with user authentication status.
     */
    @POST("/users/verifyUser")
    fun verifyUser(@Body loginInfoModel: LoginInfoModel): Call<VerifiedUserModel>

    /**
     * Updates a user's trusted contact details based on their email.
     *
     * @param email The email address of the user whose contact details need to be updated.
     * @param updateTrustedContactRequest The new trusted contact information.
     * @return A Call object containing an APIResponseModel indicating success or failure.
     */
    @PUT("/updateTrustedContactNumber")
    fun updateTrustedContact(
        @Body updateTrustedContactRequest: UpdateTrustedContactRequest
    ): Call<APIResponseModel>

    @PUT("/updateUserFullName")
    fun updateUserFullName(
        @Body updateNameModel: UpdateNameRequest
    ): Call<APIResponseModel>

    @PUT("/updateUserPhoneNo")
    fun updateUserPhoneNo(
        @Body updatePhoneNumberRequest: UpdatePhoneNumberRequest
    ): Call<APIResponseModel>

    @PUT("/updateUserPassword")
    fun updateUserPassword(
        @Body updatePasswordRequest: UpdatePasswordRequest
    ): Call<APIResponseModel>

    @PUT("/user/updateSecurityPin")
    fun updateSecurityPin(
        @Body updateSecurityPinRequest: UpdateSecurityPinRequest
    ): Call<APIResponseModel>

}
