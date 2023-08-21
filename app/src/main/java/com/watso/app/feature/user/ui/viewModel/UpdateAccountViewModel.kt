package com.watso.app.feature.user.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.watso.app.data.model.BaseResponse
import com.watso.app.feature.auth.data.AuthRepository
import com.watso.app.feature.user.data.*
import kotlinx.coroutines.launch
import okhttp3.Headers
import okhttp3.ResponseBody
import java.lang.Exception

class UpdateAccountViewModel(application: Application): AndroidViewModel(application) {

    private val authRepo = AuthRepository(application)
    private val userRepo = UserRepository(application)

    val refreshTokenHeaders: MutableLiveData<BaseResponse<Headers>> = MutableLiveData()
    val checkNicknameResponse: MutableLiveData<BaseResponse<CheckDuplicateResponse>> = MutableLiveData()
    val updatePasswordResponse: MutableLiveData<BaseResponse<ResponseBody>> = MutableLiveData()
    val updateNicknameResponse: MutableLiveData<BaseResponse<ResponseBody>> = MutableLiveData()
    val updateAccountNumberResponse: MutableLiveData<BaseResponse<ResponseBody>> = MutableLiveData()

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
                    liveData.value = BaseResponse.Error(response.message())
                }
            } catch (ex: Exception) {
                liveData.value = BaseResponse.Error(ex.message)
            }
        }
    }

    fun refreshToken() {
        refreshTokenHeaders.value = BaseResponse.Loading()
        viewModelScope.launch {
            try {
                val response = authRepo.refreshToken()
                if (response.isSuccessful) {
                    refreshTokenHeaders.value = BaseResponse.Success(response.headers())
                } else {
                    refreshTokenHeaders.value = BaseResponse.Error(response.message())
                }
            } catch (ex: Exception) {
                refreshTokenHeaders.value = BaseResponse.Error(ex.message)
            }
        }
    }

    fun checkNickname(nickname: String) {
        makeRequest(checkNicknameResponse) { userRepo.checkDuplicate("nickname", nickname) }
    }

    fun updatePassword(updatePasswordForm: UpdatePasswordForm) {
        makeRequest(updatePasswordResponse) { userRepo.updatePassword(updatePasswordForm) }
    }

    fun updateNickname(nickname: Nickname) {
        makeRequest(updateNicknameResponse) { userRepo.updateNickname(nickname) }
    }

    fun updateAccountNumber(accountNumber: AccountNumber) {
        makeRequest(updateAccountNumberResponse) { userRepo.updateAccountNumber(accountNumber) }
    }
}
