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
    @GET("/allusers")
    fun getAllUsers(): UsersListModel

    /**
     * Retrieves users belonging to a specific organization.
     *
     * @param org_name The name of the organization.
     * @return A Call object containing a list of users in the specified organization.
     */
    @GET("/organization/{org_name}/users")
    fun getUsersByOrg(@Path("org_name") org_name: String): Call<UsersListModel>

    /**
     * Registers a new user.
     *
     * @param user A UserModelItem containing the user's details.
     * @return A Call object containing an APIResponseModel with the registration status.
     */
    @POST("/user")
    fun registerNewUser(@Body user: UserModelItem): Call<APIResponseModel>

    /**
     * Verifies user login credentials.
     *
     * @param loginInfoModel A LoginInfoModel containing email and password.
     * @return A Call object containing a VerifiedUserModel with user authentication status.
     */
    @POST("/users/verify-user")
    fun verifyUser(@Body loginInfoModel: LoginInfoModel): Call<VerifiedUserModel>

    /**
     * Updates a user's trusted contact details based on their email.
     *
     * @param email The email address of the user whose contact details need to be updated.
     * @param updateTrustedContactInfo The new trusted contact information.
     * @return A Call object containing an APIResponseModel indicating success or failure.
     */
    @PUT("/updateTrustedContactNumber/{email}")
    fun updateTrustedContact(
        @Path("email") email: String,
        @Body updateTrustedContactInfo: UpdateTrustedContactInfo
    ): Call<APIResponseModel>
}
