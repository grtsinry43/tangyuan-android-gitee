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

class NotificationCardAdapter : RecyclerView.Adapter<NotificationCardAdapter.ViewHolder>() {

    private var messages = mutableListOf<NotificationInfo>()
    private var onItemClickListener: ItemActionListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_message_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val info = messages[position]

        // 头像
        Picasso.get()
            .load(ApiHelper.getFullImageURL(info.avatarGuid))
            .resize(200, 0)
            .centerCrop()
            .transform(CircleTransform())
            .into(holder.imgAvatar)

        // 标题
        holder.textTitle.text = info.title

        // 类型
        holder.textType.text = info.type
        holder.textType.setTextColor(info.typeColor)

        // 未读指示器
        holder.idcIsRead.visibility = if (info.notification?.isRead == true) View.GONE else View.VISIBLE

        // 消息内容
        holder.textMessage.text = info.message

        // 时间
        info.notification?.createDate?.let { date ->
            holder.textDateTime.text = DataTools.getLocalFriendlyDateTime(date, holder.itemView.context)
        }

        // 点击事件
        holder.mainLayout.setOnClickListener {
            onItemClickListener?.onItemAction(info)
        }
    }

    override fun getItemCount(): Int = messages.size

    fun setDataset(dataset: List<NotificationInfo>) {
        messages = dataset.toMutableList()
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: ItemActionListener) {
        this.onItemClickListener = listener
    }

    fun interface ItemActionListener {
        fun onItemAction(info: NotificationInfo)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar)
        val textTitle: TextView = itemView.findViewById(R.id.textTitle)
        val textMessage: TextView = itemView.findViewById(R.id.textMessage)
        val textDateTime: TextView = itemView.findViewById(R.id.textDateTime)
        val mainLayout: GridLayout = itemView.findViewById(R.id.mainLayout)
        val idcIsRead: View = itemView.findViewById(R.id.idcIsRead)
        val textType: TextView = itemView.findViewById(R.id.textType)
    }
}