package com.watso.app.feature.baedal.ui.view.baedalOption

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.watso.app.BaseFragment
import com.watso.app.data.model.BaseResponse
import com.watso.app.databinding.FragBaedalOptBinding
import com.watso.app.feature.baedal.data.*
import com.watso.app.feature.baedal.ui.view.baedalConfirm.FragmentBaedalConfirm
import com.watso.app.feature.baedal.ui.viewModel.BaedalOptionViewModel
import java.text.DecimalFormat

private const val GET_MENU_DETAIL = "메뉴 상세정보 조회"

class FragmentBaedalOpt :BaseFragment() {

    lateinit var storeDetail: StoreDetail
    lateinit var menuDetail: MenuDetail                   // 메뉴 정보. 현재화면 구성에 사용
    lateinit var adapter: BaedalOptGroupAdapter

    var mBinding: FragBaedalOptBinding? = null
    val binding get() = mBinding!!
    val TAG="[FragBaedalOpt]"
    val baedalOptionViewModel by viewModels<BaedalOptionViewModel> ()

    val gson = Gson()
    val dec = DecimalFormat("#,###")

    val groupNames = mutableMapOf<String, String>()
    val optionNames = mutableMapOf<String, String>()
    val quantities = mutableMapOf<String, List<Int>>()
    val optionPrice = mutableMapOf<String, MutableMap<String, Int>>()
    val optionChecked = mutableMapOf<String, MutableMap<String, Boolean>>()

    var postId = ""
    var menuId = ""
    var orderCnt = ""
    var quantity = 1
    var price = 0
    var viewClickAble = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getString("postId")!!
            menuId = it.getString("menuId")!!
            storeDetail = gson.fromJson(it.getString("storeDetail"), StoreDetail::class.java)
            orderCnt = it.getString("orderCnt")!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalOptBinding.inflate(inflater, container, false)

        setAdapters()
        setClickListeners()
        setObservers()

        getMenuDetail(storeDetail._id, menuId)

        return binding.root
    }

    fun setAdapters() {
        binding.rvOptionGroup.layoutManager =
            LinearLayoutManager(fragmentContext, LinearLayoutManager.VERTICAL, false)
        binding.rvOptionGroup.setHasFixedSize(true)
        adapter = BaedalOptGroupAdapter(fragmentContext)
        binding.rvOptionGroup.adapter = adapter
    }

    fun setClickListeners() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }

        binding.btnSub.setOnClickListener {
            if (quantity > 1) {
                binding.tvQuantity.text = "${(--quantity)}개"
                setOrderPrice()
            }
        }

        binding.btnAdd.setOnClickListener {
            if (quantity < 10) {
                binding.tvQuantity.text = "${(++quantity)}개"
                setOrderPrice()
            }
        }

        /** 메뉴 담기 버튼*/
        binding.btnCartConfirm.setOnClickListener {
            if (viewClickAble) {
                viewClickAble = false
                val groups = mutableListOf<Group>()
                optionChecked.forEach {
                    val groupId = it.key
                    val options = mutableListOf<Option>()
                    it.value.forEach {
                        if (it.value) {
                            val optionId = it.key
                            val selectedPrice = optionPrice[groupId]!![optionId]!!
                            val option = Option(optionId, optionNames[optionId]!!, selectedPrice)
                            options.add(option)
                        }
                    }
                    if (options.isNotEmpty()) { groups.add(Group(
                        groupId,
                        groupNames[groupId]!!,
                        quantities[groupId]!![0],
                        quantities[groupId]!![1],
                        options
                    ))}
                }
                val menu = MenuDetail(menuDetail._id, menuDetail.name, menuDetail.price, groups)
                val order = Order(quantity, price, menu)
                navigateTo(
                    FragmentBaedalConfirm(), mapOf(
                        "postId" to postId,
                        "order" to gson.toJson(order),
                        "storeDetail" to gson.toJson(storeDetail)
                    ),
                    1
                )
            }
        }

        adapter.setGroupOptClickListener(object: BaedalOptGroupAdapter.OnGroupOptClickListener {
            override fun onClick(groupId: String, isRadio: Boolean, optionId: String, isChecked: Boolean) {
                setChecked(groupId, isRadio, optionId, isChecked)
                setOrderPrice()
            }
        })
    }

    fun setObservers() {
        baedalOptionViewModel.getMenuDetailResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onGetMenuDetailSuccess(it.data)
                is BaseResponse.Error -> onError(TAG, GET_MENU_DETAIL, it.errorBody, it.msg)
                else -> onException(TAG, GET_MENU_DETAIL, it.toString())
            }
        }
    }

    fun onGetMenuDetailSuccess(menuDetail: MenuDetail?) {
        super.onSuccess()

        if (menuDetail == null) {
            onExceptionalProblem(TAG, GET_MENU_DETAIL)
            return
        }

        this.menuDetail = menuDetail
        menuDetail.groups?.let{ adapter.setData(it) }
        val dec = DecimalFormat("#,###")
        binding.tvMenuName.text = menuDetail.name
        binding.tvMenuPrice.text = "기본 가격 : ${dec.format(menuDetail.price)}원"
        setGroupOptionData()
        setOrderPrice()
    }

    fun getMenuDetail(storeId: String, menuId: String) {
        baedalOptionViewModel.getMenuDetail(storeId, menuId)
    }

    fun setGroupOptionData() {
        menuDetail.groups!!.forEach {
            val groupId = it._id
            val groupName = it.name
            var radioFirst = true
            val minQ = it.minOrderQuantity
            val maxQ = it.maxOrderQuantity

            groupNames[groupId] = groupName
            quantities[groupId] = listOf(minQ, maxQ)
            optionChecked[groupId] = mutableMapOf<String, Boolean>()
            optionPrice[groupId] = mutableMapOf<String, Int>()

            it.options!!.forEach {
                val optionId = it._id

                if (radioFirst && (minQ == 1 && maxQ == 1)) {
                    optionChecked[groupId]!![optionId] = true
                    radioFirst = false
                }
                else optionChecked[groupId]!![optionId] = false

                optionNames[optionId] = it.name
                optionPrice[groupId]!![optionId] = it.price
            }
        }
    }

    fun setChecked(groupId: String, isRadio:Boolean, optionId: String, isChecked: Boolean){
        if (isRadio) {
            for (i in optionChecked[groupId]!!.keys) {
                optionChecked[groupId]!![i] = (i == optionId)
            }
        } else {
            optionChecked[groupId]!![optionId] = isChecked
            var quantity = 0
            optionChecked[groupId]!!.forEach { if (it.value) quantity += 1 }
        }
    }

    fun setOrderPrice() {
        price = menuDetail.price
        optionChecked.forEach{
            val groupId = it.key
            it.value.forEach{
                val optionId = it.key
                if (it.value) price += optionPrice[groupId]!![optionId]!!
            }
        }
        val orderPriceStr = "${dec.format(price * quantity)}원"
        binding.tvOrderPrice.text = orderPriceStr
    }

    override fun onBackPressed() {
        val bundle = bundleOf("orderCnt" to orderCnt.toInt())
        activity?.supportFragmentManager?.setFragmentResult("addOrder", bundle)

        super.onBackPressed()
    }
}
