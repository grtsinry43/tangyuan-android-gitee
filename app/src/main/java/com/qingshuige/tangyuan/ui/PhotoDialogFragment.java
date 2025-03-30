package com.qingshuige.tangyuan.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.qingshuige.tangyuan.R;

public class PhotoDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View root = inflater.inflate(R.layout.photo_dialog_fragment, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(root);

        Drawable drawable = new BitmapDrawable((Bitmap) getArguments().getParcelable("bitmap"));

        ((ImageView) root.findViewById(R.id.photoView)).setImageDrawable(drawable);

        return builder.create();
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
