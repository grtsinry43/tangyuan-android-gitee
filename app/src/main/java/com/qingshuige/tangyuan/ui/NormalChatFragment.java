package com.qingshuige.tangyuan.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.qingshuige.tangyuan.PostActivity;
import com.qingshuige.tangyuan.viewmodels.PostCardAdapter;
import com.qingshuige.tangyuan.R;
import com.qingshuige.tangyuan.TangyuanApplication;
import com.qingshuige.tangyuan.databinding.FragmentNormalchatBinding;
import com.qingshuige.tangyuan.network.ApiHelper;
import com.qingshuige.tangyuan.network.PostMetadata;
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
    private SwipeRefreshLayout swp;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentNormalchatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //swp
        swp = root.findViewById(R.id.swpNormalChat);
        swp.setOnRefreshListener(() -> updateRecyclerView(10));
        swp.setColorSchemeColors(getActivity().getColor(R.color.mazarine_blue),
                getActivity().getColor(R.color.nanohanacha_gold));

        //RecyclerView
        recyclerView = root.findViewById(R.id.normalchat_recyclerview);
        PostCardAdapter adapter = new PostCardAdapter();
        adapter.setSectionVisible(false);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager rcvLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(rcvLayoutManager);
        adapter.setOnItemClickListener(postId -> {
            Intent intent = new Intent(getActivity(), PostActivity.class);
            intent.putExtra("postId", postId);
            startActivity(intent);
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
        swp.setRefreshing(true);
        new Thread(() -> {
            try {
                List<PostMetadata> metadatas = TangyuanApplication.getApi().getRandomPostMetadata(expectedCount).execute().body();
                if (metadatas != null) {
                    List<PostInfo> pis = new ArrayList<>();
                    for (PostMetadata m : metadatas) {
                        if (m.sectionId == 1) {
                            PostInfo pi = ApiHelper.getPostInfoById(m.postId);
                            if (pi != null) {
                                pis.add(pi);
                            }
                        }
                    }
                    new Handler(Looper.getMainLooper()).post(() -> {
                        ((PostCardAdapter) recyclerView.getAdapter()).prependDataset(pis);
                        recyclerView.scrollToPosition(0);
                        swp.setRefreshing(false);
                    });
                }
            } catch (IOException e) {
                swp.setRefreshing(false);
            }
        }).start();

        /*老代码
        TangyuanApplication.getApi().getRandomPostMetadata(expectedCount).enqueue(new Callback<List<PostMetadata>>() {
            @Override
            public void onResponse(Call<List<PostMetadata>> call, Response<List<PostMetadata>> response) {
                if (response.code() == 200 && response.body() != null) {
                    List<PostMetadata> metadatas = response.body();
                    //对于每一条帖子……
                    for (PostMetadata m : metadatas) {
                        if (m.sectionId == 1) {
                            ApiHelper.getPostInfoByIdAsync(m.postId, result -> {
                                if (result != null) {
                                    getActivity().runOnUiThread(() -> {
                                        ((PostCardAdapter) recyclerView.getAdapter()).prependData(result);
                                    });
                                }
                            });
                        }
                    }
                }
                swp.setRefreshing(false);


            }

            @Override
            public void onFailure(Call<List<PostMetadata>> call, Throwable throwable) {
                Log.i("TY", "Error: " + throwable.toString());
            }
        });
         */
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}