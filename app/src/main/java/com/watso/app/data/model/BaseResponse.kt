package com.watso.app.data.model
import okhttp3.ResponseBody

sealed class BaseResponse<out T> {
    data class Success<out T>(val data: T? = null) : BaseResponse<T>()
    data class Loading(val nothing: Nothing?=null) : BaseResponse<Nothing>()
    data class Error(val errorBody: ResponseBody?, val msg: String?) : BaseResponse<Nothing>()
    data class Exception(val exMsg: String?): BaseResponse<Nothing>()
}
