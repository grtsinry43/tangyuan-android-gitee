package com.qingshuige.tangyuan.viewmodels

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qingshuige.tangyuan.R
import com.qingshuige.tangyuan.data.CircleTransform
import com.qingshuige.tangyuan.data.DataTools
import com.qingshuige.tangyuan.network.ApiHelper
import com.squareup.picasso.Picasso

class PostCardAdapter : RecyclerView.Adapter<PostCardAdapter.ViewHolder>() {

    private var postInfoList = mutableListOf<PostInfo>()
    private var listener: OnItemClickListener? = null
    private var isSectionVisible = true
    private var isCategoryVisible = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.view_post_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = postInfoList[position]

        // 基本信息显示
        holder.nicknameView.text = post.userNickname
        Picasso.get()
            .load(ApiHelper.getFullImageURL(post.userAvatarGUID))
            .transform(CircleTransform())
            .into(holder.avatar)

        holder.postPreviewView.text = post.textContent

        // 板块信息
        val sectionNameRes = when (post.sectionId) {
            0 -> R.string.notice
            1 -> R.string.menu_normalchat
            2 -> R.string.menu_chitchat
            else -> R.string.menu_normalchat
        }
        holder.textSectionName.setText(sectionNameRes)
        holder.categoryView.text = post.categoryName

        // 板块和领域可见性控制
        val sectionVisibility = if (isSectionVisible) View.VISIBLE else View.GONE
        val categoryVisibility = if (isCategoryVisible) View.VISIBLE else View.GONE

        holder.imgSectionIcon.visibility = sectionVisibility
        holder.textSectionName.visibility = sectionVisibility
        holder.imgCategoryIcon.visibility = categoryVisibility
        holder.categoryView.visibility = categoryVisibility

        // 重置图片可见性 (处理 ViewHolder 复用问题)
        holder.imageLayout.visibility = View.GONE
        holder.imageView1.visibility = View.GONE
        holder.imageView2.visibility = View.GONE
        holder.imageView3.visibility = View.GONE

        // 加载图片
        val images = listOf(post.image1GUID, post.image2GUID, post.image3GUID)
        val imageViews = listOf(holder.imageView1, holder.imageView2, holder.imageView3)

        var hasImages = false
        images.forEachIndexed { index, imageGuid ->
            if (imageGuid != null) {
                hasImages = true
                imageViews[index].visibility = View.VISIBLE
                Picasso.get()
                    .load(ApiHelper.getFullImageURL(imageGuid))
                    .resize(800, 0)
                    .centerCrop()
                    .placeholder(R.drawable.img_placeholder)
                    .into(imageViews[index])
            }
        }

        if (hasImages) {
            holder.imageLayout.visibility = View.VISIBLE
        }

        // 时间显示
        holder.dateTimeView.text = DataTools.getLocalFriendlyDateTime(post.postDate, holder.itemView.context)

        // 点击事件
        holder.itemView.setOnClickListener {
            listener?.onItemClick(postInfoList[holder.adapterPosition].postId)
        }
    }

    override fun getItemCount(): Int = postInfoList.size

    // 接口和方法
    fun interface OnItemClickListener {
        fun onItemClick(postId: Int)
    }

    fun setOnItemClickListener(callback: OnItemClickListener) {
        this.listener = callback
    }

    fun setSectionVisible(sectionVisible: Boolean) {
        isSectionVisible = sectionVisible
    }

    fun setCategoryVisible(categoryVisible: Boolean) {
        isCategoryVisible = categoryVisible
    }

    fun appendData(data: PostInfo): Boolean {
        // 检查是否已存在
        if (postInfoList.any { it.postId == data.postId }) {
            return false
        }
        postInfoList.add(data)
        notifyItemInserted(postInfoList.size - 1)
        return true
    }

    fun prependData(data: PostInfo): Boolean {
        // 检查是否已存在
        if (postInfoList.any { it.postId == data.postId }) {
            return false
        }
        postInfoList.add(0, data)
        notifyItemInserted(0)
        return true
    }

    fun prependDataset(dataset: List<PostInfo>) {
        dataset.forEach { prependData(it) }
    }

    fun replaceDataSet(dataSet: List<PostInfo>): Boolean {
        postInfoList = dataSet.toMutableList()
        notifyDataSetChanged()
        return true
    }

    fun getAllPostIds(): List<Int> = postInfoList.map { it.postId }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: ImageView = itemView.findViewById(R.id.avatarView)
        val nicknameView: TextView = itemView.findViewById(R.id.nicknameView)
        val postPreviewView: TextView = itemView.findViewById(R.id.postPreviewView)
        val dateTimeView: TextView = itemView.findViewById(R.id.postDateTimeView)
        val imgSectionIcon: ImageView = itemView.findViewById(R.id.imgSectionIcon)
        val textSectionName: TextView = itemView.findViewById(R.id.textSectionName)
        val imgCategoryIcon: ImageView = itemView.findViewById(R.id.imgCategoryIcon)
        val categoryView: TextView = itemView.findViewById(R.id.textCategoryName)
        val imageLayout: GridLayout = itemView.findViewById(R.id.imageLayout)
        val imageView1: ImageView = itemView.findViewById(R.id.imageView1)
        val imageView2: ImageView = itemView.findViewById(R.id.imageView2)
        val imageView3: ImageView = itemView.findViewById(R.id.imageView3)
    }
}