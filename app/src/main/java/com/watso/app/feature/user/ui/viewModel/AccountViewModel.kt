package com.watso.app.feature.user.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.watso.app.data.model.BaseResponse
import com.watso.app.feature.auth.data.AuthRepository
import com.watso.app.feature.user.data.NotificationSubscription
import com.watso.app.feature.user.data.UserRepository
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.lang.Exception

class AccountViewModel(application: Application): AndroidViewModel(application) {

    private val userRepo = UserRepository(application)
    private val authRepo = AuthRepository(application)
    val getNotificationStatusResponse: MutableLiveData<BaseResponse<NotificationSubscription>> = MutableLiveData()
    val updateNotificationStatusResponse: MutableLiveData<BaseResponse<ResponseBody>> = MutableLiveData()
    val deleteAccountResponse: MutableLiveData<BaseResponse<ResponseBody>> = MutableLiveData()
    val logoutResponse: MutableLiveData<BaseResponse<ResponseBody>> = MutableLiveData()

    private fun <T> makeRequest(
        liveData: MutableLiveData<BaseResponse<T>>,
        request: suspend () -> retrofit2.Response<T>
    ) {
        liveData.value = BaseResponse.Loading()
        viewModelScope.launch {
            try {
                val response = request()
                if (response.isSuccessful) {
                    liveData.value = BaseResponse.Success(response.body())
                } else {
                    val errorBody = response.errorBody()
                    liveData.value = BaseResponse.Error(errorBody, response.message())
                }
            } catch (ex: Exception) {
                liveData.value = BaseResponse.Exception(ex.message)
            }
        }
    }

    fun getNotificationStatus() {
        makeRequest(getNotificationStatusResponse) { userRepo.getNotificationStatus() }
    }

    fun updateNotificationStatus(subscription: NotificationSubscription) {
        makeRequest(updateNotificationStatusResponse) { userRepo.updateNotificationStatus(subscription) }
    }

    fun deleteAccount() {
        makeRequest(deleteAccountResponse) { userRepo.deleteAccount() }
    }

    fun logout() {
        makeRequest(logoutResponse) { authRepo.logout()}
    }
}
