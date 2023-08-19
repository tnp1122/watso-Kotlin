package com.watso.app.data.network

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.watso.app.feature.auth.data.AuthApi
import com.watso.app.feature.baedal.data.BaedalApi
import com.watso.app.feature.user.data.UserApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface ApiClient: AuthApi, UserApi, BaedalApi {

    companion object {
        private const val BASE_URL = "https://api.watso.kr/"

        var mHttpLoggingInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)

        fun create(context: Context): ApiClient {
            val gson : Gson = GsonBuilder().setLenient().create();

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(provideOkHttpClient(AuthorizationInterceptor(context)))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiClient::class.java)
        }

        private fun provideOkHttpClient(interceptor: AuthorizationInterceptor):
                OkHttpClient = OkHttpClient.Builder().run {
            addInterceptor(mHttpLoggingInterceptor)
            addInterceptor(interceptor)
            build()
        }
    }
}

