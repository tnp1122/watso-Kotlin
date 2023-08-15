package com.watso.app.data.network

import android.content.Context
import android.util.Log
import com.watso.app.MainActivity
import com.watso.app.util.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import kotlin.jvm.Throws

class AuthorizationInterceptor(private val context: Context): Interceptor {
    val TAG = "AuthorizationInterceptor"
    val BASE_URL = "https://api.watso.kr/"
    val prefs = MainActivity.prefs

    private val EXCEPTION_URL = listOf(
        "${BASE_URL}auth/login",
        "${BASE_URL}user/signup",
        "${BASE_URL}user/signup/validation-check",
        "${BASE_URL}user/forgot/username",
        "${BASE_URL}user/forgot/password",
//        "${BASE_URL}user/profile/account-number",
//        "${BASE_URL}user/profile/nickname",
//        "${BASE_URL}user/profile/password"
    )

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = SessionManager.getAccessToken(context)

        val tokenAddedRequest = chain.request().newBuilder()
            .addHeader("Authorization", accessToken)
            .build()
        val response = chain.proceed(tokenAddedRequest)

        if (response.code == 401) {
            var isExpired = true
            val targetUrl = chain.request().url.toString()

            EXCEPTION_URL.forEach {
                if (targetUrl.contains(it)) isExpired = false
            }
            if (isExpired) {
                Log.d("어세스 토큰 갱신 시도 access", accessToken)
                requestRefreshToken(chain, response)
            }
        }
        return response
    }

    private fun requestRefreshToken(chain: Interceptor.Chain, response: Response): Response {
        Log.d(TAG, "토큰만료")

        val refreshToken = SessionManager.getRefreshToken(context)
        response.close()
        Log.d("어세스 토큰 갱신 시도 refresh", refreshToken)

        val refreshRequest = chain.request().newBuilder()
            .addHeader("Authorization", refreshToken)
            .method("GET", null)
            .url(BASE_URL + "auth/refresh")
            .build()

        val refreshResponse = chain.proceed(refreshRequest)

        if (refreshResponse.code== 200) {
            val token = refreshResponse.headers["Authentication"].toString()
            SessionManager.saveAccessToken(context, token)

            Log.d("[$TAG]어세스 토큰 갱신 성공", token)
            val newRequest = chain.request().newBuilder()
                .addHeader("Authorization", token)
                .build()

            val newRes = chain.proceed(newRequest)
            Log.d("API intercept newRes", newRes.toString())
            Log.d("API intercept newRes.code", newRes.code.toString())
            return newRes
        }

        Log.d(TAG, "어세스 토큰 갱신 실패")
        return refreshResponse
    }
}
