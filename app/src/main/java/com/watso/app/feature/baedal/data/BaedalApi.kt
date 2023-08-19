package com.watso.app.feature.baedal.data

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface BaedalApi {

    companion object
    {
        const val STORE = "delivery/store"
        const val POST = "delivery/post"
    }


    /** 가게 */

    @GET(STORE)                                       // 가게 리스트 조회
    suspend fun getStoreList():Response<List<Store>>

    @GET("${STORE}/{store_id}")                 // 가게 상세정보(메뉴) 조회
    suspend fun getStoreInfo(
        @Path("store_id") storeId: String
    ): Response<StoreInfo>

    @GET("${STORE}/{store_id}/{menu_id}")       // 메뉴 상세정보(옵션) 조회
    suspend fun getMenuInfo(
        @Path("store_id") storeId: String,
        @Path("menu_id") menuId: String
    ): Response<Menu>


    /** 게시글  */

    @GET(POST)                   // 게시글 목록 조회
    suspend fun getBaedalPostList(
        @Query("option") option: String
    ): Response<List<BaedalPost>>

    @POST(POST)                  // 게시글 등록
    suspend fun baedalPosting(
        @Body jsonparams: BaedalPosting
    ): Response<BaedalPostingResponse>

    @GET("${POST}/{post_id}")         // 게시글 조회
    suspend fun getBaedalPost(
        @Path("post_id") postId: String
    ): Response<BaedalPost>

    @DELETE("${POST}/{post_id}")     // 게시글 삭제
    suspend fun deleteBaedalPost(
        @Path("post_id") postId: String
    ): Response<ResponseBody>

    @PATCH("${POST}/{post_id}")      // 게시글 수정
    suspend fun updateBaedalPost(
        @Path("post_id") postId: String,
        @Body jsonparams: BaedalPostUpdate
    ): Response<ResponseBody>

    @GET("${POST}/{post_id}/account-number")    // 대표자 계좌번호 조회
    suspend fun getAccountNumber(
        @Path("post_id") postId: String,
    ): Response<AccountNumber>

    @PATCH("${POST}/{post_id}/fee")     // 배달비 수정
    suspend fun updateBaedalFee(
        @Path("post_id") postId: String,
        @Body jsonparams: Fee
    ): Response<ResponseBody>

    @PATCH("${POST}/{post_id}/status")    // 게시글 상태 변경
    suspend fun setBaedalStatus(
        @Path("post_id") postId: String,
        @Body jsonparams: BaedalStatus
    ): Response<ResponseBody>


    /** 주문  */

    @GET("${POST}/{post_id}/orders")        // 주문 조회
    suspend fun getAllOrders(
        @Path("post_id") postId: String
    ): Response<AllOrderInfo>

    @POST("${POST}/{post_id}/orders")       // 주문 작성
    suspend fun postOrders(
        @Path("post_id") postId: String,
        @Body jsonparams: UserOrder
    ): Response<ResponseBody>

    @GET("${POST}/{post_id}/orders/me")     // 내 주문 조회
    suspend fun getMyOrders(
        @Path("post_id") postId: String
    ): Response<MyOrderInfo>

    @DELETE("${POST}/{post_id}/orders/me")  // 내 주문 삭제
    suspend fun deleteOrders(
        @Path("post_id") postId: String
    ): Response<ResponseBody>


    /** 댓글 */

//    @POST("${POST}/{post_id}/comments")         // 댓글 작성
//    suspend fun postComment(
//        @Path("post_id") postId: String,
//        @Body jsonparams: PostComment
//    ): Response<ResponseBody>
//
//    @GET("${POST}/{post_id}/comments")          // 댓글 조회
//    suspend fun getComments(
//        @Path("post_id") postId: String
//    ): Response<GetComments>
//
//    @POST("${POST}/{post_id}/comments/{comment_id}")    // 대댓글 작성
//    suspend fun postSubComment(
//        @Path("post_id") postId: String,
//        @Path("comment_id") commentId: String,
//        @Body jsonparams: PostComment
//    ): Response<ResponseBody>
//
//    @DELETE("${POST}/{post_id}/comments/{comment_id}")  // 댓글 삭제
//    suspend fun deleteComment(
//        @Path("post_id") postId: String,
//        @Path("comment_id") commentId: String
//    ): Response<ResponseBody>
}
