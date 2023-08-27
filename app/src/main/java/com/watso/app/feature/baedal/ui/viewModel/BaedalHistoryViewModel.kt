package com.watso.app.feature.baedal.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.watso.app.data.model.BaseResponse
import com.watso.app.feature.baedal.data.BaedalRepository
import com.watso.app.feature.baedal.data.PostContent
import kotlinx.coroutines.launch
import java.lang.Exception

class BaedalHistoryViewModel(application: Application): AndroidViewModel(application) {

    private val baedalRepo = BaedalRepository(application)
    val getHistoryResponse: MutableLiveData<BaseResponse<List<PostContent>>> = MutableLiveData()

    fun getHistory() {
        getHistoryResponse.value = BaseResponse.Loading()
        viewModelScope.launch {
            try {
                val response = baedalRepo.getPostList("all")
                if (response.isSuccessful) {
                    getHistoryResponse.value = BaseResponse.Success(response.body())
                } else {
                    val errorBody = response.errorBody()
                    getHistoryResponse.value = BaseResponse.Error(errorBody, response.message())
                }
            } catch (ex: Exception) {
                getHistoryResponse.value = BaseResponse.Exception(ex.message)
            }
        }
    }
}
