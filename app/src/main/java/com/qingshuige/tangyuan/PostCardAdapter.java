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
        holder.getNicknameView().setText(p.getUserId());
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public PostCardAdapter(){
        postMetadataList=new ArrayList<PostMetadata>();
    }

    public void appendData(List<PostMetadata> data){
        postMetadataList.addAll(data);
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
