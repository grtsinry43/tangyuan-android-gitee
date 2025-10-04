package com.qingshuige.tangyuan

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
//import androidx.activity.EdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.qingshuige.tangyuan.network.ApiHelper
import com.qingshuige.tangyuan.network.User
import com.squareup.picasso.Picasso
import com.yalantis.ucrop.UCrop
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException

class UserProfileEditActivity : AppCompatActivity() {

    companion object {
        private const val PICK_AVATAR_REQUEST = 1
    }

    private lateinit var menu: Menu
    private var userId: Int = 0
    private var isAvatarModified: Boolean = false
    private lateinit var currentUser: User

    private lateinit var editAvatar: ImageView
    private lateinit var editNickname: EditText
    private lateinit var editBio: EditText
    private lateinit var editEmail: EditText
    private lateinit var textRegion: TextView
    private lateinit var textPhoneNumber: TextView
    private lateinit var pgBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        EdgeToEdge.enable(this)
        setContentView(R.layout.activity_user_profile_edit)
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

        editAvatar = findViewById(R.id.editAvatar)
        editNickname = findViewById(R.id.editNickname)
        editBio = findViewById(R.id.editBio)
        editEmail = findViewById(R.id.editEmail)
        textRegion = findViewById(R.id.textRegion)
        textPhoneNumber = findViewById(R.id.textPhoneNumber)
        pgBar = findViewById(R.id.progressBar)

        userId = intent.getIntExtra("userId", 0)

        initializeUI(userId)
    }

    private fun initializeUI(userId: Int) {
        TangyuanApplication.getApi().getUser(userId).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                currentUser = response.body()!!
                runOnUiThread {
                    Picasso.get()
                        .load(ApiHelper.getFullImageURL(currentUser.avatarGuid))
                        .resize(200, 0)
                        .centerCrop()
                        .placeholder(R.drawable.img_placeholder)
                        .into(editAvatar)
                    editNickname.setText(currentUser.nickName)
                    editBio.setText(currentUser.bio)
                    editEmail.setText(currentUser.email)
                    textRegion.text = currentUser.isoRegionName
                    textPhoneNumber.text = currentUser.phoneNumber

                    pgBar.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<User>, throwable: Throwable) {}
        })

        editAvatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_AVATAR_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_AVATAR_REQUEST -> {
                data?.data?.let { startAvatarCrop(it) }
            }
            UCrop.REQUEST_CROP -> {
                val resultUri = UCrop.getOutput(data!!)
                resultUri?.let {
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, it)
                        editAvatar.setImageBitmap(bitmap)
                        isAvatarModified = true
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    }
                }
            }
        }
    }

    private fun tryUpdateUserProfile() {
        pgBar.visibility = View.VISIBLE
        menu.findItem(R.id.menuOk).isEnabled = false

        if (validateData()) {
            val user = User().apply {
                userId = this@UserProfileEditActivity.userId
                nickName = editNickname.text.toString()
                bio = editBio.text.toString()
                email = editEmail.text.toString()
                isoRegionName = currentUser.isoRegionName
                phoneNumber = currentUser.phoneNumber
                password = TangyuanApplication.getTokenManager().password!!
            }

            if (isAvatarModified) {
                //上传图片
                ApiHelper.updateBitmapAsync((editAvatar.drawable as BitmapDrawable).bitmap) { result ->
                    user.avatarGuid = result as String
                    TangyuanApplication.getApi().putUser(userId, user).enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            if (response.code() == 200) {
                                runOnUiThread {
                                    Toast.makeText(
                                        this@UserProfileEditActivity,
                                        R.string.edit_profile_success,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish()
                                }
                            }
                        }

                        override fun onFailure(call: Call<ResponseBody>, throwable: Throwable) {
                            Log.i("TY", "ERROR: ${throwable.message}")
                        }
                    })
                }
            } else {
                user.avatarGuid = currentUser.avatarGuid
                TangyuanApplication.getApi().putUser(userId, user).enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.code() == 200) {
                            runOnUiThread {
                                Toast.makeText(
                                    this@UserProfileEditActivity,
                                    R.string.edit_profile_success,
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, throwable: Throwable) {
                        Log.i("TY", "ERROR: ${throwable.message}")
                    }
                })
            }
        } else {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.let {
                currentFocus?.let { view ->
                    it.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
            Snackbar.make(textPhoneNumber, getString(R.string.fields_unfinished), BaseTransientBottomBar.LENGTH_SHORT)
                .show()
            pgBar.visibility = View.GONE
            menu.findItem(R.id.menuOk).isEnabled = true
        }
    }

    private fun validateData(): Boolean {
        return !(TextUtils.isEmpty(editNickname.text) ||
                TextUtils.isEmpty(editBio.text) ||
                TextUtils.isEmpty(editEmail.text))
    }

    private fun startAvatarCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_image.jpg"))

        val uCrop = UCrop.of(uri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(300, 300)

        val options = UCrop.Options().apply {
            setCompressionFormat(Bitmap.CompressFormat.JPEG)
        }
        uCrop.withOptions(options)

        uCrop.start(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menuOk) {
            tryUpdateUserProfile()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_edit_user_profile_menu, menu)
        this.menu = menu
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}