package com.watso.app.feature.baedal.ui.view.baedalList

import android.R
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.watso.app.BaseFragment
import com.watso.app.data.model.BaseResponse
import com.watso.app.databinding.FragBaedalBinding
import com.watso.app.feature.baedal.data.PostContent
import com.watso.app.feature.baedal.ui.view.baedalAdd.FragmentBaedalAdd
import com.watso.app.feature.baedal.ui.view.baedalPost.FragmentBaedalPost
import com.watso.app.feature.baedal.ui.viewModel.BaedalListViewModel
import com.watso.app.feature.user.ui.view.FragmentAccount
import com.watso.app.fragmentBaedal.BaedalHistory.FragmentBaedalHistory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val GET_JOINED_POST = "참가한 포스트 조회"
private const val GET_JOINABLE_POST = "참가 가능한 포스트 조회"

class FragmentBaedalList :BaseFragment() {


    lateinit var joinedAdapter: TableAdapter
    lateinit var joinableAdapter: TableAdapter
    var joinablePosts = listOf<PostContent>()

    var mBinding: FragBaedalBinding? = null
    val binding get() = mBinding!!
    val TAG = "[FragBaedal]"
    val baedalListViewModel by viewModels<BaedalListViewModel> ()

    var viewClickAble = true    // 포스트 중복 클릭 방지
    var joined = true       // 참가한 게시글 여부
    var joinable = true     // 참가 가능한 게시글 여부

    override fun onAttach(context: Context) {
        super.onAttach(context)
        checkIntent()
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalBinding.inflate(inflater, container, false)

        setFragmentResultListener()
        setUpUI()
        setAdapters()
        setClickListeners()
        setObservers()
        setSpiner()

        getPostList()

        return binding.root
    }

    fun checkIntent() {
        val postId = mActivity.intent.getStringExtra("post_id")
        Log.d("[$TAG][checkIntent]", "postId: $postId")
        postId?.run { navigateTo(FragmentBaedalPost(), mapOf("postId" to postId)) }
    }

    fun setFragmentResultListener() {
        activity?.supportFragmentManager?.setFragmentResultListener("deletePost", this) {
                requestKey, bundle -> getPostList()
        }
        activity?.supportFragmentManager?.setFragmentResultListener("backToBaedalList", this) {
                requestKey, bundle -> getPostList()
        }
    }

    fun setUpUI() {
        binding.lytRefresh.setOnRefreshListener {
            binding.lytRefresh.isRefreshing = false
            getPostList()
        }
    }

    fun setAdapters() {
        joinedAdapter = TableAdapter(mActivity)
        joinableAdapter = TableAdapter(mActivity)

        binding.rvBaedalListJoined.layoutManager =
            LinearLayoutManager(fragmentContext, LinearLayoutManager.VERTICAL, false)
        binding.rvBaedalListJoined.setHasFixedSize(true)
        binding.rvBaedalListJoined.adapter = joinedAdapter

        binding.rvBaedalListJoinable.layoutManager =
            LinearLayoutManager(fragmentContext, LinearLayoutManager.VERTICAL, false)
        binding.rvBaedalListJoinable.setHasFixedSize(true)
        binding.rvBaedalListJoinable.adapter = joinableAdapter
    }

    fun setSpiner() {
        val places = listOf("모두", "생자대", "기숙사")
        val placeSpinerAdapter = ArrayAdapter(fragmentContext, R.layout.simple_list_item_1, places)
        binding.spnFilter.adapter = placeSpinerAdapter
        binding.spnFilter.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                when (position) {
                    0 -> mappingPostDate(joinablePosts)
                    1 -> mappingPostDate(getFilteredPosts(places[1]))
                    2 -> mappingPostDate(getFilteredPosts(places[2]))
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) { }
        }
    }

    fun setClickListeners() {
        binding.btnOption.setOnClickListener { navigateTo(FragmentAccount()) }
        binding.btnBaedalHistory.setOnClickListener { navigateTo(FragmentBaedalHistory()) }
        binding.btnBaedalPostAdd.setOnClickListener { navigateTo(FragmentBaedalAdd()) }
        binding.lytEmptyList.setOnClickListener { navigateTo(FragmentBaedalAdd()) }

        joinedAdapter.setPostClickListener(object: TableAdapter.OnPostClickListener {
            override fun onClick(postId: String) {
                if (viewClickAble) {
                    viewClickAble = false
                    navigateTo(FragmentBaedalPost(), mapOf("postId" to postId))
                    Handler(Looper.getMainLooper()).postDelayed({ viewClickAble = true}, 500)
                }
            }
        })
        joinableAdapter.setPostClickListener(object: TableAdapter.OnPostClickListener {
            override fun onClick(postId: String) {
                if (viewClickAble) {
                    viewClickAble = false
                    navigateTo(FragmentBaedalPost(), mapOf("postId" to postId))
                    Handler(Looper.getMainLooper()).postDelayed({ viewClickAble = true}, 500)
                }
            }
        })
    }

    fun setObservers() {
        baedalListViewModel.getJoinedPostListResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onGetPostListSuccess(it.data)
                is BaseResponse.Error -> onError(TAG, GET_JOINED_POST, it.errorBody, it.msg)
                else -> onException(TAG, GET_JOINED_POST, it.toString())
            }
        }

        baedalListViewModel.getJoinablePostListResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onGetPostListSuccess(it.data, true)
                is BaseResponse.Error -> onError(TAG, GET_JOINABLE_POST, it.errorBody, it.msg)
                else -> onException(TAG, GET_JOINABLE_POST, it.toString())
            }
        }
    }

    fun getPostList() {
        baedalListViewModel.getJoinedPostList()
        baedalListViewModel.getJoinablePostList()
    }

    fun onGetPostListSuccess(postList: List<PostContent>?, isJoinableTable: Boolean = false) {
        super.onSuccess()

        if (postList == null) {
            if (isJoinableTable) onExceptionalProblem(TAG, GET_JOINABLE_POST)
            else onExceptionalProblem(TAG, GET_JOINED_POST)
            return
        }

        val sortedPostList = postList.sortedBy { it.orderTime }
        if (isJoinableTable) {
            joinablePosts = sortedPostList
        }
        mappingPostDate(sortedPostList, isJoinableTable)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun mappingPostDate(posts: List<PostContent>, isJoinableTable: Boolean = false) {
        val tables = mutableListOf<Table>()
        val dates = mutableListOf<LocalDate>()
        var tableIdx = -1
        for (post in posts) {
            val date = LocalDate.parse(post.orderTime.substring(0 until 16), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
            if (date !in dates) {
                dates.add(date)
                tables.add(Table(date, mutableListOf(post)))
                tableIdx += 1
            } else tables[tableIdx].rows.add(post)
        }

        if (tables.isNotEmpty()) {
            if (isJoinableTable) {
                joinableAdapter.setData(tables)
                binding.divJoinable.visibility = View.VISIBLE
                binding.rvBaedalListJoinable.visibility = View.VISIBLE
                binding.lytFilter.visibility = View.VISIBLE
                joinable = true
            } else {
                joinedAdapter.setData(tables)
                binding.lytJoinedTable.visibility = View.VISIBLE
                joined = true
            }
        } else {
            if (isJoinableTable) {
                binding.divJoinable.visibility = View.GONE
                binding.rvBaedalListJoinable.visibility = View.GONE
                binding.lytFilter.visibility = View.GONE
                joinable = false
            } else {
                binding.lytJoinedTable.visibility = View.GONE
                joined = false
            }
        }

        if (!joined && !joinable) {
            binding.lytEmptyList.visibility = View.VISIBLE
        } else binding.lytEmptyList.visibility = View.GONE
    }

    fun getFilteredPosts(filterBy: String): List<PostContent> {
        val filteredPosts = mutableListOf<PostContent>()
        joinablePosts.forEach {
            if (it.place == filterBy) filteredPosts.add(it)
        }
        return filteredPosts
    }
}
