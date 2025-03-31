package com.qingshuige.tangyuan.viewmodels;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.qingshuige.tangyuan.R;
import com.qingshuige.tangyuan.data.DataTools;
import com.qingshuige.tangyuan.network.ApiHelper;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CommentCardAdapter extends RecyclerView.Adapter<CommentCardAdapter.ViewHolder> {
    private List<CommentInfo> comments;
    private ItemActionListener onReplyButtonClickListener;
    private ItemActionListener onTextClickListener;
    private ItemActionListener onItemHoldListener;
    private ItemActionListener onAvatarClickListener;

    public CommentCardAdapter() {
        comments = new ArrayList<>();
    }

    @NonNull
    @Override
    public CommentCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_comment_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentCardAdapter.ViewHolder holder, int position) {
        CommentInfo info = comments.get(position);

        //MainLayout
        holder.getMain().setOnLongClickListener(view -> {
            if (onItemHoldListener != null) {
                onItemHoldListener.onAction(info);
                return true;
            }
            return false;
        });
        //Avatar
        Picasso.get()
                .load(ApiHelper.getFullImageURL(info.getUserAvatarGuid()))
                .resize(200, 0)
                .centerCrop()
                .placeholder(R.drawable.img_placeholder)
                .into(holder.getAvatar());
        if (onAvatarClickListener != null) {
            holder.getAvatar().setOnClickListener(view -> onAvatarClickListener.onAction(info));
        }
        //Nickname
        holder.getTextNickname().setText(info.getUserNickname());
        //Text
        holder.getTextComment().setText(info.getCommentText());
        holder.getTextComment().setOnClickListener(view -> {
            if (onTextClickListener != null) {
                onTextClickListener.onAction(info);
            }
        });
        holder.getTextComment().setOnLongClickListener(view -> {
            if (onItemHoldListener != null) {
                onItemHoldListener.onAction(info);
                return true;
            }
            return false;
        });
        //Replies
        holder.getButtonSeeReplies().setVisibility(info.isHasReplies() ? View.VISIBLE : View.GONE);
        holder.getButtonSeeReplies().setOnClickListener(view -> {
            if (onReplyButtonClickListener != null) {
                onReplyButtonClickListener.onAction(info);
            }
        });
        //DateTime
        holder.getTextDateTime().setText(DataTools.getLocalFriendlyDateTime(info.getCommentDateTime(), holder.getContext()));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void appendData(CommentInfo info) {
        for (CommentInfo c : comments) {
            if (c.getCommentId() == info.getCommentId()) {
                return;
            }
        }
        comments.add(info);
        notifyItemInserted(comments.size() - 1);
        sortByDateAscending();
    }

    public void sortByDateAscending() {
        comments.sort(new Comparator<CommentInfo>() {
            @Override
            public int compare(CommentInfo commentInfo, CommentInfo t1) {
                return commentInfo.getCommentDateTime().compareTo(t1.getCommentDateTime());
            }
        });
        notifyDataSetChanged();
    }

    public void clearData() {
        int size = comments.size();
        comments.clear();
        notifyItemRangeRemoved(0, size);
    }

    public void setOnReplyButtonClickListener(ItemActionListener listener) {
        onReplyButtonClickListener = listener;
    }

    public void setOnTextClickListener(ItemActionListener onTextClickListener) {
        this.onTextClickListener = onTextClickListener;
    }

    public void setOnItemHoldListener(ItemActionListener listener) {
        this.onItemHoldListener = listener;
    }

    public void setOnAvatarClickListener(ItemActionListener onAvatarClickListener) {
        this.onAvatarClickListener = onAvatarClickListener;
    }

    public interface ItemActionListener {
        void onAction(CommentInfo info);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView avatar;
        private TextView textNickname;
        private TextView textComment;
        private TextView textDateTime;
        private MaterialButton buttonSeeReplies;
        private GridLayout main;
        private Context context;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            avatar = itemView.findViewById(R.id.avatar);
            textNickname = itemView.findViewById(R.id.textNickname);
            textComment = itemView.findViewById(R.id.textCommentText);
            textDateTime = itemView.findViewById(R.id.textDateTime);
            buttonSeeReplies = itemView.findViewById(R.id.buttonSeeReplies);
            context = itemView.getContext();
            main = itemView.findViewById(R.id.main);
        }

        public ImageView getAvatar() {
            return avatar;
        }

        public TextView getTextNickname() {
            return textNickname;
        }

        public TextView getTextComment() {
            return textComment;
        }

        public TextView getTextDateTime() {
            return textDateTime;
        }

        public MaterialButton getButtonSeeReplies() {
            return buttonSeeReplies;
        }

        public Context getContext() {
            return context;
        }

        public GridLayout getMain() {
            return main;
        }
    }
}
