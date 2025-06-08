package com.qingshuige.tangyuan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.qingshuige.tangyuan.data.CircleTransform;
import com.qingshuige.tangyuan.data.DataTools;
import com.qingshuige.tangyuan.network.ApiHelper;
import com.qingshuige.tangyuan.network.PostMetadata;
import com.qingshuige.tangyuan.network.User;
import com.qingshuige.tangyuan.viewmodels.PostCardAdapter;
import com.qingshuige.tangyuan.viewmodels.PostInfo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserActivity extends AppCompatActivity {

    private RecyclerView postList;
    private ImageView avatarView;
    private TextView nicknameView;
    private TextView bioView;
    private ProgressBar pgBar;
    private Chip chipRegion;
    private Chip chipEmail;

    private int userId;
    private TokenManager tm;

    private Menu menu;

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
        pgBar = findViewById(R.id.pgBar);

        chipRegion = findViewById(R.id.chipRegion);
        chipEmail = findViewById(R.id.chipEmail);

        userId = getIntent().getIntExtra("userId", 0);
        tm = TangyuanApplication.getTokenManager();

        initializeUI();
    }

    private void initializeUI() {
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

        updateProfile();
        ///初始刷新
        updateRecyclerView(userId);
    }

    private void updateProfile() {
        TangyuanApplication.getApi().getUser(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User user = response.body();
                runOnUiThread(() -> {
                    Picasso.get()
                            .load(ApiHelper.getFullImageURL(user.avatarGuid))
                            .transform(new CircleTransform())
                            .into(avatarView);
                    nicknameView.setText(user.nickName);
                    bioView.setText(user.bio);
                    chipRegion.setText(user.isoRegionName);
                    chipEmail.setText(user.email);
                });
            }

            @Override
            public void onFailure(Call<User> call, Throwable throwable) {
                runOnUiThread(() ->
                        Toast.makeText(UserActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void updateRecyclerView(int userId) {
        TangyuanApplication.getApi().getMetadatasByUserID(userId).enqueue(new Callback<List<PostMetadata>>() {
            @Override
            public void onResponse(Call<List<PostMetadata>> call, Response<List<PostMetadata>> response) {
                List<PostMetadata> metadatas = response.body();

                ApiHelper.getPostInfoByMetadataFastAsync(metadatas, result -> {
                    if (result != null) {
                        result.sort((postInfo, t1) -> t1.getPostDate().compareTo(postInfo.getPostDate()));
                        runOnUiThread(() -> {
                            ((PostCardAdapter) postList.getAdapter()).replaceDataSet(result);
                        });
                    } else {
                        runOnUiThread(() ->
                                Toast.makeText(UserActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show());
                    }
                    runOnUiThread(() -> pgBar.setVisibility(View.GONE));
                });
            }

            @Override
            public void onFailure(Call<List<PostMetadata>> call, Throwable throwable) {
                runOnUiThread(() ->
                        Toast.makeText(UserActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuEditProfile) {
            Intent intent = new Intent(this, UserProfileEditActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_user_profile_menu, menu);
        this.menu = menu;
        if (tm.getToken() != null && DataTools.decodeJwtTokenUserId(tm.getToken()) == userId) {
            menu.findItem(R.id.menuEditProfile).setVisible(true);
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateProfile();
    }
}