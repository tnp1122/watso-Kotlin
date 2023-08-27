package com.watso.app.feature.baedal.ui.view.baedalHistory

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.watso.app.API.BaedalPost
import com.watso.app.ActivityController
import com.watso.app.BaseFragment
import com.watso.app.MainActivity
import com.watso.app.data.model.BaseResponse
import com.watso.app.databinding.FragBaedalHistoryBinding
import com.watso.app.feature.baedal.data.PostContent
import com.watso.app.feature.baedal.ui.view.baedalAdd.FragmentBaedalAdd
import com.watso.app.feature.baedal.ui.view.baedalOrders.FragmentBaedalOrders
import com.watso.app.feature.baedal.ui.view.baedalPost.FragmentBaedalPost
import com.watso.app.feature.baedal.ui.viewModel.BaedalHistoryViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val GET_BAEDAL_HISTORY = "배달 참가 내역 조회"

class FragmentBaedalHistory :BaseFragment() {

    lateinit var historyAdapter: HistoryAdapter
//    var historyPosts = mutableListOf<PostContent>()

    var mBinding: FragBaedalHistoryBinding? = null
    val binding get() = mBinding!!
    val TAG = "[FragBaedalHistory]"
    val baedalHistoryViewModel by viewModels<BaedalHistoryViewModel> ()

    var isTouched = false

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalHistoryBinding.inflate(inflater, container, false)

        setAdapters()
//        setListeners()
        setClickListeners()
        setObservers()

        getPostPreview()

        return binding.root
    }

    fun setAdapters() {
        historyAdapter = HistoryAdapter(mActivity)


        binding.rvBaedalListJoined.layoutManager =
            LinearLayoutManager(fragmentContext, LinearLayoutManager.VERTICAL, false)
        binding.rvBaedalListJoined.setHasFixedSize(true)
        binding.rvBaedalListJoined.adapter = historyAdapter

        historyAdapter.setShowOrderListener(object : HistoryAdapter.OnOrderBtnListener {
            override fun showOrder(postJson: String) {
                navigateTo(FragmentBaedalOrders(), mapOf("postJson" to postJson, "isMyOrder" to "true"))
            }
        })
        historyAdapter.setShowPostListener(object : HistoryAdapter.OnPostBtnListener {
            override fun showPost(postId: String) {
                navigateTo(FragmentBaedalPost(), mapOf("postId" to postId))
            }
        })
    }

//    @SuppressLint("ClickableViewAccessibility")
//    fun setListeners() {
//        binding.scrollView.setOnTouchListener { _, event -> isTouched = when (event.action)
//        {
//            MotionEvent.ACTION_UP ->
//            {
//                if (isTouched && binding.scrollView.scrollY == 0) getPostPreview()
//                false
//            }
//            else -> true
//        }
//            return@setOnTouchListener false
//        }
//    }

    fun setClickListeners() {
        binding.btnPrevious.setOnClickListener { AC.onBackPressed() }
    }

    fun setObservers() {
        baedalHistoryViewModel.getHistoryResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onGetHistorySuccess(it.data)
                is BaseResponse.Error -> onError(TAG, GET_BAEDAL_HISTORY, it.errorBody, it.msg)
                is BaseResponse.Exception -> onException(TAG, GET_BAEDAL_HISTORY, it.toString())
            }
        }
    }

    fun onGetHistorySuccess(posts: List<PostContent>?) {
        super.onSuccess()

        if (posts == null) {
            onExceptionalProblem(TAG, GET_BAEDAL_HISTORY)
            return
        }

//        historyPosts.clear()
//        historyPosts.addAll(posts)

        if (posts.isEmpty()) {
            Log.d("히스토리", "빔")
            binding.rvBaedalListJoined.visibility = View.GONE
            binding.lytEmptyList.visibility = View.VISIBLE
            binding.lytEmptyList.setOnClickListener { navigateTo(FragmentBaedalAdd()) }
        } else {
            Log.d("히스토리", "안빔")
            binding.rvBaedalListJoined.visibility = View.VISIBLE
            binding.lytEmptyList.visibility = View.GONE
            historyAdapter.setData(posts)
        }
    }

    fun getPostPreview() {
        baedalHistoryViewModel.getHistory()
    }
}
