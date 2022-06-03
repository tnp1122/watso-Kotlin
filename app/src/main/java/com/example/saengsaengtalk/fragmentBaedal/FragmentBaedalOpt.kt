package com.example.saengsaengtalk.fragmentBaedal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.adapterBaedal.*
import com.example.saengsaengtalk.databinding.FragBaedalOptBinding
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat

class FragmentBaedalOpt :Fragment() {
    var menu: JSONObject? = null
    var section: String? = null

    val radioPrice = mutableMapOf<String, Int>()
    val comboPrice = mutableMapOf<String, Int>()
    val radioChecked = mutableMapOf<String, Int>()
    val comboChecked = mutableMapOf<String, Int>()
    var count = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val jsonString = it.getString("menu")
            menu = JSONObject(jsonString)
            section = it.getString("section")
            println("디테일 프래그먼트: ${jsonString}")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragBaedalOptBinding.inflate(inflater, container, false)

        refreshView(binding)

        return binding.root
    }

    fun refreshView(binding: FragBaedalOptBinding) {
        var areaMenu = mutableListOf<BaedalOptArea>()

        val id = menu!!.getInt("id")
        val menuName = menu!!.getString("menuName")
        val radios = menu!!.getJSONArray("radio")
        val combos = menu!!.getJSONArray("combo")
        val dec = DecimalFormat("#,###")


        for (i in 0 until radios.length()) {
            val radio = radios.getJSONObject(i)
            var temp = mutableListOf<BaedalOpt>()
            val area = radio.getString("area")
            val opts = radio.getJSONArray("option")

            for (j in 0 until opts.length()){
                val opt = opts.getJSONObject(j)
                val num = opt.getInt("num").toString()
                val optName = opt.getString("optName")
                val price = opt.getString("price").toInt()
                temp.add(BaedalOpt(num, optName, price, area,true))

                radioPrice[num] = price
                if (j == 0) radioChecked[num] = 1
                else radioChecked[num] = 0
            }
            areaMenu.add(BaedalOptArea(area, temp))
        }

        if (combos[0] != "") {
            for (i in 0 until combos.length()) {
                val combo = combos.getJSONObject(i)

                var temp = mutableListOf<BaedalOpt>()
                val area = combo.getString("area")
                val opts = combo.getJSONArray("option")
                val min = combo.getInt("min")
                val max = combo.getInt("max")

                for (j in 0 until opts.length()) {
                    val opt = opts.getJSONObject(j)
                    val num = opt.getInt("num").toString()
                    val optName = opt.getString("optName")
                    val price = opt.getString("price").toInt()
                    temp.add(BaedalOpt(num, optName, price, area,false, min, max))

                    comboPrice[num] = price
                    comboChecked[num] = 0
                }
                areaMenu.add(BaedalOptArea(area, temp))
            }
        }

        binding.tvMenuName.text = menuName
        binding.rvMenu.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvMenu.setHasFixedSize(true)
        binding.tvTotalPrice.text = "${dec.format(setTotalPrice())}원"
        val adapter = BaedalOptAreaAdapter(requireContext(), areaMenu)

        binding.rvMenu.addItemDecoration(BaedalOptAreaAdapter.BaedalOptAreaAdapterDecoration())
        adapter.notifyDataSetChanged()

        binding.rvMenu.adapter = adapter

        binding.btnSub.setOnClickListener {
            if (count > 1) binding.tvCount.text = (--count).toString()
            binding.tvTotalPrice.text = "${dec.format(setTotalPrice())}원"
        }
        binding.btnAdd.setOnClickListener {
            if (count < 10) binding.tvCount.text = (++count).toString()
            binding.tvTotalPrice.text = "${dec.format(setTotalPrice())}원"
        }

        adapter.addListener(object: BaedalOptAreaAdapter.OnItemClickListener {
            override fun onClick(isRadio: Boolean, area: String, num: String, isChecked: Boolean) {
                println("isRadio: ${isRadio}, area: ${area}, num:${num}, isChecked:${isChecked}")
                if (isRadio) setChecked(isRadio, area, num, isChecked, radios)
                else setChecked(isRadio, area, num, isChecked)
                binding.tvTotalPrice.text = "${dec.format(setTotalPrice())}원"
            }
        })

        binding.btnPrevious.setOnClickListener { onBackPressed() }
        binding.btnOrderConfirm.setOnClickListener {
            val jsonObject = JSONObject()
            jsonObject.put("section", section)
            jsonObject.put("id", id)
            jsonObject.put("radio", JSONObject(radioChecked as Map<*, *>))
            jsonObject.put("combo", JSONObject(comboChecked as Map<*, *>))
            jsonObject.put("count", count)

            //println("제이슨 출력: ${jsonObject.toString()}")
            val bundle = bundleOf("opt" to jsonObject.toString())
            getActivity()?.getSupportFragmentManager()?.setFragmentResult("menuWithOpt", bundle)
            onBackPressed()
        }
    }

    fun setTotalPrice(): Int {
        var totalPrice = 0

        if (radioChecked.isNotEmpty())
            for (i in radioChecked.keys)
                totalPrice += radioChecked[i]!! * radioPrice[i]!!
        if (comboChecked.isNotEmpty())
            for (i in comboChecked.keys)
                totalPrice += comboChecked[i]!! * comboPrice[i]!!

        return (totalPrice * count)
    }

    fun setChecked(isRadio: Boolean, area: String, num: String, isChecked: Boolean, optList: JSONArray= JSONArray()) {
        if (isRadio) {
            if (radioChecked[num] == 0){
                var radios = JSONObject()
                for (i in 0 until optList.length()){
                    if (optList.getJSONObject(i).getString("area") == area) {
                        radios = optList.getJSONObject(i)
                        break
                    }
                }
                var nums = mutableListOf<String>()
                val array = radios.getJSONArray("option")
                for (i in 0 until array.length()){
                    nums.add(array.getJSONObject(i).getString("num"))
                }
                for (i in nums) {
                    if (i == num) radioChecked[i] = 1
                    else radioChecked[i] = 0
                }
            }
        } else {
            if (isChecked) comboChecked[num] = 1
            else comboChecked[num] = 0
        }
        println("라디오: ${radioChecked}, 콤보: ${comboChecked}")
    }

    fun onBackPressed() {
        val mActivity = activity as MainActivity
        mActivity.onBackPressed()
    }
}