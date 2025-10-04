package com.qingshuige.tangyuan.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.qingshuige.tangyuan.PostActivity;
import com.qingshuige.tangyuan.R;
import com.qingshuige.tangyuan.TangyuanApplication;
import com.qingshuige.tangyuan.databinding.FragmentChitchatBinding;
import com.qingshuige.tangyuan.databinding.FragmentNormalchatBinding;
import com.qingshuige.tangyuan.network.ApiHelper;
import com.qingshuige.tangyuan.network.PostMetadata;
import com.qingshuige.tangyuan.viewmodels.PostCardAdapter;
import com.qingshuige.tangyuan.viewmodels.PostInfo;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//侃一侃的代码和聊一聊完全是一样的，区别在于显示帖子的sectionId
public class ChitchatFragment extends Fragment {

    private FragmentChitchatBinding binding;

    private RecyclerView recyclerView;
    private PostCardAdapter adapter;
    private SwipeRefreshLayout swp;
    private FloatingActionButton fab;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentChitchatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        //swp
        swp = root.findViewById(R.id.swpChitChat);
        swp.setOnRefreshListener(this::updateRecyclerView);
        swp.setColorSchemeColors(getActivity().getColor(R.color.mazarine_blue),
                getActivity().getColor(R.color.nanohanacha_gold));

        //fab
        fab = root.findViewById(R.id.fabTop);
        fab.setOnClickListener(view -> updateRecyclerView());

        //RecyclerView
        recyclerView = root.findViewById(R.id.chitchat_recyclerview);
        adapter = new PostCardAdapter();
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
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int firstVisiblePosition = rcvLayoutManager.findFirstVisibleItemPosition();
                if (firstVisiblePosition >= 5 && !swp.isRefreshing()) {
                    fab.show();
                } else {
                    fab.hide();
                }

            }
        });

        ///首次更新
        updateRecyclerView();

        return root;
    }


    private void updateRecyclerView() {
        swp.setRefreshing(true);
        fab.hide();

        new Thread(() -> {
            try {
                Response<List<PostMetadata>> response = TangyuanApplication.getApi().phtPostMetadata(2, adapter.getAllPostIds()).execute();
                if (response.code() == 200) {
                    List<PostMetadata> metadatas = response.body();
                    if (metadatas != null) {
                        ApiHelper.getInfoFastAsync(metadatas, new ApiHelper.PostInfoConstructor(), result -> {
                            if (result != null) {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    ((PostCardAdapter) recyclerView.getAdapter()).prependDataset(result);
                                    recyclerView.scrollToPosition(0);
                                });
                            } else {
                                new Handler(Looper.getMainLooper()).post(() ->
                                        Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_SHORT).show());
                            }
                            new Handler(Looper.getMainLooper()).post(() ->
                                    swp.setRefreshing(false));
                        });

                    } else {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                        });
                        swp.setRefreshing(false);
                    }
                } else if (response.code() == 404) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(getContext(), R.string.no_post_to_show, Toast.LENGTH_SHORT).show();
                    });
                    swp.setRefreshing(false);
                }
            } catch (IOException e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(getContext(), R.string.network_error, Toast.LENGTH_SHORT).show();
                });
                swp.setRefreshing(false);
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}