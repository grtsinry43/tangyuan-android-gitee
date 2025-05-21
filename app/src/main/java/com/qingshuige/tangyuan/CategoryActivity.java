package com.qingshuige.tangyuan;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qingshuige.tangyuan.network.ApiHelper;
import com.qingshuige.tangyuan.network.Category;
import com.qingshuige.tangyuan.network.PostMetadata;
import com.qingshuige.tangyuan.viewmodels.PostCardAdapter;
import com.qingshuige.tangyuan.viewmodels.PostInfo;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryActivity extends AppCompatActivity {

    private TextView textCategoryName;
    private TextView text24hNewPosts;
    private TextView textCategoryDisc;
    private RecyclerView rcvPosts;
    private ProgressBar pgBar;
    private ProgressBar pgBarPostLoad;

    private PostCardAdapter adapter;

    private int categoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_category);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textCategoryName = findViewById(R.id.textCategoryName);
        text24hNewPosts = findViewById(R.id.text24hNewPost);
        textCategoryDisc = findViewById(R.id.textCategoryDisc);
        rcvPosts = findViewById(R.id.rcvCategoryPosts);
        pgBar = findViewById(R.id.pgBar);
        pgBarPostLoad = findViewById(R.id.pgBarPostLoad);

        categoryId = getIntent().getIntExtra("categoryId", 0);

        //设置RecyclerView
        adapter = new PostCardAdapter();
        rcvPosts.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rcvPosts.setLayoutManager(new LinearLayoutManager(this));
        rcvPosts.setAdapter(adapter);

        initializeUI();
    }

    private void initializeUI() {
        //名字和描述
        TangyuanApplication.getApi().getCategory(categoryId).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(Call<Category> call, Response<Category> response) {
                if (response.code() == 200 && response.body() != null) {
                    textCategoryName.setText(response.body().baseName);
                    textCategoryDisc.setText(response.body().baseDescription);
                }
            }

            @Override
            public void onFailure(Call<Category> call, Throwable throwable) {

            }
        });

        //24小时新帖数
        TangyuanApplication.getApi().get24hNewPostCountByCategoryId(categoryId).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.code() == 200 && response.body() != null) {
                    text24hNewPosts.setText(getString(R.string._24hpost) + response.body());
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable throwable) {

            }
        });

        //帖子
        TangyuanApplication.getApi().getAllMetadatasByCategoryId(categoryId).enqueue(new Callback<List<PostMetadata>>() {
            @Override
            public void onResponse(Call<List<PostMetadata>> call, Response<List<PostMetadata>> response) {
                if (response.code() == 200 && response.body() != null) {
                    List<PostMetadata> metadatas = response.body();
                    new Thread(() -> {
                        List<PostInfo> infos = new ArrayList<>();
                        //对于每一条帖子……
                        for (PostMetadata m : metadatas) {
                            PostInfo pi = ApiHelper.getPostInfoById(m.postId);
                            infos.add(pi);
                            //进度条
                            runOnUiThread(() ->
                                    pgBarPostLoad.setProgress((int) Math.floor(100 / metadatas.size()) * infos.size(), true));
                        }
                        if (!infos.isEmpty()) {
                            runOnUiThread(() -> {
                                adapter.replaceDataSet(infos);
                                pgBarPostLoad.setVisibility(View.GONE);
                            });
                        }
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<List<PostMetadata>> call, Throwable throwable) {

            }
        });
    }
}