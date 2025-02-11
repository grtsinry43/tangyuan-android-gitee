package com.qingshuige.tangyuan;

import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PostCardAdapter extends RecyclerView.Adapter<PostCardAdapter.ViewHolder> {
    @NonNull
    @Override
    public PostCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.view_post_card,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PostCardAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private final ImageView avatar;

        public ViewHolder(View view){
            super(view);

            avatar=(ImageView)view.findViewById(R.id.avatarView);
        }

        public ImageView getAvatarView(){
            return avatar;
        }
    }
}
