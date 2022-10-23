package com.example.saengsaengtalk.fragmentBaedal.Baedal

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saengsaengtalk.MainActivity
import com.example.saengsaengtalk.databinding.FragBaedalBinding
import com.example.saengsaengtalk.fragmentBaedal.BaedalAdd.FragmentBaedalAdd
import com.example.saengsaengtalk.fragmentBaedal.BaedalPost.FragmentBaedalPost
import java.time.LocalDateTime

class FragmentBaedal :Fragment() {
    val fragIndex = 1

    private var mBinding: FragBaedalBinding? = null
    private val binding get() = mBinding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalBinding.inflate(inflater, container, false)

        refreshView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshView() {
        binding.btnBaedalPostAdd.setOnClickListener {
            setFrag(FragmentBaedalAdd())
        }
        val baedalList = arrayListOf(
            BaedalList(arrayListOf("주넝이", "abcc"),43, "네네치킨 같이 드실분~~", LocalDateTime.now(), "네네치킨", 3, 10000, 0),
            BaedalList(arrayListOf("abcc", "b"),11, "치킨 같이 드실분~~", LocalDateTime.now(), "네네치킨", 2, 10000, 0),
            BaedalList(arrayListOf("abcc", "c", "d"),35, "같이 드실분~~", LocalDateTime.now(), "네네치킨", 1, 10000, 0),
            BaedalList(arrayListOf("주넝이", "abcc", "e"),24, "드실분~~", LocalDateTime.now(), "네네치킨", 3, 10000, 0),
            BaedalList(arrayListOf("abcc"), 13, "네네치킨~~", LocalDateTime.now(), "네네치킨", 2, 10000, 0),
            BaedalList(arrayListOf(),7, "네네치킨 드실분~~", LocalDateTime.now(), "네네치킨", 4, 10000, 0)

        )
        binding.rvBaedalList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvBaedalList.setHasFixedSize(true)

        val adapter = BaedalListAdapter(baedalList)
        binding.rvBaedalList.adapter = adapter
        adapter.setItemClickListener(object: BaedalListAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                Toast.makeText(v.context, "${baedalList[position].postId}번", Toast.LENGTH_SHORT).show()
                Log.d("배달프래그먼트 온클릭", "${baedalList[position].postId}")
                setFrag(FragmentBaedalPost(), mapOf("postId" to baedalList[position].postId.toString()))
            }
        })
        adapter.notifyDataSetChanged()
    }

    fun setFrag(fragment: Fragment, arguments: Map<String, String>? = null) {
        val mActivity = activity as MainActivity
        mActivity.setFrag(fragment, arguments, fragIndex=fragIndex)
    }
}