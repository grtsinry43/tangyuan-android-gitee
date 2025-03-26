package com.qingshuige.tangyuan;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.qingshuige.tangyuan.data.DataTools;
import com.qingshuige.tangyuan.network.ApiHelper;
import com.qingshuige.tangyuan.network.User;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileEditActivity extends AppCompatActivity {

    private Menu menu;
    private int userId;

    private ImageView editAvatar;
    private EditText editNickname;
    private EditText editBio;
    private EditText editEmail;
    private TextView textRegion;
    private TextView textPhoneNumber;

    private ProgressBar pgBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile_edit);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        editAvatar = findViewById(R.id.editAvatar);
        editNickname = findViewById(R.id.editNickname);
        editBio = findViewById(R.id.editBio);
        editEmail = findViewById(R.id.editEmail);
        textRegion = findViewById(R.id.textRegion);
        textPhoneNumber = findViewById(R.id.textPhoneNumber);

        pgBar = findViewById(R.id.progressBar);

        userId = getIntent().getIntExtra("userId", 0);

        initializeUI(userId);

    }

    private void initializeUI(int userId) {
        TangyuanApplication.getApi().getUser(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                User user = response.body();
                runOnUiThread(() -> {
                    Picasso.get()
                            .load(ApiHelper.getFullImageURL(user.avatarGuid))
                            .resize(200, 0)
                            .centerCrop()
                            .placeholder(R.drawable.img_placeholder)
                            .into(editAvatar);
                    editNickname.setText(user.nickName);
                    editBio.setText(user.bio);
                    editEmail.setText(user.email);
                    textRegion.setText(user.isoRegionName);
                    textPhoneNumber.setText(user.phoneNumber);

                    pgBar.setVisibility(View.GONE);
                });
            }

            @Override
            public void onFailure(Call<User> call, Throwable throwable) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_user_profile_menu, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}