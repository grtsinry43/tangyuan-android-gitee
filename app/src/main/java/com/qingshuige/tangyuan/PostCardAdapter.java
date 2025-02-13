package com.qingshuige.tangyuan;

import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qingshuige.tangyuan.models.PostMetadata;

import java.util.ArrayList;
import java.util.List;

public class PostCardAdapter extends RecyclerView.Adapter<PostCardAdapter.ViewHolder> {

    private List<PostMetadata> postMetadataList;

    @NonNull
    @Override
    public PostCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.view_post_card,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PostCardAdapter.ViewHolder holder, int position) {
        PostMetadata p=postMetadataList.get(position);

        //UI
        //holder.getNicknameView().setText(p.getUserId());
        holder.getAvatarView().setImageResource(R.drawable.xianliticn_avatar);
        holder.getNicknameView().setText("线粒体XianlitiCN");
        holder.getPostPreviewView().setText("当务之急是找到关键的问题，关键的问题是什么呢？是我们要找到问题的关键，如果在关键的问题，关键的领域，关键的这个环节上，我们找不到那个关键，我们把握抓手不在关键上，我们等于就是说无法解决关键。");
    }

    @Override
    public int getItemCount() {
        return postMetadataList.size();
    }

    public PostCardAdapter(){
        postMetadataList=new ArrayList<PostMetadata>();
    }

    public void appendData(List<PostMetadata> data){
        postMetadataList.addAll(data);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final ImageView avatar;
        private final TextView nicknameView;
        private final TextView postPreviewView;

        public ViewHolder(View view){
            super(view);

            avatar=(ImageView)view.findViewById(R.id.avatarView);
            nicknameView=(TextView) view.findViewById(R.id.nicknameView);
            postPreviewView=(TextView) view.findViewById(R.id.postPreviewView);
        }

        public ImageView getAvatarView(){
            return avatar;
        }

        public TextView getNicknameView(){
            return nicknameView;
        }

        public TextView getPostPreviewView() {
            return postPreviewView;
        }
    }
}
