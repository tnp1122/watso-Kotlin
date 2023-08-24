package com.watso.app.feature.baedal.data

import android.content.Context
import com.watso.app.data.network.ApiClient
import com.watso.app.feature.user.data.AccountNumber
import okhttp3.ResponseBody
import retrofit2.Response

class BaedalRepository(context: Context) {

    private val api = ApiClient.create(context)


    /** 가게 */

    suspend fun getStoreList(): Response<List<Store>> {
        return api.getStoreList()
    }

    suspend fun getStoreDetail(storeId: String): Response<StoreDetail> {
        return api.getStoreDetail(storeId)
    }

    suspend fun getMenuInfo(storeId: String, menuId: String): Response<Menu> {
        return api.getMenuInfo(storeId, menuId)
    }


    /** 게시글  */

    suspend fun getPostList(option: String): Response<List<PostContent>> {
        return api.getPostList(option)
    }

    suspend fun makePost(makePostForm: MakePostForm): Response<MakePostResponse> {
        return api.makePost(makePostForm)
    }

    suspend fun getPostContent(postId: String): Response<PostContent> {
        return api.getPostContent(postId)
    }

    suspend fun deletePost(postId: String): Response<ResponseBody> {
        return api.deletePost(postId)
    }

    suspend fun updatePost(
        postId: String,
        updatePostForm: UpdatePostForm
    ): Response<ResponseBody> {
        return api.updatePost(postId, updatePostForm)
    }

    suspend fun getAccountNumber(postId: String): Response<AccountNumber> {
        return api.getAccountNumber(postId)
    }

    suspend fun updateFee(postId: String, fee: Fee): Response<ResponseBody> {
        return api.updateFee(postId, fee)
    }

    suspend fun updatePostStatus(postId: String, status: PostStatus): Response<ResponseBody> {
        return api.updatePostStatus(postId, status)
    }


    /** 주문  */

    suspend fun getAllOrders(postId: String): Response<AllOrderInfo> {
        return api.getAllOrders(postId)
    }

    suspend fun makeOrders(postId: String, userOrder: UserOrder): Response<ResponseBody> {
        return api.makeOrders(postId, userOrder)
    }

    suspend fun getMyOrders(postId: String): Response<MyOrderInfo> {
        return api.getMyOrders(postId)
    }

    suspend fun deleteOrders(postId: String): Response<ResponseBody> {
        return api.deleteOrders(postId)
    }


    /** 댓글 */

    suspend fun makeComment(postId: String, comment: MakeCommentForm): Response<ResponseBody> {
        return api.makeComment(postId, comment)
    }

    suspend fun makeSubComment(
        postId: String,
        commentId: String,
        comment: MakeCommentForm
    ): Response<ResponseBody> {
        return api.makeSubComment(postId, commentId, comment)
    }

    suspend fun getComments(postId: String): Response<GetCommentsResponse> {
        return api.getComments(postId)
    }

    suspend fun deleteComment(
        postId: String,
        commentId: String
    ): Response<ResponseBody> {
        return api.deleteComment(postId, commentId)
    }
}
