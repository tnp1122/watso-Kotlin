package com.example.saengsaengtalk.APIS
import com.google.gson.annotations.SerializedName
import java.util.*

/** 가게 목록 조회 모델*/
data class Store(
    val _id: String,
    val name: String,
    val fee: Int,
    @SerializedName("min_order")
    val minOrder: Int
)

/** 가게 상세 정보(메뉴) 조회 모델 */
data class StoreInfo(
    val _id: String,
    val name: String,
    @SerializedName("min_order")
    val minOrder: Int,
    val fee: Int,
    val menus: List<Menu>
)

data class Menu(
    val section: String,
    val name: String,
    val price: Int
)

/** 메뉴 상세 정보(옵션) 조회 모델 */
data class MenuInfo(
    val section: String,
    val name: String,
    val price: Int,
    val groups: List<Group>
)

data class Group(
    val _id: String,
    val name: String,
    @SerializedName("min_order_quantity")
    val minOrderQuantity: Int,
    @SerializedName("max_order_quantity")
    val maxOrderQuantity: Int,
    val options: List<Option>
)

data class Option(
    val _id: String,
    val name: String,
    val price: Int
)

/** 배달 게시글 등록 모델 */
data class BaedalPosting(
    @SerializedName("store_id")
    val storeId: String,
    val title: String,
    val content: String?,
    @SerializedName("order_time")
    val orderTime: String,
    val place: String,
    @SerializedName("min_member")
    val minMember: Int?,
    @SerializedName("max_member")
    val maxMember: Int?
)

data class BaedalPostingResponse(
    @SerializedName("post_id")
    val postId: String
)

/** 배달 게시글 조회 모델 */
data class BaedalPost(
    val _id: String,
    @SerializedName("user_id")
    val userId: Long,
    @SerializedName("nick_name")
    val nickName: String,
    val store: Store,
    val title: String,
    val place: String,
    @SerializedName("order_time")
    val orderTime: String,
    @SerializedName("update_time")
    val updateTime: String,
    @SerializedName("min_member")
    val minMember: Int,
    @SerializedName("max_member")
    val maxMember: Int,
    @SerializedName("users")
    val userOrders: List<UserOrder>
)


data class UserOrder(
    @SerializedName("user_id")
    val userId: Long,
    @SerializedName("nick_name")
    val nickName: String,
    val orders: List<Order>,
    val isMyOrder: Boolean?     // 데이터 조회 X, 어댑터 연결시에만 사용
)

data class Order(
    val _id: String,
    val quantity: Int,
    @SerializedName("order_price")
    val orderPrice: Int,
    val menu: OrderMenu
)

data class OrderMenu(
    val name: String,
    @SerializedName("menu_price")
    val menuPrice: Int,
    val groups: List<OrderGroup>
)

data class OrderGroup(
    val _id: String,
    val name: String?,  // 현재 API에 조회안됨, 수정요청 필요함
    val options: List<OrderOption>
)

data class OrderOption(
    val _id: String,
    val name: String,
    val price: Int
)

/** 206 배달 게시글 수정 모델 */
data class BaedalUpdateModel(
    val post_id: String,
    val title: String,
    val content: String?,
    val order_time: String,
    val place: String,
    val min_member: Int?,
    val max_member: Int?
)

/** 207, 305 마감 여부 응답 모델 */
data class IsClosedResponse(
    val success: Boolean,
    val post_id: String,
    val is_closed: Boolean
)

/** 배달 주문 등록 모델 */
data class Ordering(
    val store_id: String,
    val orders: List<OrderingOrder>
)

data class OrderingOrder(
    val quantity: Int,
    val menu: List<OrderingMenu>
)

data class OrderingMenu(
    val name: String,
    val groups: List<OrderingGroups>
)

data class OrderingGroups(
    val _id: String,
    val options: List<String>
)

data class OrderingResponse(
    val message: String?
)

/** 211, 306 그룹 참가 응답 모델 */
data class JoinResponse(
    val post_id: String,
    val success: Boolean,
    val join: Boolean
)

/** 배달 게시글 목록 조회 모델 */
data class BaedalPostPreview(
    val _id: String,
    val title: String,
    //val user_id: Long,
    val join_users: List<Long>,
    //val nick_name: String,
    val store: Store,
    val order_time: String
)