package com.qingshuige.tangyuan

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
// import androidx.activity.EdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.qingshuige.tangyuan.data.CircleTransform
import com.qingshuige.tangyuan.data.DataTools
import com.qingshuige.tangyuan.network.ApiHelper
import com.qingshuige.tangyuan.network.Comment
import com.qingshuige.tangyuan.network.CreateCommentDto
import com.qingshuige.tangyuan.ui.PhotoDialogFragment
import com.qingshuige.tangyuan.viewmodels.CommentCardAdapter
import com.qingshuige.tangyuan.viewmodels.CommentInfo
import com.qingshuige.tangyuan.viewmodels.GalleryAdapter
import com.qingshuige.tangyuan.viewmodels.PostInfo
import com.squareup.picasso.Picasso
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class PostActivity : AppCompatActivity() {

    private lateinit var commentAdapter: CommentCardAdapter
    private lateinit var tm: TokenManager
    private lateinit var postInfo: PostInfo
    private var postId: Int = 0
    private lateinit var gallery: RecyclerView
    private lateinit var commentsRcv: RecyclerView
    private lateinit var pgBar: ProgressBar
    private lateinit var pgBarCommentSend: ProgressBar
    private lateinit var editComment: EditText
    private lateinit var buttonSendComment: Button
    private lateinit var textCommentCounter: TextView
    private var replyDialog: BottomSheetDialog? = null
    private lateinit var menu: Menu
    private var flagScrollToCommmentId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        EdgeToEdge.enable(this)
        setContentView(R.layout.activity_post)
        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        */

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        postId = intent.getIntExtra("postId", 1)

        gallery = findViewById(R.id.imageGallery)
        commentsRcv = findViewById(R.id.commentRecyclerView)
        pgBar = findViewById(R.id.progressBar)
        editComment = findViewById(R.id.editComment)
        buttonSendComment = findViewById(R.id.buttonSendComment)
        textCommentCounter = findViewById(R.id.textCommentCounter)
        pgBarCommentSend = findViewById(R.id.progressBarCommentSend)

        tm = TangyuanApplication.getTokenManager()

        gallery.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            Resources.getSystem().displayMetrics.widthPixels * 3 / 4
        )
        // 以 4:3 比例设置 gallery
        gallery.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(gallery)

        initializeUI(postId)

        if (intent.getIntExtra("commentId", 0) != 0) {
            targetToComment(intent.getIntExtra("commentId", 0))
        }
    }

    private fun targetToComment(commentId: Int) {
        TangyuanApplication.getApi().getComment(commentId).enqueue(object : Callback<Comment> {
            override fun onResponse(call: Call<Comment>, response: Response<Comment>) {
                if (response.code() == 200) {
                    val comment = response.body()!!
                    if (comment.parentCommentId != 0) { // 是子评论
                        ApiHelper.getCommentInfoByIdAsync(comment.parentCommentId) { result ->
                            result?.let { info ->
                                runOnUiThread { showReplyBottomSheet(info) }
                            }
                        }
                    } else { // 不是子评论
                        flagScrollToCommmentId = commentId
                    }
                }
            }

            override fun onFailure(call: Call<Comment>, throwable: Throwable) {}
        })
    }

    private fun initializeUI(postId: Int) {
        // 数据无关设置
        commentAdapter = CommentCardAdapter()
        val layoutManager = LinearLayoutManager(this)
        commentAdapter.setOnReplyButtonClickListener(::showReplyBottomSheet)
        commentAdapter.setOnTextClickListener(::showReplyBottomSheet)
        commentAdapter.setOnAvatarClickListener(::startUserActivity)
        commentAdapter.setOnItemHoldListener { info -> showCommentControlDialog(info, null) }
        commentsRcv.layoutManager = layoutManager
        commentsRcv.adapter = commentAdapter
        commentsRcv.addItemDecoration(DividerItemDecoration(this, layoutManager.orientation))

        ApiHelper.getPostInfoByIdAsync(postId) { result ->
            if (result == null) {
                runOnUiThread {
                    pgBar.visibility = View.GONE
                    val dialog = MaterialAlertDialogBuilder(this@PostActivity)
                        .setTitle(R.string.load_post_error)
                        .setMessage(R.string.post_may_be_deleted_or_network_error)
                        .setPositiveButton(R.string.ok) { _, _ -> finish() }
                        .create()
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.show()
                }
            } else {
                postInfo = result
                runOnUiThread {
                    updateContent()
                    updateComment()

                    buttonSendComment.setOnClickListener {
                        trySendComment(editComment, buttonSendComment, pgBarCommentSend, null)
                    }
                }
            }
        }
    }

    private fun showCommentControlDialog(
        commentInfo: CommentInfo,
        parentCommentInfo: CommentInfo?
    ) {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle(R.string.comment)

        val token = tm.token
        val items =
            if (token != null && DataTools.decodeJwtTokenUserId(token) == commentInfo.userId) {
                // 是当前用户
                arrayOf(getString(R.string.copy), getString(R.string.delete))
            } else {
                // 非当前用户或未登录
                arrayOf(getString(R.string.copy))
            }

        builder.setItems(items) { _, i ->
            when (i) {
                0 -> {
                    (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                        .setPrimaryClip(ClipData.newPlainText("tyapp", commentInfo.commentText))
                }

                1 -> {
                    TangyuanApplication.getApi().deleteComment(commentInfo.commentId)
                        .enqueue(object : Callback<ResponseBody> {
                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {
                                if (response.code() == 200) {
                                    runOnUiThread {
                                        Toast.makeText(
                                            this@PostActivity,
                                            R.string.comment_deleted,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        updateComment()
                                        parentCommentInfo?.let { showReplyBottomSheet(it) }
                                    }
                                }
                            }

                            override fun onFailure(
                                call: Call<ResponseBody>,
                                throwable: Throwable
                            ) {
                            }
                        })
                }
            }
        }
        builder.create().show()
    }

    private fun startUserActivity(commentInfo: CommentInfo) {
        val intent = Intent(this, UserActivity::class.java)
        intent.putExtra("userId", commentInfo.userId)
        startActivity(intent)
    }

    private fun updateContent() {
        // UI
        pgBar.visibility = View.GONE
        Picasso.get()
            .load(ApiHelper.getFullImageURL(postInfo.userAvatarGUID))
            .transform(CircleTransform())
            .into(findViewById<ImageView>(R.id.avatarView))
        findViewById<TextView>(R.id.nicknameView).text = postInfo.userNickname

        // Section
        val sectionName = when (postInfo.sectionId) {
            0 -> getString(R.string.notice)
            1 -> getString(R.string.menu_normalchat)
            2 -> getString(R.string.menu_chitchat)
            else -> ""
        }
        findViewById<TextView>(R.id.textSectionName).text = sectionName

        // Category
        findViewById<TextView>(R.id.textCategoryName).text = postInfo.categoryName
        findViewById<View>(R.id.imgCategoryIcon).setOnClickListener { startCategoryActivity() }
        findViewById<TextView>(R.id.textCategoryName).setOnClickListener { startCategoryActivity() }

        // 正文区
        findViewById<TextView>(R.id.contentView).text = postInfo.textContent
        findViewById<TextView>(R.id.dateTimeView).text =
            DataTools.getLocalFriendlyDateTime(postInfo.postDate, this)
        findViewById<TextView>(R.id.tidView).text = "TID:${postInfo.postId}"

        /// Gallery
        postInfo.image1GUID?.let {
            // gallery 可见化
            gallery.visibility = View.VISIBLE
            // 沉浸状态栏
            findViewById<View>(R.id.main).requestLayout()

            val images = mutableListOf<String>().apply {
                add(postInfo.image1GUID!!)
                postInfo.image2GUID?.let { add(it) }
                postInfo.image3GUID?.let { add(it) }
            }

            val adapter = GalleryAdapter(images as ArrayList<String>)
            adapter.setOnItemClickListener(::showImageWindow)
            gallery.adapter = adapter
        }

        /// Avatar
        findViewById<ImageView>(R.id.avatarView).setOnClickListener { startUserActivity() }
        findViewById<TextView>(R.id.nicknameView).setOnClickListener { startUserActivity() }

        // 菜单栏
        val token = tm.token
        if (token != null && DataTools.decodeJwtTokenUserId(token) == postInfo.userId) {
            menu.findItem(R.id.menuDelete).isVisible = true
        }
    }

    private fun showImageWindow(drawable: Drawable) {
        val bitmap = (drawable as BitmapDrawable).bitmap
        val photoDialogFragment = PhotoDialogFragment.create(bitmap)
        photoDialogFragment.show(supportFragmentManager, null)
    }

    private fun trySendComment(
        input: EditText,
        sendButton: Button,
        pgBar: ProgressBar,
        parentCommentInfo: CommentInfo?
    ) {
        if (tm.token != null) {
            if (TextUtils.isEmpty(input.text)) {
                Toast.makeText(this, R.string.text_is_empty, Toast.LENGTH_SHORT).show()
                return
            }

            sendButton.visibility = View.GONE
            pgBar.visibility = View.VISIBLE

            val token = tm.token
            val dto = CreateCommentDto().apply {
                userId = DataTools.decodeJwtTokenUserId(token!!).toLong()
                imageGuid = null // 发图功能后期可以加
                commentDateTime = Date()
                this.postId = this@PostActivity.postId.toLong()
                this.parentCommentId = parentCommentInfo?.commentId?.toLong() ?: 0L
                content = DataTools.deleteBlankLines(input.text.toString())
            }

            TangyuanApplication.getApi().postComment(dto)
                .enqueue(object : Callback<Map<String, String>> {
                    override fun onResponse(
                        call: Call<Map<String, String>>,
                        response: Response<Map<String, String>>
                    ) {
                        if (response.code() == 200) {
                            runOnUiThread {
                                Toast.makeText(
                                    this@PostActivity,
                                    if (parentCommentInfo == null) R.string.comment_sent else R.string.reply_sent,
                                    Toast.LENGTH_SHORT
                                ).show()
                                sendButton.visibility = View.VISIBLE
                                pgBar.visibility = View.GONE
                                input.setText("")
                                val imm =
                                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.hideSoftInputFromWindow(input.windowToken, 0)
                                updateComment()
                                parentCommentInfo?.let { showReplyBottomSheet(it) }
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(
                                    this@PostActivity,
                                    R.string.send_comment_error,
                                    Toast.LENGTH_SHORT
                                ).show()
                                sendButton.visibility = View.VISIBLE
                                pgBar.visibility = View.GONE
                            }
                        }
                    }

                    override fun onFailure(call: Call<Map<String, String>>, throwable: Throwable) {
                        runOnUiThread {
                            Toast.makeText(
                                this@PostActivity,
                                R.string.send_comment_error,
                                Toast.LENGTH_SHORT
                            ).show()
                            sendButton.visibility = View.VISIBLE
                            pgBar.visibility = View.GONE
                        }
                    }
                })
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateComment() {
        commentAdapter.clearData()
        textCommentCounter.setText(R.string.loading_comments)
        TangyuanApplication.getApi().getCommentForPost(postId)
            .enqueue(object : Callback<List<Comment>> {
                override fun onResponse(
                    call: Call<List<Comment>>,
                    response: Response<List<Comment>>
                ) {
                    if (response.code() == 200 && response.body() != null) {
                        // 有评论
                        val parentCount = AtomicInteger()
                        val scrollToPosition = AtomicInteger() // 用来记录需要滚动到的位置

                        for (m in response.body()!!) {
                            ApiHelper.getCommentInfoByIdAsync(m.commentId) { info ->
                                if (m.parentCommentId == 0) { // 如果是一级评论则显示
                                    commentAdapter.appendData(info!!)
                                    parentCount.getAndIncrement()

                                    if (m.commentId == flagScrollToCommmentId) { // 如果这条评论正是想要滚动到的评论
                                        scrollToPosition.set(commentAdapter.getPositionOf(info)) // 记录这条评论在 Adapter 中的位置
                                    }

                                    runOnUiThread {
                                        textCommentCounter.text =
                                            "${parentCount.get()}${getString(R.string.of_comments)}"

                                        if (scrollToPosition.get() != 0) { // 如果已经设置了滚动位置
                                            findViewById<View>(R.id.main).postDelayed({
                                                // 滚动到指定位置
                                                val distance =
                                                    commentsRcv.layoutManager!!.findViewByPosition(
                                                        scrollToPosition.get()
                                                    )!!.top +
                                                            findViewById<View>(R.id.contentView).height
                                                findViewById<NestedScrollView>(R.id.main).smoothScrollTo(
                                                    0,
                                                    distance
                                                )
                                                // 每次新增一条父评论，都滚动到指定位置
                                            }, 2000)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        runOnUiThread { textCommentCounter.setText(R.string.no_comment) }
                    }
                }

                override fun onFailure(call: Call<List<Comment>>, throwable: Throwable) {
                    runOnUiThread { textCommentCounter.setText(R.string.load_comments_error) }
                }
            })
    }

    private fun showReplyBottomSheet(info: CommentInfo) {
        replyDialog?.dismiss()
        replyDialog = BottomSheetDialog(this).apply {
            val dialogView = layoutInflater.inflate(R.layout.bottom_sheet_comment_layout, null)
            setContentView(dialogView)
            setCancelable(true)
            setCanceledOnTouchOutside(true)

            val pgBar = dialogView.findViewById<ProgressBar>(R.id.pgBar)
            val replyRcv = dialogView.findViewById<RecyclerView>(R.id.replyRecyclerView)
            val adapter = CommentCardAdapter().apply {
                setOnAvatarClickListener(::startUserActivity)
                setOnItemHoldListener { subCommentInfo ->
                    showCommentControlDialog(
                        subCommentInfo,
                        info
                    )
                }
            }
            val layoutManager = LinearLayoutManager(this@PostActivity)
            replyRcv.adapter = adapter
            replyRcv.layoutManager = layoutManager
            replyRcv.addItemDecoration(
                DividerItemDecoration(
                    this@PostActivity,
                    layoutManager.orientation
                )
            )

            TangyuanApplication.getApi().getSubComment(info.commentId)
                .enqueue(object : Callback<List<Comment>> {
                    override fun onResponse(
                        call: Call<List<Comment>>,
                        response: Response<List<Comment>>
                    ) {
                        if (response.code() != 404) {
                            runOnUiThread {
                                dialogView.findViewById<TextView>(R.id.titleReply).text =
                                    "${response.body()!!.size}${getString(R.string.of_replies)}"
                            }
                            for (c in response.body()!!) {
                                ApiHelper.getCommentInfoByIdAsync(c.commentId) { info ->
                                    info?.let { adapter.appendData(it) }
                                }

                            }
                        } else {
                            runOnUiThread {
                                dialogView.findViewById<TextView>(R.id.titleReply)
                                    .setText(R.string.no_reply)
                            }
                        }
                        pgBar.visibility = View.GONE
                    }

                    override fun onFailure(call: Call<List<Comment>>, throwable: Throwable) {
                        runOnUiThread {
                            dialogView.findViewById<TextView>(R.id.titleReply)
                                .setText(R.string.network_error)
                        }
                    }
                })

            dialogView.findViewById<View>(R.id.buttonSendReply).setOnClickListener {
                trySendComment(
                    dialogView.findViewById(R.id.editReply),
                    dialogView.findViewById(R.id.buttonSendReply),
                    dialogView.findViewById(R.id.pgBar),
                    info
                )
            }

            dialogView.findViewById<EditText>(R.id.editReply).hint =
                "${getString(R.string.reply_to)}${info.commentText}"
            show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menuDelete) {
            AlertDialog.Builder(this)
                .setTitle(R.string.delete_post)
                .setMessage(R.string.confirm_delete_post)
                .setPositiveButton(R.string.yes) { _, _ ->
                    TangyuanApplication.getApi().deletePost(postId)
                        .enqueue(object : Callback<ResponseBody> {
                            override fun onResponse(
                                call: Call<ResponseBody>,
                                response: Response<ResponseBody>
                            ) {
                                if (response.code() == 200) {
                                    runOnUiThread {
                                        val intent = Intent().apply {
                                            putExtra("deletedPostId", postId)
                                        }
                                        setResult(RESULT_OK, intent)
                                        finish()
                                        Toast.makeText(
                                            this@PostActivity,
                                            R.string.post_deleted,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }

                            override fun onFailure(call: Call<ResponseBody>, throwable: Throwable) {
                                Toast.makeText(
                                    this@PostActivity,
                                    R.string.network_error,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                }
                .setNegativeButton(R.string.no, null)
                .create().show()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_post_menu, menu)
        this.menu = menu
        return true
    }

    private fun startUserActivity() {
        val intent = Intent(this@PostActivity, UserActivity::class.java)
        intent.putExtra("userId", postInfo.userId)
        startActivity(intent)
    }

    private fun startCategoryActivity() {
        val intent = Intent(this@PostActivity, CategoryActivity::class.java)
        intent.putExtra("categoryId", postInfo.categoryId)
        startActivity(intent)
    }
}