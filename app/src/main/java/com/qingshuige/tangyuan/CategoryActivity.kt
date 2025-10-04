package com.qingshuige.tangyuan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
//import androidx.activity.EdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qingshuige.tangyuan.network.ApiHelper
import com.qingshuige.tangyuan.network.Category
import com.qingshuige.tangyuan.network.PostMetadata
import com.qingshuige.tangyuan.viewmodels.PostCardAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CategoryActivity : AppCompatActivity() {

    private lateinit var textCategoryName: TextView
    private lateinit var text24hNewPosts: TextView
    private lateinit var textTotalPosts: TextView
    private lateinit var textCategoryDisc: TextView
    private lateinit var rcvPosts: RecyclerView
    private lateinit var pgBar: ProgressBar

    private lateinit var adapter: PostCardAdapter

    private var categoryId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        EdgeToEdge.enable(this)
        setContentView(R.layout.activity_category)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        textCategoryName = findViewById(R.id.textCategoryName)
        text24hNewPosts = findViewById(R.id.text24hNewPost)
        textTotalPosts = findViewById(R.id.textTotalPost)
        textCategoryDisc = findViewById(R.id.textCategoryDisc)
        rcvPosts = findViewById(R.id.rcvCategoryPosts)
        pgBar = findViewById(R.id.pgBar)

        categoryId = intent.getIntExtra("categoryId", 0)

        adapter = PostCardAdapter()
        adapter.setOnItemClickListener { postId ->
            val intent = Intent(this@CategoryActivity, PostActivity::class.java)
            intent.putExtra("postId", postId)
            startActivity(intent)
        }
        adapter.setCategoryVisible(false)
        rcvPosts.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        rcvPosts.layoutManager = LinearLayoutManager(this)
        rcvPosts.adapter = adapter

        initializeUI()
    }

    private fun initializeUI() {
        // 名字和描述
        TangyuanApplication.getApi().getCategory(categoryId).enqueue(object : Callback<Category> {
            override fun onResponse(call: Call<Category>, response: Response<Category>) {
                if (response.code() == 200 && response.body() != null) {
                    val category = response.body()!!
                    textCategoryName.text = category.baseName
                    textCategoryDisc.text = category.baseDescription
                }
            }

            override fun onFailure(call: Call<Category>, throwable: Throwable) {
                alertAndFinish()
            }
        })

        // 24小时新帖数
        TangyuanApplication.getApi().get24hNewPostCountByCategoryId(categoryId).enqueue(object : Callback<Int> {
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                if (response.code() == 200 && response.body() != null) {
                    text24hNewPosts.text = getString(R.string._24hpost) + response.body()
                }
            }

            override fun onFailure(call: Call<Int>, throwable: Throwable) {
                alertAndFinish()
            }
        })

        // 总帖数
        TangyuanApplication.getApi().getPostCountOfCategory(categoryId).enqueue(object : Callback<Int> {
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                textTotalPosts.text = getString(R.string.total_post_count) + response.body()
            }

            override fun onFailure(call: Call<Int>, throwable: Throwable) {
                alertAndFinish()
            }
        })

        // 帖子
        TangyuanApplication.getApi().getAllMetadatasByCategoryId(categoryId).enqueue(object : Callback<List<PostMetadata>> {
            override fun onResponse(call: Call<List<PostMetadata>>, response: Response<List<PostMetadata>>) {
                if (response.code() == 200 && response.body() != null) {
                    val metadatas = response.body()!!
                    ApiHelper.getInfoFastAsync(metadatas, ApiHelper.PostInfoConstructor()) { result ->
                        if (result != null) {
                            runOnUiThread {
                                adapter.replaceDataSet(result)
                                pgBar.visibility = View.GONE
                            }
                        }
                    }
                } else {
                    alertAndFinish()
                }
            }

            override fun onFailure(call: Call<List<PostMetadata>>, throwable: Throwable) {
                alertAndFinish()
            }
        })
    }

    private fun alertAndFinish() {
        AlertDialog.Builder(this)
            .setTitle(R.string.network_error)
            .setMessage(R.string.failed_to_load_category)
            .setPositiveButton(R.string.ok) { _, _ -> runOnUiThread { finish() } }
            .create().show()
    }
}