package com.watso.app.feature.auth.data

import android.content.Context
import com.watso.app.data.network.ApiClient
import okhttp3.ResponseBody
import retrofit2.Response

class AuthRepository(context: Context) {

    private val api = ApiClient.create(context)

    suspend fun login(loginForm: LoginForm): Response<ResponseBody> {
        return api.login(loginForm)
    }
}
