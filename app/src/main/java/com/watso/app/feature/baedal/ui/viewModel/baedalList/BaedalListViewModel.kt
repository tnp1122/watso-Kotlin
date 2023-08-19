package com.watso.app.feature.baedal.ui.viewModel.baedalList

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.watso.app.data.model.BaseResponse
import com.watso.app.feature.baedal.data.BaedalPost
import com.watso.app.feature.baedal.data.BaedalRepository
import kotlinx.coroutines.launch
import java.lang.Exception

class BaedalListViewModel(application: Application): AndroidViewModel(application) {

    private val baedalRepo = BaedalRepository(application)
    val getJoinedPostListResponse: MutableLiveData<BaseResponse<List<BaedalPost>>> = MutableLiveData()
    val getJoinablePostListResponse: MutableLiveData<BaseResponse<List<BaedalPost>>> = MutableLiveData()

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

    fun getJoinedPostList() {
        makeRequest(getJoinedPostListResponse) { baedalRepo.getBaedalPostList("joined") }
    }

    fun getJoinablePostList() {
        makeRequest(getJoinedPostListResponse) { baedalRepo.getBaedalPostList("joinable") }
    }
}
