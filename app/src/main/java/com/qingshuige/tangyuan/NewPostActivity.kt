package com.qingshuige.tangyuan

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
//import androidx.activity.EdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.qingshuige.tangyuan.data.DataTools
import com.qingshuige.tangyuan.data.MediaTools
import com.qingshuige.tangyuan.network.Category
import com.qingshuige.tangyuan.network.CreatPostMetadataDto
import com.qingshuige.tangyuan.network.PostBody
import com.qingshuige.tangyuan.viewmodels.CategorySpinnerAdapter
import com.squareup.picasso.Picasso
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class NewPostActivity : AppCompatActivity() {

    private lateinit var textEdit: EditText
    private lateinit var byteCounter: TextView
    private lateinit var pgBar: ProgressBar
    private lateinit var imageView1: ImageView
    private lateinit var imageView2: ImageView
    private lateinit var imageView3: ImageView
    private lateinit var spinnerSection: Spinner
    private lateinit var spinnerCategory: Spinner
    private lateinit var menu: Menu

    private lateinit var es: ExecutorService
    private lateinit var handler: Handler
    private lateinit var tm: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        EdgeToEdge.enable(this)
        setContentView(R.layout.activity_new_post)
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

        textEdit = findViewById(R.id.textEdit)
        byteCounter = findViewById(R.id.byteCounter)
        pgBar = findViewById(R.id.progressBar)
        imageView1 = findViewById(R.id.imageView1)
        imageView2 = findViewById(R.id.imageView2)
        imageView3 = findViewById(R.id.imageView3)
        spinnerSection = findViewById(R.id.spinnerSection)
        spinnerCategory = findViewById(R.id.spinnerCategory)

        es = Executors.newSingleThreadExecutor()
        handler = Handler(Looper.getMainLooper())
        tm = TangyuanApplication.getTokenManager()

        textEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                byteCounter.text = "${textEdit.text.length} / 200"
                if (textEdit.text.length > 200) {
                    byteCounter.setTextColor(getColor(R.color.nanohanacha_gold))
                } else {
                    byteCounter.setTextColor(getColor(R.color.electromagnetic))
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })

        imageView1.setOnClickListener(ImageViewOnClickListener())

        // 板块选择
        val sectionAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            arrayOf(getString(R.string.menu_normalchat), getString(R.string.menu_chitchat))
        )
        sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSection.adapter = sectionAdapter

        // 领域选择
        TangyuanApplication.getApi().getAllCategories().enqueue(object : Callback<List<Category>> {
            override fun onResponse(call: Call<List<Category>>, response: Response<List<Category>>) {
                if (response.code() == 200) {
                    val categoryAdapter = CategorySpinnerAdapter(
                        this@NewPostActivity,
                        android.R.layout.simple_spinner_item,
                        response.body()!!
                    )
                    categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerCategory.adapter = categoryAdapter
                } else {
                    AlertDialog.Builder(this@NewPostActivity)
                        .setTitle(R.string.network_error)
                        .setMessage(R.string.failed_to_fetch_categories)
                        .create().show()
                    this@NewPostActivity.finish()
                }
            }

            override fun onFailure(call: Call<List<Category>>, throwable: Throwable) {
                AlertDialog.Builder(this@NewPostActivity)
                    .setTitle(R.string.network_error)
                    .setMessage(R.string.failed_to_fetch_categories)
                    .create().show()
                this@NewPostActivity.finish()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_new_post_menu, menu)
        this.menu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // 选择图片
        if (item.itemId == R.id.image_button) {
            // 假如imageView有空闲
            if (imageView1.drawable == null ||
                imageView2.drawable == null ||
                imageView3.drawable == null
            ) {
                // 选择图片
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, 1)
            }
        }

        // 发帖
        if (item.itemId == R.id.send_post_button) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.let {
                currentFocus?.let { view ->
                    it.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }

            // 检查必要条件
            when {
                textEdit.text.length <= 0 -> {
                    Toast.makeText(this, R.string.text_is_empty, Toast.LENGTH_SHORT).show()
                }
                textEdit.text.length >= 200 -> {
                    Toast.makeText(this, R.string.text_is_too_long, Toast.LENGTH_SHORT).show()
                }
                spinnerCategory.adapter == null -> {
                    Toast.makeText(this, R.string.waiting_for_loading_categories, Toast.LENGTH_SHORT).show()
                }
                (spinnerCategory.selectedItem as Category).categoryId == 0 -> {
                    AlertDialog.Builder(this)
                        .setMessage(R.string.sending_post_no_category)
                        .setTitle(R.string.message)
                        .setPositiveButton(R.string.yes) { _, _ ->
                            sendPostAsync(this@NewPostActivity)
                        }
                        .setNegativeButton(R.string.no, null)
                        .create()
                        .show()
                }
                else -> {
                    sendPostAsync(this)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun sendPostAsync(context: Context) {
        menu.findItem(R.id.send_post_button).isEnabled = false
        pgBar.visibility = View.VISIBLE

        es.execute {
            try {
                // 1.上传图片
                val guids = mutableListOf<String>()
                val imageViewList = listOf(imageView1, imageView2, imageView3)
                
                for (v in imageViewList) {
                    v.drawable?.let { drawable ->
                        val bitmap = MediaTools.compressToSize(
                            this@NewPostActivity,
                            (drawable as BitmapDrawable).bitmap,
                            1.5f
                        )
                        val stream = ByteArrayOutputStream()
                        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                        val bytes = stream.toByteArray()
                        val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), bytes)
                        val part = MultipartBody.Part.createFormData("file", "image.jpg", requestBody)
                        val guid = TangyuanApplication.getApi().postImage(part).execute().body()!!.values.first()
                        guids.add(guid)
                    }
                }

                // 2.上传元数据
                val metadataDto = CreatPostMetadataDto().apply {
                    userId = DataTools.decodeJwtTokenUserId(tm.token!!)
                    postDateTime = Date()
                    sectionId = spinnerSection.selectedItemPosition + 1
                    categoryId = (spinnerCategory.selectedItem as Category).categoryId
                    isVisible = true
                }
                val postId = TangyuanApplication.getApi().postPostMetadata(metadataDto).execute().body()!!.values.first()

                // 3.上传Body
                val body = PostBody().apply {
                    this.postId = postId
                    textContent = DataTools.deleteBlankLines(textEdit.text.toString())
                    if (guids.isNotEmpty()) {
                        image1UUID = guids[0]
                        if (guids.size >= 2) {
                            image2UUID = guids[1]
                        }
                        if (guids.size == 3) {
                            image3UUID = guids[2]
                        }
                    }
                }
                TangyuanApplication.getApi().postPostBody(body).execute()

                // 4.完成
                finish()
                handler.post {
                    Toast.makeText(context, R.string.post_sent, Toast.LENGTH_SHORT).show()
                    context.startActivity(Intent(context, PostActivity::class.java).putExtra("postId", postId))
                }
            } catch (e: Exception) {
                handler.post {
                    Snackbar.make(
                        findViewById(R.id.imageLayout),
                        getString(R.string.send_post_error),
                        BaseTransientBottomBar.LENGTH_LONG
                    ).show()
                    (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                        .setPrimaryClip(ClipData.newPlainText("error", e.localizedMessage))
                    menu.findItem(R.id.send_post_button).isEnabled = true
                    pgBar.visibility = View.GONE
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            1 -> { // 选择图片
                if (resultCode == RESULT_OK && data != null) {
                    // 轮流填充三个imageView
                    when {
                        imageView1.drawable == null -> {
                            Picasso.get()
                                .load(data.data)
                                .resize(4000, 0)
                                .centerCrop()
                                .into(imageView1)
                        }
                        imageView2.drawable == null -> {
                            Picasso.get()
                                .load(data.data)
                                .resize(4000, 0)
                                .centerCrop()
                                .into(imageView2)
                        }
                        imageView3.drawable == null -> {
                            Picasso.get()
                                .load(data.data)
                                .resize(4000, 0)
                                .centerCrop()
                                .into(imageView3)
                        }
                    }
                }
            }
        }
    }

    private inner class ImageViewOnClickListener : View.OnClickListener {
        override fun onClick(view: View) {
            val imageView = view as ImageView
            if (imageView.drawable != null) {
                imageView.setImageDrawable(null)
            }
        }
    }

    companion object {
        /**
         * @param context
         * @param uri
         * @return 图片大小Byte数。
         */
        fun getImageSize(context: Context, uri: Uri): Long {
            var cursor: Cursor? = null
            return try {
                // 查询文件大小的列
                val projection = arrayOf(OpenableColumns.SIZE)
                cursor = context.contentResolver.query(uri, projection, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val sizeColumnIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeColumnIndex >= 0) {
                        cursor.getLong(sizeColumnIndex) // 返回文件大小（字节）
                    } else {
                        -1L
                    }
                } else {
                    -1L
                }
            } catch (e: Exception) {
                e.printStackTrace()
                -1L // 获取失败返回-1
            } finally {
                cursor?.close()
            }
        }
    }
}