package com.qingshuige.tangyuan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qingshuige.tangyuan.network.ApiHelper;
import com.qingshuige.tangyuan.network.PostMetadata;
import com.qingshuige.tangyuan.viewmodels.PostCardAdapter;
import com.qingshuige.tangyuan.viewmodels.PostInfo;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private String keyword;

    private Toolbar toolbar;
    private RecyclerView rcvPost;
    private ProgressBar pgBar;
    private TextView textPostSearchTitle;

    private PostCardAdapter postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        keyword = getIntent().getStringExtra("keyword");

        toolbar = findViewById(R.id.toolbar);
        rcvPost = findViewById(R.id.rcvPost);
        pgBar = findViewById(R.id.pgBar);
        textPostSearchTitle = findViewById(R.id.textPostSearchTitle);

        toolbar.setTitle(getString(R.string.search) + ": " + keyword);

        postAdapter = new PostCardAdapter();
        postAdapter.setOnItemClickListener(this::startPostActivity);
        DividerItemDecoration div = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        rcvPost.setAdapter(postAdapter);
        rcvPost.addItemDecoration(div);
        rcvPost.setLayoutManager(new LinearLayoutManager(this));

        initializeUI();

    }

    private void initializeUI() {
        //帖子搜索结果
        TangyuanApplication.getApi().searchPostByKeyword(keyword).enqueue(new Callback<List<PostMetadata>>() {
            @Override
            public void onResponse(Call<List<PostMetadata>> call, Response<List<PostMetadata>> response) {
                if (response.code() == 200 && response.body() != null) {
                    runOnUiThread(() -> textPostSearchTitle.setText(textPostSearchTitle.getText() + ": " + response.body().size()));
                    ApiHelper.getPostInfoByMetadataFastAsync(response.body(), result ->
                            runOnUiThread(() -> {
                                postAdapter.replaceDataSet(result);
                                pgBar.setVisibility(View.GONE);
                            }));
                } else {
                    runOnUiThread(() -> {
                        pgBar.setVisibility(View.GONE);
                        textPostSearchTitle.setText(R.string.no_post_result);
                    });
                }
            }

            @Override
            public void onFailure(Call<List<PostMetadata>> call, Throwable throwable) {

            }
        });
    }

    private void startPostActivity(int postId) {
        Intent intent = new Intent(this, PostActivity.class);
        intent.putExtra("postId", postId);
        startActivity(intent);
    }
}