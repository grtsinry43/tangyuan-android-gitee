package com.qingshuige.tangyuan.ui.normalchat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.qingshuige.tangyuan.databinding.FragmentNormalchatBinding;

public class NormalChatFragment extends Fragment {

    private FragmentNormalchatBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNormalchatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }
}