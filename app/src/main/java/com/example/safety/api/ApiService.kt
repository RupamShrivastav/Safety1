package com.example.safety.api

import com.example.safety.models.LoginInfoModel
import com.example.safety.models.NewUserCreatedResponse
import com.example.safety.models.UserModelItem
import com.example.safety.models.UsersListModel
import com.example.safety.models.VerifiedUserModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService{

    @GET("/allusers")
    fun getAllUsers() : UsersListModel

    @GET("/organization/{org_name}/users")
    fun getUsersByOrg(@Path("org_name") org_name : String) : Call<UsersListModel>

    @POST("/user")
    fun registerNewUser(@Body user: UserModelItem) : Call<NewUserCreatedResponse>

    @POST("/users/verify-user")
    fun verifyUser(@Body loginInfoModel: LoginInfoModel) : Call<VerifiedUserModel>


}