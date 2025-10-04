package com.qingshuige.tangyuan.ui

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.github.chrisbanes.photoview.PhotoView
import com.qingshuige.tangyuan.R
import java.util.UUID

class PhotoDialogFragment private constructor() : DialogFragment() {
    var drawable: Drawable? = null
    var bitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.photo_dialog_fragment, container, false)

        bitmap = requireArguments().getParcelable<Bitmap?>("bitmap")
        drawable = BitmapDrawable(bitmap)

        (root.findViewById<View?>(R.id.photoView) as PhotoView).setImageDrawable(drawable)
        root.findViewById<View?>(R.id.buttonSavePhoto)
            .setOnClickListener(View.OnClickListener { view: View? -> savePhotoToGallery() })

        return root
    }

    private fun savePhotoToGallery() {
        // 创建ContentValues来存储图片元数据
        val values = ContentValues()
        values.put(
            MediaStore.Images.Media.DISPLAY_NAME,
            "TY_" + UUID.randomUUID().toString() + ".jpg"
        ) // 文件名
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg") // 文件类型
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES) // 存储路径
        values.put(MediaStore.Images.Media.IS_PENDING, 1) // 标记为正在处理

        // 获取ContentResolver
        val resolver = requireContext().contentResolver

        // 插入图片到MediaStore
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        try {
            if (imageUri != null) {
                // 获取输出流并写入图片
                val outputStream = resolver.openOutputStream(imageUri)
                if (outputStream != null) {
                    bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.close()
                }

                // 更新状态，标记完成
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(imageUri, values, null, null)
                Toast.makeText(getContext(), R.string.photo_saved, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(getContext(), R.string.failed_to_save_photo, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        if (getDialog() != null && getDialog()!!.getWindow() != null) {
            getDialog()!!.getWindow()!!.setBackgroundDrawableResource(R.color.black)
            getDialog()!!.getWindow()!!.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            getDialog()!!.getWindow()!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            getDialog()!!.getWindow()!!.setStatusBarColor(Color.TRANSPARENT)
            getDialog()!!.getWindow()!!.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            )
        }
    }

    companion object {
        fun create(bitmap: Bitmap?): PhotoDialogFragment {
            val fragment = PhotoDialogFragment()
            val args = Bundle()
            args.putParcelable("bitmap", bitmap)
            fragment.setArguments(args)
            return fragment
        }
    }
}
