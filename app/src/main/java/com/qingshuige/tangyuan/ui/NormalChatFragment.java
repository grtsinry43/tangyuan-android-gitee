package com.qingshuige.tangyuan.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qingshuige.tangyuan.PostActivity;
import com.qingshuige.tangyuan.PostCardAdapter;
import com.qingshuige.tangyuan.R;
import com.qingshuige.tangyuan.TangyuanApplication;
import com.qingshuige.tangyuan.databinding.FragmentNormalchatBinding;
import com.qingshuige.tangyuan.network.ApiHelper;
import com.qingshuige.tangyuan.network.PostMetadata;

import java.io.IOException;
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

        //RecyclerView
        recyclerView = (RecyclerView) root.findViewById(R.id.normalchat_recyclerview);
        PostCardAdapter adapter = new PostCardAdapter();
        recyclerView.setAdapter(adapter);
        LinearLayoutManager rcvLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(rcvLayoutManager);
        ///监听器
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastVisibleItem = rcvLayoutManager.findLastVisibleItemPosition();
                int totalItemCount = rcvLayoutManager.getItemCount();

                // 滑动到底部
                if (dy > 0 && lastVisibleItem == totalItemCount - 1) {
                    // 用户滑动到列表底部
                    updateRecyclerView(5);
                }
            }
        });
        adapter.setOnItemClickListener(new PostCardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int postId) {
                Intent intent = new Intent(getActivity(), PostActivity.class);
                intent.putExtra("postId", postId);
                startActivity(intent);
            }
        });
        ///装饰线
        DividerItemDecoration divider = new DividerItemDecoration(getActivity(), rcvLayoutManager.getOrientation());
        recyclerView.addItemDecoration(divider);
        ///首次更新
        updateRecyclerView(10);

        return root;
    }


    /**
     * @param expectedCount 预期更新的帖子条数，不代表最终更新的条数。
     * @throws IOException
     */
    private void updateRecyclerView(int expectedCount) {
        TangyuanApplication.getApi().getRandomPostMetadata(expectedCount).enqueue(new Callback<List<PostMetadata>>() {
            @Override
            public void onResponse(Call<List<PostMetadata>> call, Response<List<PostMetadata>> response) {
                List<PostMetadata> metadatas = response.body();
                //对于每一条帖子……
                for (PostMetadata m : metadatas) {
                    ApiHelper.getPostInfoByIdAsync(m.postId, result ->
                            getActivity().runOnUiThread(() ->
                                    ((PostCardAdapter) recyclerView.getAdapter()).appendData(result)));
                }
            }

            @Override
            public void onFailure(Call<List<PostMetadata>> call, Throwable throwable) {
                Log.i("TY", "Error: " + throwable.toString());
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}