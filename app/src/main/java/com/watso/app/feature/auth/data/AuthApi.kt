package com.watso.app.feature.auth.data

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {
    /** 인증 관련 API */

    @POST("auth/login")        // 로그인
    suspend fun login(
        @Body jsonparams: LoginForm
    ): Response<ResponseBody>

    @POST("auth/logout")         // 로그아웃
    suspend fun logout(
    ): Response<ResponseBody>

    @GET("auth/refresh")        // 토큰 재발급
    suspend fun refreshToken(
    ): Response<ResponseBody>
}
