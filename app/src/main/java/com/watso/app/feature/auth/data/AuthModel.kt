package com.watso.app.feature.auth.data

import com.google.gson.annotations.SerializedName

data class LoginForm(
    val username: String,
    @SerializedName("pw")
    val password: String
)
