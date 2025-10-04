package com.qingshuige.tangyuan.viewmodels;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qingshuige.tangyuan.R;
import com.qingshuige.tangyuan.network.ApiHelper;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private final List<String> imageGuids;
    private OnItemClickListener onItemClickListener;

    public GalleryAdapter(List<String> imageGuids) {
        this.imageGuids = imageGuids;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gallery_image_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Picasso.get()
                .load(ApiHelper.getFullImageURL(imageGuids.get(position)))
                .resize(1280, 0)
                .centerInside()
                .placeholder(R.drawable.img_placeholder)
                .into(holder.getImageView(), new Callback() {
                    @Override
                    public void onSuccess() {
                        if (onItemClickListener != null) {
                            holder.getImageView().setOnClickListener(view ->
                                    onItemClickListener.onItemClick(holder.getImageView().getDrawable()));
                        }
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return imageGuids.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Drawable drawable);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.navAvatarView);
        }

        public ImageView getImageView() {
            return imageView;
        }
    }
}
