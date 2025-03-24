package com.qingshuige.tangyuan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qingshuige.tangyuan.network.ApiHelper;
import com.qingshuige.tangyuan.network.PostMetadata;
import com.qingshuige.tangyuan.network.User;
import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserActivity extends AppCompatActivity {

    private RecyclerView postList;
    private ImageView avatarView;
    private TextView nicknameView;
    private TextView bioView;
    private Button regionButton;
    private Button mailButton;

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        postList = findViewById(R.id.postList);
        avatarView = findViewById(R.id.avatarView);
        nicknameView = findViewById(R.id.nicknameTextView);
        bioView = findViewById(R.id.bioTextView);
        regionButton = findViewById(R.id.regionButton);
        mailButton = findViewById(R.id.mailButton);

        userId = getIntent().getIntExtra("userId", 0);

        initializeUI(userId);
    }

    private void initializeUI(int userId) {
        //显示基本资料
        TangyuanApplication.getApi().getUser(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User user = response.body();
                runOnUiThread(() -> {
                    Picasso.get().load(ApiHelper.getFullImageURL(user.avatarGuid)).into(avatarView);
                    nicknameView.setText(user.nickName);
                    bioView.setText(user.bio);
                    regionButton.setText(user.isoRegionName);
                    mailButton.setText(user.email);
                });
            }

            @Override
            public void onFailure(Call<User> call, Throwable throwable) {

            }
        });
        //显示所发帖子
        PostCardAdapter adapter = new PostCardAdapter();
        adapter.setOnItemClickListener(postId -> {
            Intent intent = new Intent(this, PostActivity.class);
            intent.putExtra("postId", postId);
            startActivity(intent);
        });
        DividerItemDecoration div = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        postList.addItemDecoration(div);
        postList.setLayoutManager(new LinearLayoutManager(this));
        postList.setAdapter(adapter);
        ///初始刷新
        updateRecyclerView(userId);
    }

    private void updateRecyclerView(int userId) {
        TangyuanApplication.getApi().getMetadatasByUserID(userId).enqueue(new Callback<List<PostMetadata>>() {
            @Override
            public void onResponse(Call<List<PostMetadata>> call, Response<List<PostMetadata>> response) {
                List<PostMetadata> metadatas = response.body();
                //对于每一条帖子……
                for (PostMetadata m : metadatas) {
                    ApiHelper.getPostInfoByIdAsync(m.postId, result ->
                            runOnUiThread(() ->
                                    ((PostCardAdapter) postList.getAdapter()).appendData(result)));
                }
            }

            @Override
            public void onFailure(Call<List<PostMetadata>> call, Throwable throwable) {
                Log.i("TY", "Error: " + throwable.toString());
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}