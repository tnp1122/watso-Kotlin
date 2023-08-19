package com.watso.app.feature.baedal.data

import android.content.Context
import com.watso.app.data.network.ApiClient
import okhttp3.ResponseBody
import retrofit2.Response

class BaedalRepository(context: Context) {

    private val api = ApiClient.create(context)


    /** 가게 */

    suspend fun getStoreList(): Response<List<Store>> {
        return api.getStoreList()
    }

    suspend fun getStoreInfo(storeId: String): Response<StoreInfo> {
        return api.getStoreInfo(storeId)
    }

    suspend fun getMenuInfo(storeId: String, menuId: String): Response<Menu> {
        return api.getMenuInfo(storeId, menuId)
    }


    /** 게시글  */

    suspend fun getBaedalPostList(option: String): Response<List<BaedalPost>> {
        return api.getBaedalPostList(option)
    }

    suspend fun baedalPosting(baedalPosting: BaedalPosting): Response<BaedalPostingResponse> {
        return api.baedalPosting(baedalPosting)
    }

    suspend fun getBaedalPost(postId: String): Response<BaedalPost> {
        return api.getBaedalPost(postId)
    }

    suspend fun deleteBaedalPost(postId: String): Response<ResponseBody> {
        return api.deleteBaedalPost(postId)
    }

    suspend fun updateBaedalPost(
        postId: String,
        baedalPostUpdate: BaedalPostUpdate
    ): Response<ResponseBody> {
        return api.updateBaedalPost(postId, baedalPostUpdate)
    }

    suspend fun getAccountNumber(postId: String): Response<AccountNumber> {
        return api.getAccountNumber(postId)
    }

    suspend fun updateBaedalFee(postId: String, fee: Fee): Response<ResponseBody> {
        return api.updateBaedalFee(postId, fee)
    }

    suspend fun setBaedalStatus(postId: String, status: BaedalStatus): Response<ResponseBody> {
        return api.setBaedalStatus(postId, status)
    }


    /** 주문  */

    suspend fun getAllOrders(postId: String): Response<AllOrderInfo> {
        return api.getAllOrders(postId)
    }

    suspend fun postOrders(postId: String, userOrder: UserOrder): Response<ResponseBody> {
        return api.postOrders(postId, userOrder)
    }

    suspend fun getMyOrders(postId: String): Response<MyOrderInfo> {
        return api.getMyOrders(postId)
    }

    suspend fun deleteOrders(postId: String): Response<ResponseBody> {
        return api.deleteOrders(postId)
    }


    /** 댓글 */

//    suspend fun postComment(postId: String, comment: PostComment): Response<ResponseBody> {
//        return api.postComment(postId, comment)
//    }
//
//    suspend fun getComments(postId: String): Response<GetComments> {
//        return api.getcomments(postId)
//    }
//
//    suspend fun postSubComment(
//        postId: String,
//        commentId: String,
//        comment: PostComment,
//    ): Response<ResponseBody> {
//        return api.postSubComment(postId, commentId, comment)
//    }
//
//    suspend fun deleteComment(
//        postId: String,
//        commentId: String
//    ): Response<ResponseBody> {
//        return api.deleteComment(postId, commentId)
//    }
}
