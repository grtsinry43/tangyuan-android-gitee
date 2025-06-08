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
import com.qingshuige.tangyuan.network.Comment;
import com.qingshuige.tangyuan.network.PostMetadata;
import com.qingshuige.tangyuan.network.User;
import com.qingshuige.tangyuan.viewmodels.CommentCardAdapter;
import com.qingshuige.tangyuan.viewmodels.CommentInfo;
import com.qingshuige.tangyuan.viewmodels.PostCardAdapter;
import com.qingshuige.tangyuan.viewmodels.PostInfo;
import com.qingshuige.tangyuan.viewmodels.UserCardAdapter;
import com.qingshuige.tangyuan.viewmodels.UserInfo;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private String keyword;

    private Toolbar toolbar;
    private ProgressBar pgBar;

    private RecyclerView rcvPost;
    private TextView textPostSearchTitle;
    private PostCardAdapter postAdapter;

    private RecyclerView rcvUser;
    private TextView textUserSearchTitle;
    private UserCardAdapter userAdapter;

    private RecyclerView rcvComment;
    private TextView textCommentSearchTitle;
    private CommentCardAdapter commentAdapter;

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
        rcvUser = findViewById(R.id.rcvUser);
        textUserSearchTitle = findViewById(R.id.textUserSearchTitle);
        rcvComment = findViewById(R.id.rcvComment);
        textCommentSearchTitle = findViewById(R.id.textCommentSearchTitle);

        toolbar.setTitle(getString(R.string.search) + ": " + keyword);

        DividerItemDecoration div = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);

        //Post
        postAdapter = new PostCardAdapter();
        postAdapter.setOnItemClickListener(this::startPostActivity);
        rcvPost.setAdapter(postAdapter);
        rcvPost.addItemDecoration(div);
        rcvPost.setLayoutManager(new LinearLayoutManager(this));

        //User
        userAdapter = new UserCardAdapter(this);
        rcvUser.setAdapter(userAdapter);
        rcvUser.setLayoutManager(new LinearLayoutManager(this));
        rcvUser.addItemDecoration(div);

        //Comment
        commentAdapter = new CommentCardAdapter();
        rcvComment.setAdapter(commentAdapter);
        rcvComment.setLayoutManager(new LinearLayoutManager(this));
        rcvComment.addItemDecoration(div);

        initializeUI();

    }

    private void initializeUI() {
        new Thread(() -> {
            CountDownLatch latch = new CountDownLatch(3);

            //帖子搜索结果
            TangyuanApplication.getApi().searchPostByKeyword(keyword).enqueue(new Callback<List<PostMetadata>>() {
                @Override
                public void onResponse(Call<List<PostMetadata>> call, Response<List<PostMetadata>> response) {
                    if (response.code() == 200 && response.body() != null) {
                        runOnUiThread(() -> textPostSearchTitle.setText(textPostSearchTitle.getText() + ": " + response.body().size()));
                        ApiHelper.getPostInfoByMetadataFastAsync(response.body(), result -> {
                            if (result != null) {
                                runOnUiThread(() -> {
                                    postAdapter.replaceDataSet(result);
                                });
                            }
                        });
                    } else {
                        runOnUiThread(() -> {
                            textPostSearchTitle.setText(R.string.no_post_result);
                        });
                    }
                    latch.countDown();
                }

                @Override
                public void onFailure(Call<List<PostMetadata>> call, Throwable throwable) {
                    runOnUiThread(() -> {
                        textPostSearchTitle.setText(textPostSearchTitle.getText() + ": " + getString(R.string.network_error));
                    });
                }
            });

            //用户搜索结果
            TangyuanApplication.getApi().searchUserByKeyword(keyword).enqueue(new Callback<List<User>>() {
                @Override
                public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                    if (response.code() == 200 && response.body() != null) {
                        List<User> users = response.body();
                        runOnUiThread(() -> textUserSearchTitle.setText(textUserSearchTitle.getText() + ": " + response.body().size()));
                        ApiHelper.getInfoFastAsync(users, new ApiHelper.UserInfoConstructor(), result -> {
                            if (result != null) {
                                runOnUiThread(() -> {
                                    userAdapter.replaceDataset(result);
                                });
                            }

                        });
                    } else {
                        runOnUiThread(() -> {
                            textUserSearchTitle.setText(R.string.no_user_result);
                        });
                    }
                    latch.countDown();
                }

                @Override
                public void onFailure(Call<List<User>> call, Throwable throwable) {

                }
            });

            //评论搜索结果
            TangyuanApplication.getApi().searchCommentByKeyword(keyword).enqueue(new Callback<List<Comment>>() {
                @Override
                public void onResponse(Call<List<Comment>> call, Response<List<Comment>> response) {
                    if (response.code() == 200 && response.body() != null) {
                        List<Comment> comments = response.body();
                        runOnUiThread(() -> textCommentSearchTitle.setText(textCommentSearchTitle.getText() + ": " + response.body().size()));
                        ApiHelper.getInfoFastAsync(comments, new ApiHelper.CommentInfoConstructor(), result -> {
                            runOnUiThread(() -> commentAdapter.replaceDataset(result));
                            latch.countDown();
                        });
                    } else {
                        runOnUiThread(() -> {
                            textCommentSearchTitle.setText(R.string.no_comment_result);
                        });
                    }
                }

                @Override
                public void onFailure(Call<List<Comment>> call, Throwable throwable) {

                }
            });

            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            runOnUiThread(() -> pgBar.setVisibility(View.GONE));

        }).start();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void startPostActivity(int postId) {
        Intent intent = new Intent(this, PostActivity.class);
        intent.putExtra("postId", postId);
        startActivity(intent);
    }
}