package com.qingshuige.tangyuan;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.activity.EdgeToEdge;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.qingshuige.tangyuan.data.DataTools;
import com.qingshuige.tangyuan.databinding.ActivityMainBinding;
import com.qingshuige.tangyuan.network.ApiHelper;
import com.qingshuige.tangyuan.network.PostMetadata;
import com.qingshuige.tangyuan.network.User;
import com.qingshuige.tangyuan.viewmodels.PostInfo;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.FieldMap;

/*
 *
 *  在科学上没有平坦的大道，
 *  只有不畏劳苦沿着陡峭山路攀登的人，
 *  才有希望达到光辉的顶点。
 *              ——卡尔·马克思
 */

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    ///private ActivityMainBinding binding;
    private TokenManager tm;
    private View navHeaderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);

        ///binding = ActivityMainBinding.inflate(getLayoutInflater());
        tm = TangyuanApplication.getTokenManager();

        //设置内容
        setContentView(R.layout.activity_main);

        ///setSupportActionBar(binding.appBarMain.toolbar);
        setSupportActionBar(findViewById(R.id.toolbar));

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navHeaderView = navigationView.getHeaderView(0);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_normalchat, R.id.nav_chitchat, R.id.nav_message, R.id.nav_about)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navHeaderView.findViewById(R.id.navAvatarView).setOnClickListener(view -> {
            if (tm.getToken() == null) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(MainActivity.this, UserActivity.class);
                intent.putExtra("userId", DataTools.decodeJwtTokenUserId(tm.getToken()));
                startActivity(intent);
            }
        });

        //蒲公英更新
        //apiKey:133d8c604b4d0772723a007a9ad213f7
        //appKey:123a9eba5d424ab9088069505ffeb1de
        Map<String, String> params = new HashMap<>();
        params.put("_api_key", "133d8c604b4d0772723a007a9ad213f7");
        params.put("appKey", "123a9eba5d424ab9088069505ffeb1de");
        try {
            params.put("buildVersion", getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        TangyuanApplication.getApi().checkUpdate(params).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.body().getAsJsonObject("data").get("buildHaveNewVersion").getAsBoolean()) {
                    runOnUiThread(() -> {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("糖原公测阶段")
                                .setMessage("检测到新版本，请及时更新。")
                                .setPositiveButton("下载", (dialogInterface, i) -> {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setData(Uri.parse(response.body().getAsJsonObject("data").get("downloadURL").getAsString()));
                                    // 启动浏览器
                                    startActivity(intent);
                                })
                                .show();
                    });
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.new_post_button) {
            //判断是否登录
            if (tm.getToken() == null) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, NewPostActivity.class);
                startActivity(intent);
            }
        }
        if (item.getItemId() == R.id.menuNotice) {
            TangyuanApplication.getApi().getNotice().enqueue(new Callback<PostMetadata>() {
                @Override
                public void onResponse(Call<PostMetadata> call, Response<PostMetadata> response) {
                    if (response.code() == 200) {
                        ApiHelper.getPostInfoByIdAsync(response.body().postId, result -> {
                            Intent intent = new Intent(MainActivity.this, PostActivity.class);
                            intent.putExtra("postId", result.getPostId());
                            startActivity(intent);
                        });
                    }
                }

                @Override
                public void onFailure(Call<PostMetadata> call, Throwable throwable) {
                    Toast.makeText(MainActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //更新navHeader
        updateUserStatus();
    }

    private void updateUserStatus() {
        if (tm.getToken() != null) {
            int userId = DataTools.decodeJwtTokenUserId(tm.getToken());
            TangyuanApplication.getApi().getUser(userId).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    User user = response.body();
                    runOnUiThread(() -> {
                        Picasso.get()
                                .load(ApiHelper.getFullImageURL(user.avatarGuid))
                                .resize(100, 0)
                                .centerCrop()
                                .into((ImageView) navHeaderView.findViewById(R.id.navAvatarView));
                        ((TextView) navHeaderView.findViewById(R.id.navNicknameView)).setText(user.nickName);
                        ((TextView) navHeaderView.findViewById(R.id.navBioView)).setText(user.bio);
                    });
                }

                @Override
                public void onFailure(Call<User> call, Throwable throwable) {
                    runOnUiThread(() -> ((TextView) navHeaderView.findViewById(R.id.navNicknameView)).setText(R.string.network_error));
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}