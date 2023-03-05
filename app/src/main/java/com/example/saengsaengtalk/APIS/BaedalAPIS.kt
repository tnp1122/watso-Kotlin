package com.example.saengsaengtalk.APIS

import retrofit2.Call
import retrofit2.http.*

interface BaedalAPIS {
    /** 배달 게시물 관련 api */

    @GET("delivery/store")              // 가게 리스트 조회
    fun getStoreList(): Call<List<Store>>

    @GET("delivery/store/{store_id}")   // 가게 상세정보(메뉴) 조회
    fun getStoreInfo(
        @Path("store_id") storeId: String
    ): Call<StoreInfo>

    @GET("delivery/store/{store_id}/{menu_name}")   // 메뉴 상세정보(옵션) 조회
    fun getMenuInfo(
        @Path("store_id") storeId: String,
        @Path("menu_name") menuName: String
    ): Call<MenuInfo>

    @POST("delivery/post")             // 배달 게시글 등록
    fun baedalPosting(
        @Body jsonparams: BaedalPosting
    ): Call<BaedalPostingResponse>

    @GET("delivery/post/{post_id}")     // 배달 게시글 조회
    fun getBaedalPost(
        @Path("post_id") postId: String
    ): Call<BaedalPost>

    @PATCH("order/post/update")             // 206 배달 게시글 수정
    fun updateBaedalPost(
        @Body jsonparams: BaedalUpdateModel
    ): Call<BaedalPostingResponse>

    @PATCH("order/post/isClosed/condition-switch")    // 207 배달 주문 가능 여부 변경
    fun switchBaedalIsClosed(
        @Body jsonparams: Map<String, String>
    ): Call<IsClosedResponse>

    @POST("delivery/post/{post_id}/order")                 // 배달 주문 등록
    fun baedalOrdering(
        @Path("post_id") postId: String,
        @Body jsonparams: Ordering
    ): Call<OrderingResponse>

    @GET("/order/ordering/update")          // 210 배달 주문 수정용 데이터 요청
    fun getOrders(
        @Query("post_id") postId: String
    ): Call<UserOrder?>

    @PATCH("order/ordering/update")         // 210 배달 주문 수정
    fun baedalOrderUpdate(
        @Body jsonparams: Ordering
    ): Call<OrderingResponse>

    @PATCH("delivery/post/{post_id}/join")// 배달 그룹 참여
    fun switchBaedalJoin(
        @Path("post_id") postId: String
    ): Call<JoinResponse>

    @GET("delivery/post")                 // 배달 게시글 목록 조회
    fun getBaedalPostList(
    ): Call<List<BaedalPostPreview>>
}