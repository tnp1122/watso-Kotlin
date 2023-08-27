package com.watso.app.feature.baedal.ui.view.baedalPost

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.watso.app.BaseFragment
import com.watso.app.MainActivity
import com.watso.app.R
import com.watso.app.data.model.BaseResponse
import com.watso.app.databinding.AlertdialogInputtextBinding
import com.watso.app.databinding.FragBaedalPostBinding
import com.watso.app.feature.baedal.data.*
import com.watso.app.feature.baedal.ui.view.baedalAdd.FragmentBaedalAdd
import com.watso.app.feature.baedal.ui.viewModel.BaedalPostViewModel
import com.watso.app.feature.user.data.AccountNumber
import com.watso.app.feature.baedal.ui.view.baedalMenu.FragmentBaedalMenu
import com.watso.app.feature.baedal.ui.view.baedalOrders.FragmentBaedalOrders
import com.watso.app.util.SessionManager
import okhttp3.ResponseBody
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

private const val GET_POST_CONTENT = "게시물 조회"
private const val GET_ACCOUNT_NUMBER = "대표자 계좌 조회"
private const val UPDATE_POST_STATUS = "모임 상태 변경"
private const val UPDATE_FEE = "배달비 변경"
private const val DELETE_POST = "게시물 삭제"
private const val DELETE_ORDERS = "주문 삭제"

private const val MAKE_COMMENT = "댓글 작성"
private const val MAKE_SUB_COMMENT = "대댓글 작성"
private const val GET_COMMENTS = "댓글 조회"

class FragmentBaedalPost :BaseFragment() {

    var mBinding: FragBaedalPostBinding? = null
    val binding get() = mBinding!!
    val TAG = "[FragBaedalPost]"
    val baedalPostViewModel by viewModels<BaedalPostViewModel> ()

    val gson = Gson()
    val dec = DecimalFormat("#,###")

    var isScrolled = false
    var oldPosition = 0.0.toFloat()
//    var infoHeight = 0
//    var needMargin = 0
//    var addCommentOriginY = 0f
//    var isKeyboardOpen = false
//    var originViewHeight = 0
//    var keyBoardHeight = 0
//    var viewSetted = false

    var postId: String? = null
    var userId = (-1).toLong()


    lateinit var postContent: PostContent
    var isMyPost = false
    var isMember = false
    var postStatus = ""
    var comments = mutableListOf<Comment>()
    var replyTo: Comment? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        userId = SessionManager.getUserId(mActivity)
        checkIntent()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getString("postId")!!
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragBaedalPostBinding.inflate(inflater, container, false)

        setUpUI()
        setListeners()
        setClickListeners()
        setObservers()
        getPostInfo()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        AC.hideSoftInput()
    }

    fun checkIntent() {
        val intentPostId = mActivity.intent.getStringExtra("post_id")
        Log.d("[$TAG][checkIntent]", "intentPostId: $intentPostId")
        intentPostId?.run { mActivity.intent.removeExtra("post_id") }
    }

    fun setUpUI() {
        Log.d("access", AC.getString("accessToken", ""))
        Log.d("postId", postId.toString())
        binding.btnOrder.visibility = View.GONE
        binding.btnComplete.visibility = View.GONE
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setListeners() {
//        activity?.supportFragmentManager?.setFragmentResultListener("backToList", this) {
//                _, _ -> getPostInfo()
//        }

        binding.lytHead.setOnTouchListener { _, motionEvent -> onTouchEvent(motionEvent) }
        binding.lytContent.setOnTouchListener { _, motionEvent -> onTouchEvent(motionEvent) }
        binding.rvComment.setOnTouchListener { _, motionEvent -> onTouchEvent(motionEvent) }

        binding.lytRefresh.setOnRefreshListener {
            binding.lytRefresh.isRefreshing = false
            getPostInfo()
        }
    }

    fun setClickListeners() {
        binding.btnPrevious.setOnClickListener { onBackPressed() }
        binding.btnUpdateFee.setOnClickListener { onBtnUpdateFee() }
        binding.btnCopyAccountNum.setOnClickListener {
            AC.copyToClipboard("대표자 계좌번호", binding.tvAccountNumber.text.toString())
        }

        binding.tvDelete.setOnClickListener {
            if (postStatus == "recruiting") {
                val builder = AlertDialog.Builder(fragmentContext)
                builder.setTitle("게시글 삭제하기")
                    .setMessage("게시글을 삭제하시겠습니까?")
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                        deletePost()
                    })
                    .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> })
                builder.show()
            } else AC.showAlert("모임이 모집중인 상태에서만 삭제 가능합니다.")
        }

        binding.tvUpdate.setOnClickListener {
            if (postStatus == "recruiting") {
                val builder = AlertDialog.Builder(fragmentContext)
                builder.setTitle("게시글 수정하기")
                    .setMessage("게시글을 수정하시겠습니까?")
                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                        navigateTo(FragmentBaedalAdd(), mapOf(
                            "isUpdating" to "true",
                            "postId" to postId!!,
                            "orderTime" to postContent.orderTime,
                            "storeInfo" to gson.toJson(postContent.store),
                            "place" to postContent.place,
                            "minMember" to postContent.minMember.toString(),
                            "maxMember" to postContent.maxMember.toString(),
                            "fee" to postContent.store.fee.toString()
                        ))
                    })
                    .setNegativeButton("취소",
                        DialogInterface.OnClickListener { _, _ ->
                            println("취소")
                        }
                    )
                builder.show()
            } else showAlert("모임이 모집중인 상태에서만 수정 가능합니다.")
        }

        binding.btnOrder.setOnClickListener { onBtnOrder() }
        binding.lytStatusOpen.setOnClickListener { if (postStatus == "closed") setStatus("recruiting")}
        binding.lytStatusClosed.setOnClickListener { if (postStatus == "recruiting") setStatus("closed") }
        binding.btnComplete.setOnClickListener { onBtnComplete() }

        binding.btnViewMyOrders.setOnClickListener {
            navigateTo(FragmentBaedalOrders(), mapOf(
                "postJson" to gson.toJson(postContent),
                "isMyOrder" to "true"
            ))
        }
        binding.btnViewAllOrders.setOnClickListener {
            navigateTo(FragmentBaedalOrders(), mapOf(
                "postJson" to gson.toJson(postContent),
                "isMyOrder" to "false"
            ))
        }

        binding.btnCancelReply.setOnClickListener { cancelReply() }
        binding.btnPostComment.setOnClickListener {
            val content = binding.etComment.text.toString()
            if (content.trim() != "") makeComment(content, replyTo?._id)
        }
    }

    fun setObservers() {
        baedalPostViewModel.getPostContentResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onGetPostContentSuccess(it.data)
                is BaseResponse.Error -> onError(TAG, GET_POST_CONTENT, it.errorBody, it.msg)
                else -> onException(TAG, GET_POST_CONTENT, it.toString())
            }
        }

        baedalPostViewModel.getAccountNumberResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onGetAccountNumberSuccess(it.data)
                is BaseResponse.Error -> onGetAccountNumberError(it.errorBody, it.msg)
                else -> onException(TAG, GET_ACCOUNT_NUMBER, it.toString())
            }
        }

        baedalPostViewModel.updatePostStausResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onUpdatePostStatusSuccess()
                is BaseResponse.Error -> onError(TAG, UPDATE_POST_STATUS, it.errorBody, it.msg)
                else -> onException(TAG, UPDATE_POST_STATUS, it.toString())
            }
        }

        baedalPostViewModel.updateFeeResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onUpdateFeeSuccess()
                is BaseResponse.Error -> onError(TAG, UPDATE_FEE, it.errorBody, it.msg)
                else -> onException(TAG, UPDATE_FEE, it.toString())
            }
        }

        baedalPostViewModel.deletePostResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onDeletePostSuccess()
                is BaseResponse.Error -> onError(TAG, DELETE_POST, it.errorBody, it.msg)
                else -> onException(TAG, DELETE_POST, it.toString())
            }
        }

        baedalPostViewModel.deleteOrdersResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onDeleteOrdersSuccess()
                is BaseResponse.Error -> onError(TAG, DELETE_ORDERS, it.errorBody, it.msg)
                else -> onException(TAG, DELETE_ORDERS, it.toString())
            }
        }

        baedalPostViewModel.makeCommentResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onMakeCommentSuccess()
                is BaseResponse.Error -> onError(TAG, MAKE_COMMENT, it.errorBody, it.msg)
                else -> onException(TAG, MAKE_COMMENT, it.toString())
            }
        }

        baedalPostViewModel.makeSubCommentResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onMakeSubCommentSuccess()
                is BaseResponse.Error -> onError(TAG, MAKE_SUB_COMMENT, it.errorBody, it.msg)
                else -> onException(TAG, MAKE_SUB_COMMENT, it.toString())
            }
        }

        baedalPostViewModel.getCommentsResponse.observe(mActivity) {
            when (it) {
                is BaseResponse.Loading -> onLoading()
                is BaseResponse.Success -> onGetCommentsSuccess(it.data)
                is BaseResponse.Error -> onError(TAG, GET_COMMENTS, it.errorBody, it.msg)
                else -> onException(TAG, GET_COMMENTS, it.toString())
            }
        }
    }

    fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d("[$TAG][온터치]", "터치시작 x: ${event.x}, y: ${event.y}")
                oldPosition = event.x + event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isScrolled) {
                    Log.d("[$TAG][온터치][ACTION_MOVE]", "x: ${event.x}, y: ${event.y}")
                    val newPosition = event.x + event.y
                    if (Math.abs(oldPosition - newPosition) > 5) {
                        Log.d("[$TAG][온터치][찐 움직임]", "x: ${event.x}, y: ${event.y}")
                        isScrolled = true
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                Log.d("[$TAG][온터치]", "터치끝 x: ${event.x}, y: ${event.y}")
                if (!isScrolled) AC.hideSoftInput()
                isScrolled = false
            }
        }
        return false
    }

    fun onGetPostContentSuccess(getPostContentRes: PostContent?) {
        super.onSuccess()

        if (getPostContentRes == null) {
            onExceptionalProblem(TAG, GET_POST_CONTENT)
            return
        }

        postContent = getPostContentRes
        isMyPost = postContent.userId == userId
        isMember = postContent.users.contains(userId)
        postStatus = postContent.status
        setPostUI()
    }

    fun onGetAccountNumberSuccess(accountNumber: AccountNumber?) {
        super.onSuccess()

        if (accountNumber == null) {
            onExceptionalProblem(TAG, GET_ACCOUNT_NUMBER)
            return
        }

        binding.tvAccountNumber.text = accountNumber.accountNumber
        binding.btnCopyAccountNum.setOnClickListener {
            AC.copyToClipboard("대표자 계좌번호", accountNumber.accountNumber)
        }
    }

    fun onGetAccountNumberError(errorBody: ResponseBody?, msg: String?) {
        super.onError(TAG, GET_ACCOUNT_NUMBER, errorBody, msg)

        binding.btnCopyAccountNum.visibility = View.GONE
        val gson = Gson()
        val errorBodyObject = gson.fromJson(errorBody?.string(), com.watso.app.ErrorResponse::class.java)

        if (errorBodyObject.code == 409) {
            binding.tvAccountNumber.text =
                "계좌 번호 조회 가능 시간이 끝났어요! 아직 배달비를 입금하지 않았다면 대표자와 댓글로 연락해보세요."
        } else { binding.lytAccountNumber.visibility = View.GONE }
    }

    fun onUpdatePostStatusSuccess() {
        super.onSuccess()

        showToast("상태가 변경되었습니다.")
        getPostInfo()
    }

    fun onUpdateFeeSuccess() {
        super.onSuccess()

        showToast("배달비가 변경되었습니다.")
        getPostInfo()
    }

    fun onDeletePostSuccess() {
        super.onSuccess()

        val bundle = bundleOf()
        activity?.supportFragmentManager?.setFragmentResult("deletePost", bundle)
        onBackPressed()
    }

    fun onDeleteOrdersSuccess() {
        super.onSuccess()

        showToast("주문이 취소되었습니다.")
        val bundle = bundleOf()
        activity?.supportFragmentManager?.setFragmentResult("backToBaedalList", bundle)
        getPostInfo()
    }

    fun onMakeCommentSuccess() {
        super.onSuccess()

        RP.requestNotificationPermission()
        getComments()
    }

    fun onMakeSubCommentSuccess() {
        super.onSuccess()

        RP.requestNotificationPermission()
        getComments()
    }

    fun onGetCommentsSuccess(getCommentsRes: GetCommentsResponse?) {
        super.onSuccess()

        if (getCommentsRes == null) {
            onExceptionalProblem(TAG, GET_COMMENTS)
            return
        }

        comments = getCommentsRes.comments
        setComments()
    }

    fun onBtnOrder() {
        Log.d("[$TAG]btnOrder", "isMember: $isMember, ${postContent.users.size}, ${postContent.maxMember}")
        if (isMember) {
            val builder = AlertDialog.Builder(fragmentContext)
            builder.setTitle("주문 취소하기")
                .setMessage("주문을 취소하시겠습니까?\n다시 참가하기 위해선 주문을 다시 작성해야합니다.")
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    deleteOrders()
                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> })
            builder.show()
        }
        else {
            if (postContent.users.size < postContent.maxMember) {
                AC.setString("minMember", postContent.minMember.toString())
                navigateTo(FragmentBaedalMenu(), mapOf("postId" to postId!!, "storeId" to postContent.store._id))
            } else {
                val builder = AlertDialog.Builder(fragmentContext)
                builder.setTitle("인원 마감")
                    .setMessage("참여 가능한 최대 인원에 도달했습니다.\n대표자에게 문의하세요")
                    .setPositiveButton("확인", DialogInterface.OnClickListener{_, _ ->})
                    .show()
            }
        }
    }

    fun onBtnComplete() {
        val builder = AlertDialog.Builder(fragmentContext)
        if (postStatus == "closed") {
            builder.setTitle("주문 완료")
                .setMessage("주문을 완료하셨나요?\n가게에 주문을 접수한 뒤에 확인버튼을 눌러주세요!")
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    setStatus("ordered")
                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> })
            builder.show()
        } // 주문 완료
        else {
            val builderItem = EtBuilder()
            builderItem.init()

            builder.setTitle("배달 완료")
                .setMessage("배달이 완료되었나요?\n주문 참가자들에게 알림이 전송됩니다.\n확정된 배달비를 입력해주세요.")
                .setView(builderItem.getView().root)
                .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                    updateFee(builderItem.getFee())
                    setStatus("delivered")
                })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> })
            builder.show()} // 배달 완료
    }

    fun onBtnUpdateFee() {
        val builder = AlertDialog.Builder(fragmentContext)
        val builderItem = EtBuilder()
        builderItem.init()

        builder.setTitle("배달비 변경")
            .setMessage("변경된 배달비를 입력해주세요")
            .setView(builderItem.getView().root)
            .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, id ->
                updateFee(builderItem.getFee())
            })
            .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, id -> })
        builder.show()
    }

    fun getPostInfo() {
        getPostContent()
        getComments()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getPostContent() {
        baedalPostViewModel.getPostContent(postId!!)
    }

    fun getAccountNum() {
        if (userId == postContent.userId && postStatus == "delivered") {
            binding.lytAccountNumber.visibility = View.VISIBLE
            baedalPostViewModel.getAccountNumber(postId!!)
        } else binding.lytAccountNumber.visibility = View.GONE
    }

    fun deletePost() {
        baedalPostViewModel.deletePost(postId!!)
    }

    fun setStatus(status: String) {
        baedalPostViewModel.updatePostStatus(postId!!, PostStatus(status))
    }

    fun updateFee(fee: Int) {
        if (fee != postContent.fee) {
            baedalPostViewModel.updateFee(postId!!, Fee(fee))
        }
    }

    fun deleteOrders() {
        baedalPostViewModel.deleteOrders(postId!!)
    }

    fun makeComment(content: String, parentId: String? = null) {
        if (parentId == null) {
            baedalPostViewModel.makeComment(postId!!, MakeCommentForm(content))
        } else {
            baedalPostViewModel.makeSubComment(postId!!, parentId, MakeCommentForm(content))
        }
    }

    fun getComments() {
        cancelReply()
        hideSoftInput()
        binding.etComment.setText("")
        baedalPostViewModel.getComments(postId!!)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setPostUI() {
        val joinUsers = postContent.users
        isMember = joinUsers.contains(userId)
        val store = postContent.store
        val orderTime = LocalDateTime.parse(postContent.orderTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))

        binding.tvStoreName.text = store.name
        if (userId == postContent.userId) {
            binding.tvDelete.text = "삭제"
            binding.tvUpdate.text = "수정"
            binding.tvDelete.visibility = View.VISIBLE
            binding.tvUpdate.visibility = View.VISIBLE
        } else {
            binding.tvDelete.visibility = View.GONE
            binding.tvUpdate.visibility = View.GONE
        }

        if (postStatus != "recruiting" && postStatus != "closed") {
            binding.tvDelete.visibility = View.GONE
            binding.tvUpdate.visibility = View.GONE
        }


        /** 가게 정보 */
        binding.tvTelNum.text = store.telNum
        binding.tvMinOrder.text = "${dec.format(store.minOrder)}원"
        binding.tvFee.text = "${dec.format(postContent.store.fee)}원"

        var noteStr = ""
        for ((idx, note) in store.note.withIndex()) {
            noteStr += note
            if (idx < store.note.size - 1)
                noteStr += "\n"
        }
        if (noteStr.trim() == "") noteStr = "없음"
        binding.tvNote.text = noteStr

        /** 포스트 내용 바인딩 */

        binding.tvOrderTime.text = orderTime.format(
            DateTimeFormatter.ofPattern("M월 d일(E) H시 m분",Locale.KOREAN)
        )
        binding.tvCurrentMember.text = "${postContent.users.size}명 (최소 ${postContent.minMember}명 필요)"
        binding.tvConfirmedFee.text = "${dec.format(postContent.fee)}원"
        if (postContent.userId == userId) {
            binding.lytConfirmedFee.visibility = View.VISIBLE
            binding.btnUpdateFee.visibility = View.VISIBLE
        } else {
            binding.btnUpdateFee.visibility = View.GONE
            if (postStatus == "delivered") binding.lytConfirmedFee.visibility = View.VISIBLE
            else binding.lytConfirmedFee.visibility = View.GONE
        }

        getAccountNum()

        /** 하단 버튼 바인딩 */
        setStatusBtn()
        setBottomBtn()
    }

    fun setStatusBtn() {
        binding.ivStatus.setImageResource(R.drawable.baseline_person_off_black_24)
        if (isMyPost) {
            binding.btnOrder.visibility = View.GONE
            if (postStatus == "recruiting" || postStatus == "closed") {
                binding.tvStatus.visibility = View.GONE
            } else {
                binding.tvStatus.visibility = View.VISIBLE
                binding.lytStatus.visibility = View.GONE
            }
        }
        else {
            binding.lytStatus.visibility = View.GONE
            binding.btnComplete.visibility = View.GONE
        }

        when (postStatus) {
            "recruiting" -> {
                binding.ivStatus.setImageResource(R.drawable.baseline_person_black_24)
                binding.lytStatusOpen.setBackgroundResource(R.drawable.patch_green_10_green_left)
                binding.tvStatusOpen.setTextColor(Color.WHITE)
                binding.lytStatusClosed.setBackgroundResource(R.drawable.patch_white_10_silver_right)
                binding.tvStatusClosed.setTextColor(Color.GRAY)
                binding.tvStatus.text = "모집중"
            }
            "closed" -> {
                binding.lytStatusOpen.setBackgroundResource(R.drawable.patch_white_10_silver_left)
                binding.tvStatusOpen.setTextColor(Color.GRAY)
                binding.lytStatusClosed.setBackgroundResource(R.drawable.patch_silver_10_gray_right)
                binding.tvStatusClosed.setTextColor(Color.WHITE)
                binding.tvStatus.text = "모집 마감"
            }
            "ordered" -> binding.tvStatus.text = "모집 마감 (주문 완료)"
            "delivered" -> binding.tvStatus.text = "모집 마감 (배달 완료)"
            "canceld" -> binding.tvStatus.text = "취소"
            else -> binding.tvStatus.text = "모집 마감"
        }

        //setLayoutListner(binding.constraintLayout17, "내용")
    }

    fun setBottomBtn() {
        if (isMyPost) {
            when (postStatus) {
                "recruiting" -> binding.btnComplete.visibility = View.GONE
                "closed" -> {
                    binding.tvComplete.text = "주문 완료"
                    binding.btnComplete.visibility = View.VISIBLE
                }
                "ordered" -> {
                    binding.tvComplete.text = "배달 완료"
                    binding.btnComplete.visibility = View.VISIBLE
                }
                else -> binding.btnComplete.visibility = View.GONE
            }
        } else {
            if (isMember) {
                binding.btnViewMyOrders.visibility = View.VISIBLE
            } else {
                binding.btnViewMyOrders.visibility = View.GONE
            }

            when (postStatus) {
                "recruiting" -> {
                    if (isMember) setBtnOrder(false, true, "주문 취소")
                    else setBtnOrder(true, true, "주문하기")
                }
                "closed" -> {
                    if (isMember) setBtnOrder(false, true, "주문 취소")
                    else setBtnOrder(false, false, "마감되었습니다.")
                }
                else -> setBtnOrder(false, false, "마감되었습니다.")
            }
            binding.btnOrder.visibility = View.VISIBLE
        }
    }

    fun setBtnOrder(background: Boolean, isEnabled: Boolean, text: String) {
        if (background) binding.btnOrder.setBackgroundResource(R.drawable.solid_primary)
        else binding.btnOrder.setBackgroundResource(R.drawable.solid_gray)
        binding.btnOrder.isEnabled = isEnabled
        binding.tvOrder.text = text
    }

    inner class EtBuilder {
        private val view = AlertdialogInputtextBinding.inflate(layoutInflater)

        fun init() {
            view.etFee.setText(postContent.fee.toString())
        }

        fun getView(): AlertdialogInputtextBinding { return view }

        fun getFee(): Int {
            return view.etFee.text.toString().replace(",","").toInt()
        }
    }

    fun setComments() {
        var count = 0
        for (comment in comments) {
            if (comment.status == "created") count++
        }
        binding.tvCommentCount.text = "댓글 $count"
        binding.rvComment.layoutManager =
            LinearLayoutManager(fragmentContext, LinearLayoutManager.VERTICAL, false)
        binding.rvComment.setHasFixedSize(true)
        val adapter = CommentAdapter(mActivity, comments, userId, baedalPostViewModel, AC)
        binding.rvComment.adapter = adapter

        adapter.setDeleteListener(object: CommentAdapter.OnDeleteListener {
            override fun deleteComment() {
                getComments()
            }
        })
        adapter.setReplyListener(object : CommentAdapter.OnReplyListener {
            override fun makeReply(parentComment: Comment) {
                replyTo = parentComment
                binding.tvReplyTo.text = "${replyTo!!.nickname}님에게 대댓글"
                binding.lytReplyTo.visibility = View.VISIBLE
                AC.showSoftInput(binding.etComment)
            }
        })

        binding.lytReplyTo.visibility = View.GONE
    }

    fun cancelReply() {
        replyTo = null
        binding.lytReplyTo.visibility = View.GONE
    }
}
