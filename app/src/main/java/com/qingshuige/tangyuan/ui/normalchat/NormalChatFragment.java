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
        //三层嵌套回调，用以收集三项信息
        //我承认这些代码是非常丑陋的，但我想不出更好的方法
        TangyuanApplication.getApi().getPostMetadata(1).enqueue(new Callback<PostMetadata>() {
            @Override
            public void onResponse(Call<PostMetadata> call, Response<PostMetadata> response) {
                PostMetadata metadata=response.body();
                TangyuanApplication.getApi().getUser(metadata.userId).enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        User user= response.body();
                        TangyuanApplication.getApi().getPostBody(metadata.postId).enqueue(new Callback<PostBody>() {
                            @Override
                            public void onResponse(Call<PostBody> call, Response<PostBody> response) {
                                PostBody body=response.body();
                                PostInfo info=new PostInfo(metadata.postId, user.nickName,metadata.postDateTime, body.textContent);
                                Log.i("TY",info.getUserNickname());
                                Log.i("TY",response.raw().header("Content-Type"));
                                List<PostInfo> pl=new ArrayList<>();
                                pl.add(info);
                                getActivity().runOnUiThread(()->{
                                    Log.i("TY","runOnUiThread() called.");
                                    ((PostCardAdapter)recyclerView.getAdapter()).appendData(pl);
                                    Log.i("TY","appendData() finished.");
                                });
                            }

                            @Override
                            public void onFailure(Call<PostBody> call, Throwable throwable) {

                            }
                        });
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable throwable) {

                    }
                });
            }

            @Override
            public void onFailure(Call<PostMetadata> call, Throwable throwable) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}