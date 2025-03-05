package com.qingshuige.tangyuan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qingshuige.tangyuan.network.ApiHelper;
import com.qingshuige.tangyuan.viewmodels.PostInfo;
import com.squareup.picasso.Picasso;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PostCardAdapter extends RecyclerView.Adapter<PostCardAdapter.ViewHolder> {

    private List<PostInfo> postInfoList;
    private OnItemClickListener listener;

    @NonNull
    @Override
    public PostCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_post_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PostCardAdapter.ViewHolder holder, int position) {
        PostInfo p = postInfoList.get(position);

        //UI
        holder.getNicknameView().setText(p.getUserNickname());
        holder.getAvatarView().setImageResource(R.drawable.xianliticn_avatar);
        holder.getPostPreviewView().setText(p.getTextContent());
        ///图片处理
        if (p.getImage1GUID() != null) {
            Picasso.get()
                    .load(ApiHelper.getFullImageURL(p.getImage1GUID()))
                    .into(holder.getImageView1());
        }
        if (p.getImage2GUID() != null) {
            Picasso.get()
                    .load(ApiHelper.getFullImageURL(p.getImage2GUID()))
                    .into(holder.getImageView1());
        }
        if (p.getImage3GUID() != null) {
            Picasso.get()
                    .load(ApiHelper.getFullImageURL(p.getImage3GUID()))
                    .into(holder.getImageView1());
        }
        ///时间处理
        Date date = p.getPostDate();
        ZonedDateTime zdt = date.toInstant().atZone(ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm");
        ZonedDateTime localdt = zdt.withZoneSameInstant(ZoneId.systemDefault());
        String datetimeString = localdt.format(formatter);
        holder.getDateTimeView().setText(datetimeString);
        ///点击事件
        holder.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(postInfoList.get(holder.getAdapterPosition()).getPostId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return postInfoList.size();
    }

    public PostCardAdapter() {
        postInfoList = new ArrayList<PostInfo>();
    }

    public interface OnItemClickListener {
        void onItemClick(int postId);
    }

    public void setOnItemClickListener(OnItemClickListener callback) {
        this.listener = callback;
    }

    public boolean appendData(PostInfo data) {
        for (PostInfo i : postInfoList) {
            if (i.getPostId() == data.getPostId()) {
                return false;
            }
        }
        postInfoList.add(data);
        notifyDataSetChanged();
        return true;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatar;
        private final TextView nicknameView;
        private final TextView postPreviewView;
        private final TextView dateTimeView;
        private final ImageView imageView1;
        private final ImageView imageView2;
        private final ImageView imageView3;
        private final View view;

        public ViewHolder(View view) {
            super(view);

            avatar = (ImageView) view.findViewById(R.id.avatarView);
            nicknameView = (TextView) view.findViewById(R.id.nicknameView);
            postPreviewView = (TextView) view.findViewById(R.id.postPreviewView);
            dateTimeView = (TextView) view.findViewById(R.id.postDateTimeView);
            imageView1 = (ImageView) view.findViewById(R.id.imageView1);
            imageView2 = (ImageView) view.findViewById(R.id.imageView2);
            imageView3 = (ImageView) view.findViewById(R.id.imageView3);
            this.view = view;
        }

        public ImageView getAvatarView() {
            return avatar;
        }

        public TextView getNicknameView() {
            return nicknameView;
        }

        public TextView getPostPreviewView() {
            return postPreviewView;
        }

        public TextView getDateTimeView() {
            return dateTimeView;
        }

        public View getView() {
            return view;
        }

        public ImageView getImageView1() {
            return imageView1;
        }

        public ImageView getImageView2() {
            return imageView2;
        }

        public ImageView getImageView3() {
            return imageView3;
        }
    }
}
