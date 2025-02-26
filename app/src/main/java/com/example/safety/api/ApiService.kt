package com.example.safety.api

import com.example.safety.models.LoginInfoModel
import com.example.safety.models.APIResponseModel
import com.example.safety.models.UpdateTrustedContactInfo
import com.example.safety.models.UserModelItem
import com.example.safety.models.UsersListModel
import com.example.safety.models.VerifiedUserModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService{

    @GET("/allusers")
    fun getAllUsers() : UsersListModel

    @GET("/organization/{org_name}/users")
    fun getUsersByOrg(@Path("org_name") org_name : String) : Call<UsersListModel>

    @POST("/user")
    fun registerNewUser(@Body user: UserModelItem) : Call<APIResponseModel>

    @POST("/users/verify-user")
    fun verifyUser(@Body loginInfoModel: LoginInfoModel) : Call<VerifiedUserModel>

    @PUT("/updateTrustedContactNumber/{email}")
    fun updateTrustedContact(
        @Path("email") email:String,
        @Body updateTrustedContactInfo: UpdateTrustedContactInfo
    ): Call<APIResponseModel>

}