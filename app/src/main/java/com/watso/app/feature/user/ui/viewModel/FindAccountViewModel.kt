package com.watso.app.feature.user.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.watso.app.data.model.BaseResponse
import com.watso.app.feature.user.data.TempPasswordForm
import com.watso.app.feature.user.data.UserRepository
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.lang.Exception

class FindAccountViewModel(application: Application): AndroidViewModel(application) {

    private val userRepo = UserRepository(application)
    val findUsernameResponse: MutableLiveData<BaseResponse<ResponseBody>> = MutableLiveData()
    val issueTempPasswordResponse: MutableLiveData<BaseResponse<ResponseBody>> = MutableLiveData()

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

    fun findUsername(email: String) {
        makeRequest(findUsernameResponse) { userRepo.findUsername(email) }
    }

    fun issueTempPassword(tempPasswordForm: TempPasswordForm) {
        makeRequest(issueTempPasswordResponse) { userRepo.issueTempPassword(tempPasswordForm) }
    }
}
