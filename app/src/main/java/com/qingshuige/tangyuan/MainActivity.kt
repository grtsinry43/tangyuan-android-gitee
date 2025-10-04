package com.qingshuige.tangyuan

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.google.gson.JsonObject
import com.qingshuige.tangyuan.data.DataTools
import com.qingshuige.tangyuan.network.ApiHelper
import com.qingshuige.tangyuan.network.PostMetadata
import com.qingshuige.tangyuan.network.User
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.navigation.findNavController
import androidx.core.net.toUri

/*
 *
 *  在科学上没有平坦的大道，
 *  只有不畏劳苦沿着陡峭山路攀登的人，
 *  才有希望达到光辉的顶点。
 *              ——卡尔·马克思
 */

class MainActivity : AppCompatActivity() {

    private lateinit var mAppBarConfiguration: AppBarConfiguration
    private lateinit var tm: TokenManager
    private lateinit var navHeaderView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tm = TangyuanApplication.getTokenManager()

        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navHeaderView = navigationView.getHeaderView(0)

        mAppBarConfiguration = AppBarConfiguration.Builder(
            R.id.nav_normalchat, R.id.nav_chitchat, R.id.nav_message, R.id.nav_about
        )
            .setOpenableLayout(drawer)
            .build()
        val navController = this.findNavController(R.id.nav_host_fragment_content_main)
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration)
        NavigationUI.setupWithNavController(navigationView, navController)

        navHeaderView.findViewById<View>(R.id.navAvatarView).setOnClickListener {
            if (tm.token == null) {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this@MainActivity, UserActivity::class.java)
                intent.putExtra("userId", DataTools.decodeJwtTokenUserId(tm.token!!))
                startActivity(intent)
            }
        }

        // 蒲公英更新
        val params = hashMapOf<String, String>().apply {
            put("_api_key", "133d8c604b4d0772723a007a9ad213f7")
            put("appKey", "123a9eba5d424ab9088069505ffeb1de")
            try {
                put("buildVersion", packageManager.getPackageInfo(packageName, 0).versionName)
            } catch (e: PackageManager.NameNotFoundException) {
                throw RuntimeException(e)
            }
        }

        TangyuanApplication.getApi().checkUpdate(params).enqueue(object : Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {
                val data = response.body()?.getAsJsonObject("data")
                if (data?.get("buildHaveNewVersion")?.asBoolean == true) {
                    runOnUiThread {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("糖原公测阶段")
                            .setMessage("检测到新版本，请及时更新。")
                            .setPositiveButton("下载") { _, _ ->
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = data.get("downloadURL").asString.toUri()
                                startActivity(intent)
                            }
                            .show()
                    }
                }
            }

            override fun onFailure(call: Call<JsonObject>, throwable: Throwable) {
                // Handle failure
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.action_bar, menu)

        val searchView = MenuItemCompat.getActionView(menu.findItem(R.id.menuSearch)) as SearchView
        searchView.queryHint = getString(R.string.please_enter_keyword)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                val intent = Intent(this@MainActivity, SearchActivity::class.java)
                intent.putExtra("keyword", query)
                startActivity(intent)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.new_post_button -> {
                if (tm.token == null) {
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, NewPostActivity::class.java)
                    startActivity(intent)
                }
            }

            R.id.menuNotice -> {
                TangyuanApplication.getApi().getNotice().enqueue(object : Callback<PostMetadata> {
                    override fun onResponse(
                        call: Call<PostMetadata>,
                        response: Response<PostMetadata>
                    ) {
                        if (response.code() == 200) {
                            response.body()?.let { postMetadata ->
                                ApiHelper.getPostInfoByIdAsync(postMetadata.postId) { result ->
                                    val intent = Intent(this@MainActivity, PostActivity::class.java)
                                    intent.putExtra("postId", result?.postId)
                                    startActivity(intent)
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<PostMetadata>, throwable: Throwable) {
                        Toast.makeText(
                            this@MainActivity,
                            R.string.network_error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        updateUserStatus()
    }

    private fun updateUserStatus() {
        val token = tm.token
        if (token != null) {
            val userId = DataTools.decodeJwtTokenUserId(token)
            TangyuanApplication.getApi().getUser(userId).enqueue(object : Callback<User> {
                override fun onResponse(call: Call<User>, response: Response<User>) {
                    val user = response.body()
                    if (user != null) {
                        runOnUiThread {
                            Picasso.get()
                                .load(ApiHelper.getFullImageURL(user.avatarGuid))
                                .resize(100, 0)
                                .centerCrop()
                                .into(navHeaderView.findViewById<ImageView>(R.id.navAvatarView))
                            navHeaderView.findViewById<TextView>(R.id.navNicknameView).text =
                                user.nickName
                            navHeaderView.findViewById<TextView>(R.id.navBioView).text = user.bio
                        }
                    }
                }

                override fun onFailure(call: Call<User>, throwable: Throwable) {
                    runOnUiThread {
                        navHeaderView.findViewById<TextView>(R.id.navNicknameView)
                            .setText(R.string.network_error)
                    }
                }
            })
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.nav_host_fragment_content_main)
        return NavigationUI.navigateUp(
            navController,
            mAppBarConfiguration
        ) || super.onSupportNavigateUp()
    }
}