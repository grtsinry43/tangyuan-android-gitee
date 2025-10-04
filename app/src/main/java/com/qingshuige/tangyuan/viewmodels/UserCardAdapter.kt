package com.qingshuige.tangyuan.viewmodels;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.qingshuige.tangyuan.R;
import com.qingshuige.tangyuan.data.CircleTransform;
import com.qingshuige.tangyuan.network.ApiHelper;
import com.qingshuige.tangyuan.network.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class UserCardAdapter extends RecyclerView.Adapter<UserCardAdapter.ViewHolder> {
    List<UserInfo> userInfos;
    Context context;

    private ItemActionListener onUserClickListener;
    private ItemActionListener onFollowButtonClickListener;

    public UserCardAdapter(Context context) {
        userInfos = new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public UserCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_user_card, parent, false);
        return new UserCardAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserCardAdapter.ViewHolder holder, int position) {
        UserInfo ui = userInfos.get(position);

        //Avatar
        Picasso.get()
                .load(ApiHelper.getFullImageURL(ui.getUser().avatarGuid))
                .resize(200, 0)
                .transform(new CircleTransform())
                .into(holder.getImgAvatar());

        //Nickname
        holder.getTextNickname().setText(ui.getUser().nickName);

        //isFollowed
        holder.getBtnFollow().setText(context.getString(ui.isFollowed() ? R.string.followed : R.string.follow));

        //点击事件
        if (onUserClickListener != null) {
            holder.getRoot().setOnClickListener(view -> onUserClickListener.onAction(ui));
        }
        if (onFollowButtonClickListener != null) {
            holder.getBtnFollow().setOnClickListener(view -> onFollowButtonClickListener.onAction(ui));
        }
    }

    @Override
    public int getItemCount() {
        return userInfos.size();
    }

    public void replaceDataset(List<UserInfo> list) {
        userInfos = list;
        notifyDataSetChanged();
    }

    public void setOnUserClickListener(ItemActionListener onUserClickListener) {
        this.onUserClickListener = onUserClickListener;
    }

    public void setOnFollowButtonClickListener(ItemActionListener onFollowButtonClickListener) {
        this.onFollowButtonClickListener = onFollowButtonClickListener;
    }

    public interface ItemActionListener {
        void onAction(UserInfo info);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgAvatar;
        private TextView textNickname;
        private MaterialButton btnFollow;
        private LinearLayout root;

        public ViewHolder(@NonNull View view) {
            super(view);

            imgAvatar = view.findViewById(R.id.imgAvatar);
            textNickname = view.findViewById(R.id.textNickname);
            btnFollow = view.findViewById(R.id.btnFollow);
            root = view.findViewById(R.id.lnlUserCardRoot);
        }

        public ImageView getImgAvatar() {
            return imgAvatar;
        }

        public TextView getTextNickname() {
            return textNickname;
        }

        public MaterialButton getBtnFollow() {
            return btnFollow;
        }

        public LinearLayout getRoot() {
            return root;
        }
    }
}
