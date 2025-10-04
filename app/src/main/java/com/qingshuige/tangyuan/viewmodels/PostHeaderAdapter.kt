package com.qingshuige.tangyuan.viewmodels

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class PostHeaderAdapter : RecyclerView.Adapter<PostHeaderAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // TODO: 实现具体的布局
        return ViewHolder(View(parent.context))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // TODO: 实现数据绑定
    }

    override fun getItemCount(): Int = 0

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}