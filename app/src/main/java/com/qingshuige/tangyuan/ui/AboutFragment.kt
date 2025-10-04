package com.qingshuige.tangyuan.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.qingshuige.tangyuan.R
import com.qingshuige.tangyuan.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {
    private var binding: FragmentAboutBinding? = null

    private var textVersion: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutBinding.inflate(inflater, container, false)
        val root: View = binding!!.getRoot()

        textVersion = root.findViewById<TextView>(R.id.textVersion)

        textVersion!!.text = this.appVersionName

        return root
    }

    private val appVersionName: String
        get() {
            try {
                // 使用 getActivity() 获取上下文来访问 PackageManager
                val packageInfo = requireActivity().packageManager
                    .getPackageInfo(requireActivity().packageName, 0)
                return "v" + packageInfo.versionName // 返回版本名
            } catch (e: PackageManager.NameNotFoundException) {
                return getString(R.string.unknown_version) // 如果获取失败，返回默认值
            }
        }
}
