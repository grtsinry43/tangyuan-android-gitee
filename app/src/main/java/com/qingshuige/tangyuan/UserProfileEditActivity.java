package com.qingshuige.tangyuan;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.qingshuige.tangyuan.data.DataTools;
import com.qingshuige.tangyuan.network.ApiHelper;
import com.qingshuige.tangyuan.network.User;
import com.squareup.picasso.Picasso;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileEditActivity extends AppCompatActivity {

    private static final int PICK_AVATAR_REQUEST = 1;

    private Menu menu;
    private int userId;
    private boolean isAvatarModified;
    private User currentUser;

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
                currentUser = response.body();
                runOnUiThread(() -> {
                    Picasso.get()
                            .load(ApiHelper.getFullImageURL(currentUser.avatarGuid))
                            .resize(200, 0)
                            .centerCrop()
                            .placeholder(R.drawable.img_placeholder)
                            .into(editAvatar);
                    editNickname.setText(currentUser.nickName);
                    editBio.setText(currentUser.bio);
                    editEmail.setText(currentUser.email);
                    textRegion.setText(currentUser.isoRegionName);
                    textPhoneNumber.setText(currentUser.phoneNumber);

                    pgBar.setVisibility(View.GONE);
                });
            }

            @Override
            public void onFailure(Call<User> call, Throwable throwable) {

            }
        });

        editAvatar.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_AVATAR_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_AVATAR_REQUEST:
                if (data != null)
                    startAvatarCrop(data.getData());
                break;
            case UCrop.REQUEST_CROP:
                Uri resultUri = UCrop.getOutput(data);
                if (resultUri != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
                        editAvatar.setImageBitmap(bitmap);
                        isAvatarModified = true;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
        }
    }

    private void tryUpdateUserProfile() {
        pgBar.setVisibility(View.VISIBLE);
        menu.findItem(R.id.menuOk).setEnabled(false);

        if (validateData()) {
            User user = new User();
            user.userId = userId;
            user.nickName = editNickname.getText().toString();
            user.bio = editBio.getText().toString();
            user.email = editEmail.getText().toString();
            user.isoRegionName = currentUser.isoRegionName;
            user.phoneNumber = currentUser.phoneNumber;
            user.password = TangyuanApplication.getTokenManager().getPassword();
            if (isAvatarModified) {
                //上传图片
                ApiHelper.updateBitmapAsync(((BitmapDrawable) editAvatar.getDrawable()).getBitmap(), result -> {
                    user.avatarGuid = (String) result;
                    TangyuanApplication.getApi().putUser(userId, user).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.code() == 200) {
                                runOnUiThread(() -> {
                                    Toast.makeText(UserProfileEditActivity.this, R.string.edit_profile_success, Toast.LENGTH_SHORT)
                                            .show();
                                    finish();
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                            Log.i("TY", "ERROR: " + throwable.getMessage());
                        }
                    });
                });
            } else {
                user.avatarGuid = currentUser.avatarGuid;
                TangyuanApplication.getApi().putUser(userId, user).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.code() == 200) {
                            runOnUiThread(() -> {
                                Toast.makeText(UserProfileEditActivity.this, R.string.edit_profile_success, Toast.LENGTH_SHORT)
                                        .show();
                                finish();
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                        Log.i("TY", "ERROR: " + throwable.getMessage());
                    }
                });
            }
        } else {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                View view = getCurrentFocus();
                if (view != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
            Snackbar.make(this, textPhoneNumber, getString(R.string.fields_unfinished), BaseTransientBottomBar.LENGTH_SHORT)
                    .show();
            pgBar.setVisibility(View.GONE);
            menu.findItem(R.id.menuOk).setEnabled(true);
        }
    }

    private boolean validateData() {
        if (TextUtils.isEmpty(editNickname.getText())
                || TextUtils.isEmpty(editBio.getText())
                || TextUtils.isEmpty(editEmail.getText())) {
            return false;
        }
        return true;
    }

    private void startAvatarCrop(Uri uri) {
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), "cropped_image.jpg"));

        UCrop uCrop = UCrop.of(uri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(300, 300);

        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        uCrop.withOptions(options);

        uCrop.start(this);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuOk) {
            tryUpdateUserProfile();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_edit_user_profile_menu, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}