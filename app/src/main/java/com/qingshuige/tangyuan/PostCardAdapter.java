package com.qingshuige.tangyuan;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qingshuige.tangyuan.viewmodels.PostInfo;

import java.util.ArrayList;
import java.util.List;

public class PostCardAdapter extends RecyclerView.Adapter<PostCardAdapter.ViewHolder> {

    private List<PostInfo> postInfoList;

    @NonNull
    @Override
    public PostCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.view_post_card,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PostCardAdapter.ViewHolder holder, int position) {
        PostInfo p= postInfoList.get(position);
        Log.i("TY","Binding data: PostId "+p.getPostId());
        //UI
        holder.getNicknameView().setText(p.getUserNickname());
        holder.getAvatarView().setImageResource(R.drawable.xianliticn_avatar);
        holder.getPostPreviewView().setText(p.getTextContent());
    }

    @Override
    public int getItemCount() {
        return postInfoList.size();
    }

    public PostCardAdapter(){
        postInfoList =new ArrayList<PostInfo>();
    }

    public void appendData(List<PostInfo> data){
        postInfoList.addAll(data);
        Log.i("TY","Appending data: "+data.size());
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
