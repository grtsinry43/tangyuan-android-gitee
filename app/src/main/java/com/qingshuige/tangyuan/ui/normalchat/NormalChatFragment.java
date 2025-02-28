package com.qingshuige.tangyuan.ui.normalchat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qingshuige.tangyuan.PostCardAdapter;
import com.qingshuige.tangyuan.R;
import com.qingshuige.tangyuan.TangyuanApplication;
import com.qingshuige.tangyuan.databinding.FragmentNormalchatBinding;
import com.qingshuige.tangyuan.network.PostBody;
import com.qingshuige.tangyuan.network.PostMetadata;
import com.qingshuige.tangyuan.network.User;
import com.qingshuige.tangyuan.viewmodels.PostInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NormalChatFragment extends Fragment {

    private FragmentNormalchatBinding binding;

    private RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNormalchatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView=(RecyclerView) root.findViewById(R.id.normalchat_recyclerview);
        PostCardAdapter adapter=new PostCardAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        try {
            updateRecyclerView();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return root;
    }

    private void updateRecyclerView() throws IOException {
        TangyuanApplication.getApi().getPostMetadata(1).enqueue(new Callback<PostMetadata>() {
            @Override
            public void onResponse(Call<PostMetadata> call, Response<PostMetadata> response) {
                Log.i("RETR",response.body().postDateTime);
            }

            @Override
            public void onFailure(Call<PostMetadata> call, Throwable throwable) {
                Log.i("RETR",throwable.getMessage());
            }
        });
        //TODO:以上只是一个单元测试
        /*
        PostBody b=TangyuanApplication.getApi().getPostBody(1).execute().body();
        User u=TangyuanApplication.getApi().getUser(1).execute().body();
        PostInfo info=new PostInfo(md.postId,u.nickName,md.postDateTime,b.textContent);
        List<PostInfo> list=new ArrayList<>();
        list.add(info);
        ((PostCardAdapter)recyclerView.getAdapter()).appendData(list);
        */
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}