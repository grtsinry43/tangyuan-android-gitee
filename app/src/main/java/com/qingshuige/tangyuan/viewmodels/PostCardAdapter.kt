package com.qingshuige.tangyuan.viewmodels;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qingshuige.tangyuan.R;
import com.qingshuige.tangyuan.data.CircleTransform;
import com.qingshuige.tangyuan.data.DataTools;
import com.qingshuige.tangyuan.network.ApiHelper;
import com.squareup.picasso.Picasso;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class PostCardAdapter extends RecyclerView.Adapter<PostCardAdapter.ViewHolder> {

    private List<PostInfo> postInfoList;
    private OnItemClickListener listener;
    private boolean isSectionVisible = true;
    private boolean isCategoryVisible = true;

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
        Picasso.get()
                .load(ApiHelper.getFullImageURL(p.getUserAvatarGUID()))
                .transform(new CircleTransform())
                .into(holder.getAvatarView());
        holder.getPostPreviewView().setText(p.getTextContent());
        switch (p.getSectionId()) {
            case 0:
                holder.getTextSectionName().setText(R.string.notice);
                break;
            case 1:
                holder.getTextSectionName().setText(R.string.menu_normalchat);
                break;
            case 2:
                holder.getTextSectionName().setText(R.string.menu_chitchat);
                break;
        }
        holder.getCategoryView().setText(p.getCategoryName());
        ///板块和领域可见性
        holder.getImgSectionIcon().setVisibility(isSectionVisible ? View.VISIBLE : View.GONE);
        holder.getTextSectionName().setVisibility(isSectionVisible ? View.VISIBLE : View.GONE);
        holder.getImgCategoryIcon().setVisibility(isCategoryVisible ? View.VISIBLE : View.GONE);
        holder.getCategoryView().setVisibility(isCategoryVisible ? View.VISIBLE : View.GONE);
        ///图片处理
        holder.getImageLayout().setVisibility(View.GONE);
        holder.getImageView1().setVisibility(View.GONE);
        holder.getImageView2().setVisibility(View.GONE);
        holder.getImageView3().setVisibility(View.GONE);
        ////这些主要是考虑ViewHolder的复用问题，
        ////假设拿到的是一个被复用的ViewHolder，恰好之前又填充过图片，那么就有可能出现
        ////图片出现在没有图片的帖子上，因此要重置可见性
        if (p.getImage1GUID() != null) {
            holder.imageLayout.setVisibility(ViewGroup.VISIBLE);
            holder.getImageView1().setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(ApiHelper.getFullImageURL(p.getImage1GUID()))
                    .resize(800, 0)
                    .centerCrop()
                    .placeholder(R.drawable.img_placeholder)
                    .into(holder.getImageView1());
        }
        if (p.getImage2GUID() != null) {
            holder.getImageView2().setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(ApiHelper.getFullImageURL(p.getImage2GUID()))
                    .resize(800, 0)
                    .centerCrop()
                    .placeholder(R.drawable.img_placeholder)
                    .into(holder.getImageView2());
        }
        if (p.getImage3GUID() != null) {
            holder.getImageView3().setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(ApiHelper.getFullImageURL(p.getImage3GUID()))
                    .resize(800, 0)
                    .centerCrop()
                    .placeholder(R.drawable.img_placeholder)
                    .into(holder.getImageView3());
        }
        //有一种手动控制引用计数的美
        ///时间处理
        holder.getDateTimeView().setText(DataTools.getLocalFriendlyDateTime(p.getPostDate(), holder.getView().getContext()));
        ///点击事件
        if (listener != null) {
            holder.getView().setOnClickListener(view ->
                    listener.onItemClick(postInfoList.get(holder.getAdapterPosition()).getPostId()));
        }
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

    public void setSectionVisible(boolean sectionVisible) {
        isSectionVisible = sectionVisible;
    }

    public void setCategoryVisible(boolean categoryVisible) {
        isCategoryVisible = categoryVisible;
    }

    public boolean appendData(PostInfo data) {
        for (PostInfo i : postInfoList) {
            if (i.getPostId() == data.getPostId()) {
                return false;
            }
        }
        postInfoList.add(data);
        notifyItemInserted(postInfoList.size() - 1);
        return true;
    }

    //TODO: 这几个要关注一下线程锁问题
    public boolean prependData(PostInfo data) {
        for (PostInfo i : postInfoList) {
            if (i.getPostId() == data.getPostId()) {
                return false;
            }
        }
        postInfoList.add(0, data);
        notifyItemInserted(0);
        return true;
    }

    public void prependDataset(List<PostInfo> dataset) {
        for (PostInfo i : dataset) {
            prependData(i);
        }
    }

    public boolean replaceDataSet(List<PostInfo> dataSet) {
        postInfoList = dataSet;
        notifyDataSetChanged();
        return true;
    }

    public List<Integer> getAllPostIds() {
        List<Integer> ids = new ArrayList<>();
        for (PostInfo p : postInfoList) {
            ids.add(p.getPostId());
        }
        return ids;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatar;
        private final TextView nicknameView;
        private final TextView postPreviewView;
        private final TextView dateTimeView;
        private final ImageView imgSectionIcon;
        private final TextView textSectionName;
        private final ImageView imgCategoryIcon;
        private final TextView categoryView;
        private final GridLayout imageLayout;
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
            imgSectionIcon = view.findViewById(R.id.imgSectionIcon);
            textSectionName = view.findViewById(R.id.textSectionName);
            imgCategoryIcon = view.findViewById(R.id.imgCategoryIcon);
            categoryView = view.findViewById(R.id.textCategoryName);
            imageLayout = (GridLayout) view.findViewById(R.id.imageLayout);
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

        public GridLayout getImageLayout() {
            return imageLayout;
        }

        public TextView getCategoryView() {
            return categoryView;
        }

        public ImageView getImgSectionIcon() {
            return imgSectionIcon;
        }

        public TextView getTextSectionName() {
            return textSectionName;
        }

        public ImageView getImgCategoryIcon() {
            return imgCategoryIcon;
        }
    }
}
