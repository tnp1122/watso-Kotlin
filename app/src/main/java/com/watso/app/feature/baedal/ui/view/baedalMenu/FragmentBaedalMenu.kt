package com.watso.app.feature.baedal.ui.view.baedalMenu

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.watso.app.BaseFragment
import com.watso.app.data.model.BaseResponse
import com.watso.app.databinding.FragBaedalMenuBinding
import com.watso.app.feature.baedal.data.StoreDetail
import com.watso.app.feature.baedal.ui.view.baedalOption.FragmentBaedalOpt
import com.watso.app.feature.baedal.ui.viewModel.BaedalMenuViewModel
//import com.watso.app.fragmentBaedal.BaedalConfirm.FragmentBaedalConfirm
import java.text.DecimalFormat

private const val GET_STORE_DETAIL = "가게 상세정보 조회"

class FragmentBaedalMenu :BaseFragment() {

    lateinit var adapter: BaedalMenuSectionAdapter
    lateinit var storeDetail: StoreDetail

    var mBinding: FragBaedalMenuBinding? = null
    val binding get() = mBinding!!
    val TAG = "[FragBaedalMenu]"
    val baedalMenuViewModel by viewModels<BaedalMenuViewModel> ()

    val gson = Gson()
    val dec = DecimalFormat("#,###")

    var postId = ""
    var storeId = "0"
    var orderCnt = 0
    var viewClickAble = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getString("postId")!!
            storeId = it.getString("storeId")!!
        }

        AC.removeString("userOrder")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalMenuBinding.inflate(inflater, container, false)

        setFragmentResultListener()
        setAdapters()
        setClickListeners()
        setObservers()
        setCartCount()

        getStoreInfo()


        return binding.root
    }

    fun setFragmentResultListener() {
        getActivity()?.getSupportFragmentManager()?.setFragmentResultListener("addOrder", this) {
                requestKey, bundle ->
            orderCnt = bundle.getInt("orderCnt")
            setCartCount()
        }
    }

    fun setAdapters() {
        adapter = BaedalMenuSectionAdapter(fragmentContext)

        /** 이중 어댑터안의 메뉴 이름을 선택할 경우 해당 메뉴의 옵션을 보여주는 프래그먼트로 이동 */
        adapter.setSecMenuClickListener(object : BaedalMenuSectionAdapter.OnSecMenuClickListener {
            override fun onClick(sectionName: String, menuId: String) {
                Log.d("메뉴 프래그먼트", "리스너")
                if (viewClickAble) {
                    viewClickAble = false
                    navigateTo(
                        FragmentBaedalOpt(), mapOf(
                        "postId" to postId,
                        "menuId" to menuId,
                        "storeDetail" to gson.toJson(storeDetail),
                        "orderCnt" to orderCnt.toString()
                    ))
                    Handler(Looper.getMainLooper()).postDelayed({ viewClickAble = true}, 500)
                }
            }
        })
        binding.rvMenuSection.layoutManager =
            LinearLayoutManager(fragmentContext, LinearLayoutManager.VERTICAL, false)
        binding.rvMenuSection.setHasFixedSize(true)
        binding.rvMenuSection.adapter = adapter
    }

    fun setClickListeners() {
        binding.btnPrevious.setOnClickListener { AC.onBackPressed() }
        binding.btnCart.setOnClickListener { onCartClick() }
    }

    fun setObservers() {
        baedalMenuViewModel.getStoreDetailResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onGetStoreDetailSuccess(it.data)
                is BaseResponse.Error -> onError(TAG, GET_STORE_DETAIL, it.errorBody, it.msg)
                else -> onException(TAG, GET_STORE_DETAIL, it.toString())
            }
        }
    }

    fun onGetStoreDetailSuccess(storeDetail: StoreDetail?) {
        super.onSuccess()

        if (storeDetail == null) {
            onExceptionalProblem(TAG, GET_STORE_DETAIL)
            return
        }

        this.storeDetail = storeDetail
        binding.tvStoreName.text = storeDetail.name
        adapter.setData(storeDetail.sections)
    }

    fun getStoreInfo() {
        baedalMenuViewModel.getStoreDetail(storeId)
    }

    fun setCartCount() {
        binding.tvCartCount.text = orderCnt.toString()

        if (orderCnt > 0) binding.lytFooter.visibility = View.VISIBLE
        else binding.lytFooter.visibility = View.GONE
    }

    fun onCartClick(){
        val map = mutableMapOf(
            "postId" to postId,
            "order" to "",
            "storeDetail" to gson.toJson(storeDetail)
        )
//        navigateTo(FragmentBaedalConfirm(), map)
    }
}
