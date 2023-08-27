package com.watso.app.feature.baedal.ui.view.baedalOrders

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.watso.app.API.DataModels.ErrorResponse
import com.watso.app.BaseFragment
import com.watso.app.data.model.BaseResponse
import com.watso.app.databinding.FragBaedalOrdersBinding
import com.watso.app.feature.baedal.data.AllOrderInfo
import com.watso.app.feature.baedal.data.MyOrderInfo
import com.watso.app.feature.baedal.data.PostContent
import com.watso.app.feature.baedal.data.UserOrder
import com.watso.app.feature.baedal.ui.viewModel.BaedalOrdersViewModel
import com.watso.app.util.SessionManager
import java.lang.Exception
import java.text.DecimalFormat

private const val GET_MY_ORDERS = "내 주문 조회"
private const val GET_ALL_ORDERS = "전체 주문 조회"

class FragmentBaedalOrders :BaseFragment() {

    lateinit var userOrders: MutableList<UserOrder>
    lateinit var adapter: BaedalUserOrderAdapter

    lateinit var postContent: PostContent

    var mBinding: FragBaedalOrdersBinding? = null
    val binding get() = mBinding!!
    val TAG = "[FragBaedalOrders]"
    val baedalOrdersviewModel by viewModels<BaedalOrdersViewModel> ()

    val dec = DecimalFormat("#,###")

    var userId = (-1).toLong()
    var isMyorder = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postContent = Gson().fromJson(it.getString("postJson")!!, PostContent::class.java)
            isMyorder = it.getString("isMyOrder")!!.toBoolean()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalOrdersBinding.inflate(inflater, container, false)

        setUpUI()
        setAdapters()
        setClickListeners()
        setObservers()

        getOrders()

        return binding.root
    }

    fun setUpUI() {
        userId = SessionManager.getUserId(fragmentContext)
        binding.tvStoreName.text = postContent.store.name
        binding.tvOrder.text = if (isMyorder) "내가 고른 메뉴" else "주문할 메뉴"
    }

    fun setAdapters() {
        userOrders = mutableListOf<UserOrder>()
        adapter = BaedalUserOrderAdapter(fragmentContext, userOrders, isMyorder)

        binding.rvOrders.layoutManager =
            LinearLayoutManager(fragmentContext, LinearLayoutManager.VERTICAL, false)
        binding.rvOrders.setHasFixedSize(true)
        binding.rvOrders.adapter = adapter
    }

    fun setClickListeners() {
        binding.btnPrevious.setOnClickListener { AC.onBackPressed() }
    }

    fun setObservers() {
        baedalOrdersviewModel.getMyOrdersResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onGetOrdersSuccess(it.data)
                is BaseResponse.Error -> onError(TAG, GET_MY_ORDERS, it.errorBody, it.msg)
                is BaseResponse.Exception -> onException(TAG, GET_MY_ORDERS, it.toString())
            }
        }

        baedalOrdersviewModel.getAllOrdersResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onGetOrdersSuccess(it.data)
                is BaseResponse.Error -> onError(TAG, GET_ALL_ORDERS, it.errorBody, it.msg)
                is BaseResponse.Exception -> onException(TAG, GET_ALL_ORDERS, it.toString())
            }
        }
    }

    fun onGetOrdersSuccess(myOrderInfo: MyOrderInfo?) {
        super.onSuccess()

        if (myOrderInfo == null) {
            onExceptionalProblem(TAG, GET_MY_ORDERS)
            return
        }

        val userOrder = mutableListOf(myOrderInfo.userOrder)
        setUserOrder(userOrder)
    }

    fun onGetOrdersSuccess(allOrderInfo: AllOrderInfo?) {

        super.onSuccess()

        if (allOrderInfo == null) {
            onExceptionalProblem(TAG, GET_ALL_ORDERS)
            return
        }

        setUserOrder(allOrderInfo.userOrders)
    }

    fun getOrders() {
        if (isMyorder) {
            baedalOrdersviewModel.getMyOrders(postContent._id)
        } else {
            baedalOrdersviewModel.getAllOrders(postContent._id)
        }
    }

    fun setUserOrder(userOrderData: List<UserOrder>) {
        Log.d("FragBaedalOrders userOrderData", userOrderData.toString())
        Log.d("FragBaedalOrders userOrders", userOrders.toString())
        userOrders.clear()
        Log.d("FragBaedalOrders userOrders clear", userOrders.toString())
        userOrders.addAll(userOrderData)
        Log.d("FragBaedalOrders userOrders addAll", userOrders.toString())
        userOrders.forEach {
            it.isMyOrder = it.userId == userId
            it.orders.forEach { it.setPrice() }
        }
        binding.rvOrders.adapter!!.notifyDataSetChanged()

        setPriceText()
    }

    fun setPriceText() {
        val dec = DecimalFormat("#,###")

        if (isMyorder) {
            if (postContent.status == "delivered") {
                binding.lbFee.text = "1인당 배달비"
                binding.lbTotalPrice.text = "본인 부담 금액"
            }

            val price = userOrders[0].getTotalPrice()
            val personalFee = postContent.fee / postContent.users.size
            binding.tvOrderPrice.text = "${dec.format(price)}원"
            binding.tvFee.text = "${dec.format(personalFee)}원"
            binding.tvTotalPrice.text = "${dec.format(price + personalFee)}원"
        } else {
            var price = 0
            userOrders.forEach { price += it.getTotalPrice() }

            binding.tvOrderPrice.text = "${dec.format(price)}원"
            binding.tvFee.text = "${dec.format(postContent.fee)}원"
            binding.tvTotalPrice.text = "${dec.format(price + postContent.fee)}원"

            if (postContent.status == "delivered") {
                binding.lbFee.text = "배달비"
                binding.lbTotalPrice.text = "총 결제 금액"
            } else {
                binding.lbFee.text = "예상 배달비"
                binding.lbTotalPrice.text = "예상 총 결제 금액"
            }
        }
    }
}
