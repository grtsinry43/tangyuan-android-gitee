package com.qingshuige.tangyuan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
//import androidx.activity.EdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qingshuige.tangyuan.network.ApiHelper
import com.qingshuige.tangyuan.network.Comment
import com.qingshuige.tangyuan.network.PostMetadata
import com.qingshuige.tangyuan.network.User
import com.qingshuige.tangyuan.viewmodels.CommentCardAdapter
import com.qingshuige.tangyuan.viewmodels.PostCardAdapter
import com.qingshuige.tangyuan.viewmodels.UserCardAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.CountDownLatch
import kotlin.concurrent.thread

class SearchActivity : AppCompatActivity() {

    private lateinit var keyword: String
    private lateinit var toolbar: Toolbar
    private lateinit var pgBar: ProgressBar
    private lateinit var rcvPost: RecyclerView
    private lateinit var textPostSearchTitle: TextView
    private lateinit var postAdapter: PostCardAdapter
    private lateinit var rcvUser: RecyclerView
    private lateinit var textUserSearchTitle: TextView
    private lateinit var userAdapter: UserCardAdapter
    private lateinit var rcvComment: RecyclerView
    private lateinit var textCommentSearchTitle: TextView
    private lateinit var commentAdapter: CommentCardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        EdgeToEdge.enable(this)
        setContentView(R.layout.activity_search)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        keyword = intent.getStringExtra("keyword") ?: ""

        toolbar = findViewById(R.id.toolbar)
        rcvPost = findViewById(R.id.rcvPost)
        pgBar = findViewById(R.id.pgBar)
        textPostSearchTitle = findViewById(R.id.textPostSearchTitle)
        rcvUser = findViewById(R.id.rcvUser)
        textUserSearchTitle = findViewById(R.id.textUserSearchTitle)
        rcvComment = findViewById(R.id.rcvComment)
        textCommentSearchTitle = findViewById(R.id.textCommentSearchTitle)

        toolbar.title = "${getString(R.string.search)}: $keyword"

        val div = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)

        //Post
        postAdapter = PostCardAdapter()
        postAdapter.setOnItemClickListener { postId -> startPostActivity(postId, 0) }
        rcvPost.adapter = postAdapter
        rcvPost.addItemDecoration(div)
        rcvPost.layoutManager = LinearLayoutManager(this)

        //User
        userAdapter = UserCardAdapter(this)
        userAdapter.setOnUserClickListener { info -> startUserActivity(info.user.userId) }
        rcvUser.adapter = userAdapter
        rcvUser.layoutManager = LinearLayoutManager(this)
        rcvUser.addItemDecoration(div)

        //Comment
        commentAdapter = CommentCardAdapter()
        commentAdapter.setReplyButtonVisible(false)
        commentAdapter.setOnAvatarClickListener { info -> startUserActivity(info.userId) }
        commentAdapter.setOnTextClickListener { info -> startPostActivity(info.comment?.postId!!, info.commentId) }
        rcvComment.adapter = commentAdapter
        rcvComment.layoutManager = LinearLayoutManager(this)
        rcvComment.addItemDecoration(div)

        initializeUI()
    }

    private fun startUserActivity(userId: Int) {
        val intent = Intent(this, UserActivity::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
    }

    private fun initializeUI() {
        thread {
            val latch = CountDownLatch(3)

            //帖子搜索结果
            TangyuanApplication.getApi().searchPostByKeyword(keyword).enqueue(object : Callback<List<PostMetadata>> {
                override fun onResponse(call: Call<List<PostMetadata>>, response: Response<List<PostMetadata>>) {
                    if (response.code() == 200 && response.body() != null) {
                        runOnUiThread { 
                            textPostSearchTitle.text = "${textPostSearchTitle.text}: ${response.body()!!.size}" 
                        }
                        ApiHelper.getInfoFastAsync(response.body()!!, ApiHelper.PostInfoConstructor()) { result ->
                            result?.let {
                                runOnUiThread { postAdapter.replaceDataSet(it) }
                            }
                        }
                    } else {
                        runOnUiThread {
                            textPostSearchTitle.setText(R.string.no_post_result)
                        }
                    }
                    latch.countDown()
                }

                override fun onFailure(call: Call<List<PostMetadata>>, throwable: Throwable) {
                    runOnUiThread {
                        textPostSearchTitle.text = "${textPostSearchTitle.text}: ${getString(R.string.network_error)}"
                    }
                }
            })

            //用户搜索结果
            TangyuanApplication.getApi().searchUserByKeyword(keyword).enqueue(object : Callback<List<User>> {
                override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                    if (response.code() == 200 && response.body() != null) {
                        val users = response.body()!!
                        runOnUiThread { 
                            textUserSearchTitle.text = "${textUserSearchTitle.text}: ${users.size}" 
                        }
                        ApiHelper.getInfoFastAsync(users, ApiHelper.UserInfoConstructor()) { result ->
                            result?.let {
                                runOnUiThread {
                                    userAdapter.replaceDataset(it)
                                }
                            }
                        }
                    } else {
                        runOnUiThread {
                            textUserSearchTitle.setText(R.string.no_user_result)
                        }
                    }
                    latch.countDown()
                }

                override fun onFailure(call: Call<List<User>>, throwable: Throwable) {}
            })

            //评论搜索结果
            TangyuanApplication.getApi().searchCommentByKeyword(keyword).enqueue(object : Callback<List<Comment>> {
                override fun onResponse(call: Call<List<Comment>>, response: Response<List<Comment>>) {
                    if (response.code() == 200 && response.body() != null) {
                        val comments = response.body()!!
                        runOnUiThread { 
                            textCommentSearchTitle.text = "${textCommentSearchTitle.text}: ${comments.size}" 
                        }
                        ApiHelper.getInfoFastAsync(comments, ApiHelper.CommentInfoConstructor()) { result ->
                            runOnUiThread { commentAdapter.replaceDataset(result!!) }
                        }
                    } else {
                        runOnUiThread {
                            textCommentSearchTitle.setText(R.string.no_comment_result)
                        }
                    }
                    latch.countDown()
                }

                override fun onFailure(call: Call<List<Comment>>, throwable: Throwable) {}
            })

            try {
                latch.await()
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }

            runOnUiThread { pgBar.visibility = View.GONE }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun startPostActivity(postId: Int, commentId: Int) {
        val intent = Intent(this, PostActivity::class.java)
        intent.putExtra("postId", postId)
        intent.putExtra("commentId", commentId)
        startActivity(intent)
    }
}