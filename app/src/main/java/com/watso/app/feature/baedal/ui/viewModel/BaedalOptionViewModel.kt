package com.watso.app.feature.baedal.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.watso.app.data.model.BaseResponse
import com.watso.app.feature.baedal.data.BaedalRepository
import com.watso.app.feature.baedal.data.MenuDetail
import kotlinx.coroutines.launch

class BaedalOptionViewModel(application: Application): AndroidViewModel(application) {

    private val baedalRepo = BaedalRepository(application)
    val getMenuDetailResponse: MutableLiveData<BaseResponse<MenuDetail>> = MutableLiveData()

    fun getMenuDetail(stordId: String, menuId: String) {
        getMenuDetailResponse.value = BaseResponse.Loading()
        viewModelScope.launch {
            try {
                val response = baedalRepo.getMenuDetail(stordId, menuId)
                if (response.isSuccessful) {
                    getMenuDetailResponse.value = BaseResponse.Success(response.body())
                } else {
                    val errorBody = response.errorBody()
                    getMenuDetailResponse.value = BaseResponse.Error(errorBody, response.message())
                }
            } catch (ex: Exception) {
                getMenuDetailResponse.value = BaseResponse.Exception(ex.message)
            }
        }
    }
}
