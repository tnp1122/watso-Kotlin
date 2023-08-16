package com.watso.app.util

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.watso.app.data.model.BaseResponse
import com.watso.app.feature.user.data.FcmToken
import com.watso.app.feature.user.data.UserInfo
import com.watso.app.feature.user.data.UserRepository
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.lang.Exception

class InitAppViewModel(application: Application): AndroidViewModel(application)  {

    private val userRepo = UserRepository(application)
    val userInfo: MutableLiveData<BaseResponse<UserInfo>> = MutableLiveData()
    val sendFcmTokenResult: MutableLiveData<BaseResponse<ResponseBody>> = MutableLiveData()

    fun getUserInfo() {
        userInfo.value = BaseResponse.Loading()
        viewModelScope.launch {
            try {
                val response = userRepo.getUserInfo()
                if (response.code() == 200) {
                    userInfo.value = BaseResponse.Success(response.body())
                } else {
                    userInfo.value = BaseResponse.Error(response.message())
                }
            } catch (ex: Exception) {
                userInfo.value = BaseResponse.Error(ex.message)
            }
        }
    }

    fun sendFcmToken(token: FcmToken) {
        sendFcmTokenResult.value = BaseResponse.Loading()
        viewModelScope.launch {
            try {
                val response = userRepo.sendFcmToken(token)
                if (response.code() == 204) {
                    sendFcmTokenResult.value = BaseResponse.Success(response.body())
                } else {
                    sendFcmTokenResult.value = BaseResponse.Error(response.message())
                }
            } catch (ex: Exception) {
                sendFcmTokenResult.value = BaseResponse.Error(ex.message)
            }
        }
    }
}
