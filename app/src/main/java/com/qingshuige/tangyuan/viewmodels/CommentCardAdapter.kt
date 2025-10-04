package com.qingshuige.tangyuan.viewmodels

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.qingshuige.tangyuan.R
import com.qingshuige.tangyuan.data.CircleTransform
import com.qingshuige.tangyuan.data.DataTools
import com.qingshuige.tangyuan.network.ApiHelper
import com.squareup.picasso.Picasso

class CommentCardAdapter : RecyclerView.Adapter<CommentCardAdapter.ViewHolder>() {

    private var comments = mutableListOf<CommentInfo>()
    private var onReplyButtonClickListener: ItemActionListener? = null
    private var onTextClickListener: ItemActionListener? = null
    private var onItemHoldListener: ItemActionListener? = null
    private var onAvatarClickListener: ItemActionListener? = null
    private var isReplyButtonVisible = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_comment_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val info = comments[position]

        // 主布局长按事件
        holder.main.setOnLongClickListener {
            onItemHoldListener?.onAction(info)
            true
        }

        // 头像
        Picasso.get()
            .load(ApiHelper.getFullImageURL(info.userAvatarGuid))
            .resize(200, 0)
            .centerCrop()
            .placeholder(R.drawable.img_placeholder)
            .transform(CircleTransform())
            .into(holder.avatar)

        holder.avatar.setOnClickListener {
            onAvatarClickListener?.onAction(info)
        }

        // 昵称
        holder.textNickname.text = info.userNickname

        // 评论内容
        holder.textComment.text = info.commentText
        holder.textComment.setOnClickListener {
            onTextClickListener?.onAction(info)
        }
        holder.textComment.setOnLongClickListener {
            onItemHoldListener?.onAction(info)
            true
        }

        // 回复按钮
        val shouldShowReplies = info.isHasReplies && isReplyButtonVisible
        holder.buttonSeeReplies.visibility = if (shouldShowReplies) View.VISIBLE else View.GONE
        holder.buttonSeeReplies.setOnClickListener {
            onReplyButtonClickListener?.onAction(info)
        }

        // 时间
        holder.textDateTime.text = DataTools.getLocalFriendlyDateTime(info.commentDateTime, holder.itemView.context)
    }

    override fun getItemCount(): Int = comments.size

    fun getPositionOf(info: CommentInfo): Int = comments.indexOf(info)

    fun appendData(info: CommentInfo) {
        // 检查是否已存在
        if (comments.any { it.commentId == info.commentId }) {
            return
        }
        comments.add(info)
        notifyItemInserted(comments.size - 1)
        sortByDateAscending()
    }

    fun sortByDateAscending() {
        comments.sortBy { it.commentDateTime }
        notifyDataSetChanged()
    }

    fun replaceDataset(list: List<CommentInfo>) {
        comments = list.toMutableList()
        notifyDataSetChanged()
    }

    fun clearData() {
        val size = comments.size
        comments.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun setOnReplyButtonClickListener(listener: ItemActionListener) {
        onReplyButtonClickListener = listener
    }

    fun setOnTextClickListener(listener: ItemActionListener) {
        this.onTextClickListener = listener
    }

    fun setOnItemHoldListener(listener: ItemActionListener) {
        this.onItemHoldListener = listener
    }

    fun setOnAvatarClickListener(listener: ItemActionListener) {
        this.onAvatarClickListener = listener
    }

    fun setReplyButtonVisible(replyButtonVisible: Boolean) {
        isReplyButtonVisible = replyButtonVisible
    }

    fun interface ItemActionListener {
        fun onAction(info: CommentInfo)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: ImageView = itemView.findViewById(R.id.avatar)
        val textNickname: TextView = itemView.findViewById(R.id.textNickname)
        val textComment: TextView = itemView.findViewById(R.id.textCommentText)
        val textDateTime: TextView = itemView.findViewById(R.id.textDateTime)
        val buttonSeeReplies: MaterialButton = itemView.findViewById(R.id.buttonSeeReplies)
        val main: GridLayout = itemView.findViewById(R.id.main)
    }
}