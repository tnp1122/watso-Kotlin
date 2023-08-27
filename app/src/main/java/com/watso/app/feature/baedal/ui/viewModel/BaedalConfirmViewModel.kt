package com.watso.app.feature.baedal.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.watso.app.data.model.BaseResponse
import com.watso.app.feature.baedal.data.BaedalRepository
import com.watso.app.feature.baedal.data.MakePostForm
import com.watso.app.feature.baedal.data.MakePostResponse
import com.watso.app.feature.baedal.data.UserOrder
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.lang.Exception

class BaedalConfirmViewModel(application: Application): AndroidViewModel(application) {

    private val baedalRepo = BaedalRepository(application)
    val makePostResponse: MutableLiveData<BaseResponse<MakePostResponse>> = MutableLiveData()
    val makeOrdersResponse: MutableLiveData<BaseResponse<ResponseBody>> = MutableLiveData()

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

    fun makePost(makePostForm: MakePostForm) {
        makeRequest(makePostResponse) { baedalRepo.makePost(makePostForm) }
    }

    fun makeOrders(postId: String, userOrder: UserOrder) {
        makeRequest(makeOrdersResponse) { baedalRepo.makeOrders(postId, userOrder) }
    }
}
