package com.qingshuige.tangyuan.viewmodels

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.qingshuige.tangyuan.R
import com.qingshuige.tangyuan.network.ApiHelper
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class GalleryAdapter(private val imageGuids: List<String>) : RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.gallery_image_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Picasso.get()
            .load(ApiHelper.getFullImageURL(imageGuids[position]))
            .resize(1280, 0)
            .centerInside()
            .placeholder(R.drawable.img_placeholder)
            .into(holder.imageView, object : Callback {
                override fun onSuccess() {
                    holder.imageView.setOnClickListener {
                        onItemClickListener?.onItemClick(holder.imageView.drawable)
                    }
                }

                override fun onError(e: Exception?) {
                    // 处理错误
                }
            })
    }

    override fun getItemCount(): Int = imageGuids.size

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun interface OnItemClickListener {
        fun onItemClick(drawable: Drawable)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.navAvatarView)
    }
}