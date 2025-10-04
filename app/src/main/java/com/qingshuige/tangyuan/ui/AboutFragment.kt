package com.qingshuige.tangyuan.ui;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.qingshuige.tangyuan.*;
import com.qingshuige.tangyuan.databinding.FragmentAboutBinding;

import org.w3c.dom.Text;

public class AboutFragment extends Fragment {

    private FragmentAboutBinding binding;

    private TextView textVersion;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAboutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        textVersion = root.findViewById(R.id.textVersion);

        textVersion.setText(getAppVersionName());

        return root;
    }

    private String getAppVersionName() {
        try {
            // 使用 getActivity() 获取上下文来访问 PackageManager
            PackageInfo packageInfo = getActivity().getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0);
            return "v" + packageInfo.versionName; // 返回版本名
        } catch (PackageManager.NameNotFoundException e) {
            return getString(R.string.unknown_version); // 如果获取失败，返回默认值
        }
    }
}
