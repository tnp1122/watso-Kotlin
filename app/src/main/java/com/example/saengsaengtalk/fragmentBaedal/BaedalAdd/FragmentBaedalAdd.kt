package com.example.saengsaengtalk.fragmentBaedal.BaedalAdd

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.APIS.*
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.R
import com.example.saengsaengtalk.databinding.FragBaedalAddBinding
import com.example.saengsaengtalk.fragmentBaedal.BaedalConfirm.SelectedMenuAdapter
import com.example.saengsaengtalk.fragmentBaedal.BaedalMenu.FragmentBaedalMenu
import com.example.saengsaengtalk.fragmentBaedal.BaedalOrder
import com.example.saengsaengtalk.fragmentBaedal.Group
import com.example.saengsaengtalk.fragmentBaedal.BaedalPost.FragmentBaedalPost
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class FragmentBaedalAdd :Fragment() {
    var isUpdating = false
    var postId:String? = null
    var title: String? = null
    var content: String? = null
    var orderTime: String? = null
    var storeName: String? = null
    var place: String? = null
    var minMember: Int? = null
    var maxMember: Int? = null
    var fee: Int? = null

    var baedalfee = 0
    var orderPrice = 0
    var orders = JSONArray()

    var stores = listOf<StoreListModel>()
    var storeIds = mutableListOf<String>()
    var storeNames = mutableListOf<String>()
    var storeFees = mutableListOf<Int>()
    var selectedIdx = 0

    private var mBinding: FragBaedalAddBinding? = null
    private val binding get() = mBinding!!
    val gson = Gson()
    val api = APIS.create()
    var decDt = DecimalFormat("00")
    val decPrice = DecimalFormat("#,###")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isUpdating = it.getString("isUpdating").toBoolean()
            if (isUpdating) {
                postId = it.getString("postId")
                title = it.getString("title")
                content = it.getString("content")
                orderTime = it.getString("orderTime")
                storeName = it.getString("storeName")
                place = it.getString("place")
                minMember = it.getString("minMember")?.toInt()
                maxMember = it.getString("maxMember")?.toInt()
                fee = it.getString("fee")?.toInt()
            }
        }
        println("${isUpdating}, ${postId}, ${title}, ${content}, ${orderTime}, ${storeName}, ${place}, ${minMember}, ${maxMember}, ${fee}")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalAddBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {

        binding.btnPrevious.setOnClickListener { onBackPressed() }

        binding.lytTime.setOnClickListener { showCalendar() }


        val places = listOf("생자대", "기숙사")
        val placeSpinerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, places)
        binding.spnPlace.adapter = placeSpinerAdapter

        if (!isUpdating) {
            orderTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")).toString()
            binding.tvOrderTime.text = getDateTimeFormating(orderTime.toString())
            setStoreSpinner()
            binding.lytChoice.setOnClickListener {
                setFrag(
                    FragmentBaedalMenu(), mapOf(
                        "member" to "0",
                        "isPosting" to "true",
                        "storeName" to storeNames[selectedIdx],
                        "storeId" to storeIds[selectedIdx].toString(),
                        "baedalFee" to storeFees[selectedIdx].toString(),
                        "orders" to orders.toString()
                    )
                )
            }

            getActivity()?.getSupportFragmentManager()
                ?.setFragmentResultListener("ConfirmToPosting", this) { requestKey, bundle ->
                    orders = JSONArray(bundle.getString("ordersString"))

                    binding.rvMenuList.layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

                    val adapter = SelectedMenuAdapter(requireContext(), orders, false)
                    binding.rvMenuList.adapter = adapter

                    orderPrice = 0
                    for (i in 0 until orders.length()) {
                        val order = orders.getJSONObject(i)
                        orderPrice += order.getInt("sumPrice") * order.getInt("count")
                    }
                    setBindText()

                    if (orderPrice > 0) {
                        binding.btnPostAdd.isEnabled = true
                        binding.btnPostAdd.setBackgroundResource(R.drawable.btn_baedal_confirm)
                    }
                }
        } else {
            binding.lytStore.visibility = View.GONE
            binding.lytChoice.visibility = View.GONE
            binding.lytTable.visibility = View.GONE

            binding.etTitle.setText(title)
            binding.etContent.setText(content)
            binding.tvOrderTime.text = getDateTimeFormating(orderTime!!)
            binding.tvStoreName.text = storeName
            if (place == "기숙사") binding.spnPlace.setSelection(1)
            if (minMember != 0) {
                binding.cbMinMember.setChecked(true)
                binding.etMinMember.setText(minMember.toString())
            }
            if (maxMember != 0) {
                binding.cbMaxMember.setChecked(true)
                binding.etMaxMember.setText(maxMember.toString())
            }
            binding.tvBaedalStoreFee.text = "${decPrice.format(fee)}원"

            binding.btnPostAdd.isEnabled = true
            binding.btnPostAdd.setBackgroundResource(R.drawable.btn_baedal_confirm)
        }
        binding.btnPostAdd.setOnClickListener {
            if (binding.etTitle.text.toString() == "") {
                makeToast("제목을 입력해주세요.")
            } else {
                baedalPosting()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showCalendar() {
        val cal = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            run {
                var dateString = "${year}-${decDt.format(month + 1)}-${decDt.format(dayOfMonth)}T"

                val timeSetListener =
                    TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                        run {
                            var timeString = "${decDt.format(hourOfDay)}:${decDt.format(minute)}:00"
                            orderTime = LocalDateTime.parse(dateString+timeString).toString()
                            //println(orderTime)
                            binding.tvOrderTime.text = getDateTimeFormating(orderTime.toString())
                        }
                    }

                TimePickerDialog(requireContext(), timeSetListener,
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show()
            }
        }

        val dpd = DatePickerDialog(requireContext(), dateSetListener,
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        dpd.datePicker.minDate = System.currentTimeMillis() - 1000;
        dpd.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDateTimeFormating(dateTimeStr: String): String {
        val dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
        val formatter = DateTimeFormatter.ofPattern("MM/dd(E) HH:mm").withLocale(Locale.forLanguageTag("ko"))
        return dateTime.format(formatter)
    }

    fun setStoreSpinner(){
        api.getStoreList().enqueue(object : Callback<List<StoreListModel>> {
            override fun onResponse(
                call: Call<List<StoreListModel>>,
                response: Response<List<StoreListModel>>
            ) {
                Log.d("log", response.toString())
                Log.d("log", response.body().toString())
                stores = response.body()!!
                stores.forEach {
                    storeIds.add(it.store_id)
                    storeNames.add(it.store_name)
                    storeFees.add(it.fee)
                }

                val searchmethod =
                    ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, storeNames)

                searchmethod.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spnStore!!.adapter = searchmethod
                binding.spnStore.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        baedalfee = storeFees[position]
                        setBindText()
                        selectedIdx = position
                    }

                    override fun onNothingSelected(p0: AdapterView<*>?) { }
                }
            }

            override fun onFailure(call: Call<List<StoreListModel>>, t: Throwable) {
                // 실패
                Log.d("log", t.message.toString())
                Log.d("log", "fail")
            }
        })
    }

    fun setBindText() {
        binding.tvBaedalStoreFee.text = "${decPrice.format(baedalfee)}원"
        binding.tvBaedalFee.text = "${decPrice.format(baedalfee)}원"
        binding.tvOrderPrice.text = "${decPrice.format(orderPrice)}원"
        binding.tvTotalPrice.text = "${decPrice.format(baedalfee+orderPrice)}원"//"${decPrice.format(totalPrice)}원"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun baedalPosting() {
        val minMember = if (binding.cbMinMember.isChecked)
            binding.etMinMember.text.toString().toInt() else null
        val maxMember = if (binding.cbMaxMember.isChecked)
            binding.etMaxMember.text.toString().toInt() else null


        if (isUpdating) {
            val baedalUpdateModel = BaedalUpdateModel(
                postId.toString(),
                title!!,
                if (content!="") content!! else null,
                orderTime!!,
                place!!,
                if (binding.cbMinMember.isChecked) binding.etMinMember.text.toString().toInt() else null,
                if (binding.cbMaxMember.isChecked) binding.etMaxMember.text.toString().toInt() else null
            )
            api.updateBaedalPost(baedalUpdateModel)
                .enqueue(object : Callback<BaedalPostingResponse> {
                    override fun onResponse(
                        call: Call<BaedalPostingResponse>,
                        response: Response<BaedalPostingResponse>
                    ) {
                        println("성공")
                        Log.d("log", response.toString())
                        Log.d("log", response.body().toString())
                        val postingResult = response.body()!!
                        println(postingResult)

                        val bundle = bundleOf("updateResult" to postingResult.sucess)
                        println(bundle)
                        getActivity()?.getSupportFragmentManager()?.setFragmentResult("updatePost", bundle)
                        onBackPressed()
                    }

                    override fun onFailure(call: Call<BaedalPostingResponse>, t: Throwable) {
                        println("실패")
                        Log.d("log", t.message.toString())
                        Log.d("log", "fail")
                    }
                })
        } else {
            var orderTimeString = orderTime!!//formattedToDateTimeString(binding.tvOrderTime.text.toString())
            val baedalPostModel = BaedalPostingModel(
                storeIds[selectedIdx],
                binding.etTitle.text.toString(),
                binding.etContent.text.toString(),
                orderTimeString,
                binding.spnPlace.selectedItem.toString(),
                minMember,
                maxMember
            )

            api.baedalPosting(baedalPostModel)
                .enqueue(object : Callback<BaedalPostingResponse> {
                    override fun onResponse(
                        call: Call<BaedalPostingResponse>,
                        response: Response<BaedalPostingResponse>
                    ) {
                        println("성공")
                        Log.d("log", response.toString())
                        Log.d("log", response.body().toString())
                        val postingResult = response.body()!!
                        //println(postingResult)
                        if (!postingResult.sucess) {
                            makeToast("게시글을 작성하지 못 했습니다.\n다시 시도해 주세요.")
                            println(postingResult)
                        }
                        else baedalOrdering(postingResult.post_id)

                    }

                    override fun onFailure(call: Call<BaedalPostingResponse>, t: Throwable) {
                        println("실패")
                        Log.d("log", t.message.toString())
                        Log.d("log", "fail")
                    }
                })
        }
    }

    fun baedalOrdering(postId: String){
        val ordersObject: List<BaedalOrder> = gson.fromJson(orders.toString(), object: TypeToken<MutableList<BaedalOrder>>() {}.type)
        val orderings = mutableListOf<OrderingOrder>()
        for (order in ordersObject) {
            orderings.add(getOrdering(order))
        }

        val orderingModel = OrderingModel(
            storeIds[selectedIdx],
            postId,
            orderings
        )
        println(orderingModel)
        api.baedalOrdering(orderingModel).enqueue(object : Callback<OrderingResponse> {
            override fun onResponse(call: Call<OrderingResponse>, response: Response<OrderingResponse>) {
                println("성공")
                Log.d("log", response.toString())
                Log.d("log", response.body().toString())
                val result = response.body()!!
                if (!result.sucess) {
                    makeToast("게시글을 작성하지 못 했습니다.\n다시 시도해 주세요.")
                    println(result)
                }
                else setFrag(FragmentBaedalPost(), mapOf("postId" to "0"))
                //setFrag(FragmentBaedalPost(), mapOf("postId" to result.post_id))
            }

            override fun onFailure(call: Call<OrderingResponse>, t: Throwable) {
                println("실패")
                Log.d("log", t.message.toString())
                Log.d("log", "fail")
            }
        })
    }

    fun getOrdering(order: BaedalOrder): OrderingOrder {
        val groups = mutableListOf<OrderingGroup>()
        for (group in order.groups) {
            groups.add(getGroup(group))
        }
        return OrderingOrder(order.count, order.menuName, groups)
    }

    fun getGroup(group: Group): OrderingGroup {
        val options = mutableListOf<Int>()
        for (option in group.options){
            options.add(option.optionId!!)
        }
        return OrderingGroup(group.groupId!!, options)
    }

    fun makeToast(message: String) {
        val mActivity = activity as MainActivity
        mActivity.makeToast(message)
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null, popBackStack:Int =-1) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments, popBackStack, 1)
    }

    fun onBackPressed() {
        val mActivity =activity as MainActivity
        mActivity.onBackPressed()
    }
}