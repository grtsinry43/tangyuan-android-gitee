package com.qingshuige.tangyuan

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
//import androidx.activity.EdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.qingshuige.tangyuan.data.CircleTransform
import com.qingshuige.tangyuan.data.DataTools
import com.qingshuige.tangyuan.network.ApiHelper
import com.qingshuige.tangyuan.network.PostMetadata
import com.qingshuige.tangyuan.network.User
import com.qingshuige.tangyuan.viewmodels.PostCardAdapter
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserActivity : AppCompatActivity() {

    private lateinit var postList: RecyclerView
    private lateinit var avatarView: ImageView
    private lateinit var nicknameView: TextView
    private lateinit var bioView: TextView
    private lateinit var pgBar: ProgressBar
    private lateinit var chipRegion: Chip
    private lateinit var chipEmail: Chip
    private var userId: Int = 0
    private lateinit var tm: TokenManager
    private lateinit var menu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        EdgeToEdge.enable(this)
        setContentView(R.layout.activity_user)
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

        postList = findViewById(R.id.postList)
        avatarView = findViewById(R.id.avatarView)
        nicknameView = findViewById(R.id.nicknameTextView)
        bioView = findViewById(R.id.bioTextView)
        pgBar = findViewById(R.id.pgBar)
        chipRegion = findViewById(R.id.chipRegion)
        chipEmail = findViewById(R.id.chipEmail)

        userId = intent.getIntExtra("userId", 0)
        tm = TangyuanApplication.getTokenManager()

        initializeUI()
    }

    private fun initializeUI() {
        //显示所发帖子
        val adapter = PostCardAdapter()
        adapter.setOnItemClickListener { postId ->
            val intent = Intent(this, PostActivity::class.java)
            intent.putExtra("postId", postId)
            startActivity(intent)
        }
        val div = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        val layoutManager = LinearLayoutManager(this)
        postList.addItemDecoration(div)
        postList.layoutManager = LinearLayoutManager(this)
        postList.adapter = adapter

        updateProfile()
        ///初始刷新
        updateRecyclerView(userId)
    }

    private fun updateProfile() {
        TangyuanApplication.getApi().getUser(userId).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                val user = response.body()!!
                runOnUiThread {
                    Picasso.get()
                        .load(ApiHelper.getFullImageURL(user.avatarGuid))
                        .transform(CircleTransform())
                        .into(avatarView)
                    nicknameView.text = user.nickName
                    bioView.text = user.bio
                    chipRegion.text = user.isoRegionName
                    chipEmail.text = user.email
                }
            }

            override fun onFailure(call: Call<User>, throwable: Throwable) {
                runOnUiThread {
                    Toast.makeText(this@UserActivity, R.string.network_error, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun updateRecyclerView(userId: Int) {
        TangyuanApplication.getApi().getMetadatasByUserID(userId).enqueue(object : Callback<List<PostMetadata>> {
            override fun onResponse(call: Call<List<PostMetadata>>, response: Response<List<PostMetadata>>) {
                val metadatas = response.body()

                ApiHelper.getInfoFastAsync(metadatas!!, ApiHelper.PostInfoConstructor()) { result ->
                    if (result != null) {
                        val sortedResult = result.sortedWith { postInfo, t1 -> t1.postDate.compareTo(postInfo.postDate) }
                        runOnUiThread { (postList.adapter as PostCardAdapter).replaceDataSet(sortedResult) }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this@UserActivity, R.string.network_error, Toast.LENGTH_SHORT).show()
                        }
                    }
                    runOnUiThread { pgBar.visibility = View.GONE }
                }
            }

            override fun onFailure(call: Call<List<PostMetadata>>, throwable: Throwable) {
                runOnUiThread {
                    Toast.makeText(this@UserActivity, R.string.network_error, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menuEditProfile) {
            val intent = Intent(this, UserProfileEditActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_user_profile_menu, menu)
        this.menu = menu
        if (tm.token != null && DataTools.decodeJwtTokenUserId(tm.token!!) == userId) {
            menu.findItem(R.id.menuEditProfile).isVisible = true
        }

        return true
    }

    override fun onResume() {
        super.onResume()
        updateProfile()
    }
}