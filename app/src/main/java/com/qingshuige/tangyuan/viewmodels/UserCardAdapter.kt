package com.qingshuige.tangyuan.viewmodels

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.qingshuige.tangyuan.R
import com.qingshuige.tangyuan.data.CircleTransform
import com.qingshuige.tangyuan.network.ApiHelper
import com.squareup.picasso.Picasso

class UserCardAdapter(private val context: Context) :
    RecyclerView.Adapter<UserCardAdapter.ViewHolder>() {

    private var userInfos = mutableListOf<UserInfo>()
    private var onUserClickListener: ItemActionListener? = null
    private var onFollowButtonClickListener: ItemActionListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.view_user_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userInfo = userInfos[position]

        // 头像加载
        Picasso.get()
            .load(ApiHelper.getFullImageURL(userInfo.user.avatarGuid))
            .resize(200, 0)
            .transform(CircleTransform())
            .into(holder.imgAvatar)

        // 昵称
        holder.textNickname.text = userInfo.user.nickName

        // 关注状态
        val followTextRes = if (userInfo.isFollowed) R.string.followed else R.string.follow
        holder.btnFollow.setText(followTextRes)

        // 点击事件
        holder.root.setOnClickListener {
            onUserClickListener?.onAction(userInfo)
        }

        holder.btnFollow.setOnClickListener {
            onFollowButtonClickListener?.onAction(userInfo)
        }
    }

    override fun getItemCount(): Int = userInfos.size

    fun replaceDataset(list: List<UserInfo>) {
        userInfos = list.toMutableList()
        notifyDataSetChanged()
    }

    fun setOnUserClickListener(listener: ItemActionListener) {
        this.onUserClickListener = listener
    }

    fun setOnFollowButtonClickListener(listener: ItemActionListener) {
        this.onFollowButtonClickListener = listener
    }

    fun interface ItemActionListener {
        fun onAction(info: UserInfo)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAvatar: ImageView = itemView.findViewById(R.id.imgAvatar)
        val textNickname: TextView = itemView.findViewById(R.id.textNickname)
        val btnFollow: MaterialButton = itemView.findViewById(R.id.btnFollow)
        val root: LinearLayout = itemView.findViewById(R.id.lnlUserCardRoot)
    }
}