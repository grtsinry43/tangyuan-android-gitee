package com.qingshuige.tangyuan.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.github.chrisbanes.photoview.PhotoView;
import com.qingshuige.tangyuan.R;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class PhotoDialogFragment extends DialogFragment {

    Drawable drawable;
    Bitmap bitmap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.photo_dialog_fragment, container, false);

        bitmap = getArguments().getParcelable("bitmap");
        drawable = new BitmapDrawable(bitmap);

        ((PhotoView) root.findViewById(R.id.photoView)).setImageDrawable(drawable);
        root.findViewById(R.id.buttonSavePhoto).setOnClickListener(view -> savePhotoToGallery());

        return root;
    }

    private void savePhotoToGallery() {
        // 创建ContentValues来存储图片元数据
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "TY_" + UUID.randomUUID().toString() + ".jpg");  // 文件名
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); // 文件类型
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES); // 存储路径
        values.put(MediaStore.Images.Media.IS_PENDING, 1); // 标记为正在处理

        // 获取ContentResolver
        ContentResolver resolver = getContext().getContentResolver();

        // 插入图片到MediaStore
        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try {
            if (imageUri != null) {
                // 获取输出流并写入图片
                OutputStream outputStream = resolver.openOutputStream(imageUri);
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();
                }

                // 更新状态，标记完成
                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                resolver.update(imageUri, values, null, null);
                Toast.makeText(getContext(), R.string.photo_saved, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.failed_to_save_photo, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(R.color.black);
            getDialog().getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            getDialog().getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
            getDialog().getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
    }

    public static PhotoDialogFragment create(Bitmap bitmap) {
        PhotoDialogFragment fragment = new PhotoDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("bitmap", bitmap);
        fragment.setArguments(args);
        return fragment;
    }

    private PhotoDialogFragment() {
    }
}
