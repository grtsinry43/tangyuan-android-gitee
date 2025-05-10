package com.qingshuige.tangyuan.viewmodels;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qingshuige.tangyuan.R;
import com.qingshuige.tangyuan.data.DataTools;
import com.qingshuige.tangyuan.network.ApiHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NotificationCardAdapter extends RecyclerView.Adapter<NotificationCardAdapter.ViewHolder> {

    List<NotificationInfo> messages;

    private ItemActionListener onItemClickListener;

    public NotificationCardAdapter() {
        messages = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_message_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationCardAdapter.ViewHolder holder, int position) {
        NotificationInfo info = messages.get(position);

        //Avatar
        Picasso.get()
                .load(ApiHelper.getFullImageURL(info.getSourceUserAvatarGuid()))
                .resize(200, 0)
                .centerCrop()
                .into(holder.getImgAvatar());
        //Title
        if (info.getTargetCommentId() != 0) {//针对评论评论
            holder.getTextTitle().setText(info.getSourceUserNickname() + holder.getContext().getString(R.string.replied_your_comment));
        } else {
            holder.getTextTitle().setText(info.getSourceUserNickname() + holder.getContext().getString(R.string.commented_your_post));
        }
        //Message
        holder.getTextMessage().setText(info.getSourceCommentContent());
        //DateTime
        holder.getTextDateTime().setText(DataTools.getLocalFriendlyDateTime(info.getDateTime(), holder.getContext()));
        //MainLayout
        holder.getMainLayout().setOnClickListener(view -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemAction(info);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void appendAndSortDesc(NotificationInfo message) {
        messages.add(message);
        messages.sort((notificationInfo, t1) -> t1.getDateTime().compareTo(notificationInfo.getDateTime()));
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(ItemActionListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface ItemActionListener {
        public void onItemAction(NotificationInfo info);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgAvatar;
        private TextView textTitle;
        private TextView textMessage;
        private TextView textDateTime;
        private GridLayout mainLayout;

        private Context context;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            textTitle = itemView.findViewById(R.id.textTitle);
            textMessage = itemView.findViewById(R.id.textMessage);
            textDateTime = itemView.findViewById(R.id.textDateTime);
            mainLayout = itemView.findViewById(R.id.mainLayout);

            context = itemView.getContext();
        }

        public ImageView getImgAvatar() {
            return imgAvatar;
        }

        public TextView getTextTitle() {
            return textTitle;
        }

        public TextView getTextMessage() {
            return textMessage;
        }

        public TextView getTextDateTime() {
            return textDateTime;
        }

        public Context getContext() {
            return context;
        }

        public GridLayout getMainLayout() {
            return mainLayout;
        }
    }
}
