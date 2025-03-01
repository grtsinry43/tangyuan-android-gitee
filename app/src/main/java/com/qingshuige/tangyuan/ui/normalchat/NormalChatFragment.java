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

        recyclerView = (RecyclerView) root.findViewById(R.id.normalchat_recyclerview);
        PostCardAdapter adapter = new PostCardAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        try {
            updateRecyclerView(5);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return root;
    }


    /**
     * @param expectedCount 预期更新的帖子条数，不代表最终更新的条数。
     * @throws IOException
     */
    private void updateRecyclerView(int expectedCount) throws IOException {
        //三层嵌套回调，用以收集三项信息
        //我承认这些代码是非常丑陋的，但我想不出更好的方法

        //第一层：获取随机帖子集合
        TangyuanApplication.getApi().getRandomPostMetadata(expectedCount).enqueue(new Callback<List<PostMetadata>>() {
            @Override
            public void onResponse(Call<List<PostMetadata>> call, Response<List<PostMetadata>> response) {
                Log.i("TY",response.toString());
                List<PostMetadata> metadatas = response.body();
                //对于每一条帖子……
                for (PostMetadata m : metadatas) {
                    //第二层：获取用户信息
                    TangyuanApplication.getApi().getUser(m.userId).enqueue(new Callback<User>() {
                        @Override
                        public void onResponse(Call<User> call, Response<User> response) {
                            User user = response.body();
                            //第三层：获取正文信息
                            TangyuanApplication.getApi().getPostBody(m.postId).enqueue(new Callback<PostBody>() {
                                @Override
                                public void onResponse(Call<PostBody> call, Response<PostBody> response) {
                                    PostBody body = response.body();
                                    //汇总
                                    PostInfo info = new PostInfo(m.postId, user.nickName, m.postDateTime, body.textContent);
                                    Log.i("TY", info.getUserNickname());
                                    Log.i("TY", response.raw().header("Content-Type"));
                                    getActivity().runOnUiThread(() -> {
                                        Log.i("TY", "runOnUiThread() called.");
                                        ((PostCardAdapter) recyclerView.getAdapter()).appendData(info);
                                        Log.i("TY", "appendData() finished.");
                                    });
                                }

                                @Override
                                public void onFailure(Call<PostBody> call, Throwable throwable) {
                                    //TODO
                                }
                            });
                        }

                        @Override
                        public void onFailure(Call<User> call, Throwable throwable) {
                            //TODO
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<PostMetadata>> call, Throwable throwable) {
                //TODO
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}