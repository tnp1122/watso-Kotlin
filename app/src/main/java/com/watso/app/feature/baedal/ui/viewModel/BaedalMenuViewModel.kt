package com.watso.app.feature.baedal.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.watso.app.data.model.BaseResponse
import com.watso.app.feature.baedal.data.BaedalRepository
import com.watso.app.feature.baedal.data.StoreDetail
import kotlinx.coroutines.launch
import java.lang.Exception

class BaedalMenuViewModel(application: Application): AndroidViewModel(application) {

    private val baedalRepo = BaedalRepository(application)
    val getStoreDetailResponse: MutableLiveData<BaseResponse<StoreDetail>> = MutableLiveData()

    fun getStoreDetail(storeId: String) {
        getStoreDetailResponse.value = BaseResponse.Loading()
        viewModelScope.launch {
            try {
                val response = baedalRepo.getStoreDetail(storeId)
                if (response.isSuccessful) {
                    getStoreDetailResponse.value = BaseResponse.Success(response.body())
                } else {
                    val errorBody = response.errorBody()
                    getStoreDetailResponse.value = BaseResponse.Error(errorBody, response.message())
                }
            } catch (ex: Exception) {
                getStoreDetailResponse.value = BaseResponse.Exception(ex.message)
            }
        }
    }
}
