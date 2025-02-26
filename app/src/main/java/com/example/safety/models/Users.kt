package com.example.safety.models


data class Users(
    var name: String = "",
    var phoneNumber: String="",
    var lat : String="0",
    var long : String="0",
    var batPer : Int =0,
    var connectionInfo : String = ""
)

