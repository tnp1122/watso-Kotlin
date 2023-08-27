package com.watso.app.feature.baedal.ui.view.baedalConfirm

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.watso.app.MainActivity
import com.watso.app.R
import com.watso.app.databinding.FragBaedalConfirmBinding
import com.google.gson.Gson
import com.watso.app.BaseFragment
import com.watso.app.data.model.BaseResponse
import com.watso.app.feature.baedal.data.*
import com.watso.app.feature.baedal.ui.view.baedalList.FragmentBaedalList
import com.watso.app.feature.baedal.ui.view.baedalPost.FragmentBaedalPost
import com.watso.app.feature.baedal.ui.viewModel.BaedalConfirmViewModel
import com.watso.app.util.RequestPermission
import com.watso.app.util.SessionManager
import java.text.DecimalFormat

private const val MAKE_POST = "게시글 작성"
private const val MAKE_ORDERS = "주문 작성"

class FragmentBaedalConfirm :BaseFragment() {

    lateinit var userOrder: UserOrder
    lateinit var storeDetail: StoreDetail
    lateinit var makePostForm: MakePostForm

    var mBinding: FragBaedalConfirmBinding? = null
    val binding get() = mBinding!!
    val TAG = "[FragBaedalConfirm]"
    val baedalConfirmViewModel by viewModels<BaedalConfirmViewModel> ()

    val gson = Gson()
    val dec = DecimalFormat("#,###")

    var postId = ""
    var orderString = ""
    var fee = 0
    var complete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getString("postId")!!
            orderString = it.getString("order")!!
            storeDetail = gson.fromJson(it.getString("storeDetail"), StoreDetail::class.java)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalConfirmBinding.inflate(inflater, container, false)

        setOrderInfo()
        setUpUI()
        setAdapters()
        setClickListeners()
        setObservers()

        return binding.root
    }

    override fun onDestroyView() {
        Log.d("[$TAG]onDestroyView", complete.toString())
        if (complete) {
            AC.removeString("makeBaedalPostForm")
            AC.removeString("store")
            AC.removeString("userOrder")
            AC.removeString("minMember")
        } else {
            AC.setString("userOrder", gson.toJson(userOrder))
            val bundle = bundleOf("orderCnt" to userOrder.orders.size)
            activity?.supportFragmentManager?.setFragmentResult("addOrder", bundle)
        }
        super.onDestroyView()
    }

    fun setOrderInfo() {
        /** 전체 주문 데이터 */
        val userOrderString = AC.getString("userOrder", "")
        if (userOrderString != "") {
            userOrder = gson.fromJson(userOrderString, UserOrder::class.java)
        } else {
            val userId = SessionManager.getUserId(fragmentContext)
            val nickname = SessionManager.getNickname(fragmentContext)
            userOrder = UserOrder(userId, nickname, "", mutableListOf<Order>(), null)
        }

        /** FragBaedalOpt 프래그먼트에서 추가한 주문 */
        if (orderString != "") userOrder.orders.add(gson.fromJson(orderString, Order::class.java))

        /** FragBaedalAdd 프래그먼트에서 작성한 게시글 내용 */
        val postString = AC.getString("baedalPosting", "")
        if (postString != "") {
            makePostForm = gson.fromJson(postString, MakePostForm::class.java)
            fee = storeDetail.fee / makePostForm.minMember
        }
        else fee = storeDetail.fee / AC.getString("minMember", "").toInt()

        Log.d("FragBaedalConfirm onCreate View orders", userOrder.toString())
    }

    fun setUpUI() {
        binding.tvStoreName.text = storeDetail.name
        binding.tvBaedalFee.text = "${dec.format(fee)}원"
        if (postId != "-1") {
            binding.tvConfirm.text = "주문 등록"
            binding.lytRequest.visibility = View.GONE
        }
        setPriceText()
    }

    fun setAdapters() {
        binding.rvOrderList.layoutManager =
            LinearLayoutManager(fragmentContext, LinearLayoutManager.VERTICAL, false)
        binding.rvOrderList.setHasFixedSize(true)

        val adapter = SelectedMenuAdapter(fragmentContext, userOrder.orders)
        binding.rvOrderList.adapter = adapter

        adapter.setItemClickListener(object: SelectedMenuAdapter.OnItemClickListener {
            override fun onChange(position: Int, change: String) {
                val order = userOrder.orders[position]

                when (change) {
                    "remove" -> userOrder.orders.removeAt(position)//order.quantity = 0
                    "sub" -> order.quantity -= 1
                    else -> order.quantity += 1
                }

                setPriceText()

                var confirmAble = false
                userOrder.orders.forEach {
                    if (it.quantity > 0) confirmAble = true
                }
                if (!confirmAble) {
                    binding.btnConfirm.setEnabled(false)
                    binding.btnConfirm.setBackgroundResource(R.drawable.solid_gray_10)
                }
            }
        })
    }

    fun setClickListeners() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }
        binding.lytAddMenu.setOnClickListener { onBackPressed() }
        binding.btnConfirm.setOnClickListener { onConfirmBtn() }
    }

    fun setObservers() {
        baedalConfirmViewModel.makePostResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onMakePostSuccess(it.data)
                is BaseResponse.Error -> onError(TAG, MAKE_POST, it.errorBody, it.msg)
                is BaseResponse.Exception -> onException(TAG, MAKE_POST, it.toString())
            }
        }

        baedalConfirmViewModel.makeOrdersResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onMakeOrdersSuccess()
                is BaseResponse.Error -> onError(TAG, MAKE_ORDERS, it.errorBody, it.msg)
                is BaseResponse.Exception -> onException(TAG, MAKE_ORDERS, it.toString())
            }
        }
    }

    fun setPriceText() {
        var totalPrice = userOrder.getTotalPrice()
        binding.tvOrderPrice.text = "${dec.format(totalPrice)}원"
        binding.tvTotalPrice.text = "${dec.format(totalPrice + fee)}원"
    }

    fun onMakePostSuccess(makePostResponse: MakePostResponse?) {
        super.onSuccess()

        if (makePostResponse == null) {
            onExceptionalProblem(TAG, MAKE_POST)
            return
        }

        complete = true
        navigateToPost(true)
    }

    fun onMakeOrdersSuccess() {
        super.onSuccess()

        complete = true
        val bundle = bundleOf()
        activity?.supportFragmentManager?.setFragmentResult("backToBaedalList", bundle)
        navigateToPost()
    }

    fun onConfirmBtn(){
        if (postId == "-1") {   // 게시글 작성일 때
            makePostForm.order = userOrder

            baedalConfirmViewModel.makePost(makePostForm)
        } else {            // 게시글에 참가할 때
            userOrder.requestComment = binding.etRequest.text.toString()

            baedalConfirmViewModel.makeOrders(postId, userOrder)
        }
    }

    fun navigateToPost(isPostiong: Boolean=false) {
        RequestPermission(activity as MainActivity).requestNotificationPermission()
        if (isPostiong)
            navigateTo(FragmentBaedalList(), popBackStack = 0)
        else
            navigateTo(FragmentBaedalPost(), mapOf("postId" to postId), 3)
    }
}
