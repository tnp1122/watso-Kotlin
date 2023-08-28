package com.watso.app.feature.baedal.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.watso.app.data.model.BaseResponse
import com.watso.app.feature.baedal.data.AllOrderInfo
import com.watso.app.feature.baedal.data.BaedalRepository
import com.watso.app.feature.baedal.data.MyOrderInfo
import kotlinx.coroutines.launch
import java.lang.Exception

class BaedalOrdersViewModel(application: Application): AndroidViewModel(application) {

    private val baedalRepo = BaedalRepository(application)
    val getMyOrdersResponse: MutableLiveData<BaseResponse<MyOrderInfo>> = MutableLiveData()
    val getAllOrdersResponse: MutableLiveData<BaseResponse<AllOrderInfo>> = MutableLiveData()

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

    fun getMyOrders(postId: String) {
        makeRequest(getMyOrdersResponse) { baedalRepo.getMyOrders(postId) }
    }

    fun getAllOrders(postId: String) {
        makeRequest(getAllOrdersResponse) { baedalRepo.getAllOrders(postId) }
    }
}