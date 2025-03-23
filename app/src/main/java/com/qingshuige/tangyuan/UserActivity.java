package com.qingshuige.tangyuan;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.qingshuige.tangyuan.network.ApiHelper;
import com.qingshuige.tangyuan.network.User;
import com.squareup.picasso.Picasso;

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

                    //TODO:postList
                });
            }

            @Override
            public void onFailure(Call<User> call, Throwable throwable) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}