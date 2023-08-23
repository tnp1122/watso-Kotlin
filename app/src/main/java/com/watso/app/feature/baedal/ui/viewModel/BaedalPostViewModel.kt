package com.watso.app.feature.baedal.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.watso.app.data.model.BaseResponse
import com.watso.app.feature.baedal.data.*
import com.watso.app.feature.user.data.AccountNumber
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.lang.Exception

class BaedalPostViewModel(application: Application): AndroidViewModel(application) {

    private val baedalRepo = BaedalRepository(application)

    val getPostContentResponse: MutableLiveData<BaseResponse<PostContent>> = MutableLiveData()
    val getAccountNumberResponse: MutableLiveData<BaseResponse<AccountNumber>> = MutableLiveData()
    val updatePostStausResponse: MutableLiveData<BaseResponse<ResponseBody>> = MutableLiveData()
    val updateFeeResponse: MutableLiveData<BaseResponse<ResponseBody>> = MutableLiveData()
    val deletePostResponse: MutableLiveData<BaseResponse<ResponseBody>> = MutableLiveData()
    val deleteOrdersResponse: MutableLiveData<BaseResponse<ResponseBody>> = MutableLiveData()

    val makeCommentResponse: MutableLiveData<BaseResponse<ResponseBody>> = MutableLiveData()
    val makeSubCommentResponse: MutableLiveData<BaseResponse<ResponseBody>> = MutableLiveData()
    val getCommentsResponse: MutableLiveData<BaseResponse<GetCommentsResponse>> = MutableLiveData()
    val deleteCommentResponse: MutableLiveData<BaseResponse<ResponseBody>> = MutableLiveData()

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

    fun getAccountNumber(postId: String) {
        makeRequest(getAccountNumberResponse) { baedalRepo.getAccountNumber(postId) }
    }

    fun getPostContent(postId: String) {
        makeRequest(getPostContentResponse) { baedalRepo.getPostContent(postId) }
    }

    fun updatePostStatus(postId: String, status: PostStatus) {
        makeRequest(updatePostStausResponse) { baedalRepo.updatePostStatus(postId, status) }
    }

    fun updateFee(postId: String, fee: Fee) {
        makeRequest(updateFeeResponse) { baedalRepo.updateFee(postId, fee) }
    }

    fun deletePost(postId: String) {
        makeRequest(deletePostResponse) { baedalRepo.deletePost(postId) }
    }

    fun deleteOrders(postId: String) {
        makeRequest(deleteOrdersResponse) { baedalRepo.deleteOrders(postId) }
    }

    fun makeComment(postId: String, comment: MakeCommentForm) {
        makeRequest(makeCommentResponse) { baedalRepo.makeComment(postId, comment) }
    }

    fun makeSubComment(postId: String, parentId: String, comment: MakeCommentForm) {
        makeRequest(makeSubCommentResponse) { baedalRepo.makeSubComment(postId, parentId, comment) }
    }

    fun getComments(postId: String) {
        makeRequest(getCommentsResponse) { baedalRepo.getComments(postId) }
    }

    fun deleteComment(postId: String, commentId: String) {
        makeRequest(deleteCommentResponse) { baedalRepo.deleteComment(postId, commentId) }
    }
}
